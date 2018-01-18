package com.example.ranggarizky.karanganyar.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Fajar GBP on 12/4/2017.
 */

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "session_krngyar";

    private static final String KEY_IS_FIRSTIME = "isFirst";
    private static final String KEY_IS_FIRSTLOAD = "isFirstLOAD";
    private static final String KEY_IS_LOC = "loc";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstime(boolean isFirst) {

        editor.putBoolean(KEY_IS_FIRSTIME, isFirst);
        editor.commit();
    }

    public boolean isFirstime(){
        return pref.getBoolean(KEY_IS_FIRSTIME,true);
    }

    public void setFirstLoad(boolean isFirst) {

        editor.putBoolean(KEY_IS_FIRSTLOAD, isFirst);
        editor.commit();
    }

    public boolean isFirsLoad(){
        return pref.getBoolean(KEY_IS_FIRSTLOAD,true);
    }

    public void setLoc(String loc) {

        editor.putString(KEY_IS_LOC, loc);
        editor.commit();
    }

    public String getLoc(){
        return pref.getString(KEY_IS_LOC,null);
    }

}
