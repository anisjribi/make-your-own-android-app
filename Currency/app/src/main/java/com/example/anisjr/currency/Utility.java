package com.example.anisjr.currency;

/**
 * Created by anisjr on 2015-03-10.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.example.anisjr.currency.data.CurrencyContract;
import java.text.DateFormat;
import java.util.Date;

public class Utility {
    public static String getPreferredCurrencyFrom(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_currencyFrom_key),
                context.getString(R.string.pref_currencyFrom_default));
    }

    static String formatDate(String dateString) {
                Date date = CurrencyContract.getDateFromDb(dateString);
                return DateFormat.getDateInstance().format(date);
            }


}
