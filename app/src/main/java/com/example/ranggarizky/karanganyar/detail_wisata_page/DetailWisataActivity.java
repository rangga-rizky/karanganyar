package com.example.ranggarizky.karanganyar.detail_wisata_page;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ranggarizky.karanganyar.API;
import com.example.ranggarizky.karanganyar.R;
import com.example.ranggarizky.karanganyar.map_page.MapActivity;
import com.example.ranggarizky.karanganyar.model.GooglePlaceRespond;
import com.example.ranggarizky.karanganyar.model.GooglePlaceSingleRespond;
import com.example.ranggarizky.karanganyar.model.Photo;
import com.example.ranggarizky.karanganyar.model.Result;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailWisataActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressbar;
    @BindView(R.id.main_layout)
    ConstraintLayout main_layout;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtAlamat)
    TextView txtAlamat;
    @BindView(R.id.txtProvinsi)
    TextView txtProvinsi;
    @BindView(R.id.txtRating)
    TextView txtRating;
    @BindView(R.id.txtTelepon)
    TextView txtTelepon;
    @BindView(R.id.txtSenin)
    TextView txtSenin;
    @BindView(R.id.txtSelasa)
    TextView txtSelasa;
    @BindView(R.id.txtRabu)
    TextView txtRabu;
    @BindView(R.id.txtKamis)
    TextView txtKamis;
    @BindView(R.id.txtJumat)
    TextView txtJumat;
    @BindView(R.id.txtSabtu)
    TextView txtSabtu;
    @BindView(R.id.txtMinggu)
    TextView txtMinggu;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private String lat,lng,lokasi;
    Drawable greyArrow,whiteArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_wisata);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        greyArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24px);
        greyArrow.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        whiteArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24px);
        whiteArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);


        String id = getIntent().getStringExtra("id");
        String name = getIntent().getStringExtra("name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
       // getSupportActionBar().setTitle(name);
        //toolbarConfig();
        ViewCompat.setTransitionName(findViewById(R.id.appBarLayout), "EXTRA_IMAGE");
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(name);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));
        collapsingToolbarLayout.setStatusBarScrimColor(getResources().getColor(R.color.colorPrimary));
        setBackButton();
        loadData(id);
    }

    private void  setBackButton(){
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
                //Initialize the size of the scroll
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                //Check if the view is collapsed
                if (scrollRange + verticalOffset == 0) {
                    getSupportActionBar().setHomeAsUpIndicator(whiteArrow);
                }else{
                    getSupportActionBar().setHomeAsUpIndicator(greyArrow);
                }
            }
        });
    }



    private void settingVIewPager(List<Photo> images){
        mCardAdapter = new CardPagerAdapter(this);
        for(int i = 0 ;i < images.size();i++){
            String key = getResources().getString(R.string.google_key);
            String fotoRequest = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+
                    images.get(i).getPhotoReference()+"&key="+key;
            mCardAdapter.addCardItem(fotoRequest);

        }
        mCardShadowTransformer = new ShadowTransformer(viewPager, mCardAdapter);
        viewPager.setAdapter(mCardAdapter);
        viewPager.setPageTransformer(false, mCardShadowTransformer);
        viewPager.setOffscreenPageLimit(3);
    }

    private void loadData(String id) {
        progressbar.setVisibility(View.VISIBLE);
        API apiService = API.client.create(API.class);
        String key = getResources().getString(R.string.google_key);
        Call<GooglePlaceSingleRespond> call = apiService.getPlacebyId(id,"id",key);

        //proses call
        call.enqueue(new Callback<GooglePlaceSingleRespond>() {
            @Override
            public void onResponse(Call<GooglePlaceSingleRespond> call, Response<GooglePlaceSingleRespond> response) {
                progressbar.setVisibility(View.GONE);
                main_layout.setVisibility(View.VISIBLE);
                if(response.body()!=null && response.body().getStatus().equals("OK")){
                    Result apiresponse = response.body().getResult();
                    txtName.setText(apiresponse.getName());
                    lat = String.valueOf(apiresponse.getGeometry().getLocation().getLat());
                    lng = String.valueOf(apiresponse.getGeometry().getLocation().getLng());
                    txtAlamat.setText(apiresponse.getFormattedAddress());
                    if(apiresponse.getOpeningHours() == null){
                        txtSenin.setVisibility(View.GONE);
                        txtSelasa.setVisibility(View.GONE);
                        txtRabu.setVisibility(View.GONE);
                        txtKamis.setVisibility(View.GONE);
                        txtJumat.setVisibility(View.GONE);
                        txtSabtu.setVisibility(View.GONE);
                        txtMinggu.setText("Jam Buka Tidak diketahui");
                    }else{
                        txtSenin.setText(apiresponse.getOpeningHours().getWeekdayText().get(0));
                        txtSelasa.setText(apiresponse.getOpeningHours().getWeekdayText().get(1));
                        txtRabu.setText(apiresponse.getOpeningHours().getWeekdayText().get(2));
                        txtKamis.setText(apiresponse.getOpeningHours().getWeekdayText().get(3));
                        txtJumat.setText(apiresponse.getOpeningHours().getWeekdayText().get(4));
                        txtSabtu.setText(apiresponse.getOpeningHours().getWeekdayText().get(5));
                        txtMinggu.setText(apiresponse.getOpeningHours().getWeekdayText().get(6));
                    }
                    if(apiresponse.getFormatted_phone_number()!=null){
                        txtTelepon.setText(apiresponse.getFormatted_phone_number());
                    }else{
                        txtTelepon.setText("Tidak ada Nomor Telepon");
                    }
                    txtRating.setText(String.valueOf(apiresponse.getRating()));
                    ratingBar.setRating(apiresponse.getRating());
                    txtProvinsi.setText(apiresponse.getAddressComponents().get(3).getLongName());
                    lokasi = apiresponse.getAddressComponents().get(2).getLongName()+", "+
                            apiresponse.getAddressComponents().get(3).getLongName();
                    settingVIewPager(apiresponse.getPhotos());


                }else{
                    Toast.makeText(getApplicationContext(), "Problem in the server", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<GooglePlaceSingleRespond> call, Throwable t) {
                progressbar.setVisibility(View.GONE);
                Log.e("cok", "onFailure: ", t.fillInStackTrace());
                Toast.makeText(getApplicationContext(), "Failed to Connect Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.btnDirection)
    public void direction(){
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("nama", txtName.getText().toString());
        intent.putExtra("alamat",txtAlamat.getText().toString());
        intent.putExtra("lat",lat);
        intent.putExtra("lokasi",lokasi);
        intent.putExtra("lng",lng);
        startActivity(intent);
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
