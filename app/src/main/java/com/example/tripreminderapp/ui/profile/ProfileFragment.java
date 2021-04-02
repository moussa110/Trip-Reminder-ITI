package com.example.tripreminderapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tripreminderapp.database.FirebaseHandler;
import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.trip.Trip;
import com.example.tripreminderapp.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class ProfileFragment extends Fragment
{

    private FragmentProfileBinding binding;
    private FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();



    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Initialize view
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        List<Trip> trips = TripDatabase.getInstance(getActivity()).tripDao().getAll(user.getEmail());
        int done=0;
        int cancel = 0;
       for (int i =0 ; i <trips.size();i++){
           if (trips.get(i).isDone())
               done++;

           if(trips.get(i).isCanceled())
               cancel++;

       }

      // binding.profileTvDoneCount.setText(user.getDisplayName());
       binding.profileTvDoneCount.setText(""+(trips.size()-done));
       binding.profileTvCancelCount.setText(""+(trips.size()-cancel));

        Glide
                .with(getActivity())
                .load(user.getPhotoUrl())
                .centerCrop()
                .into(binding.imageView5);
        FirebaseHandler  handler=new FirebaseHandler(getActivity());
        binding.profileTvEmail.setText(user.getEmail().toString());
        binding.profileBtnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.syncDataWithFirebaseDatabase();
            }
        });

        return  view;
    }


}
