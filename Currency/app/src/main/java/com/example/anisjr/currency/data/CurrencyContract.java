package com.example.anisjr.currency.data;

/**
 * Created by anisjr on 2015-03-05.
 */

import android.provider.BaseColumns;
import android.content.ContentUris;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
public class CurrencyContract {


    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.anisjr.currency";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.anisjr.currency/currency/ is a valid path for
    // looking at currency data.
    public static final String PATH_CURRENCY= "currency";

    public static String getDbDateString(Date date){
                // Because the API returns a unix timestamp (measured in seconds),
                        // it must be converted to milliseconds in order to be converted to valid date.
                               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
               return sdf.format(date);
           }

    /**
     * Converts a dateText to a long Unix time representation
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }


    /* Inner class that defines the table contents of the currency table */
    public static final class CurrencyEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CURRENCY).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CURRENCY;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CURRENCY;

        public static final String TABLE_NAME = "currency";


        // Date, stored as Text with format yyyy-MM-dd
        public static final String COLUMN_DATETEXT = "date";

        public static final String COLUMN_FROM_Currency = "fromCurrency";
        public static final String COLUMN_TO_Currency = "toCurrency";

        public static final String COLUMN_RATE = "rate";


        public static Uri buildCurrencyUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


        public static Uri buildCurrencyFrom(String fromCurrency) {
                        return CONTENT_URI.buildUpon().appendPath(fromCurrency).build();
                    }

        public static Uri buildCurrencyFromWithStartDate(
                                String fromCurrency, String StartDate) {
                        return CONTENT_URI.buildUpon().appendPath(fromCurrency)
                                       .appendQueryParameter(COLUMN_DATETEXT, StartDate).build();
                    }

        public static Uri buildCurrencyFromWithHistoryDate(String fromCurrency, String date) {
                        return CONTENT_URI.buildUpon().appendPath(fromCurrency).appendPath(date).build();
                    }

        public static String getFromCurrencyFromUri(Uri uri) {
                        return uri.getPathSegments().get(1);
                   }

        public static String getDateFromUri(Uri uri) {

            return uri.getPathSegments().get(2);
                    }

        public static String getStartDateFromUri(Uri uri) {
                        return uri.getQueryParameter(COLUMN_DATETEXT);
                   }



    }
}
