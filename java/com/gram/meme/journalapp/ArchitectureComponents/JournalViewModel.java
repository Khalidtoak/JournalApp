package com.gram.meme.journalapp.ArchitectureComponents;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gram.meme.journalapp.JournalPojos.JournalPojo;

import static android.content.ContentValues.TAG;

/**
 * Created by user on 6/28/2018.
 */
//ViewModel for persisting data from the LiveData class across configuration changes
public class JournalViewModel extends ViewModel {
    private MediatorLiveData<JournalPojo> journalPojoMediatorLiveData = new MediatorLiveData<>();
    public JournalViewModel() {
        journalPojoMediatorLiveData.addSource(liveData, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable final DataSnapshot dataSnapshot) {
                if (dataSnapshot!= null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            journalPojoMediatorLiveData.postValue(dataSnapshot.getValue(JournalPojo.class));
                            Log.i(TAG, "run: posted");
                        }
                    }).start();
                }

            }
        });
    }


    private static final DatabaseReference databaseReference
            = FirebaseDatabase.getInstance().getReference().child("Journals").child(
                    FirebaseAuth.getInstance().getCurrentUser().getUid()
    );
    private final JournalLiveData liveData = new JournalLiveData(databaseReference);

    //this will get the DataSnapshot that has been deserialize toa JournalSnapShot
    @NonNull
    public LiveData<JournalPojo> getDataSnapshotLiveData() {
        return journalPojoMediatorLiveData;
    }

    /**
     * class Deserializer has one simple Job!! to desirialize a DataSnapShot into a JournalPojo
     */



}
