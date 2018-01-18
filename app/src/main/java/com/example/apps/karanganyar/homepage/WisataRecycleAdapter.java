package com.example.apps.karanganyar.homepage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.apps.karanganyar.R;
import com.example.apps.karanganyar.detail_wisata_page.DetailWisataActivity;
import com.example.apps.karanganyar.model.Result;
import com.example.apps.karanganyar.util.SessionManager;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by RanggaRizky on 12/19/2017.
 */

public class WisataRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Result> verticalList;
    private Context context;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public class MyViewHolder extends RecyclerView.ViewHolder {


        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    public WisataRecycleAdapter(Context context, List<Result> verticalList) {
        this.verticalList = verticalList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //beri space pada atas list
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.blank, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wisata, parent, false);
            return new GenericViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GenericViewHolder) {
            final Result item = verticalList.get(position - 1);
            final GenericViewHolder genericHolder = (GenericViewHolder) holder;
            String key = context.getResources().getString(R.string.google_key);

            if(item.getFromDatabase()!=null){
                if(item.getFromDatabase()){
                    genericHolder.imgPhoto.setImageBitmap(item.getPicture());
                }
            }
            else{
                if (item.getPhotos() != null) {
                    String fotoRequest = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" +
                            item.getPhotos().get(0).getPhotoReference() + "&key=" + key;
                    Picasso.with(context)
                            .load(fotoRequest)
                            .placeholder(R.drawable.dummy)
                            .error(R.drawable.dummy)
                            .into(genericHolder.imgPhoto);
                }
            }


            LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);

            String dist = "-";

            SessionManager sessionManager = new SessionManager(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && sessionManager.getLoc()!=null) {
                float[] results = new float[1];

                String[] loc =sessionManager.getLoc().split(",");
                Location.distanceBetween(
                        Double.valueOf(loc[0]),
                        Double.valueOf(loc[1]),
                        item.getGeometry().getLocation().getLat(),
                        item.getGeometry().getLocation().getLng(),
                        results);
                dist = String.format("%.02f", results[0]/1000);
            }

           genericHolder.txtTitle.setText(item.getName());
           genericHolder.txtBody.setText(dist+" km -"+item.getFormattedAddress());
           genericHolder.main_layout.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent intent = new Intent(context, DetailWisataActivity.class);
                   intent.putExtra("id",item.getPlaceId());
                   intent.putExtra("name",item.getName());
                   context.startActivity(intent);
               }
           });

       }

    }


    public void addData(Result dataModel){
        verticalList.add(dataModel);
        notifyDataSetChanged();
    }

    // need to override this method
    @Override
    public int getItemViewType (int position) {

        if(isPositionHeader (position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader (int position) {
        return position < 1;
    }

    @Override
    public int getItemCount () {
        return verticalList.size () + 1;
    }


    class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder (View itemView) {
            super (itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    class GenericViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txtTitle)
        TextView txtTitle;
        @BindView(R.id.imgPhoto)
        ImageView imgPhoto;
        @BindView(R.id.txtBody)
        TextView txtBody;
        @BindView(R.id.main_layout)
        ConstraintLayout main_layout;


        public GenericViewHolder (View itemView) {
            super (itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    public void reset(){
        verticalList.clear();
        notifyDataSetChanged();
    }

}

