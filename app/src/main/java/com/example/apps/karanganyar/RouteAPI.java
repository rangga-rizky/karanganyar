package com.example.apps.karanganyar;

import com.example.apps.karanganyar.model.ResponseDirection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ranggarizky on 6/4/2016.
 */
//setting retrofit untuk server GOOGLE DIRECTION API
public interface RouteAPI {
    //variable base URL
    public static String baseURL = "https://maps.googleapis.com/maps/api/directions/";

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




    @GET("json")
    public Call<ResponseDirection> getDirections(@Query("origin") String origin,
                                                 @Query("destination") String destination,
                                                 @Query("sensor") String sensor,
                                                 @Query("mode") String mode,
                                                 @Query("alternatives") String alternative);


}

