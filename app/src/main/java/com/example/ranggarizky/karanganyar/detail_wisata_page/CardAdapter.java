package com.example.ranggarizky.karanganyar.detail_wisata_page;

import android.support.v7.widget.CardView;

/**
 * Created by RanggaRizky on 12/20/2017.
 */

public interface CardAdapter {

    int MAX_ELEVATION_FACTOR = 5;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();
}
