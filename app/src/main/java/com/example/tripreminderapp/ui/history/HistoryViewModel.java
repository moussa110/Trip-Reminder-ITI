package com.example.tripreminderapp.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.trip.Trip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private FirebaseAuth auth =FirebaseAuth.getInstance();
    private final TripDatabase database;
    private final MutableLiveData<List<Trip>> tripsListLiveData = new MutableLiveData<>();


    public HistoryViewModel(@NonNull Application application) {
        super(application);
        database = TripDatabase.getInstance(getApplication());
        getTripsFromDatabase();
    }



    private void getTripsFromDatabase() {
        tripsListLiveData.setValue(database.tripDao().getTripDone(auth.getCurrentUser().getEmail()));
    }

    public MutableLiveData<List<Trip>> getTripsListLiveData() {
        return tripsListLiveData;
    }

//    public LiveData<String> getText() {
//        return mText;
//    }
}