package com.example.anisjr.currency;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

public class MainActivity extends ActionBarActivity implements HistoryFragment.Callback  {
    private boolean mTwoPane;
     private String mCurrencyFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrencyFrom = Utility.getPreferredCurrencyFrom(this);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.currency_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.currency_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String CurrencyFrom = Utility.getPreferredCurrencyFrom( this );
        // update the currencyFrom in our second pane using the fragment manager

                        if (CurrencyFrom != null && !CurrencyFrom.equals(CurrencyFrom)) {
                            HistoryFragment ff = (HistoryFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_history);

                            mCurrencyFrom = CurrencyFrom;
            }
        }

    @Override
       public void onItemSelected(String date) {
                if (mTwoPane) {
                        // In two-pane mode, show the detail view in this activity by
                                // adding or replacing the detail fragment using a
                                        // fragment transaction.
                                                Bundle args = new Bundle();
                        args.putString(DetailActivity.DATE_KEY, date);

                                DetailFragment fragment = new DetailFragment();
                        fragment.setArguments(args);

                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.currency_detail_container, fragment)
                                        .commit();
                    } else {
                        Intent intent = new Intent(this, DetailActivity.class)
                                        .putExtra(DetailActivity.DATE_KEY, date);
                        startActivity(intent);
                    }
            }

}
