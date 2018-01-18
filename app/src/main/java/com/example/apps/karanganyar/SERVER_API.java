package com.example.apps.karanganyar;


import com.example.apps.karanganyar.model.RedZone;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by ranggarizky on 6/4/2016.
 */
//setting retrofit untuk server sendiri
public interface SERVER_API {
    //variable base URL
    public static String baseURL = "http://prayuga.com/karanganyar/api/";

    Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    Retrofit client = new Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();


    @GET("disaster-histories")
    public Call<ArrayList<RedZone>> getDisasterHistory();

    @GET("red-zones")
    public Call<ArrayList<RedZone>> getRedzone();




}