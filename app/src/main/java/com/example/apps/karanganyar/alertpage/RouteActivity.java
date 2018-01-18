package com.example.apps.karanganyar.alertpage;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apps.karanganyar.R;
import com.example.apps.karanganyar.RouteAPI;
import com.example.apps.karanganyar.SERVER_API;
import com.example.apps.karanganyar.map_page.DirectionsJSONParser;
import com.example.apps.karanganyar.model.RedZone;
import com.example.apps.karanganyar.model.ResponseDirection;
import com.example.apps.karanganyar.model.Result;
import com.example.apps.karanganyar.model.Route;
import com.example.apps.karanganyar.model.RouteData;
import com.example.apps.karanganyar.util.DBHandler;
import com.example.apps.karanganyar.util.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private Location currentLocation;
    private SessionManager sessionManager;
    private LatLng selectedLoc;
    private String selectedName;
    private ArrayAdapter<Result> adapter;
    private ArrayList<RedZone> redzones;
    private ArrayList<Result> placePredictions;
    private ArrayList<Marker> markers;
    private ArrayList<Marker> markers_info;
    private ArrayList<Marker> redzone_marker;
    private ArrayList<Polyline> polylines;
    private ArrayList<MarkerOptions> trasnparantMarkers;
    private RouteData currentRouteData;
    private DBHandler db;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtLokasi)
    TextView txtLokasi;
    @BindView(R.id.editSearch)
    AutoCompleteTextView editSearch;
    @BindView(R.id.warningLayout)
    ConstraintLayout warningLayout;
    @BindView(R.id.dangerLayout)
    ConstraintLayout dangerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        String alamat = getIntent().getStringExtra("alamat");
        txtLokasi.setText(alamat);

        db = new DBHandler(this);
        placePredictions = new ArrayList<>();
        redzones = new ArrayList<>();
        markers = new ArrayList<>();
        redzone_marker = new ArrayList<>();
        markers_info = new ArrayList<>();
        polylines = new ArrayList<>();
        trasnparantMarkers = new ArrayList<>();
        sessionManager = new SessionManager(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                if (s.toString().length() > 2 && (!editSearch.isPerformingCompletion())) {
                    // loadData(s.toString());
                    fetchDB(s.toString());
                }

            }
        });

        loadRedZone();
        editSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View v = getCurrentFocus();
                if (v != null) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                Log.e("ayam", String.valueOf(arg2));
                Result selected = (Result) arg0.getAdapter().getItem(arg2);
                selectedName = (String) arg0.getSelectedItem();
                String origin = "";
                if (currentLocation == null) {
                    origin = sessionManager.getLoc();
                } else {
                    origin = String.valueOf(currentLocation.getLatitude()) + "," +
                            String.valueOf(currentLocation.getLongitude());
                }
                selectedLoc = new LatLng(selected.getGeometry().getLocation().getLat(),
                        selected.getGeometry().getLocation().getLng());
                String dest = String.valueOf(selected.getGeometry().getLocation().getLat()) + "," +
                        String.valueOf(selected.getGeometry().getLocation().getLng());
                if (markers != null) {
                    for (Marker marker : markers) {
                        marker.remove();
                    }
                }

                loadRoute(origin, dest);

                // PlacePrediction selected = (PlacePrediction) arg0.getAdapter().getItem(arg2);
                // loadPlace(selected.getPlaceId());

            }

        });


    }

    private void loadRedZone() {
        SERVER_API apiService = SERVER_API.client.create(SERVER_API.class);
        Call<ArrayList<RedZone>> call = apiService.getRedzone();

        //proses call
        call.enqueue(new Callback<ArrayList<RedZone>>() {
            @Override
            public void onResponse(Call<ArrayList<RedZone>> call, Response<ArrayList<RedZone>> response) {
                ArrayList<RedZone> apiresponse = response.body();
                if (apiresponse != null) {
                    redzones = apiresponse;

                   // RedZone tes= new RedZone();
                   // tes.setLocation(new HistoryLocation(-7.288757, 112.708129));
                    //redzones.add(tes);
                    for (Marker redZoneMarker : redzone_marker) {
                        redZoneMarker.remove();
                    }
                    redzone_marker.clear();
                    for (int i = 0; i < redzones.size(); i++) {

                        LatLng latLng = new LatLng(redzones.get(i).getLocation().getLat(),
                                redzones.get(i).getLocation().getLong());
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_redzone));
                        redzone_marker.add(mGoogleMap.addMarker(markerOptions));
                    }


                }

            }


            @Override
            public void onFailure(Call<ArrayList<RedZone>> call, Throwable t) {
                // Log error
                String message = t.getMessage();
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
            }
        });


    }

    private void fetchDB(String q) {
        placePredictions = db.getAllWisataByName(q);
        Log.e("ayam", String.valueOf(placePredictions.size()));
        adapter = new ArrayAdapter<Result>(
                getApplicationContext(),
                android.R.layout.simple_list_item_1, placePredictions) {
            @Override
            public View getView(int position,
                                View convertView, ViewGroup parent) {
                View view = super.getView(position,
                        convertView, parent);
                TextView text = (TextView) view
                        .findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                return view;
            }
        };
        editSearch.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

   /* private void loadPlace(String id) {
        API apiService = API.client.create(API.class);
        String key = getResources().getString(R.string.google_key);
        Call<GooglePlaceSingleRespond> call = apiService.getPlacebyId(id,"id",key);

        //proses call
        call.enqueue(new Callback<GooglePlaceSingleRespond>() {
            @Override
            public void onResponse(Call<GooglePlaceSingleRespond> call, Response<GooglePlaceSingleRespond> response) {

                if(response.body()!=null){
                    Result apiresponse = response.body().getResult();
                    String origin ="";
                    if(currentLocation == null){
                       origin =  sessionManager.getLoc();
                    }else{
                        origin =  String.valueOf(currentLocation.getLatitude())+ ","+
                                String.valueOf(currentLocation.getLongitude());
                    }

                    String dest = String.valueOf(apiresponse.getGeometry().getLocation().getLat())+ ","+
                            String.valueOf(apiresponse.getGeometry().getLocation().getLng());
                    loadRoute(origin,dest);
                }
            }


            @Override
            public void onFailure(Call<GooglePlaceSingleRespond> call, Throwable t) {
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }*/



   /* private void loadData(String q) {

        API apiService = API.client.create(API.class);
        String key = getResources().getString(R.string.google_key);
        Call<ResponsePlace> call = apiService.getAutoComplete(q,
                key);

        //proses call
        call.enqueue(new Callback<ResponsePlace>() {
            @Override
            public void onResponse(Call<ResponsePlace> call, Response<ResponsePlace> response) {

                ResponsePlace respond = response.body();
                if (respond != null) {

                    placePredictions.clear();
                    placePredictions = respond.getPredictions();
                    adapter = new ArrayAdapter<PlacePrediction>(
                            getApplicationContext(),
                            android.R.layout.simple_list_item_1, placePredictions) {
                        @Override
                        public View getView(int position,
                                            View convertView, ViewGroup parent) {
                            View view = super.getView(position,
                                    convertView, parent);
                            TextView text = (TextView) view
                                    .findViewById(android.R.id.text1);
                            text.setTextColor(Color.BLACK);
                            return view;
                        }
                    };
                    editSearch.setAdapter(adapter);
                    //adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onFailure(Call<ResponsePlace> call, Throwable t) {
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }*/


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(35000);
        mLocationRequest.setFastestInterval(35000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // mGoogleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        // mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        // mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);


        //mMap.setMinZoomPreference(15);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);

            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }


    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(RouteActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        currentLocation = location;
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude()
                ,location.getLongitude()), 15));
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void loadRoute(String origin,String dest){
        RouteAPI apiService = RouteAPI.client.create(RouteAPI.class);
        Call<ResponseDirection> call = apiService.getDirections(origin,dest,"false","driving","true");

        //proses call
        call.enqueue(new Callback<ResponseDirection>() {
            @Override
            public void onResponse(Call<ResponseDirection> call, Response<ResponseDirection> response) {
                if(response.body()!=null){
                    RouteActivity.ParserTask parserTask = new RouteActivity.ParserTask();
                    ArrayList<Route> routes_list = response.body().getRoutes();
                    parserTask.execute(routes_list);
                }else{
                    try {
                        Log.e("miaow",response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onFailure(Call<ResponseDirection> call, Throwable t) {
                // Log error
                String message = t.getMessage();

                Toast.makeText(getApplicationContext(),"Failed to Connect Internet",Toast.LENGTH_SHORT).show();
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
            }
        });


    }

    private class ParserTask extends AsyncTask<ArrayList<Route>, Integer, RouteData> {

        // Parsing the data in non-ui thread
        @Override
        protected RouteData doInBackground(ArrayList<Route>... jsonData) {

            RouteData route = null;

            try {
                DirectionsJSONParser parser = new DirectionsJSONParser();
                route = parser.parse(jsonData[0], redzones);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return route;
        }


        @Override
        protected void onPostExecute(RouteData hasil) {
            ArrayList points = null;
            warningLayout.setVisibility(View.GONE);
            dangerLayout.setVisibility(View.GONE);
            currentRouteData = hasil;
            List<List<HashMap<String, String>>> result = hasil.getLineData();
            PolylineOptions lineOptions = null;

            for (Polyline polyline : polylines){
                polyline.remove();
            }
            polylines.clear();
            trasnparantMarkers.clear();
            for (Marker marker : markers_info) {
                marker.remove();
            }
            markers_info.clear();
            trasnparantMarkers = hasil.getInfoWindows();

            for (int i = result.size() - 1; i >= 0; i--) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);

                // if(hasil.getIsDangerous().get(i)){
                //   lineOptions.color(getResources().getColor(R.color.red));
                //}else
                if (i == 0) {
                    lineOptions.color(getResources().getColor(R.color.blue_young));
                } else {
                    lineOptions.color(Color.GRAY);
                }
                lineOptions.geodesic(true);

                // Drawing polyline in the Google Map for the i-th route
                if (lineOptions != null) {
                    Polyline polyline = mGoogleMap.addPolyline(lineOptions);
                    polyline.setClickable(true);
                     polylines.add(polyline);
                }

                Marker marker = mGoogleMap.addMarker(trasnparantMarkers.get(i));
                marker.showInfoWindow();
                markers_info.add(marker);
            }

            if(hasil.getIsDangerous().size()> 0){
                if(!hasil.getIsDangerous().contains(false)) {
                    openAllRedDialog();
                    dangerLayout.setVisibility(View.VISIBLE);

                }
            }

            mGoogleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                public void onPolylineClick(Polyline polyline) {
                    int index = (polylines.indexOf(polyline));
                    markers_info.get(index).showInfoWindow();
                    for (int i = 0 ; i < polylines.size();i++){
                        if(i==index){
                            polylines.get(i).setColor(getResources().getColor(R.color.blue_young));
                        }else{
                            polylines.get(i).setColor(Color.GRAY);
                        }
                    }

                }
            });

            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.snippet("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.empty));
            markers.add(mGoogleMap.addMarker(markerOptions));

            LatLng DestlatLng = new LatLng(selectedLoc.latitude, selectedLoc.longitude);
            MarkerOptions DestmarkerOptions = new MarkerOptions();
            DestmarkerOptions.position(DestlatLng);
            DestmarkerOptions.title(selectedName);
            DestmarkerOptions.snippet("Destination");
            DestmarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markers.add(mGoogleMap.addMarker(DestmarkerOptions));

           LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
              builder.include(marker.getPosition());
            }


            LatLngBounds bounds = builder.build();
            int padding = 55; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mGoogleMap.animateCamera(cu);
        }
    }

    private void openAllRedDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_danger_alert);
        Button btnLihatPeta = (Button) dialog.findViewById(R.id.btnLihatPeta);

        btnLihatPeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        dialog.show();
    }
}
