package com.gram.meme.journalapp.ArchitectureComponents;

import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by user on 6/28/2018.
 */

public class JournalLiveData extends LiveData<DataSnapshot> {
    private  boolean listenerRemovePending = false;
    private final Handler handler = new Handler();
    private final Runnable removeListener = new Runnable() {
        @Override
        public void run() {
            query.removeEventListener(listener);
            listenerRemovePending = false;
        }
    } ;
    //this will be used for logging errors
    private static final String LOG_TAG = "JournalLiveDataDataBase";
    private final Query query;
    //valueEvent listener is for reading data from the real time database
    private final MyValueEventListener listener = new MyValueEventListener();
    public JournalLiveData(Query query) {
        this.query = query;
    }
    /** Constructor for Initialization in the view model class
     * @param databaseReference
     * this will point to the journal in the database**/
    public JournalLiveData(DatabaseReference databaseReference) {
        query = databaseReference;
    }
    //onActive to add value eventListener to start reading
    @Override
    protected void onActive() {
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener);
        }
        else {
            query.addValueEventListener(listener);
        }
        listenerRemovePending = false;
    }
    // remove valueEventListener to stop reading
    @Override
    protected void onInactive() {
        // Listener removal is schedule on a two second delay
        handler.postDelayed(removeListener, 2000);
        listenerRemovePending = true;
    }

    //MyValueEventListener which extends ValueEventListener and sets the value to the DataSnapshot
    private class MyValueEventListener implements ValueEventListener {
        //when the data change
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot journalSnapShot : dataSnapshot.getChildren()){
                setValue(journalSnapShot);
                Log.i(LOG_TAG, journalSnapShot.toString());
            }

          // postValue(dataSnapshot);

        }
        //when there is an error
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(LOG_TAG, "CANCELLED");

        }
    }

}
