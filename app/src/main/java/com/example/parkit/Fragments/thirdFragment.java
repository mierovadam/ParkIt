package com.example.parkit.Fragments;

import android.content.Intent;

import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.example.parkit.Activities.LoginActivity;
import com.example.parkit.R;
import com.example.parkit.Utils.BaseFragment;
import com.example.parkit.Utils.MySP;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import id.ionbit.ionalert.IonAlert;

public class thirdFragment extends BaseFragment {
    private MaterialButton settings_BTN_signout,settings_BTN_delete,settings_BTN_add,settings_BTN_reset;
    private AutoCompleteTextView settings_ACTV_followers;
    private TextInputLayout settings_EDT_follow;
    private String userID;
    private TextView settings_LBL_email;


    public thirdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        initFirebase();
        findViews(view);
        initViews();
        updateDropBox();
        MySP.init(container.getContext());

        return view;
    }

    private void deleteFollower() {
        String email = removeSpecialChars(settings_ACTV_followers.getText().toString());
        usersRef.child(firebaseUser.getUid()).child("followers").child(email).removeValue();

        emailsRef.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userID = snapshot.getValue().toString();
                usersRef.child(userID).child("following").child(removeSpecialChars(firebaseUser.getEmail())).removeValue();
                settings_ACTV_followers.setText("");
                updateDropBox();
                highSnack("Follower removed successfully!");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("pttt" ,"The read failed: updateUI in third fragment");
            }
        });

    }

    public void addFollower(){
        String email = removeSpecialChars(settings_EDT_follow.getEditText().getText().toString());
        usersRef.child(firebaseUser.getUid()).child("followers").child(email).setValue(email);

        //set as following at follower's user
        updateOtherUser(email);

        updateDropBox();
        highSnack("Follower added successfully!");
    }

    private void updateOtherUser(String email) {
        DatabaseReference ref = database.getReference("emails");
        userID = "";

        ref.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userID = snapshot.getValue().toString();

                String myEmail = removeSpecialChars(firebaseUser.getEmail());
                usersRef.child(userID).child("following").child(myEmail).setValue(myEmail);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("pttt" ,"The read failed: updateUI in third fragment");
            }
        });
    }

    public void updateDropBox(){
        ArrayList<String> userArray = new ArrayList<String>();

        //get followers array from database
        usersRef.child(firebaseUser.getUid()).child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    String follower  = dsp.getValue().toString();
                    userArray.add(follower); //add result into array list
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("pttt" ,"The read failed: updateUI in third fragment");

            }
        });

        //insert array into dropbox
        ArrayAdapter arrayAdapterLevels = new ArrayAdapter(getContext(),R.layout.drop_down_list_item,userArray);
        settings_ACTV_followers.setAdapter(arrayAdapterLevels);
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();

        // TODO: 1/5/21  remove all sharedPreference

        Intent myIntent = new Intent(getActivity(), LoginActivity.class);
        startActivity(myIntent);
        getActivity().finish();
        return;
    }

    private void findViews(View view){
        settings_BTN_signout = view.findViewById(R.id.settings_BTN_signout);
        settings_BTN_delete = view.findViewById(R.id.settings_BTN_delete);
        settings_BTN_add = view.findViewById(R.id.settings_BTN_add);
        settings_ACTV_followers  = view.findViewById(R.id.settings_ACTV_followers);
        settings_EDT_follow = view.findViewById(R.id.settings_EDT_follow);
        settings_BTN_reset  = view.findViewById(R.id.settings_BTN_reset);
        settings_LBL_email  = view.findViewById(R.id.settings_LBL_email);
    }

    private void resetData(){
        new IonAlert(getContext())
                .setTitleText("Are you sure you want to reset saved locations ?")
                .setContentText("Can't be undone!")
                .setConfirmClickListener(new IonAlert.ClickListener() {
                    @Override
                     public void onClick(IonAlert sDialog) {
                        MySP.getInstance().resetSP();
                        usersRef.child(firebaseUser.getUid()).child("OutdoorParkingLocation").removeValue();
                        sDialog.dismiss();
                        highSnack("Data has been reset!");
                    }
                 })
                .showCancelButton(true)
                .setCancelClickListener(new IonAlert.ClickListener() {
                    @Override
                    public void onClick(IonAlert sDialog) {
                        sDialog.dismiss();
                    }
                })
                .show();
    }

    public void initViews(){
        settings_LBL_email.setText("Logged in as "+firebaseUser.getEmail());

        settings_BTN_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        settings_BTN_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        settings_BTN_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFollower();
            }
        });
        settings_BTN_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFollower();
            }
        });
        settings_BTN_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
            }
        });
    }
}