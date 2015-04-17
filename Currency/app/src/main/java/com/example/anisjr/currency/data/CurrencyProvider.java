package com.example.anisjr.currency.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by anisjr on 2015-03-06.
 */

public class CurrencyProvider extends ContentProvider {

    private static final int CURRENCY = 100;
    private static final int CURRENCY_WITH_FROMCURRENCY = 101;
    private static final int CURRENCY_WITH_FROMCURRENCY_AND_DATE = 102;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CurrencyDbHelper mOpenHelper;


    private static final SQLiteQueryBuilder sCurrencyByFromCurrencySettingQueryBuilder;

    static{
        sCurrencyByFromCurrencySettingQueryBuilder = new SQLiteQueryBuilder();
        sCurrencyByFromCurrencySettingQueryBuilder.setTables(
                CurrencyContract.CurrencyEntry.TABLE_NAME  );


    }

    private static final String sFromCurrencySelection =
            CurrencyContract.CurrencyEntry.TABLE_NAME+
                    "." + CurrencyContract.CurrencyEntry.COLUMN_FROM_Currency + " = ? ";
    private static final String sFromCurrencyWithStartDateSelection =
            CurrencyContract.CurrencyEntry.TABLE_NAME+
                    "." + CurrencyContract.CurrencyEntry.COLUMN_FROM_Currency + " = ? AND " +
                    CurrencyContract.CurrencyEntry.COLUMN_DATETEXT + " <= ? ";
    private static final String sFromCurrencyAndDaySelection =
            CurrencyContract.CurrencyEntry.TABLE_NAME+
                    "." + CurrencyContract.CurrencyEntry.COLUMN_FROM_Currency + " = ? AND " +
                    CurrencyContract.CurrencyEntry.COLUMN_DATETEXT + " = ? ";


    private Cursor getCurrencyByCurrencyFrom(Uri uri, String[] projection, String sortOrder) {
        String currencyFromSetting = CurrencyContract.CurrencyEntry.getFromCurrencyFromUri(uri);
        String startDate = CurrencyContract.CurrencyEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sFromCurrencySelection;
            selectionArgs = new String[]{currencyFromSetting};
        } else {
            selectionArgs = new String[]{currencyFromSetting, startDate};
            selection = sFromCurrencyWithStartDateSelection;
        }

        return sCurrencyByFromCurrencySettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getCurrencyByCurrencyFromAndDate(Uri uri, String[] projection, String sortOrder) {
        String currencyFromSetting = CurrencyContract.CurrencyEntry.getFromCurrencyFromUri(uri);
        String Date = CurrencyContract.CurrencyEntry.getDateFromUri(uri);
        return sCurrencyByFromCurrencySettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                               projection,
                               sFromCurrencyAndDaySelection,
                              new String[]{currencyFromSetting, Date},
                               null,
                               null,
                               sortOrder
                                );
    }


    private static UriMatcher buildUriMatcher() {
        // Why create a UriMatcher when you can use regular
        // expressions instead?
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CurrencyContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, CurrencyContract.PATH_CURRENCY, CURRENCY);
        matcher.addURI(authority, CurrencyContract.PATH_CURRENCY + "/*", CURRENCY_WITH_FROMCURRENCY);
        matcher.addURI(authority, CurrencyContract.PATH_CURRENCY + "/*/*", CURRENCY_WITH_FROMCURRENCY_AND_DATE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new CurrencyDbHelper(getContext());
        return true;
    }


    @Override
    // public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "currency/*/*"
            case CURRENCY_WITH_FROMCURRENCY_AND_DATE:
            {
                retCursor = getCurrencyByCurrencyFromAndDate(uri, projection, sortOrder);
                break;
            }
            // "currency/*"
            case CURRENCY_WITH_FROMCURRENCY: {
                retCursor = getCurrencyByCurrencyFrom(uri, projection, sortOrder);
                break;
            }
            // "currency"
            case CURRENCY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CurrencyContract.CurrencyEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CURRENCY_WITH_FROMCURRENCY_AND_DATE:
                return CurrencyContract.CurrencyEntry.CONTENT_ITEM_TYPE;//can only a single row
            case CURRENCY_WITH_FROMCURRENCY:
                return CurrencyContract.CurrencyEntry.CONTENT_TYPE;//return multiple items
            case CURRENCY:
                return CurrencyContract.CurrencyEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CURRENCY: {
                long _id = db.insert(CurrencyContract.CurrencyEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CurrencyContract.CurrencyEntry.buildCurrencyUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case CURRENCY:
                rowsDeleted = db.delete(
                        CurrencyContract.CurrencyEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CURRENCY:
                rowsUpdated = db.update(CurrencyContract.CurrencyEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CURRENCY:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(CurrencyContract.CurrencyEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
