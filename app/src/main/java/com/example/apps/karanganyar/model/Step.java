package com.example.apps.karanganyar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by RanggaRizky on 11/25/2017.
 */
public class Step {
    @SerializedName("polyline")
    @Expose
    private PolylineObject polyline;

    @SerializedName("start_location")
    @Expose
    private Location start_location;

    public void setStart_location(Location start_location) {
        this.start_location = start_location;
    }

    public Location getStart_location() {
        return start_location;
    }

    public void setPolyline(PolylineObject polyline) {
        this.polyline = polyline;
    }

    public PolylineObject getPolyline() {
        return polyline;
    }
}
