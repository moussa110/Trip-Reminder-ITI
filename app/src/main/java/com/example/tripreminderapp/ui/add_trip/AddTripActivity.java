package com.example.tripreminderapp.ui.add_trip;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.tripreminderapp.LoginActivity;
import com.example.tripreminderapp.R;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.databinding.ActivityAddTripBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddTripActivity extends AppCompatActivity {

    private static final String TAG = "ADD TRIP ACTIVITY";
    private ActivityAddTripBinding binding;
    private static final int REQ_CODE = 111;
    private AddTripViewModel viewModel;
    private Trip trip = new Trip();
    private Calendar calendar;
    private Calendar roundCalendar;
    private FirebaseAuth auth =FirebaseAuth.getInstance();

    @Override
    protected void onStart() {
        super.onStart();
        calendar =Calendar.getInstance();
        roundCalendar =Calendar.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //view binding
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //view model
        viewModel = ViewModelProviders.of(this).get(AddTripViewModel.class);
        getSupportActionBar().hide();

        //init google places autocomplete
        Places.initialize(getApplicationContext(), getString(R.string.api_places_key));
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(AddTripActivity.this);

        viewModel.getIsInsertedLiveData().observe(this,aBoolean -> {
            if (aBoolean)
                finish();
        });

        binding.edStartPoint.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent, REQ_CODE);
            }
        });

        binding.edEndPoint.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent, REQ_CODE + 1);
            }
        });

        binding.edDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Calendar c = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(AddTripActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR , year);
                        calendar.set(Calendar.MONTH , month);
                        calendar.set(Calendar.DAY_OF_MONTH , dayOfMonth);
                        String date = DateFormat.getDateInstance(DateFormat.DEFAULT).format(calendar.getTime());
                        binding.edDate.setText(date);
                    }
                } , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.show();
            }

        });

        binding.edTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTripActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        calendar.set(Calendar.MINUTE , selectedMinute);
                        calendar.set(Calendar.HOUR_OF_DAY , selectedHour);
                        calendar.set(Calendar.SECOND , 0);
                        binding.edTime.setText(selectedHour+ " : " + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        binding.addNewTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFromEditText();
                if(validateError()) {
                    Calendar current = Calendar.getInstance();
                    long nowMillis = current.getTimeInMillis();
                    long diff = calendar.getTimeInMillis() - nowMillis;
                    Log.e(TAG, "onClick: diff millis "+diff);
                    viewModel.insertTrip(trip,diff);
                    if (binding.roundSwitch.isChecked()){
                         nowMillis = current.getTimeInMillis();
                         diff = roundCalendar.getTimeInMillis() - nowMillis;
                        Log.e(TAG, "onClick: diff millis 2"+diff);
                         setRoundTripData();
                        viewModel.insertTrip(trip,diff);
                    }
                }
            }
        });

        binding.edRoundDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(AddTripActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        roundCalendar.set(Calendar.YEAR , year);
                        roundCalendar.set(Calendar.MONTH , month);
                        roundCalendar.set(Calendar.DAY_OF_MONTH , dayOfMonth);
                        String date = DateFormat.getDateInstance(DateFormat.DEFAULT).format(roundCalendar.getTime());
                        binding.edRoundDate.setText(date);
                    }
                } , roundCalendar.get(Calendar.YEAR), roundCalendar.get(Calendar.MONTH), roundCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.show();
            }

        });

        binding.edRoundTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTripActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        roundCalendar.set(Calendar.MINUTE , selectedMinute);
                        roundCalendar.set(Calendar.HOUR_OF_DAY , selectedHour);
                        roundCalendar.set(Calendar.SECOND , 0);
                        binding.edRoundTime.setText(selectedHour+ " : " + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        binding.roundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    binding.edRoundDate.setVisibility(View.GONE);
                    binding.edRoundTime.setVisibility(View.GONE);
                } else {
                    binding.edRoundDate.setVisibility(View.VISIBLE);
                    binding.edRoundTime.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setRoundTripData() {
        LatLng endLatLng=new LatLng(trip.getStartLatitude(),trip.getEndLongitude());
        trip.setDate(binding.edRoundDate.getText().toString());
        trip.setTime(binding.edRoundTime.getText().toString());
        trip.setStartPoint(trip.getEndPoint());
        trip.setStartLongitude(trip.getEndLongitude());
        trip.setStartLatitude(trip.getEndLatitude());
        trip.setEndPoint(binding.edStartPoint.getEditText().getText().toString());
        trip.setEndLongitude(endLatLng.longitude);
        trip.setEndLatitude(endLatLng.latitude);
    }

    private void getDataFromEditText() {
        trip.setName(binding.edName.getEditText().getText().toString());
        trip.setStartPoint(binding.edStartPoint.getEditText().getText().toString());
        trip.setEndPoint(binding.edEndPoint.getEditText().getText().toString());
        trip.setEmail(LoginActivity.EMAIL);
        trip.setDate(binding.edDate.getText().toString());
        trip.setTime(binding.edTime.getText().toString());
        trip.setDate_time(binding.edDate.getText().toString()+" "+binding.edTime.getText().toString());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            binding.edStartPoint.getEditText().setText(place.getAddress());
            trip.setStartLatitude( place.getLatLng().latitude);
            trip.setStartLongitude(place.getLatLng().longitude);

        } else if (requestCode == REQ_CODE + 1 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            binding.edEndPoint.getEditText().setText(place.getAddress());
            trip.setEndLatitude( place.getLatLng().latitude);
            trip.setEndLongitude(place.getLatLng().longitude);
        } else {
            Toast.makeText(this, "check network connection ..", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean validateError() {
        String tNameVal = binding.edName.getEditText().getText().toString();
        String spointVal = binding.edStartPoint.getEditText().getText().toString();
        String epointVal = binding.edEndPoint.getEditText().getText().toString();
        String dateVal = binding.edDate.getText().toString();
        String timeVal = binding.edTime.getText().toString();
        String date2Val = binding.edRoundDate.getText().toString();
        String time2Val = binding.edRoundTime.getText().toString();
        if(tNameVal.isEmpty() ){
            binding.edName.setError("TripName Required");
            binding.edName.requestFocus();
            return false;
        }
        else if(spointVal.isEmpty()) {
            binding.edStartPoint.setError("Start Point required");
            binding.edStartPoint.requestFocus();

            return false;
        }
        else if(epointVal.isEmpty()) {
            binding.edEndPoint.setError("End Point required");
            binding.edEndPoint.requestFocus();
            return false;
        }
        else if(dateVal.isEmpty()) {
            binding.edDate.setError("date reqquired");
            binding.edDate.requestFocus();
            return false;
        }
        else if(timeVal.isEmpty()) {
            binding.edTime.setError("Time required");
            binding.edTime.requestFocus();
            return false;
        }else if(time2Val.isEmpty() && binding.roundSwitch.isChecked()) {
            binding.edRoundTime.setError("Time required");
            binding.edRoundTime.requestFocus();
            return false;
        }else if(date2Val.isEmpty() && binding.roundSwitch.isChecked()) {
            binding.edRoundDate.setError("date reqquired");
            binding.edRoundDate.requestFocus();
            return false;
        }
        else {
            binding.edName.setError(null);
            binding.edName.setErrorEnabled(false);

            binding.edStartPoint.setError(null);
            binding.edStartPoint.setErrorEnabled(false);

            binding.edEndPoint.setError(null);
            binding.edEndPoint.setErrorEnabled(false);

            binding.edDate.setError(null);

            binding.edTime.setError(null);

            binding.edRoundDate.setError(null);

            binding.edRoundTime.setError(null);



            return true;
        }

    }


}




