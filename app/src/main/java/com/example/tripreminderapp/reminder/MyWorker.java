package com.example.tripreminderapp.reminder;


import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {
    private Context context;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String date_time = getInputData().getString("data");

        Intent intent1 = new Intent(context,MyService.class);
        //intent1.putExtra("date_time",intent.getStringExtra("date_time"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent1);
        }else
            context.startService(intent1);

        return Result.success();
    }
}