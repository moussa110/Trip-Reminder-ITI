package com.example.tripreminderapp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.tripreminderapp.HomeActivity;
import com.example.tripreminderapp.LoginActivity;
import com.example.tripreminderapp.database.trip.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FirebaseHandler {
    private Context context;
    public OnReceiveDataFroFirebase onReceiveDataFroFirebase;
    private String userEmail = LoginActivity.EMAIL.replace('.','%');
    public FirebaseHandler(Context context) {
        this.context=context;

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            userEmail = user.getEmail();
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getTripsByEmail() {
        if (isFirstStartAfterLogin()) {
            SharedPreferences.Editor editor = context.getSharedPreferences("start", MODE_PRIVATE).edit();
            editor.putBoolean("start", false);
            editor.commit();
                FirebaseDatabase.getInstance().getReference().child("trips").child(userEmail).get().addOnCompleteListener(task -> {
                    DataSnapshot result = task.getResult();
                    Iterable<DataSnapshot> children = result.getChildren();
                    children.forEach(dataSnapshot -> {
                        Trip value = dataSnapshot.getValue(Trip.class);
                        if (onReceiveDataFroFirebase != null)
                            onReceiveDataFroFirebase.onReceive(value);
                    });
                });
           /* SharedPreferences.Editor editor = context.getSharedPreferences("start", MODE_PRIVATE).edit();
            editor.putBoolean("start", false);
            editor.commit();

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

                    Log.e("TAGss", "onChildAdded: " + trip.getId());
                    onReceiveDataFroFirebase.onReceive(trip);

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

            });*/
        }
    }
    public void syncDataWithFirebaseDatabase() {
        deleteAllData();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference();
        List<Trip> tripList = TripDatabase.getInstance(context).tripDao().getAll(LoginActivity.EMAIL);
        for (int indx = 0; indx < tripList.size(); ++indx) {
            Trip trip = tripList.get(indx);
            reference.child("trips").child(userEmail).push().setValue(trip).addOnCompleteListener(task -> {
                Toast.makeText(context, "done", Toast.LENGTH_SHORT).show();
            });
        }
    }
    public void deleteAllData(){
        DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference("trips").child(userEmail);
        mPostReference.removeValue();
    }

    public interface OnReceiveDataFroFirebase{
         void onReceive(Trip trip);
    }

    public boolean isFirstStartAfterLogin(){
        SharedPreferences sharedPreferences=context.getSharedPreferences("start",MODE_PRIVATE);
        boolean result=sharedPreferences.getBoolean("start",true);
        return result;
    }
}
