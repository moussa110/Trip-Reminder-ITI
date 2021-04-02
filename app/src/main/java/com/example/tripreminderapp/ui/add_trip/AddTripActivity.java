package com.example.tripreminderapp.ui.add_trip;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

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

    private ActivityAddTripBinding binding;
    private static final int REQ_CODE = 111;
    private AddTripViewModel viewModel;
    private Trip trip = new Trip();
    private Calendar fCalendar;
    private Calendar rCalendar;
    private FirebaseAuth auth =FirebaseAuth.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = ViewModelProviders.of(this).get(AddTripViewModel.class);
        getSupportActionBar().hide();
        fCalendar =Calendar.getInstance();
        rCalendar =Calendar.getInstance();


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
                Calendar c = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(AddTripActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fCalendar.set(Calendar.YEAR , year);
                        fCalendar.set(Calendar.MONTH , month);
                        fCalendar.set(Calendar.DAY_OF_MONTH , dayOfMonth);
                        String date = DateFormat.getDateInstance(DateFormat.DEFAULT).format(fCalendar.getTime());
                        binding.edDate.setText(date);
                    }
                } , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
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
                mTimePicker = new TimePickerDialog(AddTripActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        fCalendar.set(Calendar.MINUTE , selectedMinute);
                        fCalendar.set(Calendar.HOUR_OF_DAY , selectedHour);
                        fCalendar.set(Calendar.SECOND , 0);
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
                trip.setName(binding.edName.getEditText().getText().toString());
                trip.setStartPoint(binding.edStartPoint.getEditText().getText().toString());
                trip.setEndPoint(binding.edEndPoint.getEditText().getText().toString());
                trip.setEmail(auth.getCurrentUser().getEmail());
                trip.setDate(binding.edDate.getText().toString());
                trip.setTime(binding.edTime.getText().toString());
                trip.setDate_time(binding.edDate.getText().toString()+" "+binding.edTime.getText().toString());
                if(validateError() == true) {
                    Calendar calendar = Calendar.getInstance();
                    long nowMillis = calendar.getTimeInMillis();
                    long diff = fCalendar.getTimeInMillis() - nowMillis;
                    viewModel.insertTrip(trip,diff);
                    if (binding.switch1.isChecked()){
                         nowMillis = calendar.getTimeInMillis();
                         diff = rCalendar.getTimeInMillis() - nowMillis;
                        LatLng endLatLng=new LatLng(trip.getStartLatitude(),trip.getEndLongitude());
                        trip.setDate(binding.edDate2.getText().toString());
                        trip.setTime(binding.edTime2.getText().toString());
                        trip.setStartPoint(trip.getEndPoint());
                        trip.setStartLongitude(trip.getEndLongitude());
                        trip.setStartLatitude(trip.getEndLatitude());
                        trip.setEndPoint(binding.edStartPoint.getEditText().getText().toString());
                        trip.setEndLongitude(endLatLng.longitude);
                        trip.setEndLatitude(endLatLng.latitude);
                        viewModel.insertTrip(trip,diff);
                    }
                }
            }
        });

        binding.edDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(AddTripActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        rCalendar.set(Calendar.YEAR , year);
                        rCalendar.set(Calendar.MONTH , month);
                        rCalendar.set(Calendar.DAY_OF_MONTH , dayOfMonth);
                        String date = DateFormat.getDateInstance(DateFormat.FULL).format(fCalendar.getTime());
                        binding.edDate2.setText(date);
                    }
                } , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.MONTH));
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                dialog.show();
            }

        });

        binding.edTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTripActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        rCalendar.set(Calendar.MINUTE , minute);
                        rCalendar.set(Calendar.HOUR_OF_DAY , selectedHour);
                        rCalendar.set(Calendar.SECOND , 0);
                        binding.edTime2.setText(selectedHour+ " : " + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
       binding.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    binding.edDate2.setVisibility(View.GONE);
                    binding.edTime2.setVisibility(View.GONE);
                } else {
                    binding.edDate2.setVisibility(View.VISIBLE);
                    binding.edTime2.setVisibility(View.VISIBLE);
                }
            }
        });
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
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, "error  " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean validateError() {
        String tNameVal = binding.edName.getEditText().getText().toString();
        String spointVal = binding.edStartPoint.getEditText().getText().toString();
        String epointVal = binding.edEndPoint.getEditText().getText().toString();
        String dateVal = binding.edDate.getText().toString();
        String timeVal = binding.edTime.getText().toString();
        String date2Val = binding.edDate2.getText().toString();
        String time2Val = binding.edTime2.getText().toString();
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
        }else if(time2Val.isEmpty() && binding.switch1.isChecked()) {
            binding.edTime2.setError("Time required");
            binding.edTime2.requestFocus();
            return false;
        }else if(date2Val.isEmpty() && binding.switch1.isChecked()) {
            binding.edDate2.setError("date reqquired");
            binding.edDate2.requestFocus();
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

            binding.edDate2.setError(null);

            binding.edTime2.setError(null);



            return true;
        }

    }


}




