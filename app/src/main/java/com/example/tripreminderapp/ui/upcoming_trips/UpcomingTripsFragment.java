package com.example.tripreminderapp.ui.upcoming_trips;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tripreminderapp.FloatWidgetService;
import com.example.tripreminderapp.LoginActivity;
import com.example.tripreminderapp.R;
import com.example.tripreminderapp.database.FirebaseHandler;
import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.note.Note;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.databinding.FragmentUpcomingBinding;
import com.example.tripreminderapp.ui.add_trip.AddTripActivity;
import com.example.tripreminderapp.ui.trip_details.TripDetailsActivity;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class UpcomingTripsFragment extends Fragment {
    public static boolean FLAG_START=false;
    private FragmentUpcomingBinding binding;
    public static final String UPCOMING_DETAILS_EXTRA = "UPCOMING_DETAILS_EXTRA";
    private final UpcomingTripAdapter upcomingTripAdapter = new UpcomingTripAdapter();
    private AlertDialog addNoteDialog;
    private AlertDialog addToHistory;
    private AlertDialog deletTripDialog;
    private UpcomingTripsViewModel tripsViewModel;

    List<Trip> trips;

    @Override
    public void onStart() {
        super.onStart();
        tripsViewModel.getTripsFromDatabase();
        trips = TripDatabase.getInstance(getActivity()).tripDao().getAll(LoginActivity.EMAIL);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUpcomingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        tripsViewModel = new ViewModelProvider(this).get(UpcomingTripsViewModel.class);
        binding.homeRvTrips.setAdapter(upcomingTripAdapter);
        trips = TripDatabase.getInstance(getActivity()).tripDao().getAll(LoginActivity.EMAIL);
        tripsViewModel.getTripsListLiveData().observe(getViewLifecycleOwner(), trips -> {
            upcomingTripAdapter.changeData(trips);
        });


        if (isFirstStartAfterLogin())
        {
            if (trips.size()==0) {
                SharedPreferences.Editor editor=getActivity().getSharedPreferences("start",MODE_PRIVATE).edit();
                editor.putBoolean("start",false);
                editor.commit();

                FirebaseHandler handler = new FirebaseHandler(getActivity());
                handler.getTripsByEmail();
                handler.onReceiveDataFroFirebase = new FirebaseHandler.OnReceiveDataFroFirebase() {
                    @Override
                    public void onReceive(Trip trip) {
                        tripsViewModel.insertInDatabase(trip);
                    }
                };
            }
        }

        upcomingTripAdapter.setAddNoteClickListener = new UpcomingTripAdapter.AddNoteClickListener() {
            @Override
            public void onClick(Trip trip) {
                showAddNoteDialog(trip.getId());
            }
        };

        upcomingTripAdapter.setTripClickListener = new UpcomingTripAdapter.TripClickListener() {
            @Override
            public void onClick(Trip trip) {
                Intent intent = new Intent(getActivity(), TripDetailsActivity.class);
                intent.putExtra(UPCOMING_DETAILS_EXTRA, trip);
                startActivity(intent);
            }
        };

        binding.homeBtnAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddTripActivity.class));
            }
        });



        upcomingTripAdapter.setStartTripListener =new UpcomingTripAdapter.StartTripListener() {
            @Override
            public void onClick(Trip trip) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getActivity().getPackageName()));
                    startActivityForResult(intent, 106);
                }
                showTripDialog(trip);
            }
        };

        upcomingTripAdapter.setCancelTripListener = new UpcomingTripAdapter.CancelTripListener() {
            @Override
            public void onClick(Trip trip) {
                showCancelDialog(trip);
            }
        };

        return view;
    }

    public void showAddNoteDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.dialog_add_note, null);
        addNoteDialog = new AlertDialog.Builder(getActivity()).create();
        addNoteDialog.setView(view);
        TextInputLayout title = view.findViewById(R.id.ed_note_ttle);
        TextInputLayout description = view.findViewById(R.id.ed_note_disc);
        view.findViewById(R.id.btn_add_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "note added ☺", Toast.LENGTH_SHORT).show();
                TripDatabase.getInstance(getActivity()).noteDao().insertNote(new Note(id, title.getEditText().getText().toString(), description.getEditText().getText().toString()));
                addNoteDialog.dismiss();
            }
        });
        addNoteDialog.show();
    }
    //Mido
    //to display track on map from user location to destination
    private void DisplayTrack( String sDestination) {
        //if device dosnt have mape installed then redirect it to play store

        try {
            //when google map installed
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/" + "/" + sDestination);

            //Action view with uri
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            //set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);


        } catch (ActivityNotFoundException e) {
            //when google map is not initialize
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            //set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }
    //show dialog to history
    public void showTripDialog(Trip trip) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.custom_dialog_for_trip, null);
        addToHistory = new AlertDialog.Builder(getActivity()).create();
        addToHistory.setView(view);
        view.findViewById(R.id.btn_okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getActivity().getPackageName()));
                    startActivityForResult(intent, 106);
                } else {
                    Intent startIntent = new Intent(getContext(), FloatWidgetService.class);
                    startIntent.putExtra("notes", (ArrayList<Note>) TripDatabase.getInstance(getActivity()).noteDao().getNotes(trip.getId()));
                    getActivity().startService(startIntent);
                }
                
                DisplayTrack(trip.getEndPoint());
                trip.setDone(true);
                tripsViewModel.updateTrip(trip);
                addToHistory.dismiss();
            }
        });

        addToHistory.show();
    }

    public void showCancelDialog(Trip trip){
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.custom_dialog_for_delete, null);
        deletTripDialog = new AlertDialog.Builder(getActivity()).create();
        deletTripDialog.setView(view);
        view.findViewById(R.id.btn_okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trip.setCanceled(true);
                tripsViewModel.updateTrip(trip);
                deletTripDialog.dismiss();
            }
        });
        deletTripDialog.show();


    }

    public boolean isFirstStartAfterLogin(){
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences("start",MODE_PRIVATE);
        boolean result=sharedPreferences.getBoolean("start",true);
        return result;
    }


}