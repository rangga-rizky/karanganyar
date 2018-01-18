package com.example.ranggarizky.karanganyar.model;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by RanggaRizky on 12/31/2017.
 */

public class RouteData {

    private List<List<HashMap<String,String>>> lineData;
    private ArrayList<MarkerOptions> infoWindows = new ArrayList<>();
    private ArrayList<Boolean> isDangerous = new ArrayList<>();

    public void setLineData(List<List<HashMap<String, String>>> lineData) {
        this.lineData = lineData;
    }

    public List<List<HashMap<String, String>>> getLineData() {
        return lineData;
    }

    public void setInfoWindows(ArrayList<MarkerOptions> infoWindows) {
        this.infoWindows = infoWindows;
    }

    public ArrayList<MarkerOptions> getInfoWindows() {
        return infoWindows;
    }

    public void setIsDangerous(ArrayList<Boolean> isDangerous) {
        this.isDangerous = isDangerous;
    }

    public ArrayList<Boolean> getIsDangerous() {
        return isDangerous;
    }
}
