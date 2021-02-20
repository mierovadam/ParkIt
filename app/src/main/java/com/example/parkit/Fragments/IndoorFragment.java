package com.example.parkit.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.parkit.Objects.IndoorParkingLocation;
import com.example.parkit.R;
import com.example.parkit.Utils.BaseFragment;
import com.example.parkit.Utils.MySP;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.stream.IntStream;

import id.ionbit.ionalert.IonAlert;

public class IndoorFragment extends BaseFragment {

    private MaterialButton  indoor_BTN_capture, indoor_BTN_save,indoor_BTN_clear;
    private TextInputLayout indoor_TXTLAY_descrp;
    private ImageView indoor_IMG_photo;
    private AutoCompleteTextView indoor_ACTV_level,indoor_ACTV_color,indoor_ACTV_number;

    private IndoorParkingLocation indoorParkingLocation;
    private Uri photoUri;

    public IndoorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.indoor_fragment_layout, container, false);

        MySP.init(container.getContext());
        findViews(view);
        initViews(view);
        load();

        return view;
    }

    @Override       //get result from camera activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            deletePreviousPicture(photoUri);
            photoUri = data.getData();
            indoor_IMG_photo.setImageURI(photoUri);
            save();
        }
    }

    private void save(){
        String uriString = "";

        if(photoUri != null)
            uriString = photoUri.toString() ;

        MySP.getInstance().putString(MySP.SP_PHOTO_INDOOR_KEY,uriString);
        MySP.getInstance().putString(MySP.SP_COLOR_KEY,indoor_ACTV_color.getText().toString());
        MySP.getInstance().putString(MySP.SP_NUMBER_KEY,indoor_ACTV_number.getText().toString());
        MySP.getInstance().putString(MySP.SP_LEVEL_KEY,indoor_ACTV_level.getText().toString());
        MySP.getInstance().putString(MySP.SP_INDOOR_DESCR_KEY,indoor_TXTLAY_descrp.getEditText().getText().toString());

        highSnack("Saved!");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void load(){
        indoorParkingLocation = new IndoorParkingLocation(
                MySP.getInstance().getString(MySP.SP_COLOR_KEY, ""),
                MySP.getInstance().getString(MySP.SP_NUMBER_KEY, ""),
                MySP.getInstance().getString(MySP.SP_LEVEL_KEY, ""),
                MySP.getInstance().getString(MySP.SP_INDOOR_DESCR_KEY, ""),
                MySP.getInstance().getString(MySP.SP_PHOTO_INDOOR_KEY, ""));

            updateUI();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateUI(){
        try {
            indoor_ACTV_level.setText(indoorParkingLocation.getLevel(), false);
            indoor_ACTV_number.setText(indoorParkingLocation.getNumber(), false);
            indoor_ACTV_color.setText(indoorParkingLocation.getColor(), false);
            indoor_TXTLAY_descrp.getEditText().setText(indoorParkingLocation.getDescription());

            if(!indoorParkingLocation.getUriString().equals("") ){
                photoUri = Uri.parse(indoorParkingLocation.getUriString());
                indoor_IMG_photo.setImageURI(photoUri);
            }else{
                Glide.with(getContext()).load(R.drawable.map_background).into(indoor_IMG_photo);
            }
        } catch (Exception e){
            highSnack("Nothing saved yet!");
        }
    }

    //reset fragment data.
    private void clear(){
        new IonAlert(getContext())
                .setTitleText("Warning")
                .setContentText("Are you sure you want to clear indoor parking data ?")
                .setConfirmClickListener(new IonAlert.ClickListener() {
                    @Override
                    public void onClick(IonAlert sDialog) {
                        deletePreviousPicture(photoUri);  //deletes image from local storage.
                        MySP.getInstance().removeKey(MySP.SP_PHOTO_INDOOR_KEY);
                        MySP.getInstance().removeKey(MySP.SP_COLOR_KEY);
                        MySP.getInstance().removeKey(MySP.SP_NUMBER_KEY);
                        MySP.getInstance().removeKey(MySP.SP_LEVEL_KEY);
                        MySP.getInstance().removeKey(MySP.SP_INDOOR_DESCR_KEY);
                        load();
                        highSnack("Cleared!");
                        sDialog.dismiss();
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


    private void findViews(View view){
        indoor_BTN_capture   = view.findViewById(R.id.indoor_BTN_capture);
        indoor_BTN_save      = view.findViewById(R.id.indoor_BTN_save);
        indoor_TXTLAY_descrp = view.findViewById(R.id.indoor_TXTLAY_descrp);
        indoor_IMG_photo     = view.findViewById(R.id.indoor_IMG_photo);
        indoor_ACTV_level    = view.findViewById(R.id.indoor_ACTV_level);
        indoor_ACTV_color    = view.findViewById(R.id.indoor_ACTV_color);
        indoor_ACTV_number   = view.findViewById(R.id.indoor_ACTV_number);
        indoor_BTN_clear     = view.findViewById(R.id.indoor_BTN_clear);
    }


    //initialize buttons and dropdown menu's
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initViews(View view) {
        indoor_BTN_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        indoor_BTN_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        indoor_BTN_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        //set levels drop menu
        String[] optionLevels = {"-5","-4","-3","-2","-1","0","1","2","3","4","5"};
        ArrayAdapter arrayAdapterLevels = new ArrayAdapter(getContext(),R.layout.drop_down_list_item,optionLevels);
        indoor_ACTV_level.setAdapter(arrayAdapterLevels);

        //set Section drop menu
        String[] optionSection = {"Black","Blue","Brown","Green","Grey","White","Orange","Purple","Pink","Red","Yellow"};
        ArrayAdapter arrayAdapterSection = new ArrayAdapter(getContext(),R.layout.drop_down_list_item,optionSection);
        indoor_ACTV_color.setAdapter(arrayAdapterSection);

        //set Number Drop menu
        int[] nums = IntStream.range(0, 100).toArray(); // From 0 to 99
        String[] optionNumbers = Arrays.toString(nums).split("[\\[\\]]")[1].split(", ");
        ArrayAdapter arrayAdapterSectionNumber = new ArrayAdapter(getContext(),R.layout.drop_down_list_item,optionNumbers);
        indoor_ACTV_number.setAdapter(arrayAdapterSectionNumber);
    }
}