package com.digital.ayaz.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.digital.ayaz.stockhawk.R;
import com.digital.ayaz.stockhawk.Util.Utils;
import com.digital.ayaz.stockhawk.data.Constants;
import com.digital.ayaz.stockhawk.databinding.ActivityLineGraphBinding;
import com.digital.ayaz.stockhawk.sync.StockIntentService;
import com.digital.ayaz.stockhawk.sync.QuoteSyncJob;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;


/**
 * Created by Shakeeb on 12/01/17.
 */


public class StockDetailsActivity extends BaseActivity {

    public static Stock mStock;
    String symbol = "";
    private BroadcastEquityHistory mBroadcastEquityHistory;
    private ActivityLineGraphBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_line_graph);
        mStock = null;
        symbol = getIntent().getStringExtra(Constants.EXTRA_EQUITY_NAME);
        setSupportActionBar(mBinding.toolbar);

        getHistoricalData();
    }

    private void getHistoricalData() {
        mBroadcastEquityHistory = new BroadcastEquityHistory();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastEquityHistory,
                new IntentFilter(QuoteSyncJob.ACTION_STOCK_HISTORY));
        Intent nowIntent = new Intent(this, StockIntentService.class);
        nowIntent.putExtra(Constants.EXTRA_SYMBOL, symbol);
        nowIntent.putExtra(Constants.IS_GET_HISTORY, true);
        startService(nowIntent);
        showProgress();
    }

    public class BroadcastEquityHistory extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equalsIgnoreCase(QuoteSyncJob.ACTION_STOCK_HISTORY)) {
                hideProgress();
                LocalBroadcastManager.getInstance(StockDetailsActivity.this).unregisterReceiver(mBroadcastEquityHistory);
                getHistoryData();
            }
        }
    }

    private void getHistoryData() {
        if (mStock != null) {
            mBinding.setData(mStock);
            ArrayList<Entry> entries = new ArrayList<>();
            ArrayList<String> xvalues = new ArrayList<>();
            try {
                List<HistoricalQuote> historicalQuotes = mStock.getHistory();
                for (int i = 0; i < historicalQuotes.size(); i += 2) {
                    HistoricalQuote historicalData = historicalQuotes.get(i);
                    BigDecimal yValue = historicalData.getClose();
                    xvalues.add(Utils.getFormatedDate(historicalData.getDate().getTimeInMillis(), Constants.STOCK_COMPARE_DATE_FORMAT));
                    entries.add(new Entry(yValue.floatValue(), i));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            YAxis left = mBinding.lineChart.getAxisLeft();
            left.setEnabled(true);
            left.setLabelCount(10, true);
            left.setTextColor(Color.WHITE);

            XAxis xAxis = mBinding.lineChart.getXAxis();
            xAxis.setLabelsToSkip(5);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(13f);
            xAxis.setTextColor(Color.WHITE);


            mBinding.lineChart.setGridBackgroundColor(Color.rgb(24, 120, 218));
            mBinding.lineChart.setDescriptionColor(Color.WHITE);
            mBinding.lineChart.getAxisRight().setEnabled(false);
            mBinding.lineChart.getLegend().setTextSize(15f);
            mBinding.lineChart.setDrawGridBackground(true);

            mBinding.lineChart.setDescription(getResources().getString(R.string.last_one_year));
            mBinding.lineChart.animateX(2000);
            mBinding.lineChart.setData(new LineData(xvalues, new LineDataSet(entries, getResources().getString(R.string.equity))));
        } else {
            final Snackbar snackbar = Snackbar
                    .make(mBinding.lineGraphLayout, getString(R.string.no_data_show), Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getHistoricalData();
                        }
                    })
                    .setActionTextColor(Color.GREEN);
            TextView tv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.RED);
            snackbar.show();
        }
    }


}