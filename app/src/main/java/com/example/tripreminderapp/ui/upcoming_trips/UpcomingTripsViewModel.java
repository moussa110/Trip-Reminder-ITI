package com.example.tripreminderapp.ui.upcoming_trips;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.tripreminderapp.LoginActivity;
import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.trip.Trip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UpcomingTripsViewModel extends AndroidViewModel {
    private final TripDatabase database;
    private FirebaseAuth auth =FirebaseAuth.getInstance();
    private final MutableLiveData<List<Trip>> tripsListLiveData = new MutableLiveData<>();

    public UpcomingTripsViewModel(@NonNull Application application) {
        super(application);
        database = TripDatabase.getInstance(getApplication());
        getTripsFromDatabase();
    }


    public void getTripsFromDatabase() {
        tripsListLiveData.setValue(database.tripDao().getUpComing(LoginActivity.EMAIL));
    }

    public void deleteTrip(Trip trip){
        TripDatabase.getInstance(getApplication()).tripDao().delete(trip);
    }

    public void insertInDatabase(Trip trip) {
        database.tripDao().insertTrip(trip);
        getTripsFromDatabase();
    }



    public void updateTrip(Trip trip) {
        database.tripDao().update(trip);
        getTripsFromDatabase();
    }

    public MutableLiveData<List<Trip>> getTripsListLiveData() {
        return tripsListLiveData;
    }

}
