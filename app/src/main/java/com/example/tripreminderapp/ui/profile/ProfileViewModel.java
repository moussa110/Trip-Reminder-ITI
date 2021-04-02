package com.example.tripreminderapp.ui.profile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;

    public ProfileViewModel(Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }


    public LiveData<String> getText() {
        return mText;
    }
}