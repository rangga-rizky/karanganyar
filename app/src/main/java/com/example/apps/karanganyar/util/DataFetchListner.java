package com.example.apps.karanganyar.util;


import com.example.apps.karanganyar.model.Result;

/**
 * Created by vaishakha on 27/10/16.
 */
public interface DataFetchListner {
    // void onDeliverAllData(List<DataModel> dataModels);

    void onDeliverData(Result dataModel);

    void onHideDialog();
}
