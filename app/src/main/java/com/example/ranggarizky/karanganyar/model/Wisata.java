package com.example.ranggarizky.karanganyar.model;

/**
 * Created by RanggaRizky on 12/19/2017.
 */

public class Wisata {
    private String place_id;
    private String title;
    private String body;
    private String image;

    public Wisata(String title,String body,String image,String place_id){
        this.title = title;
        this.body = body;
        this.image = image;
        this.place_id = place_id;
    }

    public Wisata(){};

    public void setBody(String body) {
        this.body = body;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getPlace_id() {
        return place_id;
    }
}
