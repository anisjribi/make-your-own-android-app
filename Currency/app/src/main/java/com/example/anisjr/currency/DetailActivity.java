package com.example.anisjr.currency;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.content.Intent;
import android.widget.TextView;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.anisjr.currency.data.CurrencyContract;
import com.example.anisjr.currency.data.CurrencyContract.CurrencyEntry;

public class DetailActivity extends ActionBarActivity {
    public static final String DATE_KEY = "history_date";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
                        // using a fragment transaction.
                                String date = getIntent().getStringExtra(DATE_KEY);

                               Bundle arguments = new Bundle();
                        arguments.putString(DetailActivity.DATE_KEY, date);

                                DetailFragment fragment = new DetailFragment();
                       fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.currency_detail_container,fragment)
                    .commit();
        }
    }





}
