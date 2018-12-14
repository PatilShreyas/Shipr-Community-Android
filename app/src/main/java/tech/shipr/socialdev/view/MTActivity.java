package tech.shipr.socialdev.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import tech.shipr.socialdev.R;
import tech.shipr.socialdev.model.DeveloperMessage;


public class MTActivity extends FragmentActivity {

    private static final int RC_PHOTO_PICKER = 2;
    private static final int RC_CHAT_PHOTO_PICKER = 3;

    private StorageReference mProfileStroageReference;

    private String mName;
    private String mPlatform;
    private String mChannel = "general";
    private String mDate;
    private String mTime;
    private String mMessage;
    private String mProfilePic;
    private String mVersion;

    private DatabaseReference mMessagesDatabaseReference;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_chat:
                    FragmentManager frag = getSupportFragmentManager();
                    frag.beginTransaction().replace(R.id.content_frame, new ChatChannel()).commit();

                    return true;

                case R.id.navigation_profile:
                    FragmentManager frag2 = getSupportFragmentManager();
                    frag2.beginTransaction().replace(R.id.content_frame, new ProfileActivity()).commit();

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mt);
        Log.d("TAG", "MT_ACTIVITY OPENED");
/*
        FirebaseApp.initializeApp(this);*/
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();

        mProfileStroageReference = mFirebaseStorage.getReference().child("profile_pic");
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("general");


        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FragmentManager frag1 = getSupportFragmentManager();
        frag1.beginTransaction().replace(R.id.content_frame, new ChatChannel()).commit();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MT_Activity", "onActivityResult executed");
      /*if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at chat_photos/<FILENAME>
            StorageReference photoRef = mProfileStroageReference.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // When the image has successfully uploaded, we get its download URL
                            Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                            Log.d("url : ", downloadUrl.toString());

                            // Set the download URL to the message box, so that the user can send it to the database
 *//*                         FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, downloadUrl.toString());
                            mMessagesDatabaseReference.push().setValue(friendlyMessage);*//*
                        }
                    });
        }else */

        //TODO fix this
        if (requestCode == RC_CHAT_PHOTO_PICKER && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            // Get a reference to store file at chat_photos/<FILENAME>
            final StorageReference photoRef = mProfileStroageReference.child(selectedImageUri.getLastPathSegment());


            photoRef.putFile(selectedImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        //TODO return the url of the image uploaded here


                        // Getting the time
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
                        mTime = sdf.format(new Date());

                        // Getting the date
                        final Calendar c = Calendar.getInstance();
                        int year = c.get(Calendar.YEAR);
                        int month = c.get(Calendar.MONTH);
                        int day = c.get(Calendar.DAY_OF_MONTH);
                        mDate = String.valueOf(day) + "-" + String.valueOf(month) + "-" + String.valueOf(year);


                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            for (UserInfo profile : user.getProviderData()) {
                                // Id of the provider (ex: google.com)
                                String providerId = profile.getProviderId();

                                // UID specific to the provider
                                String uid = profile.getUid();

                                // Name, email address, and profile photo Url
                                mName = profile.getDisplayName();
                                /* Uri uri = profile.getPhotoUrl();*/
                                /*  mProfilePic = uri.toString();*/
                            }

                        }

                        DeveloperMessage developerMessage = new DeveloperMessage(
                                mName,

                                mProfilePic,
                                mMessage,
                                downloadUri.toString(),
                                mTime,
                                mDate,
                                mPlatform,
                                mVersion
                        );
                        mMessagesDatabaseReference.push().setValue(developerMessage);
                    } else {
                        Toast.makeText(MTActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}

