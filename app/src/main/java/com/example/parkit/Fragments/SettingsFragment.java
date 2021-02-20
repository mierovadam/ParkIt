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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.parkit.Activities.LoginActivity;
import com.example.parkit.R;
import com.example.parkit.Utils.BaseFragment;
import com.example.parkit.Utils.MySP;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import id.ionbit.ionalert.IonAlert;

public class SettingsFragment extends BaseFragment {
    private MaterialButton settings_BTN_signout,settings_BTN_delete,settings_BTN_add,settings_BTN_reset,settings_BTN_info,settings_BTN_deleteFollowing;
    private AutoCompleteTextView settings_ACTV_followers,settings_ACTV_following;
    private TextInputLayout settings_EDT_follow;
    private String userID,followers,following;
    private TextView settings_LBL_email;
    private ImageView settings_IMG_logo;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment_layout, container, false);

        followers  = "followers";
        following = "following";

        initFirebase();
        findViews(view);
        initViews();
        updateDropMenu(settings_ACTV_following,following);          //initialize both dropdown menu's
        updateDropMenu(settings_ACTV_followers,followers);

        MySP.init(container.getContext());

        return view;
    }

    //delete and update will wither be holding following or followers but never the same. Delete from my user and update other users list.
    private void deleteFollower(String delete,String update,AutoCompleteTextView actv) {
        String email = removeSpecialChars(settings_ACTV_followers.getText().toString());
        usersRef.child(firebaseUser.getUid()).child(delete).child(email).removeValue();

        emailsRef.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userID = snapshot.getValue().toString();
                usersRef.child(userID).child(update).child(removeSpecialChars(firebaseUser.getEmail())).removeValue();
                actv.setText("");
                updateDropMenu(actv,followers);
                highSnack("User removed successfully!");
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

        updateDropMenu(settings_ACTV_followers,followers);
        highSnack("Follower added successfully!");
    }

    private void updateOtherUser(String email) {        //set myself as following on who this email belongs to
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

    public void updateDropMenu(AutoCompleteTextView actv,String mode){
        ArrayList<String> userArray = new ArrayList<String>();

        //get followers array from database
        usersRef.child(firebaseUser.getUid()).child(mode).addListenerForSingleValueEvent(new ValueEventListener() {
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

        //insert array into drop menu
        ArrayAdapter arrayAdapterLevels = new ArrayAdapter(getContext(),R.layout.drop_down_list_item,userArray);
        actv.setAdapter(arrayAdapterLevels);
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();

        //if you'd like to delete all Shared Pref on signing out, enable the next line.
        //MySP.getInstance().resetSP();

        Intent myIntent = new Intent(getActivity(), LoginActivity.class);
        startActivity(myIntent);
        getActivity().finish();
        return;
    }


    private void resetData(){
        new IonAlert(getContext())
                .setTitleText("Are you sure you want to reset saved locations ?")
                .setContentText("Cannot be undone!!!")
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

    private void infoPopup(){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setTitle("Managing your following/followers lists.")
                .setMessage("-You can enter another user's email to allow him to view your outdoor parking location as well.\n\n" +
                        "-You can delete a following/followers user from your list but selecting them in the list and hitting the trash button.\n\n" +
                        "-Reset button deletes all saved info on phone and cloud.").show();
    }

    private void findViews(View view){
        settings_BTN_deleteFollowing = view.findViewById(R.id.settings_BTN_deleteFollowing);
        settings_ACTV_following      = view.findViewById(R.id.settings_ACTV_following);
        settings_ACTV_followers      = view.findViewById(R.id.settings_ACTV_followers);
        settings_BTN_signout         = view.findViewById(R.id.settings_BTN_signout);
        settings_BTN_delete          = view.findViewById(R.id.settings_BTN_delete);
        settings_EDT_follow          = view.findViewById(R.id.settings_EDT_follow);
        settings_BTN_reset           = view.findViewById(R.id.settings_BTN_reset);
        settings_LBL_email           = view.findViewById(R.id.settings_LBL_email);
        settings_BTN_info            = view.findViewById(R.id.settings_BTN_info);
        settings_BTN_add             = view.findViewById(R.id.settings_BTN_add);

        settings_IMG_logo = view.findViewById(R.id.settings_IMG_logo);
        Glide.with(getContext()).load(R.drawable.login_logo).into(settings_IMG_logo);
    }

    public void initViews(){
        settings_LBL_email.setText("Signed in as "+firebaseUser.getEmail());

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
                deleteFollower(followers,following,settings_ACTV_followers);
            }
        });
        settings_BTN_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetData();
            }
        });
        settings_BTN_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPopup();
            }
        });
        settings_BTN_deleteFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFollower(following,followers,settings_ACTV_following);
            }
        });
    }
}