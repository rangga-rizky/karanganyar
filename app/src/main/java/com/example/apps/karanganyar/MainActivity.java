package com.example.apps.karanganyar;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.apps.karanganyar.alertpage.GeofenceTrasitionService;
import com.example.apps.karanganyar.firstpage.FirstActivity;
import com.example.apps.karanganyar.historypage.HistoryActivity;
import com.example.apps.karanganyar.homepage.WisataRecycleAdapter;
import com.example.apps.karanganyar.model.GooglePlaceRespond;
import com.example.apps.karanganyar.model.HistoryLocation;
import com.example.apps.karanganyar.model.RedZone;
import com.example.apps.karanganyar.model.Result;
import com.example.apps.karanganyar.util.DBHandler;
import com.example.apps.karanganyar.util.DataFetchListner;
import com.example.apps.karanganyar.util.DataFetchWHereListner;
import com.example.apps.karanganyar.util.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements DataFetchListner, DataFetchWHereListner, SearchView.OnQueryTextListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        ResultCallback<Status> {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.item_list)
    RecyclerView recyclerView;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.progressBar)
    ProgressBar progressbar;
    private SessionManager sessionManager;
    private ArrayList<Result> list_wisata;
    private WisataRecycleAdapter mAdapter;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private DBHandler db;
    private static final int GEOFENCE_RADIUS = 1000; // radius  1 km dari redzone. akan ada notif
    private EditText editSearch;
    private Boolean isFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        db = new DBHandler(this);

        editSearch = (EditText) searchView.findViewById(R.id.search_src_text);
        //setting searchview agar tidak memunculkan keyboard saat awal
        searchView.setActivated(true);
        searchView.setQueryHint("Ayo Liburan!");
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.clearFocus();

        //cek apakah pertama kali setelah install
        sessionManager = new SessionManager(this);
        if (sessionManager.isFirstime()) {
            startActivity(new Intent(this, FirstActivity.class));
            finish();
        } else {

            list_wisata = new ArrayList<>();
            mAdapter = new WisataRecycleAdapter(this, list_wisata);
            LinearLayoutManager mLayoutManagerOrder = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManagerOrder);
            recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
            searchView.setOnQueryTextListener(this);


        }

        //ketika swipe load ulang data
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(false);
                mAdapter.reset();
                list_wisata.clear();
                loadData("wisata+karanganyar");
            }
        });

        setLoc();
    }

    //load data redzones dari SERVER
    private void loadRedZone() {
        SERVER_API apiService = SERVER_API.client.create(SERVER_API.class);
        Call<ArrayList<RedZone>> call = apiService.getRedzone();

        //proses call
        call.enqueue(new Callback<ArrayList<RedZone>>() {
            @Override
            public void onResponse(Call<ArrayList<RedZone>> call, Response<ArrayList<RedZone>> response) {
                ArrayList<RedZone> apiresponse = response.body();
                if (apiresponse != null) {
                    for (int i = 0; i < apiresponse.size(); i++) {

                        LatLng latLng = new LatLng(apiresponse.get(i).getLocation().getLat(),
                                apiresponse.get(i).getLocation().getLong());

                        //start geofence untuk mendeteksi ketika dekat dengan redzones
                        startGeofence(latLng,String.valueOf(latLng)+"_"+
                                apiresponse.get(i).getLocation().getAddress());
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


    //mengambil lokasi sekarang
    private void setLoc() {
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
        } else {
            buildGoogleApiClient();
        }
    }

    //load data dai database
    private void getFeedFromDatabase() {
        db.fetchData(this);
    }

    private GooglePlaceRespond respondData;
    //load data dari google places API
    private void loadData(String q) {

        recyclerView.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        isFinished = false;
        editSearch.setEnabled(false);
        API apiService = API.client.create(API.class);
        String key = getResources().getString(R.string.google_key);
        Call<GooglePlaceRespond> call = apiService.getPOI(q,
                "id",
                key);

        //proses call
        call.enqueue(new Callback<GooglePlaceRespond>() {
            @Override
            public void onResponse(Call<GooglePlaceRespond> call, Response<GooglePlaceRespond> response) {

                 respondData = response.body();
                if (respondData != null) {

                    list_wisata.addAll(respondData.getResults());
                    mAdapter.notifyDataSetChanged();

                    Log.e("atam",String.valueOf(list_wisata.size()));
                    db.clearWisata();
                    for (int i = 0; i < respondData.getResults().size(); i++) {

                        Result dataModel = respondData.getResults().get(i);
                        SaveIntoDatabase task = new SaveIntoDatabase();
                        task.execute(dataModel);
                        //mAdapter.addData(dataModel);
                    }
                    if (respondData.getNextPageToken() != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadMore(respondData.getNextPageToken());
                            }
                        }, 2000);
                    }
                }
            }

            @Override
            public void onFailure(Call<GooglePlaceRespond> call, Throwable t) {
                progressbar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                editSearch.setEnabled(true);
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //load data pagination google places api
    private void loadMore(String pageToken) {

        API apiService = API.client.create(API.class);
        String key = getResources().getString(R.string.google_key);

        Log.e("ayam",String.valueOf(pageToken));
        Call<GooglePlaceRespond> call = apiService.getPOI("wisata+karanganyar",
                "id",
                pageToken,
                key);

        //proses call
        call.enqueue(new Callback<GooglePlaceRespond>() {
            @Override
            public void onResponse(Call<GooglePlaceRespond> call, Response<GooglePlaceRespond> response) {
                isFinished = true;
                GooglePlaceRespond respond = response.body();
                if (respond != null) {
                    Log.e("ayam",String.valueOf(respond.getStatus()));
                    Log.e("ayam",String.valueOf(respond.getResults().size()));
                    list_wisata.addAll(respond.getResults());
                    mAdapter.notifyDataSetChanged();
                    //db.clearWisata();
                    for (int i = 0; i < respond.getResults().size(); i++) {

                        Result dataModel = respond.getResults().get(i);
                        SaveIntoDatabaseMore taskMore = new SaveIntoDatabaseMore();
                        taskMore.execute(dataModel);
                        //mAdapter.addData(dataModel);
                    }
                }
            }

            @Override
            public void onFailure(Call<GooglePlaceRespond> call, Throwable t) {

                progressbar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                editSearch.setEnabled(true);
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        //search data dari database
        mAdapter.reset();
        db.searchWisata(this, query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //ketika mengetikkan search
        if (newText.length() > 2) {
            mAdapter.reset();
            db.searchWisata(this, newText);

        } else if (newText.length() == 0) {
            mAdapter.reset();
            getFeedFromDatabase();
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, HistoryActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeliverData(Result dataModel) {
        mAdapter.addData(dataModel);
    }

    @Override
    public void onHideDialog() {
        swipe.setRefreshing(false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //inisiasi location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }

        loadRedZone();

        //cek jika pertama maka load dari google places API jika tidak load dari database
        if (!sessionManager.isFirstime()) {
            if (sessionManager.isFirsLoad()) {
                loadData("wisata+karanganyar");
                sessionManager.setFirstLoad(false);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.GONE);
                getFeedFromDatabase();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //dijalankan ketika lokasi terupdate
        mLastLocation = location;
        sessionManager = new SessionManager(this);
        sessionManager.setLoc(String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.e("kucing",String.valueOf(status));
    }


    //simpan data ke dalam database
    public class SaveIntoDatabase extends AsyncTask<Result, Void, Void> {
        // can use UI thread here
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Void doInBackground(Result... params) {
            Result dataModel = params[0];
            try {
                String key = getResources().getString(R.string.google_key);
                String fotoRequest = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" +
                        dataModel.getPhotos().get(0).getPhotoReference() + "&key=" + key;
                InputStream inputStream = new URL(fotoRequest).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                //set bitmap value to Picture
                dataModel.setPicture(bitmap);
                //add data to database
                db.addWisata(dataModel);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


    }

    //simpan data pagination ke dalam database
    public class SaveIntoDatabaseMore extends AsyncTask<Result, Void, Void> {
        // can use UI thread here
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Void doInBackground(Result... params) {
            Result dataModel = params[0];
            try {
                String key = getResources().getString(R.string.google_key);
                String fotoRequest = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" +
                        dataModel.getPhotos().get(0).getPhotoReference() + "&key=" + key;
                InputStream inputStream = new URL(fotoRequest).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                //set bitmap value to Picture
                dataModel.setPicture(bitmap);
                //add data to database
                db.addWisata(dataModel);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("ayam","jalan");
            progressbar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            editSearch.setEnabled(true);
        }
    }

    //build googleapi client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    //cek permission untuk lokasi
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
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    //memulai geofence
    private void startGeofence(LatLng latlong,String id) {
        Geofence geofence = createGeofence(latlong, GEOFENCE_RADIUS, id);
        GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
        addGeofence(geofenceRequest);
    }

    //build geofence request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofence(geofence)
                .build();
    }

    //Membuild class geofence
    private Geofence createGeofence(LatLng latLng, float radius, String key) {
        return new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(5000)
                .build();
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);

        }
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    //membuat pending intent untuk menjalankan service mendeteksi apakah berada dalam radius redzones
    private PendingIntent createGeofencePendingIntent() {
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }


}