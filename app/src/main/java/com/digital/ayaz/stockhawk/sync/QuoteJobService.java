package com.digital.ayaz.stockhawk.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;


public class QuoteJobService extends JobService {


    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Intent nowIntent = new Intent(getApplicationContext(), StockIntentService.class);
        getApplicationContext().startService(nowIntent);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }


}