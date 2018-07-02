package com.gram.meme.journalapp.ActivitiesAndFragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gram.meme.journalapp.Adapters.JournalAdapter;
import com.gram.meme.journalapp.ArchitectureComponents.JournalViewModel;
import com.gram.meme.journalapp.JournalPojos.JournalPojo;
import com.gram.meme.journalapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashBoardActvity extends AppCompatActivity {
    private static final String TAG = "DashBoardActivity";
    RecyclerView recyclerView;
    public static final int RC_SIGN_IN = 1;
    public static final String ANONYMOUS = "anonymous";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListner;
    private String mUsername;
    private ListView JournalListView;
    private JournalAdapter journalAdapter;
    ProgressBar mProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board_actvity);
        getSupportActionBar();
        mUsername = ANONYMOUS;
        mProgressbar = findViewById(R.id.progressBar);
        mProgressbar.setVisibility(View.VISIBLE);
        JournalListView =  findViewById(R.id.messageList);
        final List<JournalPojo> journalPojoArrayList= new ArrayList<>();
        journalAdapter = new JournalAdapter(this, R.layout.journal_list_layout, journalPojoArrayList);
        JournalListView.setAdapter(journalAdapter);
        JournalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DashBoardActvity.this, JournalDetail.class);
                intent.putExtra("Title" , journalPojoArrayList.get(position).getName());
                intent.putExtra("Description" , journalPojoArrayList.get(position).getJournalContent());
                intent.putExtra("UrlOfImage",journalPojoArrayList.get(position).getPhotoUrl() );
                startActivity(intent);
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
   //   database = FirebaseDatabase.getInstance();
        checkAuthState();

        // Initialize references to views

        // Obtain a new or prior instance of JournalViewModel from the
        // ViewModelProviders utility class.
        JournalViewModel viewModel = ViewModelProviders.of(this).get(JournalViewModel.class);
        LiveData<JournalPojo> journalLiveDataLiveData = viewModel.getDataSnapshotLiveData();
        journalLiveDataLiveData.observe(this, new Observer<JournalPojo>() {
            @Override
            public void onChanged(@Nullable JournalPojo journalPojo) {
                if (journalPojo != null) {
                    mProgressbar.setVisibility(View.GONE);
                    Log.i("read", "journal pojo was read successfully");
                    journalAdapter.add(journalPojo);
                    Log.i(TAG, "onChanged: " + journalPojoArrayList.size());

                }
                else{
                    mProgressbar.setVisibility(View.INVISIBLE);
                    Toast.makeText(DashBoardActvity.this, "You don't have any journals yet.. please add yours", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void checkAuthState() {
        firebaseAuthStateListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth1) {
                FirebaseUser user = firebaseAuth1.getCurrentUser();
                if (user != null) {
                    OnSignedInitialize(user.getDisplayName());
                    Toast.makeText(DashBoardActvity.this, "you are logged in bro", Toast.LENGTH_SHORT).show();
                } else {
                    onSignedOutCleanUp();
                    ConnectivityManager cm =
                            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnectedOrConnecting();
                    if (!isConnected) {
                        return;
                    }
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder().setLogo(R.mipmap.ic_launcher)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void onSignedOutCleanUp() {
        mUsername = ANONYMOUS;
        journalAdapter.clear();
    }

    private void OnSignedInitialize(String displayName) {
        mUsername = displayName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListner);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuthStateListner != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthStateListner);
        }
        journalAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "signed in!!", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    public void openAddJournalActivity(View view) {
        Intent intent = new Intent(DashBoardActvity.this, AddJournalActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out_menu) {
            AuthUI.getInstance().signOut(this);
        }
        else if (item.getItemId() == R.id.delete_menu){
            AuthUI.getInstance().delete(this);
        }
        return super.onOptionsItemSelected(item);

    }

}
