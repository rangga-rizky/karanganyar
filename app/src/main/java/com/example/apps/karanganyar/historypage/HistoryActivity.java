package com.example.apps.karanganyar.historypage;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apps.karanganyar.R;
import com.example.apps.karanganyar.SERVER_API;
import com.example.apps.karanganyar.model.RedZone;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity   implements OnMapReadyCallback{


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.item_list)
    RecyclerView recyclerView;
    @BindView(R.id.btnExpand)
    RelativeLayout btnExpand;
    @BindView(R.id.txtExpand)
    TextView txtExpand;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.menu)
    ImageView menu;
    private GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    ArrayList<RedZone> list_history;
    ArrayList<RedZone> list_red_zone;

    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    HistoryAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Riwayat Longsor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //init map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        list_history = new ArrayList<>();
        mAdapter = new HistoryAdapter(this, list_history);
        LinearLayoutManager mLayoutManagerOrder = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManagerOrder);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


    }

    //load data history
    private void loadHistory() {

        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        SERVER_API apiService = SERVER_API.client.create(SERVER_API.class);
        Call<ArrayList<RedZone>> call = apiService.getDisasterHistory();

        //proses call
        call.enqueue(new Callback<ArrayList<RedZone>>() {
            @Override
            public void onResponse(Call<ArrayList<RedZone>> call, Response<ArrayList<RedZone>> response) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ArrayList<RedZone> respond = response.body();
                if (respond != null) {
                    list_history.addAll(respond);
                    mAdapter.notifyDataSetChanged();
                    if(mGoogleMap!=null){
                        for(RedZone history : respond){
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(history.getLocation().getLat(),history.getLocation().getLong()));
                            markerOptions.title(history.getType());
                            //markerOptions.snippet("Current Position");
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_redzone));
                            mGoogleMap.addMarker(markerOptions);
                        }
                    }
                }


            }

            @Override
            public void onFailure(Call<ArrayList<RedZone>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    //ketika tombol dibawah di click
    @OnClick(R.id.btnExpand)
    public void expand(){
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            txtExpand.setText("LIHAT PEMETAAN");
            menu.setImageDrawable(getResources().getDrawable(R.drawable.ic_lihat_pemetaan));
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            txtExpand.setText("LIHAT KEJADIAN DETAIL");
            menu.setImageDrawable(getResources().getDrawable(R.drawable.ic_lihat_detail_kejadian));
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        loadHistory();
        LatLng karangayar = new LatLng(-7.607, 110.9866);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(karangayar,10.5f));


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

}
