package com.example.tripreminderapp.ui.trip_details;

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
import com.example.tripreminderapp.database.note.Note;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.reminder.MyWorker;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TripDetailsViewModel extends AndroidViewModel {
    public TripDetailsViewModel(@NonNull Application application) {
        super(application);
    }
    private final TripDatabase database =TripDatabase.getInstance(getApplication());
    private MutableLiveData<List<Note>> notesLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> doneLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isEditable = new MutableLiveData<>(false);

    public MutableLiveData<Boolean> getDoneLiveData() {
        return doneLiveData;
    }

    public void getNotesFromDatabase(int id){
        notesLiveData.setValue(database.noteDao().getNotes(id));
    }

    public void deleteNoteFromDatabase(Note note){
        database.noteDao().delete(note);
    }

    public void updateTripInDatabase(Trip trip, long diff){
       if (isEditable.getValue()) {
           WorkManager.getInstance().cancelAllWorkByTag(trip.getWorkerTag());
           String tag = generateTag();
           trip.setWorkerTag(tag);
           database.tripDao().update(trip);
           runWorker(diff,trip.getDate_time(),tag);
           isEditable.setValue(false);
       }else {
           isEditable.setValue(true);
        }
       }
    public void updateTriptoDone(Trip trip){
        WorkManager.getInstance().cancelAllWorkByTag(trip.getWorkerTag());
        String tag = generateTag();
        trip.setWorkerTag(tag);
        database.tripDao().update(trip);
        doneLiveData.setValue(true);
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

    public MutableLiveData<List<Note>> getNotesLiveData() {
        return notesLiveData;
    }

    public MutableLiveData<Boolean> getIsEditable() {
        return isEditable;
    }



}
