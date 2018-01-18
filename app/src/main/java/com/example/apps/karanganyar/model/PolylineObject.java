package com.example.apps.karanganyar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by RanggaRizky on 11/25/2017.
 */
public class PolylineObject {
    @SerializedName("points")
    @Expose
    private String points;

    public void setPoints(String points) {
        this.points = points;
    }

    public String getPoints() {
        return points;
    }


}
