package com.example.ranggarizky.karanganyar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by RanggaRizky on 11/25/2017.
 */
public class ResponseDirection {
    @SerializedName("routes")
    @Expose
    private ArrayList<Route> routes = new ArrayList<>();

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }
}
