package com.example.pety.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.pety.R;
import com.example.pety.objects.User;
import com.example.pety.utils.FirebaseDB;
import com.example.pety.utils.MySP;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

public class Profile_Activity extends AppCompatActivity {

    TextInputLayout profile_LAY_firstName;
    TextInputLayout profile_LAY_lastName;
    MaterialButton profile_BTN_continue;
    FirebaseDB firebaseDB = FirebaseDB.getInstance();

    FloatingActionButton fab_add_photo;
    AppCompatImageView imgProfile;
    String imageFileName;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViews();
        initViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            imageUri = data.getData();
            File file = ImagePicker.Companion.getFile(data);
            imgProfile.setImageURI(imageUri);
            imgProfile.setScaleType(ImageView.ScaleType.FIT_XY);
            imageFileName = file.getName();
            Log.d("TAG", "onActivityResult: Image Uri:  " + imageFileName);
        }
    }

    private void updateUserProfile() {

        String firstName = profile_LAY_firstName.getEditText().getText().toString();
        String LastName = profile_LAY_lastName.getEditText().getText().toString();

        User user = new User();
        user.setF_name(firstName);
        user.setL_name(LastName);

        firebaseDB.updateFirstNameAndLastNameAndImage(user, imageFileName, imageUri);

        Intent myIntent = new Intent(this, Main_Activity.class);
        startActivity(myIntent);
        finish();
        return;
    }

    private void initViews() {
        fab_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(Profile_Activity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });
        profile_BTN_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });
    }

    private void findViews() {
        profile_LAY_firstName = findViewById(R.id.profile_LAY_firstName);
        profile_LAY_lastName = findViewById(R.id.profile_LAY_lastName);
        profile_BTN_continue = findViewById(R.id.profile_BTN_continue);
        fab_add_photo = findViewById(R.id.fab_add_photo);
        imgProfile = findViewById(R.id.imgProfile);
    }
}