package com.example.tripreminderapp.ui.trip_details;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripreminderapp.FloatWidgetService;
import com.example.tripreminderapp.R;
import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.note.Note;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.databinding.ActivityTripDetailsBinding;
import com.example.tripreminderapp.reminder.MyService;
import com.example.tripreminderapp.ui.upcoming_trips.UpcomingTripAdapter;
import com.example.tripreminderapp.ui.upcoming_trips.UpcomingTripsFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;



public class TripDetailsActivity extends AppCompatActivity {

    private final int REQ_CODE = 2615;
    private NotesAdapter notesAdapter = new NotesAdapter();
    private boolean isEditable = false;
    private Trip currentTrip;
    private ActivityTripDetailsBinding binding;
    private TripDetailsViewModel viewModel;
    //private Spinner spinner;

    private AlertDialog notesDialog=null;


    //Mido.com
    final Calendar myCalendar = Calendar.getInstance();
    private AlertDialog addNoteDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = ViewModelProviders.of(this).get(TripDetailsViewModel.class);
        getSupportActionBar().hide();

        Intent intent2 = new Intent(this, MyService.class);
        stopService(intent2);

        changeBehaviour(false);

        Places.initialize(getApplicationContext(), getString(R.string.api_places_key));
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(this);

        viewModel.getDoneLiveData().observe(this,aBoolean -> {
            if (aBoolean){
                Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
            }
        });
        binding.edStartPoint.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent, REQ_CODE);
            }
        });
        binding.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    currentTrip.setDone(true);
                    viewModel.updateTriptoDone(currentTrip);
                }else {
                    currentTrip.setDone(false);
                    viewModel.updateTriptoDone(currentTrip);
                }
            }
        });
        binding.edEndPoint.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent, REQ_CODE + 1);
            }
        });


        binding.imageView11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getBaseContext())) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getBaseContext().getPackageName()));
                            startActivityForResult(intent, 106);
                        } else {
                            Intent startIntent = new Intent(getBaseContext(), FloatWidgetService.class);
                            startIntent.putExtra("notes", (ArrayList<Note>) TripDatabase.getInstance(getBaseContext()).noteDao().getNotes(currentTrip.getId()));
                            getBaseContext().startService(startIntent);
                        };
                        displayTrack(currentTrip.getEndPoint());
                        currentTrip.setDone(true);
                        viewModel.updateTriptoDone(currentTrip);
                        finish();
                    }
                });

        currentTrip = (Trip) getIntent().getSerializableExtra(UpcomingTripsFragment.UPCOMING_DETAILS_EXTRA);
        binding.edName.getEditText().setText(currentTrip.getName());
        binding.edStartPoint.getEditText().setText(currentTrip.getStartPoint());
        binding.edEndPoint.getEditText().setText(currentTrip.getEndPoint());
        binding.edDate.setText(currentTrip.getDate());
        binding.edTime.setText(currentTrip.getTime());


       // viewModel.getNotesFromDatabase(currentTrip.getId());

        viewModel.getNotesLiveData().observe(this,notes -> {
            notesAdapter.changeData(notes);
        });

        viewModel.getNotesLiveData().observe(this,notes -> {
            showNotesDialog(notes);
        });

        viewModel.getIsEditable().observe(this,aBoolean -> {
                changeBehaviour(aBoolean);

        });

        binding.detailsBtnShownotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.getNotesFromDatabase(currentTrip.getId());
            }
        });

        notesAdapter.setNoteDeleteClickListener = new NotesAdapter.NoteDeleteClickListener() {
            @Override
            public void onClick(Note note) {
                viewModel.deleteNoteFromDatabase(note);
                viewModel.getNotesFromDatabase(currentTrip.getId());
            }
        };

        notesAdapter.setNoteClickListener = new NotesAdapter.NoteClickListener() {
            @Override
            public void onClick(Note note) {
                //notesDialog.dismiss();
                showNoteDetailsDialog(note);
            }
        };

        binding.detailsBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTrip.setName( binding.edName.getEditText().getText().toString());
                currentTrip.setStartPoint( binding.edStartPoint.getEditText().getText().toString());
                currentTrip.setEndPoint( binding.edEndPoint.getEditText().getText().toString());
                currentTrip.setDate( binding.edDate.getText().toString());
                currentTrip.setTime( binding.edTime.getText().toString());
                currentTrip.setDate_time(binding.edDate.getText()+" "+ binding.edTime.getText().toString());
                viewModel.updateTripInDatabase(currentTrip);

            }
        });

        //Mido
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        binding.edDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(TripDetailsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String _year = String.valueOf(year);
                        String _month = (month+1) < 10 ? "0" + (month+1) : String.valueOf(month+1);
                        String _date = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                        String _pickedDate = _year + "-" + _month + "-" + _date;
                        binding.edDate.setText(_pickedDate);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.MONTH));
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.show();


            }
        });


        binding.edTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TripDetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        binding.edTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            binding.edStartPoint.getEditText().setText(place.getAddress());
        } else if (requestCode == REQ_CODE + 1 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            binding.edEndPoint.getEditText().setText(place.getAddress());
        } else {
            //Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, "check network connection", Toast.LENGTH_SHORT).show();
        }
    }
    public void displayTrack( String sDestination) {
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

    public void changeBehaviour(Boolean isEditable) {
        if (isEditable) {
            binding.edName.getEditText().setEnabled(true);
            binding.edStartPoint.getEditText().setEnabled(true);
            binding.edEndPoint.getEditText().setEnabled(true);
            binding.edDate.setEnabled(true);
            binding.edTime.setEnabled(true);
            binding.detailsBtnEdit.setText("save");
        } else {
            binding.edName.getEditText().setEnabled(false);
            binding.edStartPoint.getEditText().setEnabled(false);
            binding.edEndPoint.getEditText().setEnabled(false);
            binding.edDate.setEnabled(false);
            binding.edTime.setEnabled(false);
            binding.detailsBtnEdit.setText("update");
        }
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        binding.edDate.setText(sdf.format(myCalendar.getTime()));
    }


    public void showNotesDialog(List<Note> data) {
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.dialog_show_notes, null);
        notesDialog = new AlertDialog.Builder(this).create();
        notesDialog.setView(view);
        RecyclerView recyclerView = view.findViewById(R.id.details_rv_note);
        TextView noNoteTv = view.findViewById(R.id.tv_no_note);
        recyclerView.setAdapter(notesAdapter);
        notesAdapter.changeData(data);
        notesDialog.show();
        if (data.size()==0) {
            noNoteTv.setVisibility(View.VISIBLE);
        }
    }


    public void showNoteDetailsDialog(Note note) {
        final int[] flag = {0};
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.dialog_add_note, null);
        addNoteDialog = new AlertDialog.Builder(this).create();
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
                    TripDatabase.getInstance(TripDetailsActivity.this).noteDao().update(note);
                    addNoteDialog.dismiss();
                    notesDialog.dismiss();

                }

            }
        });
        addNoteDialog.show();
    }

}