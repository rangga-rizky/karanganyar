package com.example.apps.karanganyar.map_page;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apps.karanganyar.R;
import com.example.apps.karanganyar.RouteAPI;
import com.example.apps.karanganyar.SERVER_API;
import com.example.apps.karanganyar.WeatherAPI;
import com.example.apps.karanganyar.model.RedZone;
import com.example.apps.karanganyar.model.ResponseDirection;
import com.example.apps.karanganyar.model.Route;
import com.example.apps.karanganyar.model.RouteData;
import com.example.apps.karanganyar.model.WeatherResponse;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnMarkerClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtSuhu)
    TextView txtSuhu;
    @BindView(R.id.txtLokasi)
    TextView txtLokasi;
    @BindView(R.id.imgSuhu)
    ImageView imgSuhu;
    @BindView(R.id.txtAwal)
    TextView txtAwal;
    @BindView(R.id.txtTujuan)
    TextView txtTujuan;
    @BindView(R.id.txtAlamatTujuan)
    TextView txtAlamatTujuan;
    @BindView(R.id.txtWarning)
    TextView txtWarning;
    @BindView(R.id.imgWarning)
    ImageView imgWarning;
    @BindView(R.id.main_layout)
    ConstraintLayout main_layout;
    @BindView(R.id.warningLayout)
    ConstraintLayout warningLayout;
    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private ArrayList<Marker> markers;
    private ArrayList<RedZone> redzones;
    private ArrayList<Marker> markers_info;
    private ArrayList<Marker> redzone_marker;
    private ArrayList<MarkerOptions> trasnparantMarkers;
    private ArrayList<Polyline> polylines;
    private ArrayList<Integer> redzoneLines;
    private String nama, alamat, lng, lat, lokasi, origin, dest, cuaca;
    private RouteData currentRouteData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);



        //init bundle
        nama = getIntent().getStringExtra("nama");
        alamat = getIntent().getStringExtra("alamat");
        lng = getIntent().getStringExtra("lng");
        lat = getIntent().getStringExtra("lat");
        lokasi = getIntent().getStringExtra("lokasi");
        main_layout.setVisibility(View.GONE);
        warningLayout.setVisibility(View.GONE);
        txtTujuan.setText(nama);
        txtAlamatTujuan.setText(alamat);
        txtLokasi.setText(lokasi);

        //init arraylist
        markers = new ArrayList<>();
        redzoneLines = new ArrayList<>();
        markers_info = new ArrayList<>();
        redzone_marker = new ArrayList<>();
        redzones = new ArrayList<>();
        trasnparantMarkers = new ArrayList<>();
        polylines = new ArrayList<Polyline>();

        //init map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loadWeather(lat, lng);

    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    //load data redzone
    private void loadRedZone() {
        SERVER_API apiService = SERVER_API.client.create(SERVER_API.class);
        Call<ArrayList<RedZone>> call = apiService.getRedzone();

        //proses call
        call.enqueue(new Callback<ArrayList<RedZone>>() {
            @Override
            public void onResponse(Call<ArrayList<RedZone>> call, Response<ArrayList<RedZone>> response) {
                ArrayList<RedZone> apiresponse = response.body();
                loadRoute(origin, dest);
                if (apiresponse != null) {
                    redzones = apiresponse;
                    /*RedZone redZone = new RedZone();
                    redZone.setLocation(new HistoryLocation(-7.294653, 112.698001));
                    redzones.add(redZone);*/
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
                loadRoute(origin, dest);
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(this);
        // mGoogleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
       // mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.setPadding(0, 50, 0, 0);
        //mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        //mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
        }
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setMinZoomPreference(15);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
        }


    }


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
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(20000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (markers != null) {
            for (Marker marker : markers) {
                marker.remove();
            }
        }

        //mendapat posisi alamat sekarang
        resolveAddress(location);

        //set marker asal dan tujuan
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.snippet("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_current));
        markers.add(mGoogleMap.addMarker(markerOptions));

        LatLng DestlatLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng));
        MarkerOptions DestmarkerOptions = new MarkerOptions();
        DestmarkerOptions.position(DestlatLng);
        DestmarkerOptions.title("Destination");
        DestmarkerOptions.snippet("Destination");
        DestmarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markers.add(mGoogleMap.addMarker(DestmarkerOptions));

        origin = String.valueOf(location.getLatitude())+ ","+String.valueOf(location.getLongitude());
        dest = lat+","+lng;
        loadRedZone();

    }


    //mendapatkan alamt posisi sekarang
    private void resolveAddress(Location location){
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
            txtAwal.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //load route dari direction API
    private void loadRoute(String origin,String dest){
        RouteAPI apiService = RouteAPI.client.create(RouteAPI.class);
        Call<ResponseDirection> call = apiService.getDirections(origin,dest,"false","driving","true");

        //proses call
        call.enqueue(new Callback<ResponseDirection>() {
            @Override
            public void onResponse(Call<ResponseDirection> call, Response<ResponseDirection> response) {
               if(response.body()!=null){
                   ParserTask parserTask = new ParserTask();
                   ArrayList<Route> routes_list = response.body().getRoutes();
                   //menggambarkan rute secara Asynctask
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


    //load data cuaca
    private void loadWeather(String lat,String lng){
        WeatherAPI apiService = WeatherAPI.client.create(WeatherAPI.class);
        String appkey = getResources().getString(R.string.weather_key);
        Call<WeatherResponse> call = apiService.getWheater(lat,
                                                            lng,
                                                            appkey,
                                                        "metric");

        //proses call
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    WeatherResponse apiresponse = response.body();
                    if(apiresponse!=null){
                        String suhu = apiresponse.getMain().getTemp()+""+(char) 0x00B0 +" C";
                        txtSuhu.setText(suhu);
                        cuaca = apiresponse.getWeather().get(0).getMain();
                        if(cuaca.equals("Rain")){
                            imgSuhu.setImageDrawable(getResources().getDrawable(R.drawable.ic_rain));
                        }
                    }

            }


            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Log error
                String message = t.getMessage();

                Toast.makeText(getApplicationContext(),"Failed to Connect Internet",Toast.LENGTH_SHORT).show();
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
            }
        });


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        return false;
    }

    //Asynctask untuk menggambarkan rute
    private class ParserTask extends AsyncTask<ArrayList<Route>, Integer, RouteData> {

        // Parsing the data in non-ui thread
        @Override
        protected RouteData doInBackground(ArrayList<Route>... jsonData) {

            RouteData route = null;

            try {
                DirectionsJSONParser parser = new DirectionsJSONParser();
                route = parser.parse(jsonData[0],redzones);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return route;
        }



        @Override
        protected void onPostExecute(RouteData hasil) {
            ArrayList points = null;
            currentRouteData = hasil;
            List<List<HashMap<String,String>>> result = hasil.getLineData();
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

            //menggambarkan rute
            for (int i = result.size()-1; i >=0 ; i--) {
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
                Log.e("miaow",String.valueOf(hasil.getIsDangerous().get(i)));
               // if(hasil.getIsDangerous().get(i)){
                 //   lineOptions.color(getResources().getColor(R.color.red));
                //}else
                if(i==0){
                    lineOptions.color(getResources().getColor(R.color.blue_young));
                }else{
                    lineOptions.color(Color.GRAY);
                }
                lineOptions.geodesic(true);

                // Drawing polyline in the Google Map for the i-th route
                if(lineOptions!=null){
                    Polyline polyline = mGoogleMap.addPolyline(lineOptions);
                    polyline.setClickable(true);
                    polylines.add(polyline);
                }

                Marker marker = mGoogleMap.addMarker(trasnparantMarkers.get(i));
                marker.showInfoWindow();
                markers_info.add(marker);
            }

            //ketika rute di klik
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

                    if(!currentRouteData.getIsDangerous().contains(false)) { // ketika semua berbahaya
                        openAllRedDialog();
                        imgWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_danger_route));
                        txtWarning.setText("semua rute berbahaya");
                    }else if(currentRouteData.getIsDangerous().get(index) && (!cuaca.equals("Rain"))){
                        imgWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning_route));
                        txtWarning.setText(getResources().getString(R.string.warning));
                    }else if(currentRouteData.getIsDangerous().get(index) && (cuaca.equals("Rain"))) {
                        imgWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_danger_route));
                        txtWarning.setText(getResources().getString(R.string.danger_warning));
                    }else{
                        imgWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_safe_route));
                        txtWarning.setText("Rute aman untuk dilewati");
                    }

                }
            });

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }

            //cek apakah aman atau tidak
            if(hasil.getIsDangerous().size()> 0){
                if(!hasil.getIsDangerous().contains(false) && (cuaca.equals("Rain"))) {
                    openAllRedDialog();
                    imgWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_danger_route));
                    txtWarning.setText("semua rute berbahaya");
                }else if(hasil.getIsDangerous().get(0) && (!cuaca.equals("Rain"))){
                    imgWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning_route));
                    txtWarning.setText(getResources().getString(R.string.warning));
                }else if(hasil.getIsDangerous().get(0) && (cuaca.equals("Rain"))) {
                    imgWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_danger_route));
                    txtWarning.setText(getResources().getString(R.string.danger_warning));
                }

            }

            //camera untuk map
            LatLngBounds bounds = builder.build();
            int padding = 55; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mGoogleMap.animateCamera(cu);
            main_layout.setVisibility(View.VISIBLE);
            warningLayout.setVisibility(View.VISIBLE);

            //padding untuk atur letak tombol direction dan mylocation
            ViewTreeObserver viewTreeObserver = main_layout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        main_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        mGoogleMap.setPadding(35, 50, 0, main_layout.getMeasuredHeight()+160);
                    }
                });
            }
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
                                ActivityCompat.requestPermissions(MapActivity.this,
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //dialog ketika semua berbahaya
    private void openAllRedDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_danger);
        Button btnKembali = (Button) dialog.findViewById(R.id.btnKembali);
        Button btnLihatPeta = (Button) dialog.findViewById(R.id.btnLihatPeta);
        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();

            }
        });
        btnLihatPeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

   /* class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.info_window_polyline, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView txtWaktu = ((TextView)myContentsView.findViewById(R.id.txtWaktu));
            txtWaktu.setText(marker.getTitle());
            TextView txtJarak = ((TextView)myContentsView.findViewById(R.id.txtJarak));
            txtJarak.setText(marker.getSnippet());

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }*/



}
