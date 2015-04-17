package com.example.anisjr.currency.data;

/**
 * Created by anisjr on 2015-03-05.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.example.anisjr.currency.data.CurrencyContract.CurrencyEntry;

public class CurrencyDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "currency.db";

    public CurrencyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        final String SQL_CREATE_CURRENCY_TABLE = "CREATE TABLE " + CurrencyEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for currency
                // history, it's reasonable to assume the user will want information
                // for a certain date and all dates before, so the history data
                // should be sorted accordingly.
                CurrencyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +


                CurrencyEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                CurrencyEntry.COLUMN_FROM_Currency + " TEXT NOT NULL, " +
                CurrencyEntry.COLUMN_TO_Currency + " TEXT NOT NULL, " +

                CurrencyEntry.COLUMN_RATE + " REAL NOT NULL, " +



                // To assure the application have just one currency entry per day
                //  it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + CurrencyEntry.COLUMN_DATETEXT + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_CURRENCY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CurrencyEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
