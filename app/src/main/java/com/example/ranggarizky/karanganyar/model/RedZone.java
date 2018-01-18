package com.example.ranggarizky.karanganyar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by RanggaRizky on 12/27/2017.
 */

public class RedZone {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("location")
    @Expose
    private HistoryLocation location;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("loss")
    @Expose
    private String loss;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HistoryLocation getLocation() {
        return location;
    }

    public void setLocation(HistoryLocation location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLoss() {
        return loss;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }


}
