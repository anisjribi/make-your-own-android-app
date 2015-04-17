package com.example.anisjr.currency;

/**
 * Created by anisjr on 16/04/2015.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anisjr.currency.data.CurrencyContract;
import com.example.anisjr.currency.data.CurrencyContract.CurrencyEntry;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String CurrencyFrom_KEY = "currencyFrom";
    private String mCurrencyFrom;
    private String mHISTORY;
    private String mDateStr;

    private static final int DETAIL_LOADER = 0;
    private static final String[] HISTORY_COLUMNS = {
            CurrencyEntry.TABLE_NAME + "." + CurrencyEntry._ID,
            CurrencyEntry.COLUMN_DATETEXT,
            CurrencyEntry.COLUMN_RATE,
            CurrencyEntry.COLUMN_FROM_Currency,
            CurrencyEntry.COLUMN_TO_Currency,
    };

    private ImageView mIconView;
    private TextView mDateView;
    private TextView mRateView;
    private TextView mCurrencyFromView;
    private TextView mCurrencyToView;


    public DetailFragment() {
       // setHasOptionsMenu(true);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CurrencyFrom_KEY, mCurrencyFrom);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
                if (arguments != null) {
                        mDateStr = arguments.getString(DetailActivity.DATE_KEY);
                    }

                        if (savedInstanceState != null) {
                        mCurrencyFrom = savedInstanceState.getString(CurrencyFrom_KEY);
                    }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);

        mRateView = (TextView) rootView.findViewById(R.id.detail_rate_textview);
        mCurrencyFromView = (TextView) rootView.findViewById(R.id.detail_currencyFrom_textview);
        mCurrencyToView = (TextView) rootView.findViewById(R.id.detail_currencyTO_textview);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
                if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY) &&
                                mCurrencyFrom != null &&
                !mCurrencyFrom.equals(Utility.getPreferredCurrencyFrom(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }




    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrencyFrom = savedInstanceState.getString(CurrencyFrom_KEY);
        }
        Bundle arguments = getArguments();
                if (arguments != null && arguments.containsKey(DetailActivity.DATE_KEY)) {
                        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
                    }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        // Sort order:  Ascending, by date.
        String sortOrder = CurrencyContract.CurrencyEntry.COLUMN_DATETEXT + " DESC";

        mCurrencyFrom = Utility.getPreferredCurrencyFrom(getActivity());
        Uri currencyForCurrencyFromUri = CurrencyContract.CurrencyEntry.buildCurrencyFromWithHistoryDate(
                mCurrencyFrom, mDateStr);
        Log.v(LOG_TAG, currencyForCurrencyFromUri.toString());

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
        if (data != null && data.moveToFirst()) {
            // Read currency condition ID from cursor
//            int currencyId = data.getInt(data.getColumnIndex(currencyEntry.COLUMN_currency_ID));
            // Use placeholder Image
            mIconView.setImageResource(R.drawable.ic_launcher);

            // Read date from cursor and update views for day of week and date
            String date = data.getString(data.getColumnIndex(CurrencyEntry.COLUMN_DATETEXT));
            String dateText =  Utility.formatDate( date);
              mDateView.setText(dateText);

            // Read description from cursor and update view
            String currencyRate = data.getString(data.getColumnIndex(
                    CurrencyEntry.COLUMN_RATE));
            mRateView.setText(currencyRate);



            String currencyFrom = data.getString(data.getColumnIndex(CurrencyEntry.COLUMN_FROM_Currency));
            mCurrencyFromView.setText(currencyFrom);

            String currencyTo = data.getString(data.getColumnIndex(CurrencyEntry.COLUMN_TO_Currency));
            mCurrencyToView.setText(currencyTo);



            // We still need this for the share intent
            mHISTORY = String.format("%s - %s - %s/%s", dateText, currencyRate,currencyFrom , currencyTo);


            Log.v(LOG_TAG, "History String: " +  mHISTORY);



        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
