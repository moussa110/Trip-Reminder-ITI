package com.example.tripreminderapp.ui.add_trip;

import android.app.Application;
import android.util.Log;

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

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AddTripViewModel extends AndroidViewModel {
    private MutableLiveData<Boolean> isInsertedLiveData =new MutableLiveData<Boolean>(false);
    private TripDatabase tripDatabase = TripDatabase.getInstance(getApplication());
    public AddTripViewModel(@NonNull Application application) {
        super(application);
    }

    public void insertTrip(Trip trip,long millis){
        String tag = generateTag();
        trip.setWorkerTag(tag);
        tripDatabase.tripDao().insertTrip(trip);
        isInsertedLiveData.setValue(true);
        runWorker(millis,trip.getDate_time(),tag);
    }

    private void runWorker(long millis, String dateTime, String tag){
        Data inputData = new Data.Builder()
                .putString("data", dateTime)
                .build();
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(MyWorker.class)
                        .setInputData(inputData)
                        .setInitialDelay(millis, TimeUnit.MILLISECONDS)
                        .addTag(tag)
                        .build();
        WorkManager.getInstance(getApplication()).enqueue(uploadWorkRequest);
    }

    private String generateTag(){
        // create a string of all characters
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        int length = 7;
        for(int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }
        Log.e("TAGdasasa", "generateTag: "+sb.toString() );
        return  sb.toString();
    }

    public MutableLiveData<Boolean> getIsInsertedLiveData() {
        return isInsertedLiveData;
    }
}
