package com.example.apps.karanganyar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by RanggaRizky on 11/25/2017.
 */
public class Leg {
    @SerializedName("steps")
    @Expose
    private ArrayList<Step> steps = new ArrayList<>();

    @SerializedName("duration")
    @Expose
    private Duration duration;

    @SerializedName("distance")
    @Expose
    private Distance distance;

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Distance getDistance() {
        return distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setSteps(ArrayList<Step> steps) {
        this.steps = steps;
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }
}
