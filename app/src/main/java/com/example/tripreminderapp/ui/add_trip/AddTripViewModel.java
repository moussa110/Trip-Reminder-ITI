package com.example.tripreminderapp.ui.add_trip;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.reminder.MyWorker;

import java.util.concurrent.TimeUnit;

public class AddTripViewModel extends AndroidViewModel {
    private MutableLiveData<Boolean> isInsertedLiveData =new MutableLiveData<Boolean>(false);
    private TripDatabase tripDatabase = TripDatabase.getInstance(getApplication());

    public AddTripViewModel(@NonNull Application application) {
        super(application);
    }

    public void insertTrip(Trip trip,long millis){
        tripDatabase.tripDao().insertTrip(trip);
        isInsertedLiveData.setValue(true);
        runWorker(millis,trip.getDate_time());
    }

    private void runWorker(long millis, String date_time){
        Data inputData = new Data.Builder()
                .putString("data", date_time)
                .build();
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(MyWorker.class)
                        .setInputData(inputData)
                        .setInitialDelay(millis, TimeUnit.MILLISECONDS)
                        .build();
        WorkManager.getInstance(getApplication()).enqueue(uploadWorkRequest);
    }

    public MutableLiveData<Boolean> getIsInsertedLiveData() {
        return isInsertedLiveData;
    }
}
