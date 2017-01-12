package com.digital.ayaz.stockhawk.ui.widgets;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;


import com.digital.ayaz.stockhawk.R;
import com.digital.ayaz.stockhawk.data.Constants;
import com.digital.ayaz.stockhawk.data.Contract;
import com.digital.ayaz.stockhawk.data.Preference;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StockWidgetRemoteViewService extends RemoteViewsService {

    public StockWidgetRemoteViewService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetItemRemoteView(this.getApplicationContext(), intent);
    }

    class WidgetItemRemoteView implements RemoteViewsFactory {
        Context mContext;
        Cursor mCursor;
        Intent mIntent;

        public WidgetItemRemoteView(Context mContext, Intent mIntent) {
            this.mContext = mContext;
            this.mIntent = mIntent;
        }

        @Override
        public void onCreate() {
            // nothing To DO
        }

        @Override
        public int getCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null)
                mCursor.close();

            final long pId = Binder.clearCallingIdentity();

            mCursor = getContentResolver().query(
                    Contract.Quote.uri,
                    null,
                    null,
                    null,
                    null
            );

            Binder.restoreCallingIdentity(pId);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            try {
                mCursor.moveToPosition(position);
                int priceChangeColorId;

                RemoteViews listItemRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget);
                listItemRemoteView.setTextViewText(R.id.stock_symbol, mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                listItemRemoteView.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex(Contract.Quote.COLUMN_PRICE)));


                float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                if (rawAbsoluteChange > 0) {
                    listItemRemoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.change_green);
                } else {
                    listItemRemoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.change_red);
                }

                DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                String change = dollarFormatWithPlus.format(rawAbsoluteChange);

                if (Preference.getInstance(getApplicationContext()).getDisplayMode()
                        .equals(Preference.DISPLAY_MODE_ABS)) {
                    listItemRemoteView.setTextViewText(R.id.change, change);
                } else {
                    listItemRemoteView.setTextViewText(R.id.change, change);
                }


                // set Onclick Item Intent
				Intent onClickItemIntent = new Intent();
				onClickItemIntent.putExtra(Constants.KEY_TAB_POSITION,position);
				listItemRemoteView.setOnClickFillInIntent(R.id.list_item_stock_quote,onClickItemIntent);
                return listItemRemoteView;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(mCursor.getColumnIndex(Contract.Quote._ID));
        }

        @Override
        public void onDestroy() {
            if (mCursor != null)
                mCursor.close();
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
