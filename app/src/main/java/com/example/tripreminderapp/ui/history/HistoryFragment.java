package com.example.tripreminderapp.ui.history;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripreminderapp.GeoLocation;
import com.example.tripreminderapp.R;
import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.note.Note;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.databinding.FragmentHistoryBinding;
import com.example.tripreminderapp.ui.trip_details.NotesAdapter;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private  final HistoryAdapter historyAdapter = new HistoryAdapter();
    private FirebaseAuth auth =FirebaseAuth.getInstance();
    private NotesAdapter notesAdapter = new NotesAdapter();
    HistoryViewModel historyViewModel;
    AlertDialog notesDialog;
 private AlertDialog addNoteDialog;
    String address;
    String lat,lang;
    double latitude,langtude;

    public HistoryFragment(){}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        binding.dashRvTrip.setAdapter(historyAdapter);

        historyAdapter.setOnDeletTripListener =new HistoryAdapter.OnDeletTripListener(){

            @Override
            public void onClick(Trip trip) {
                historyViewModel.deleteTrip(trip);
            }
        };

        historyAdapter.setOnShowNoteListener = new HistoryAdapter.OnShowNoteListener() {
            @Override
            public void onClick(Trip trip) {

                historyViewModel.getNotesFromDatabase(trip.getId());
            }
        };

        historyViewModel.getNotesLiveData().observe(getActivity(),notes -> {
            showNotesDialog(notes);
            notesAdapter.changeData(notes);
        });

        notesAdapter.setNoteDeleteClickListener = new NotesAdapter.NoteDeleteClickListener() {
            @Override
            public void onClick(Note note) {
                historyViewModel.deleteNoteFromDatabase(note);
                historyViewModel.getNotesFromDatabase(note.getTripId());
            }
        };

        notesAdapter.setNoteClickListener = new NotesAdapter.NoteClickListener() {
            @Override
            public void onClick(Note note) {
                //notesDialog.dismiss();
                showNoteDetailsDialog(note);
            }
        };

        binding.homeBtnAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),MapActivity.class));
            }
        });

        historyViewModel.getTripsFromDatabase();
        historyViewModel.getTripsListLiveData().observe(getActivity(),trips -> {
            historyAdapter.changeData(trips);
        });


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }






    //togetLangtude and latitude
    public void getLangLat(String address){

        GeoLocation geoLocation = new GeoLocation();
        geoLocation.getAddress(address, getActivity().getApplicationContext(),new GeoHandler());
    }

    private class GeoHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {

            switch(msg.what){
                case 1:
                    Bundle bundle = msg.getData();
                    address =bundle.getString("address");
                    break;
                default:
                    address=null;
            }
            Log.e("lll","the item is "+ address);
            //sentenseArray = address.split(",,,");
            //lat = sentenseArray[0];
            //lang = sentenseArray[1];

            //latitude = Double.parseDouble(lat);
            //langtude = Double.parseDouble(lang);


            Log.e("lll","lat item is "+ latitude);
            Log.e("lll","lang item is "+ langtude);


        }
    }

    public void showNotesDialog(List<Note> data) {


        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.dialog_show_notes, null);
        notesDialog = new AlertDialog.Builder(getActivity()).create();
        notesDialog.setView(view);
        if (data.size()==0)
            notesDialog.dismiss();
        RecyclerView recyclerView = view.findViewById(R.id.details_rv_note);
        recyclerView.setAdapter(notesAdapter);
        notesAdapter.changeData(data);
        notesDialog.show();

    }

    public void showNoteDetailsDialog(Note note) {
        final int[] flag = {0};
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.dialog_add_note, null);
        addNoteDialog = new AlertDialog.Builder(getActivity()).create();
        addNoteDialog.setView(view);
        TextInputLayout title = view.findViewById(R.id.ed_note_ttle);
        TextInputLayout description = view.findViewById(R.id.ed_note_disc);
        Button save = view.findViewById(R.id.btn_add_note);
        title.getEditText().setText(note.getTitle());
        description.getEditText().setText(note.getDescription());

        title.getEditText().setEnabled(false);
        description.getEditText().setEnabled(false);
        save.setText(R.string.noteUpdate);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag[0] == 0) {
                    title.getEditText().setEnabled(true);
                    description.getEditText().setEnabled(true);
                    save.setText(R.string.moteConfirm);
                    flag[0] = 1;
                }else {
                    note.setTitle(title.getEditText().getText().toString());
                    note.setDescription(description.getEditText().getText().toString());
                    TripDatabase.getInstance(getActivity()).noteDao().update(note);
                    addNoteDialog.dismiss();
                    //notesDialog.dismiss();

                }

            }
        });
        addNoteDialog.show();
    }




}