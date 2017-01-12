package com.digital.ayaz.stockhawk.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.digital.ayaz.stockhawk.R;

/**
 * Created by Shakeeb on 12/01/17.
 */

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog mProgress;
protected  Context mContext;
    public void showProgress() {
        showProgress(getString(R.string.progress_msg));
    }

    public void showProgress(String msg) {
        if (mProgress == null) {
            mProgress = new ProgressDialog(this);
            mProgress.setCancelable(false);
            mProgress.getWindow().setGravity(Gravity.CENTER);
            mProgress.setMessage(msg);
            mProgress.setIndeterminate(true);
        }

        if (!mProgress.isShowing()) {
            mProgress.show();
        }
    }

    public void hideProgress() {
        if (getMainLooper().getThread().equals(Thread.currentThread())) {

            hideProgressInternal();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressInternal();
                }
            });
        }

    }

    private void hideProgressInternal() {
        if (mProgress != null && mProgress.isShowing() && !isFinishing()) {
            mProgress.dismiss();
        }
    }


    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showToast(final String msg) {

        if (getMainLooper().getThread().equals(Thread.currentThread())) {

            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        hideProgress();
        super.onDestroy();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
