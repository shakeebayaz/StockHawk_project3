package com.digital.ayaz.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.digital.ayaz.stockhawk.data.Constants;
import com.digital.ayaz.stockhawk.data.Contract;
import com.digital.ayaz.stockhawk.data.Preference;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.digital.ayaz.ACTION_DATA_UPDATED";
    public static final String ACTION_STOCK_EXIST = "com.digital.ayaz.STOCK_EXIST";
    public static final String ACTION_STOCK_HISTORY = "com.digital.ayaz.STOCK_HISTROY";

    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;

    static void getQuotes(Context context) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -2);
        try {
            Set<String> equityPref = Preference.getInstance(context).getStocks();
            Set<String> equityCopy = new HashSet<>();
            equityCopy.addAll(equityPref);
            String[] stockArray = equityPref.toArray(new String[equityPref.size()]);
            if (stockArray.length == 0) {
                return;
            }
            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = equityCopy.iterator();
            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();
                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();
                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);
                StringBuilder historyBuilder = new StringBuilder();
                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    historyBuilder.append(", ");
                    historyBuilder.append(it.getClose());
                    historyBuilder.append("\n");
                }
                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.uri,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
        }
    }

    public static boolean isStockFound(String stockSymbol) {
        try {
            Set<String> equityPref = new HashSet<>();
            equityPref.add(stockSymbol);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockCopy);
            String[]equity_arr = equityPref.toArray(new String[equityPref.size()]);
            if (equity_arr.length == 0) {
                return false;
            }
            Map<String, Stock> quotes = YahooFinance.get(equity_arr);
            Stock stock = quotes.get(stockSymbol);
            StockQuote quote = stock.getQuote();
            if (quote != null && quote.getPrice() != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    private static void initializescheduler(Context context) {
        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }
    synchronized public static void initialize(final Context context) {
        initializescheduler(context);
        syncImmediately(context);
    }

    synchronized public static void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, StockIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());
        }
    }

    public static void checkStockSymbolExistence(String symbol, Context context) {
        Intent nowIntent = new Intent(context, StockIntentService.class);
        nowIntent.putExtra(Constants.EXTRA_SYMBOL, symbol);
        nowIntent.putExtra(Constants.IS_CHECK_STOCK_SYMBOL, true);
        context.startService(nowIntent);
    }

    public static Stock getHistory(String symbol) {

        Stock stock = null;
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -1);
        try {
            Set<String> equityPref = new HashSet<>();
            equityPref.add(symbol);
            Set<String> equityCopy = new HashSet<>();
            equityCopy.addAll(equityCopy);
            String[] equityArray = equityPref.toArray(new String[equityPref.size()]);

            if (equityArray.length == 0) {
                return stock;
            }

            Map<String, Stock> quotes = YahooFinance.get(equityArray);
            stock = quotes.get(symbol);
            stock.getHistory(from, to, Interval.WEEKLY);



        } catch (IOException exception) {

        }
        return stock;
    }
}
