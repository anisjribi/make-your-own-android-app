package com.example.anisjr.currency;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import com.example.anisjr.currency.data.CurrencyContract;
import com.example.anisjr.currency.data.CurrencyContract.CurrencyEntry;
import java.util.Vector;
import android.database.DatabaseUtils;

/**
 * Created by anisjr on 2015-03-06.
 */
public class FetchCurrencyTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchCurrencyTask.class.getSimpleName();
    private ArrayAdapter<String> mHistoryAdapter;
    private final Context mContext;

    public FetchCurrencyTask(Context context)
    {
                mContext = context;

    }

    private boolean DEBUG = true;


    /**
     * Take the String representing the complete history in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     */
    private void getCurrencyDataFromJson(String historyJsonStr, int numDays)
            throws JSONException {


        JSONObject historyJson = new JSONObject(historyJsonStr);
        JSONObject daysHistory = historyJson.getJSONObject("rates");
        String fromCurrency = (String) historyJson.get("from");

        Vector<ContentValues> cVVector = new Vector<ContentValues>(daysHistory.length());
        int i=0;


        Iterator<String> keys = daysHistory.keys();
        String date;

        Calendar cal = GregorianCalendar.getInstance();

        while (i<numDays) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date daysBeforeDate = cal.getTime();
            date = new SimpleDateFormat("yyyy-MM-dd").format(daysBeforeDate);
            JSONObject values = daysHistory.getJSONObject(date);
            Double histrate = values.getDouble("rate");

            ContentValues testValues = new ContentValues();
            testValues.put(CurrencyEntry.COLUMN_DATETEXT,  date);
            testValues.put(CurrencyEntry.COLUMN_FROM_Currency,  fromCurrency);
            testValues.put(CurrencyEntry.COLUMN_TO_Currency,"TND");
            testValues.put(CurrencyEntry.COLUMN_RATE,histrate);
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

            mContext.getContentResolver().bulkInsert(CurrencyEntry.CONTENT_URI, cvArray);
        }


    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no currency code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

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
        Date daysBeforeDate = cal.getTime();
        String dateStart = new SimpleDateFormat("yyyy-MM-dd").format(daysBeforeDate);

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
                    .appendQueryParameter(fromCurrency_PARAM, params[0])
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
                return null;
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
                return null;
            }
            historyJsonStr = buffer.toString();

            Log.v(LOG_TAG,"History JSON String: "+historyJsonStr);
        } catch (IOException e) {
            Log.e("HistoryFragment", "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return null;
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
            getCurrencyDataFromJson(historyJsonStr, numDays);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the history.
        return null;
    }


}
