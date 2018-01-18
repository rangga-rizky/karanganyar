package com.example.ranggarizky.karanganyar.historypage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ranggarizky.karanganyar.R;
import com.example.ranggarizky.karanganyar.model.RedZone;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by RanggaRizky on 12/27/2017.
 */

public class HistoryAdapter  extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private List<RedZone> verticalList;
    private Context context;
    private int limit;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txtTanggal)
        TextView txtTanggal;
        @BindView(R.id.txtBulan)
        TextView txtBulan;
        @BindView(R.id.txtTahun)
        TextView txtTahun;
        @BindView(R.id.txtAlamat)
        TextView txtAlamat;
        @BindView(R.id.txtKerugian)
        TextView txtKerugian;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }


    public HistoryAdapter(Context context, List<RedZone> verticalList ){
        this.verticalList = verticalList;
        this.context = context;
        this.limit = limit;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        RedZone item = verticalList.get(position);
        String[] tanggal = item.getTime().split("-");
        String bulan = context.getResources().getStringArray(R.array.bulan)[Integer.valueOf(tanggal[1])];

        holder.txtAlamat.setText(item.getLocation().getAddress());
        holder.txtBulan.setText(bulan);
        holder.txtKerugian.setText(item.getLoss());
        holder.txtTahun.setText(tanggal[0]);
        holder.txtTanggal.setText(tanggal[2]);


    }


    @Override
    public int getItemCount() {
        return verticalList.size();
    }



}

