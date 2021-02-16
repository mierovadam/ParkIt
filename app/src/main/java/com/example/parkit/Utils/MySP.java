package com.example.parkit.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MySP {
    public final static String SP_FILE = "sharedPreferencesFile";

    public final static String SP_LATITUDE_KEY       = "latitudeKay";
    public final static String SP_LONGITUDE_KEY      = "longitudeKey";
    public final static String SP_DESCR_KEY          = "descriptionKey";
    public final static String SP_PHOTO_KEY          = "photoKey";

    public final static String SP_PHOTO_INDOOR_KEY   = "photoIndoorKey";
    public final static String SP_COLOR_KEY          = "colorKey";
    public final static String SP_LEVEL_KEY          = "levelKey";
    public final static String SP_NUMBER_KEY         = "numberKey";
    public final static String SP_INDOOR_DESCR_KEY   = "descriptionIndoorKey";

    private static MySP instance;
    private SharedPreferences prefs;
    private Context context;

    public static MySP getInstance(){
        return instance;
    }

    private MySP(Context context){
        prefs = context.getSharedPreferences(SP_FILE,Context.MODE_PRIVATE);
        this.context = context;
    }

    public static void init(Context context){
        if(instance == null)
            instance = new MySP(context);
    }

    public void putString(String key, String value){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public void putFloat(String key, float value){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key,value);
        editor.apply();
    }

    public float getFloat(String key,float def) {
        return prefs.getFloat(key,def);
    }

    public String getString(String key,String def) {
        return prefs.getString(key,def);
    }

    public void removeKey(String key){
        SharedPreferences.Editor editor = prefs.edit() ;
        editor.remove(key).apply();
    }

    public void resetSP(){
        prefs.edit().clear().commit();
    }
}
