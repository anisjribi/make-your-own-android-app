package com.example.anisjr.currency;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.net.Uri;
import android.text.format.Time;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import android.widget.AdapterView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.Calendar;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import com.example.anisjr.currency.data.CurrencyContract;
import com.example.anisjr.currency.data.CurrencyContract.CurrencyEntry;
import java.util.Date;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import com.example.anisjr.currency.sync.CurrencySyncAdapter;


/**
 * Encapsulates fetching the history and displaying it as a {@link android.widget.ListView} layout.
 */
public class HistoryFragment extends Fragment implements LoaderCallbacks<Cursor>  {

    private HistoryAdapter mHistoryAdapter;

    private static final int HISTORY_LOADER = 0;
    private String  mCurrencuFrom;

    // For the HISTORY view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] HISTORY_COLUMNS = {

            CurrencyEntry.TABLE_NAME + "." + CurrencyEntry._ID,
            CurrencyEntry.COLUMN_DATETEXT,
            CurrencyEntry.COLUMN_RATE,
            CurrencyEntry.COLUMN_FROM_Currency,
            CurrencyEntry.COLUMN_TO_Currency

    };


    // These indices are tied to HISTORY_COLUMNS.  If HISTORY_COLUMNS changes, these
    // must change.
    public static final int COL_CURRENCY_ID = 0;
    public static final int COL_CURRENCY_DATE = 1;
    public static final int COL_CURRENCY_RATE = 2;
    public static final int COL_CURRENCY_FROM_Currency = 3;
    public static final int COL_CURRENCY_TO_Currency = 4;

/**
      * A callback interface that all activities containing this fragment must
      * implement. This mechanism allows activities to be notified of item
      * selections.
     */
        public interface Callback {
                /**
                 * DetailFragmentCallback for when an item has been selected.
                  */
                        public void onItemSelected(String date);
            }
    public HistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //FetchCurrencyTask currencyTask = new FetchCurrencyTask();
            //currencyTask.execute("USD");
                updateCurrency();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

                // The ArrayAdapter will take data from a source and
               // use it to populate the ListView it's attached to.

        mHistoryAdapter = new HistoryAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_history);
        listView.setAdapter(mHistoryAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    Cursor cursor = mHistoryAdapter.getCursor();
                    if (cursor != null && cursor.moveToPosition(position)) {

                        ((Callback)getActivity())
                                          .onItemSelected(cursor.getString(COL_CURRENCY_DATE));
                    }
                           }
                   });


        return rootView;
    }

    @Override
        public void onActivityCreated(Bundle savedInstanceState) {
                getLoaderManager().initLoader(HISTORY_LOADER, null, this);
               super.onActivityCreated(savedInstanceState);
            }


    private void updateCurrency() {
        CurrencySyncAdapter.syncImmediately(getActivity());
    }

    @Override
        public void onStart() {
                super.onStart();
                 updateCurrency();
            }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrencuFrom != null && !mCurrencuFrom.equals(Utility.getPreferredCurrencyFrom(getActivity()))) {
            getLoaderManager().restartLoader(HISTORY_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return currency only for dates after or including today.
        // Only return data after today.
        String startDate = CurrencyContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = CurrencyEntry.COLUMN_DATETEXT + " DESC";

        mCurrencuFrom = Utility.getPreferredCurrencyFrom(getActivity());
        Uri currencyForCurrencyFromUri = CurrencyEntry.buildCurrencyFromWithStartDate(
                mCurrencuFrom, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                currencyForCurrencyFromUri,
                HISTORY_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mHistoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHistoryAdapter.swapCursor(null);
    }

}