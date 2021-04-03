package com.example.tripreminderapp.ui.profile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tripreminderapp.LoginActivity;
import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.trip.Trip;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private MutableLiveData<List<Trip>> tripsLiveData = new MutableLiveData<>();

    public ProfileViewModel(Application application) {
        super(application);
        tripsLiveData.setValue(TripDatabase.getInstance(getApplication()).tripDao().getTripDone(LoginActivity.EMAIL));
    }

    public MutableLiveData<List<Trip>> getTripsLiveData() {
        return tripsLiveData;
    }
}