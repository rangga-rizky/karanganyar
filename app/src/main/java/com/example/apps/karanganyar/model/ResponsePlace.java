package com.example.apps.karanganyar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by RanggaRizky on 1/6/2018.
 */

public class ResponsePlace {
    @SerializedName("predictions")
    @Expose
    private ArrayList<PlacePrediction> predictions = null;
    @SerializedName("status")
    @Expose
    private String status;

    public ArrayList<PlacePrediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(ArrayList<PlacePrediction> predictions) {
        this.predictions = predictions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
