package com.gram.meme.journalapp.ActivitiesAndFragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.gram.meme.journalapp.R;

public class JournalDetail extends AppCompatActivity {
    private static final String TAG =  "DETAIL" ;
    ImageView imageView;
    TextView textView;
     ProgressDialog progressDoalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("Title"));
        imageView= findViewById(R.id.imageForScroll);
        textView = findViewById(R.id.descriptionFor);
      // CollapsingToolbarLayout collapsingToolbarLayout= findViewById(R.id.toolbar_layout);
        Glide.with(this).load(getIntent().getStringExtra("UrlOfImage")).into(imageView);
        textView.setText(getIntent().getStringExtra("Description"));

    }
    public void deleteFromDb(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query applesQuery = ref.child("Journal").orderByChild(FirebaseAuth.getInstance().getCurrentUser().getUid());

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot journalSnapshot: dataSnapshot.getChildren()) {
                    journalSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(JournalDetail.this, "Deleted", Toast.LENGTH_LONG).show();
                            progressDoalog.dismiss();
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_journal_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            progressDoalog = new ProgressDialog(JournalDetail.this);
            progressDoalog.setMessage("Its saving....");
            progressDoalog.setTitle("Save Journal");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();
            deleteFromDb();
        }
        return super.onOptionsItemSelected(item);

    }
}
