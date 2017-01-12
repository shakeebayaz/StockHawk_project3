package com.digital.ayaz.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Preference {
    public static final String DISPLAY_TITLE="display_mode_title";
    public static final String DISPLAY_MODE="display_mode";
    public static final String DISPLAY_MODE_DEFAULT="percentage";
    public static final String DISPLAY_MODE_ABS="absolute";
    public static final String DISPLAY_PERCENTAGE="percentage";
    public static final String INITIALIZE="initialized";
    public static final String EQUITY="equity";
    public  void editStockPref(String symbol, Boolean add) {
        Set<String> stocks = getStocks();
        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }
        sharedPrefrence.edit().putStringSet(EQUITY, stocks).commit();
    }

    public  void addStock(String symbol) {
        editStockPref(symbol, true);
    }

    public  void removeStock(String symbol) {
        editStockPref(symbol, false);
    }

    public  String getDisplayMode() {

        return sharedPrefrence.getString(DISPLAY_MODE, DISPLAY_MODE_DEFAULT);
    }

    public  void toggleDisplayMode() {
        String displayMode = getDisplayMode();
        if (displayMode.equals(DISPLAY_MODE_ABS)) {
            sharedPrefrence.edit().putString(DISPLAY_MODE, DISPLAY_PERCENTAGE).commit();
        } else {
            sharedPrefrence.edit().putString(DISPLAY_MODE, DISPLAY_MODE_ABS).commit();
        }

    }

    private final String FILENAME = "ayaz_digital_equity";
    private static Preference instance = null;
    private SharedPreferences sharedPrefrence;

    public static Preference getInstance(Context context) {
        if (instance == null) {
            instance = new Preference(context);
        }
        return instance;
    }

    private Preference(Context context) {
        sharedPrefrence = context.getApplicationContext().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
    }
    public  Set<String> getStocks() {
        String[] defaultStocksList = new String[]{"YHOO","AAPL","MSFT","FB"};
        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        boolean initialized = sharedPrefrence.getBoolean(INITIALIZE, false);
        if (!initialized) {
            SharedPreferences.Editor editor = sharedPrefrence.edit();
            editor.putBoolean(INITIALIZE, true);
            editor.putStringSet(EQUITY, defaultStocks);
            editor.apply();
            return defaultStocks;
        }
        return sharedPrefrence.getStringSet(EQUITY, new HashSet<String>());

    }

}
