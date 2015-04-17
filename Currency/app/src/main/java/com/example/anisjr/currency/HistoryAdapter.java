package com.example.anisjr.currency;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link HistoryAdapter} exposes a list of currency history
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
/**
 * Created by anisjr on 11/04/2015.
 */
public class HistoryAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
        private static final int VIEW_TYPE_TODAY = 0;
        private static final int VIEW_TYPE_HISTORY_DAY = 1;

    /**
          * Cache of the children views for a history list item.
     */
        public static class ViewHolder {
                public final ImageView iconView;
                public final TextView dateView;
                public final TextView rateView;
                public final TextView currencyFromView;
                public final TextView currencyToView;

                        public ViewHolder(View view) {
                        iconView = (ImageView) view.findViewById(R.id.list_item_icon);
                        dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
                            rateView = (TextView) view.findViewById(R.id.list_item_historyRate_textview);
                            currencyFromView = (TextView) view.findViewById(R.id.list_item_currencyFrom_textview);
                            currencyToView = (TextView) view.findViewById(R.id.list_item_currencyTo_textview);
                    }
            }

                public HistoryAdapter(Context context, Cursor c, int flags) {
                super(context, c, flags);
            }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_today;
                break;
            }
            case VIEW_TYPE_HISTORY_DAY: {
                layoutId = R.layout.list_item_history;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

                        ViewHolder viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);

                        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        // Use placeholder image for now
        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

        // Read date from cursor
        String dateString = cursor.getString(HistoryFragment.COL_CURRENCY_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.formatDate(dateString));

        // Read currency history from cursor
        String rate = cursor.getString(HistoryFragment.COL_CURRENCY_RATE);
        // Find TextView and set currency history  on it
        viewHolder.rateView.setText(rate);


        // Read currencyFrom from cursor
        String currencyFrom = cursor.getString(HistoryFragment.COL_CURRENCY_FROM_Currency);
        viewHolder.currencyFromView.setText(currencyFrom);

        // Read currencyTo from cursor
        String currencyTO = cursor.getString(HistoryFragment.COL_CURRENCY_TO_Currency);
        viewHolder.currencyToView.setText(currencyTO);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_HISTORY_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}