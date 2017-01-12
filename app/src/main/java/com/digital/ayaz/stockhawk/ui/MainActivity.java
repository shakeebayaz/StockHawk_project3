package com.digital.ayaz.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.digital.ayaz.stockhawk.R;
import com.digital.ayaz.stockhawk.data.Constants;
import com.digital.ayaz.stockhawk.data.Contract;
import com.digital.ayaz.stockhawk.data.Preference;
import com.digital.ayaz.stockhawk.databinding.ActivityMainBinding;
import com.digital.ayaz.stockhawk.sync.QuoteSyncJob;


public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        equityAdapter.EquitydapterOnClickHandler, SwipeRefreshLayout.OnRefreshListener {

    private static final int STOCK_LOADER = 0;
    private equityAdapter adapter;
    private BroadcastReceiver mEquityBroadcast;
    private ActivityMainBinding mBinding;


    @Override
    public void onClick(Cursor cursor, int position) {
        if (cursor.moveToPosition(position)) {
            Intent intent = new Intent(MainActivity.this, StockDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_EQUITY_NAME, adapter.getSymbolAtPosition(position));
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        if (!isNetworkConnected()) {
            mBinding.error.setText(getString(R.string.network_toast));
            mBinding.error.setVisibility(View.VISIBLE);
        }
        mBinding.refresh.setOnRefreshListener(this);
        mBinding.refresh.setRefreshing(true);
        setSupportActionBar(mBinding.toolbarActivityMyStocks);
        adapter = new equityAdapter(this, this);
        mBinding.recycler.setAdapter(adapter);
        mBinding.recycler.setLayoutManager(new LinearLayoutManager(this));
        showProgress();
        onRefresh();
        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                Preference.getInstance(mContext).removeStock(symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(mBinding.recycler);


    }
    @Override
    public void onRefresh() {

        QuoteSyncJob.syncImmediately(this);

        if (!isNetworkConnected() && adapter.getItemCount() == 0) {
            mBinding.refresh.setRefreshing(false);
            mBinding.error.setText(getString(R.string.network_toast));
            mBinding.error.setVisibility(View.VISIBLE);
        } else if (!isNetworkConnected()) {
            mBinding.refresh.setRefreshing(false);
            Toast.makeText(this, R.string.network_toast, Toast.LENGTH_LONG).show();
        } else if (Preference.getInstance(this).getStocks().size() == 0) {
            mBinding.refresh.setRefreshing(false);
            mBinding.error.setText(getString(R.string.no_data_show));
            mBinding.error.setVisibility(View.VISIBLE);
        } else {
            mBinding.error.setVisibility(View.GONE);
        }
    }

    public void button(View view) {
        new StockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.trim().isEmpty()) {

            if (isNetworkConnected()) {
                mEquityBroadcast = new StockCheckBroadcast();
                LocalBroadcastManager.getInstance(this).registerReceiver(mEquityBroadcast,
                        new IntentFilter(QuoteSyncJob.ACTION_STOCK_EXIST));
                QuoteSyncJob.checkStockSymbolExistence(symbol.trim(), MainActivity.this);
                showProgress();
            } else {
                showToast(getString(R.string.equity_added_no_internet));
            }
        }
    }

    public void addStockAndCallSyncData(String symbol) {
        if (!isNetworkConnected()) {
            showToast(getString(R.string.equity_added_no_internet));
        }else {
            mBinding.refresh.setRefreshing(true);
        }
        Preference.getInstance(this).addStock(symbol);
        QuoteSyncJob.syncImmediately(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.uri,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mBinding.refresh.setRefreshing(false);

        if (cursor.getCount() != 0) {
            mBinding.error.setVisibility(View.GONE);
        }
        adapter.setCursor(cursor);
        hideProgress();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (Preference.getInstance(this).getDisplayMode().equals(Preference.DISPLAY_MODE_ABS)) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_currency);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            Preference.getInstance(this).toggleDisplayMode();
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class StockCheckBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent in) {
            if (in != null && (in.getAction().equalsIgnoreCase(QuoteSyncJob.ACTION_STOCK_EXIST)) ) {
                hideProgress();
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mEquityBroadcast);
                if (in.getBooleanExtra(Constants.IS_STOCK_EXIST, false)) {
                    String symbol = in.getStringExtra(Constants.SYMBOL);
                    addStockAndCallSyncData(symbol);
                } else {
                    showToast(getString(R.string.stock_not_available));
                }
            }
        }
    }


}
