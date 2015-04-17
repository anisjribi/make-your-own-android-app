package com.example.anisjr.currency.sync;

/**
 * Created by anisjr on 17/04/2015.
 */


import android.accounts.Account;
        import android.accounts.AccountManager;
        import android.content.AbstractThreadedSyncAdapter;
        import android.content.ContentProviderClient;
        import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
        import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
        import android.util.Log;

        import com.example.anisjr.currency.R;
import com.example.anisjr.currency.Utility;
import com.example.anisjr.currency.data.CurrencyContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

public class CurrencySyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = CurrencySyncAdapter.class.getSimpleName();

    public CurrencySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        String currencyFromQuery = Utility.getPreferredCurrencyFrom(getContext());

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String historyJsonStr = null;

        String toCurrency = "TND";
        int numDays = 7;
        String dateEnd = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -6);
        Date daysBeforeDate2 = cal.getTime();
        String dateStart = new SimpleDateFormat("yyyy-MM-dd").format(daysBeforeDate2);

        try {
            // Construct the URL for the  query
            //URL url = new URL("http://jsonrates.com/historical/?apiKey=jr-26819dd94d982b400989e59f7007119e&from=USD&to=TND&dateStart=2014-06-17&dateEnd=2014-06-23");

            final String HISTORY_BASE_URL =
                    "http://jsonrates.com/historical/?";
            final String fromCurrency_PARAM = "from";
            final String toCurrency_PARAM = "to";
            final String dateStart_PARAM = "dateStart";
            final String dateEnd_PARAM = "dateEnd";
            final String apiKey_PARAM = "apiKey";

            Uri builtUri = Uri.parse(HISTORY_BASE_URL).buildUpon()
                    .appendQueryParameter(fromCurrency_PARAM, currencyFromQuery)
                    .appendQueryParameter(toCurrency_PARAM,toCurrency)
                            //   .appendQueryParameter(dateStart_PARAM, "2015-02-26")
                    .appendQueryParameter(dateStart_PARAM,dateStart)
                    .appendQueryParameter(dateEnd_PARAM,dateEnd )
                    .appendQueryParameter(apiKey_PARAM,"jr-26819dd94d982b400989e59f7007119e" )
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            // Create the request , and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            historyJsonStr = buffer.toString();

            Log.v(LOG_TAG,"History JSON String: "+historyJsonStr);
        } catch (IOException e) {
            Log.e("HistoryFragment", "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("HistoryFragment", "Error closing stream", e);
                }
            }
        }

        try {


            JSONObject historyJson = new JSONObject(historyJsonStr);
            JSONObject daysHistory = historyJson.getJSONObject("rates");
            String fromCurrency = (String) historyJson.get("from");

            Vector<ContentValues> cVVector = new Vector<ContentValues>(daysHistory.length());
            int i=0;


            Iterator<String> keys = daysHistory.keys();
            String date;

            Calendar cal2 = GregorianCalendar.getInstance();

            while (i<numDays) {
                cal2.setTime(new Date());
                cal2.add(Calendar.DAY_OF_YEAR, -i);
                Date daysBeforeDate = cal2.getTime();
                date = new SimpleDateFormat("yyyy-MM-dd").format(daysBeforeDate);
                JSONObject values = daysHistory.getJSONObject(date);
                Double histrate = values.getDouble("rate");

                ContentValues testValues = new ContentValues();
                testValues.put(CurrencyContract.CurrencyEntry.COLUMN_DATETEXT,  date);
                testValues.put(CurrencyContract.CurrencyEntry.COLUMN_FROM_Currency,  fromCurrency);
                testValues.put(CurrencyContract.CurrencyEntry.COLUMN_TO_Currency,"TND");
                testValues.put(CurrencyContract.CurrencyEntry.COLUMN_RATE,histrate);
                cVVector.add(testValues);


                i++;
            }
            // while (keys.hasNext()) {
            //  date = (String) keys.next();
            // JSONObject values = daysHistory.getJSONObject(date);
            // Double histrate = values.getDouble("rate");
            // resultStrs[i] =  date + " - " + histrate;
            // i++;
            //}






            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                getContext().getContentResolver().bulkInsert(CurrencyContract.CurrencyEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the history.
        return;


    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */


        }
        return newAccount;
    }

}