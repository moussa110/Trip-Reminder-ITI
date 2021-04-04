package com.example.tripreminderapp.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.tripreminderapp.LoginActivity;
import com.example.tripreminderapp.database.FirebaseHandler;
import com.example.tripreminderapp.database.TripDatabase;
import com.example.tripreminderapp.database.trip.Trip;


import com.example.tripreminderapp.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class ProfileFragment extends Fragment
{
    private ProfileViewModel viewModel;
    private FragmentProfileBinding binding;
    private FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();



    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //view binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //init view model
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getTripsLiveData().observe(getActivity(),trips -> {
            updateView(trips);
        });


        Glide
                .with(getActivity())
                .load(user.getPhotoUrl())
                .centerCrop()
                .into(binding.imageView5);


        binding.profileTvEmail.setText(LoginActivity.EMAIL);
        binding.profileBtnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseHandler  handler=new FirebaseHandler(getActivity());
                handler.syncDataWithFirebaseDatabase();
            }
        });

        binding.profileBtnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                LoginActivity.EMAIL="";
                getActivity().startActivity(new Intent(getActivity(),LoginActivity.class));
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("start", MODE_PRIVATE).edit();
                editor.putBoolean("start", true);
                editor.commit();
            }
        });
        return  view;
    }

    private void updateView(List<Trip> trips) {
        int done=0;
        int cancel = 0;
        for (int i =0 ; i <trips.size();i++){
            if (trips.get(i).isDone())
                done++;
            if (trips.get(i).isCanceled())
                cancel++;
        }

        binding.profileTvName.setText(user.getDisplayName());
        binding.profileTvDoneCount.setText(""+done);
        binding.profileTvCancelCount.setText(""+cancel);

    }
}
