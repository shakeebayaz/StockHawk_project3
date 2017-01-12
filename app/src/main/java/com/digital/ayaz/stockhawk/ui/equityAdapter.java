package com.digital.ayaz.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digital.ayaz.stockhawk.R;
import com.digital.ayaz.stockhawk.data.Contract;
import com.digital.ayaz.stockhawk.data.Preference;
import com.digital.ayaz.stockhawk.databinding.ListItemQuoteBinding;
import com.digital.ayaz.stockhawk.model.EquityDataModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by ats on 12/01/17.
 */
public class equityAdapter extends RecyclerView.Adapter<equityAdapter.RecordReceivedAdapterViewHolder> {

    private final Context context;
    private final EquitydapterOnClickHandler clickHandler;
    private final DecimalFormat mDollarFormat;
    private final DecimalFormat mDollarFormat2;
    private final DecimalFormat mPercentageFormat;
    private Cursor cursor;

    equityAdapter(Context context, EquitydapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

        mDollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mDollarFormat2 = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mDollarFormat2.setPositivePrefix("+$");
        mPercentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        mPercentageFormat.setMaximumFractionDigits(2);
        mPercentageFormat.setMinimumFractionDigits(2);
        mPercentageFormat.setPositivePrefix("+");

    }

    public interface EquitydapterOnClickHandler {
        void onClick(Cursor cursor, int position);
    }

    @Override
    public RecordReceivedAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ListItemQuoteBinding listItemQuoteBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.list_item_quote, parent, false);
        return new RecordReceivedAdapterViewHolder(listItemQuoteBinding);
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {

        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public void onBindViewHolder(RecordReceivedAdapterViewHolder holder, int position) {
        cursor.moveToPosition(position);
        EquityDataModel mEquity = new EquityDataModel();
        mEquity.symbal = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        mEquity.price = mDollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        if (rawAbsoluteChange > 0) {
            mEquity.isPositive = true;
        } else {
            mEquity.isPositive = false;
        }
        mEquity.change = mDollarFormat2.format(rawAbsoluteChange);
        mEquity.percentage = mPercentageFormat.format(percentageChange / 100);

        if (Preference.getInstance(context).getDisplayMode()
                .equals(Preference.DISPLAY_MODE_ABS)) {
           mEquity.isPercentage=false;
        } else {
            mEquity.isPercentage=true;
        }
        holder.bindItem(mEquity);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }

    public class RecordReceivedAdapterViewHolder extends RecyclerView.ViewHolder {
        private final ListItemQuoteBinding mBinding;

        public RecordReceivedAdapterViewHolder(ListItemQuoteBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void OnEquityClicked(View view) {
            clickHandler.onClick(cursor, getAdapterPosition());

        }

        public void bindItem(EquityDataModel object) {
            mBinding.setData(object);
            mBinding.setClickHandler(this);
        }
    }
}
