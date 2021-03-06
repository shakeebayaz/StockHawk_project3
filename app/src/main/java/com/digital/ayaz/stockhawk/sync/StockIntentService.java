package com.digital.ayaz.stockhawk.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.digital.ayaz.stockhawk.R;
import com.digital.ayaz.stockhawk.data.Constants;
import com.digital.ayaz.stockhawk.ui.StockDetailsActivity;

import yahoofinance.Stock;


public class StockIntentService extends IntentService {

    public StockIntentService() {
        super(StockIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isEquityExist = false;
        isEquityExist = intent.getBooleanExtra(Constants.IS_CHECK_STOCK_SYMBOL, false);
        if (isEquityExist) {
            Intent dataUpdaInt = new Intent(QuoteSyncJob.ACTION_STOCK_EXIST);
            dataUpdaInt.putExtra(Constants.IS_STOCK_EXIST, QuoteSyncJob.isStockFound(intent.getStringExtra(Constants.EXTRA_SYMBOL)));
            dataUpdaInt.putExtra(Constants.SYMBOL, intent.getStringExtra(Constants.EXTRA_SYMBOL));
            LocalBroadcastManager.getInstance(this).sendBroadcast(dataUpdaInt);
        } else if (intent.getBooleanExtra(Constants.IS_GET_HISTORY, false)) {
            Stock equity = QuoteSyncJob.getHistory(intent.getStringExtra(Constants.EXTRA_SYMBOL));
            StockDetailsActivity.mStock = equity;
            Intent dataUpdaInt = new Intent(QuoteSyncJob.ACTION_STOCK_HISTORY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(dataUpdaInt);

        } else {


            try {

                QuoteSyncJob.getQuotes(getApplicationContext());
            } catch (Exception e) {
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getApplicationContext();
                        Toast.makeText(context, context.getString(R.string.stock_not_available), Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

        }

    }
}
