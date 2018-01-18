package com.example.ranggarizky.karanganyar;


import com.example.ranggarizky.karanganyar.model.GooglePlaceRespond;
import com.example.ranggarizky.karanganyar.model.GooglePlaceSingleRespond;
import com.example.ranggarizky.karanganyar.model.ResponsePlace;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ranggarizky on 6/4/2016.
 */
public interface API {
    //variable base URL
    public static String baseURL = "https://maps.googleapis.com/maps/api/place/";

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


    @GET("textsearch/json")
    public Call<GooglePlaceRespond> getPOI(@Query("query") String query,
                                           @Query("language") String language,
                                              @Query("key") String key);

    @GET("textsearch/json")
    public Call<GooglePlaceRespond> getPOI(@Query("query") String query,
                                           @Query("language") String language,
                                           @Query("pagetoken") String pagetoken,
                                           @Query("key") String key);

    @GET("autocomplete/json")
    public Call<ResponsePlace> getAutoComplete(@Query("input") String query,
                                      @Query("key") String key);

    @GET("details/json")
    public Call<GooglePlaceSingleRespond> getPlacebyId(@Query("placeid") String id,
                                                       @Query("language") String language,
                                                       @Query("key") String key);


}