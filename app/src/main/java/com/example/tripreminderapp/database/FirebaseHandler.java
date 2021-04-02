package com.example.tripreminderapp.database;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tripreminderapp.database.trip.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import java.util.List;

public class FirebaseHandler {
    private Context context;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    String email = auth.getCurrentUser().getEmail();
    private String userEmail = email.replace('.','%');
    public FirebaseHandler(Context context) {
        this.context=context;
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            userEmail = user.getEmail();
//        }
    }

    public void getTripsByEmail() {

       // @RequiresApi(api = Build.VERSION_CODES.N) List<Trip> ssss(){ List<Trip>tripList = new ArrayList<>(); FirebaseDatabase.getInstance().getReference().child("trips").get().addOnCompleteListener(task -> { DataSnapshot result = task.getResult(); Iterable<DataSnapshot> children = result.getChildren(); children.forEach(dataSnapshot -> { Trip value = dataSnapshot.getValue(Trip.class); tripList.add(value); }); }); return tripList; }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("trips").child(userEmail);
        // calling add value event listener method
        // for getting the values from database.
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method is called when new child is added to
                // our data base and after adding new child
                // we are adding that item inside our array list and
                // notifying our adapter that the data in adapter is changed.
                Trip trip = snapshot.getValue(Trip.class);
                TripDatabase.getInstance(context).tripDao().insertTrip(trip);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method is called when the new child is added.
                // when the new child is added to our list we will be
                // notifying our adapter that data has changed.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // below method is called when we remove a child from our database.
                // inside this method we are removing the child from our array list
                // by comparing with it's value.
                // after removing the data we are notifying our adapter that the
                // data has been changed.
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method is called when we move our
                // child in our database.
                // in our code we are note moving any child.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }
    public void syncDataWithFirebaseDatabase() {
        deleteAllData();
        Toast.makeText(context, userEmail, Toast.LENGTH_SHORT).show();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference();
        List<Trip> tripList = TripDatabase.getInstance(context).tripDao().getAllf(email);
        for (int indx = 0; indx < tripList.size(); ++indx) {
            Trip trip = tripList.get(indx);
            reference.child("trips").child(userEmail).push().setValue(trip).addOnCompleteListener(task -> {
                Toast.makeText(context, "done", Toast.LENGTH_SHORT).show();
            });
        }
    }
    public void deleteAllData(){
        DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference("trips");
        mPostReference.removeValue();
    }
}
