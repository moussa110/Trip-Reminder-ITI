package com.example.tripreminderapp.ui.upcoming_trips;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.trip.Trip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UpcomingTripsViewModel extends AndroidViewModel {
    private final TripDatabase database;
    private FirebaseAuth auth =FirebaseAuth.getInstance();
    private final MutableLiveData<List<Trip>> tripsListLiveData = new MutableLiveData<>();
  //  private MutableLiveData<Boolean> isInsertedLiveData =new MutableLiveData<Boolean>(false);

    public UpcomingTripsViewModel(@NonNull Application application) {
        super(application);
        database = TripDatabase.getInstance(getApplication());
        getTripsFromDatabase();
    }


    public void getTripsFromDatabase() {
        tripsListLiveData.setValue(database.tripDao().getAll(auth.getCurrentUser().getEmail()));
    }

    public void deleteTrip(Trip trip){
        TripDatabase.getInstance(getApplication()).tripDao().delete(trip);
    }



    private void updateTrip(Trip trip) {
        database.tripDao().update(trip);
    }

    public MutableLiveData<List<Trip>> getTripsListLiveData() {
        return tripsListLiveData;
    }

}
