package com.example.ranggarizky.karanganyar.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.ranggarizky.karanganyar.model.Geometry;
import com.example.ranggarizky.karanganyar.model.Location;
import com.example.ranggarizky.karanganyar.model.Photo;
import com.example.ranggarizky.karanganyar.model.Result;
import com.example.ranggarizky.karanganyar.model.Wisata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RanggaRizky on 12/24/2017.
 */

public class DBHandler  extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 4;
    // Database Name
    private static final String DATABASE_NAME = "kdb";
    // table name
    private static final String TABLE_WISATA = "wisata";
    private static final String TABLE_IMAGE = "image";
    private static final String TABLE_DETAIL = "detail";
    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ALAMAT = "alamat";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_IMAGE_BLOB = "image_blob";
    private static final String KEY_PLACESID = "place_id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "lng";
    private static final String KEY_JAMBUKA = "jambuka";
    private static final String KEY_PROVINSI = "provinsi";
    private static final String KEY_RATING = "rating";
    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WISATA_TABLE = "CREATE TABLE " + TABLE_WISATA + "("
        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
        + KEY_ALAMAT + " TEXT," + KEY_IMAGE + " TEXT," +KEY_PLACESID + " TEXT,"
                +KEY_LAT + " TEXT," +KEY_LONG + " TEXT,"  + KEY_IMAGE_BLOB + " blob"  + ")";
        /*String CREATE_DETAIL_TABLE = "CREATE TABLE " + TABLE_WISATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_ALAMAT + " TEXT," + KEY_IMAGE + " TEXT," +KEY_PLACESID + " TEXT,"
                +KEY_LAT + " TEXT," +KEY_LONG + " TEXT"  + ")";*/
        db.execSQL(CREATE_WISATA_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISATA);
        // Creating tables again
        onCreate(db);
    }

    public void addWisata(Result data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, data.getName()); // Shop Name
        values.put(KEY_ALAMAT, data.getFormattedAddress());
        values.put(KEY_IMAGE, data.getPhotos().get(0).getPhotoReference());
        values.put(KEY_PLACESID, data.getPlaceId());
        values.put(KEY_LAT, data.getGeometry().getLocation().getLat());
        values.put(KEY_LONG, data.getGeometry().getLocation().getLng());
        values.put(KEY_IMAGE_BLOB, Utility.getPictureByteOfArray(data.getPicture()));
        // Inserting Row
        db.insert(TABLE_WISATA, null, values);
        //db.close(); // Closing database connection
    }

    public void addListWisata(List<Result> data) {
        SQLiteDatabase db = this.getWritableDatabase();
        for(int i = 0; i< data.size();i++){
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, data.get(i).getName()); // Shop Name
            values.put(KEY_ALAMAT, data.get(i).getFormattedAddress());
            values.put(KEY_IMAGE, data.get(i).getPhotos().get(0).getPhotoReference());
            values.put(KEY_PLACESID, data.get(i).getPlaceId());
            values.put(KEY_LAT, data.get(i).getGeometry().getLocation().getLat());
            values.put(KEY_LONG, data.get(i).getGeometry().getLocation().getLng());
            values.put(KEY_IMAGE_BLOB, Utility.getPictureByteOfArray(data.get(i).getPicture()));
            // Inserting Row
            db.insert(TABLE_WISATA, null, values);
        }
     //   db.close(); // Closing database connection
    }

    // Getting All Shops
    public ArrayList<Result> getAllWisataByName(String name) {
        ArrayList<Result> dataList = new ArrayList<Result>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_WISATA +  " WHERE " +  KEY_NAME +
                "  LIKE  '%" +name + "%' ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Result data = new Result();
                data.setName(cursor.getString(1));
                data.setFormattedAddress(cursor.getString(2));
                data.setPlaceId(cursor.getString(4));
                Location loc = new Location();
                loc.setLat(Double.valueOf(cursor.getString(5)));
                loc.setLng(Double.valueOf(cursor.getString(6)));
                Geometry geo = new Geometry();
                geo.setLocation(loc);
                data.setGeometry(geo);
                // Adding contact to list
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        // return contact list
        return dataList;
    }



    public void clearWisata() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WISATA,null,null);
      //  db.close();
    }

    public void fetchData(DataFetchListner listener) {
        DataFetcher fetcher = new DataFetcher(listener, this.getWritableDatabase());
        fetcher.start();
    }

    public void searchWisata(DataFetchWHereListner listener,String q) {
        DataFetcherWhere fetcher = new DataFetcherWhere(listener, this.getWritableDatabase(),q);
        fetcher.start();
    }

    public class DataFetcher extends Thread{
        private final DataFetchListner mListener;
        private final SQLiteDatabase mDb;

        public DataFetcher(DataFetchListner listener, SQLiteDatabase db) {
            mListener = listener;
            mDb = db;
        }

        @Override
        public void run() {

            Cursor cursor = mDb.rawQuery("SELECT * FROM " + TABLE_WISATA, null);

            final List<Result> dataList = new ArrayList<>();

            if (cursor.getCount() > 0) {

                if (cursor.moveToFirst()) {
                    do {
                        Result data = new Result();
                        data.setName(cursor.getString(1));
                        data.setFormattedAddress(cursor.getString(2));
                        ArrayList<Photo> photos = new ArrayList<>();
                        Photo foto = new Photo();
                        foto.setPhotoReference(cursor.getString(3));
                        photos.add(foto);
                        data.setPhotos(photos);
                        data.setPlaceId(cursor.getString(4));
                        Location loc = new Location();
                        loc.setLat(Double.valueOf(cursor.getString(5)));
                        loc.setLng(Double.valueOf(cursor.getString(6)));
                        Geometry geo = new Geometry();
                        geo.setLocation(loc);
                        data.setGeometry(geo);
                        // Adding contact to list
                        dataList.add(data);
                        data.setFromDatabase(true);
                        data.setPicture(Utility.getBitmapFromByte(cursor.getBlob(cursor.getColumnIndex(KEY_IMAGE_BLOB))));
                         Log.e("VALUE_Got",data.getName());
                        dataList.add(data);
                        publishFlower(data);
                    } while (cursor.moveToNext());
                }
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onHideDialog();

                }
            });
        }


        public void publishFlower(final Result data) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onDeliverData(data);
                }
            });
        }
    }

    public class DataFetcherWhere extends Thread{
        private final DataFetchWHereListner mListener;
        private final SQLiteDatabase mDb;
        private String q="";

        public DataFetcherWhere(DataFetchWHereListner listener, SQLiteDatabase db,String q) {
            mListener = listener;
            mDb = db;
            this.q = q;
        }

        @Override
        public void run() {

            Cursor cursor = mDb.rawQuery( "SELECT * FROM " + TABLE_WISATA +
                    " WHERE " +  KEY_NAME +
                    "  LIKE  '%" +this.q + "%' "
                    , null);

            final List<Result> dataList = new ArrayList<>();

            if (cursor.getCount() > 0) {

                if (cursor.moveToFirst()) {
                    do {
                        Result data = new Result();
                        data.setName(cursor.getString(1));
                        data.setFormattedAddress(cursor.getString(2));
                        ArrayList<Photo> photos = new ArrayList<>();
                        Photo foto = new Photo();
                        foto.setPhotoReference(cursor.getString(3));
                        photos.add(foto);
                        data.setPhotos(photos);
                        data.setPlaceId(cursor.getString(4));
                        Location loc = new Location();
                        loc.setLat(Double.valueOf(cursor.getString(5)));
                        loc.setLng(Double.valueOf(cursor.getString(6)));
                        Geometry geo = new Geometry();
                        geo.setLocation(loc);
                        data.setGeometry(geo);
                        // Adding contact to list
                        dataList.add(data);
                        data.setFromDatabase(true);
                        data.setPicture(Utility.getBitmapFromByte(cursor.getBlob(cursor.getColumnIndex(KEY_IMAGE_BLOB))));
                        Log.e("VALUE_Got_where",data.getName());
                        dataList.add(data);
                        publishFlower(data);
                    } while (cursor.moveToNext());
                }
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onHideDialog();

                }
            });
        }


        public void publishFlower(final Result data) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onDeliverData(data);
                }
            });
        }
    }
}
