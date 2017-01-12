package com.digital.ayaz.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;




public class DbHelper extends SQLiteOpenHelper {
    static final String NAME = "StockHawk.db";
    private static final int VERSION = 1;
    public DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Contract.Quote.TABLE_NAME + " (" +
                Contract.Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Contract.Quote.COLUMN_SYMBOL + " TEXT NOT NULL, " +
                Contract.Quote.COLUMN_PRICE + " REAL NOT NULL, " +
                Contract.Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, " +
                Contract.Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, " +
                Contract.Quote.COLUMN_HISTORY + " TEXT NOT NULL, " +
                "UNIQUE (" +  Contract.Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        db.execSQL(builder);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " +  Contract.Quote.TABLE_NAME);
        onCreate(db);
    }
}
