package com.example.ranggarizky.karanganyar.alertpage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ranggarizky.karanganyar.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlertActivity extends AppCompatActivity {
    String alamat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        ButterKnife.bind(this);
        alamat = getIntent().getStringExtra("alamat");
    }

    @OnClick(R.id.btnRute)
    public void toRute(){
        Intent intent = new Intent(this,RouteActivity.class);
        intent.putExtra("alamat",alamat);
        startActivity(intent);
        finish();
    }
}
