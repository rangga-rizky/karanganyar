package com.example.apps.karanganyar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by RanggaRizky on 11/25/2017.
 */
public class Route {
    @SerializedName("legs")
    @Expose
    private ArrayList<Leg> legs = new ArrayList<>();

    public void setLegs(ArrayList<Leg> legs) {
        this.legs = legs;
    }

    public ArrayList<Leg> getLegs() {
        return legs;
    }


}
