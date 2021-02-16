package com.example.parkit.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.parkit.Fragments.firstFragment;
import com.github.drjacky.imagepicker.ImagePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import static com.google.android.material.snackbar.Snackbar.make;

public class BaseFragment extends Fragment {
    public static final int MY_CAMERA_PERMISSION_CODE = 100;    //was 101
    public static final int MY_LOCATION_PERMISSION_CODE = 101;

    public FirebaseDatabase database;
    public FirebaseAuth firebaseAuth;
    public FirebaseUser firebaseUser;
    public DatabaseReference usersRef;
    public DatabaseReference emailsRef;
    public FirebaseStorage storage;
    public StorageReference storageRef;


    //checks and asks for permission, and take a picture.
    public void takePicture() {
        //if no permission, ask for it else, take pic
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            ImagePicker.Companion.with(this)
                    .cameraOnly()   //no need for gallery option
                    .compress(220)
                    .start();
        }
    }

    //delete previous pictures
    public void deletePreviousPicture(Uri uri){
        File file = new File(uri.getPath());
        if (file.exists()) {
            file.delete();
            Log.d("pttt", "previous picture deleted.");
        }
    }


    public void highSnack(String str){
        Snackbar snackbar = make(getView(),str,Snackbar.LENGTH_LONG);
        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)snackbar.getView().getLayoutParams();
        params.gravity = Gravity.TOP;
        snackbar.getView().setLayoutParams(params);
        snackbar.show();
    }

    public void initFirebase(){
        database     = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage      = FirebaseStorage.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageRef   = storage.getReference();
        emailsRef    = database.getReference("emails");
        usersRef     = database.getReference("users");
    }

    public String removeSpecialChars(String str){
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_LOCATION_PERMISSION_CODE) {
            if (((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                firstFragment firstFragment = new firstFragment();
                firstFragment.getCurrentLocation();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                takePicture();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
