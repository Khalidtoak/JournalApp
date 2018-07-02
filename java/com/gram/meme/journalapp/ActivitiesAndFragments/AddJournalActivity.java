package com.gram.meme.journalapp.ActivitiesAndFragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gram.meme.journalapp.JournalPojos.JournalPojo;
import com.gram.meme.journalapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddJournalActivity extends AppCompatActivity {
    private static final int REQUEST_CAPTURE_IMAGE = 457 ;
    EditText titleEditText, descriptionEditText;
    ImageButton pickImage, saveJournal;
    private int RC_PHOTO_PICKER = 456;
    FirebaseDatabase firebaseDatabase;
    private StorageReference mStorageReference;
    DatabaseReference reference;

    public Bitmap getX() {
        return x;
    }

    public void setX(Bitmap x) {
        this.x = x;
    }

    Bitmap x;
    FirebaseStorage mFireBaseStorage;

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    Uri uri;

    public Uri getUri() {
        return uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);
        titleEditText = findViewById(R.id.title);
        descriptionEditText = findViewById(R.id.description);
        pickImage = findViewById(R.id.showImagePicker);
        saveJournal = findViewById(R.id.saveImage);
        saveJournal.setEnabled(false);
        firebaseDatabase = FirebaseDatabase.getInstance();
        mFireBaseStorage = FirebaseStorage.getInstance();

        saveJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDoalog = new ProgressDialog(AddJournalActivity.this);
                progressDoalog.setMessage("Its saving....");
                progressDoalog.setTitle("Save Journal");
                progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDoalog.show();

                final StorageReference photoReference = mStorageReference.child(getUri().getLastPathSegment());
                UploadTask uploadTask = photoReference.putFile(getUri());
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return photoReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            progressDoalog.dismiss();
                            @SuppressLint("UseSparseArrays")
                            Uri downloadUri = task.getResult();

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            JournalPojo journalPojo = new JournalPojo(descriptionEditText.getText().toString()
                                    , titleEditText.getText().toString(), downloadUri.toString(), timeStamp);
                            reference.push().setValue(journalPojo);
                            Toast.makeText
                                    (getApplicationContext(),
                                            "your dear Journal has been saved successfully", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "your Journal couldn't be saved," +
                                    "please try again", Toast.LENGTH_SHORT).show();
                            // Handle failures
                            // ...
                        }
                    }
                });
            }
        });
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = firebaseDatabase.getReference().child("Journals").child(firebaseUser.getUid());
        mStorageReference = mFireBaseStorage.getReference().child("journal_photos");
    }

    public void showImagePicker(View view) {
        // TODO: Fire an intent to show an image picker
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            setUri(selectedImageUri);
            new drawableFromUrl().execute(getUri());
            saveJournal.setEnabled(true);

        }
    }
    private void addImageBetweentext(Drawable drawable) {
        drawable .setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        int selectionCursor = descriptionEditText.getSelectionStart();
        descriptionEditText.getText().insert(selectionCursor, ".");
        selectionCursor = descriptionEditText.getSelectionStart();

        SpannableStringBuilder builder = new SpannableStringBuilder(descriptionEditText.getText());
        builder.setSpan(new ImageSpan(drawable), selectionCursor - ".".length(), selectionCursor, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        descriptionEditText.setText(builder);
        descriptionEditText.setSelection(selectionCursor);
    }
    public class drawableFromUrl extends AsyncTask<Uri, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Uri... strings) {
            if (strings.length == 0) {
                return null;
            }
            Uri url = strings[0];

            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url.toString()).openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                setX(  BitmapFactory.decodeStream(inputStream));


            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BitmapDrawable(Resources.getSystem(), getX());
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            addImageBetweentext(drawable);
        }
    }
}
