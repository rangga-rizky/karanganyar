package com.example.ranggarizky.karanganyar.util;


import com.example.ranggarizky.karanganyar.model.Result;

/**
 * Created by vaishakha on 27/10/16.
 */
public interface DataFetchWHereListner {
    // void onDeliverAllData(List<DataModel> dataModels);

    void onDeliverData(Result dataModel);

    void onHideDialog();
}
