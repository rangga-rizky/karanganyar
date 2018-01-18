package com.example.ranggarizky.karanganyar;

import com.example.ranggarizky.karanganyar.model.ResponseDirection;
import com.example.ranggarizky.karanganyar.model.WeatherResponse;
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
public interface WeatherAPI {
    //variable base URL
    public static String baseURL = "http://api.openweathermap.org/data/2.5/";

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


    @GET("weather")
    public Call<WeatherResponse> getWheater(@Query("lat") String lat,
                                               @Query("lon") String lon,
                                               @Query("appid") String appid,
                                               @Query("units") String units);


}

