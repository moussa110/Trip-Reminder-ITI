package com.example.tripreminderapp.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.note.Note;
import com.example.tripreminderapp.database.trip.Trip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private FirebaseAuth auth =FirebaseAuth.getInstance();
    private final TripDatabase database;
    private final MutableLiveData<List<Trip>> tripsListLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Note>> notesLiveData = new MutableLiveData<>();


    public HistoryViewModel(@NonNull Application application) {
        super(application);
        database = TripDatabase.getInstance(getApplication());
        getTripsFromDatabase();
    }



     void getTripsFromDatabase() {
        tripsListLiveData.setValue(database.tripDao().getTripDone(auth.getCurrentUser().getEmail()));
    }

    public  void deleteTrip(Trip trip){
        TripDatabase.getInstance(getApplication()).tripDao().delete(trip);
        getTripsFromDatabase();
    }


    public void getNotesFromDatabase(int id){
        notesLiveData.setValue(database.noteDao().getNotes(id));
    }

    public void deleteNoteFromDatabase(Note note){
        database.noteDao().delete(note);
    }


    public MutableLiveData<List<Note>> getNotesLiveData() {
        return notesLiveData;
    }


    public MutableLiveData<List<Trip>> getTripsListLiveData() {
        return tripsListLiveData;
    }

}