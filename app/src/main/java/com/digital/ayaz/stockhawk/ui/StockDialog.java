package com.digital.ayaz.stockhawk.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;

import com.digital.ayaz.stockhawk.R;
import com.digital.ayaz.stockhawk.databinding.AddStockDialogBinding;


public class StockDialog extends DialogFragment {


    private AddStockDialogBinding mBinding;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());

     mBinding=   DataBindingUtil.inflate(inflater,R.layout.add_stock_dialog,null,false);


        mBinding.dialogStock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addStock();
                return true;
            }
        });
        builder.setView(mBinding.getRoot());

        builder.setMessage(getString(R.string.popup_title));
        builder.setPositiveButton(getString(R.string.popup_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                });
        builder.setNegativeButton(getString(R.string.popup_cancel), null);

        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    private void addStock() {
        Activity parent = getActivity();
        if (parent instanceof MainActivity) {
            ((MainActivity) parent).addStock(mBinding.dialogStock.getText().toString());
        }
        dismissAllowingStateLoss();
    }


}
