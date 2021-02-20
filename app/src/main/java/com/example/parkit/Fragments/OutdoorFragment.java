package com.example.parkit.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parkit.Objects.OutdoorParkingLocation;
import com.example.parkit.R;
import com.example.parkit.Utils.BaseFragment;
import com.example.parkit.Utils.MySP;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OutdoorFragment extends BaseFragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private Location currentLocation;
    private  Marker marker;
    private FusedLocationProviderClient client;

    private OutdoorParkingLocation outdoorParkingLocation;

    //lists contain current user and other following users emails and id's
    private ArrayList<String> userIds;
    private ArrayList<String> usersArray;
    private String selectedUser;

    private MaterialButton map_BTN_save,map_BTN_refresh,map_BTN_picture,map_BTN_load,map_BTN_capture,map_BTN_info;
    private TextInputLayout map_TIL_address;
    private AutoCompleteTextView map_ACTV_followers;

    private Uri photoUri;

    public OutdoorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.outdoor_fragment_layout, container, false);

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        MySP.init(container.getContext());

        initFirebase();                     //initialize firebase related variables at the BaseFragment
        findViews(view);
        initArrays();
        initViews();
        load(firebaseUser.getUid());        //load "this" user's saved location
        loadSharedLocations();              //load shared locations ready up in the dropbox

        return view;
    }

    private void initArrays() {
        //init users array and add current user
        usersArray = new ArrayList<String>();
        usersArray.add(getString(R.string.my_car)); //add "My Car" as first value, sets as default in initDropdownMenu()
        userIds = new ArrayList<String>();
        userIds.add(firebaseUser.getUid());
        selectedUser = firebaseUser.getUid();
    }

    @Override       //get result from camera activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            deletePreviousPicture(photoUri);
            photoUri = data.getData();
            save();
        }
    }

    //show the saved image of the currently selected user in the dropdown menu.
    private void showImagePopUp(){
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.image_popup);
        ImageView img = dialog.findViewById(R.id.popup_img);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //if user tries to view photo of his own parking, load from SP, else load from firebase storage.
        if(selectedUser.equals(firebaseUser.getUid())){
            if(photoUri != null) {
                Glide.with(getContext()).load(photoUri).into(img);
                dialog.show();
                return;
            } else {
                Toast.makeText(getActivity(), "Nothing to see here!", Toast.LENGTH_LONG).show();
            }
        } else if(isNetworkAvailable()){
            StorageReference ref = storage.getReferenceFromUrl("gs://parkit-93ad8.appspot.com/images/users/"+selectedUser+".jpg");
            final Uri[] imageURL = new Uri[1];
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    imageURL[0] = Uri.parse(uri.toString());
                    Glide.with(getContext()).load(imageURL[0]).into(img);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getActivity(), "Failed loading image.", Toast.LENGTH_LONG).show();

                }
            });
        }else {
            Toast.makeText(getActivity(), "Nothing to see here!", Toast.LENGTH_LONG).show();
            return;
        }
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)){
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    currentLocation = task.getResult();

                    if(currentLocation != null){
                        moveCameraOnMap(currentLocation);
                        updateAddressEdt(currentLocation);
                    } else {
                        LocationRequest locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(1000).setNumUpdates(1);
                        LocationCallback locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                currentLocation = locationResult.getLastLocation();
                                moveCameraOnMap(currentLocation);
                            }
                        };
                        client.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                    }
                }
            });
        } else {
            //if location service is off, open location settings.
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    //update description box with location address
    private void updateAddressEdt(Location currentLocation) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        String address;

        try {
            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            map_TIL_address.getEditText().setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //initialize userArray list with all "following" user's emails.
    private void loadSharedLocations(){
        //get followers array from database
        usersRef.child(firebaseUser.getUid()).child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    String follower  = dsp.getValue(String.class);
                    usersArray.add(follower); //add result into array list
                }
                getLocationsFromUsers(usersArray);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("pttt" ,"The read failed: updateUI in first fragment");
            }
        });
    }

    //load userIds list with the user id's of the users we loaded into usersArray in the previous function. and when done loading, load the names into the dropdown list.
    private void getLocationsFromUsers(ArrayList<String> usersArray){
        DatabaseReference ref = database.getReference("emails");

        //translate users into id's
        for(int i=1 ; i<usersArray.size() ; i++){
            ref.child(usersArray.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot){
                    userIds.add(snapshot.getValue().toString());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("pttt" ,"The read failed: updateUI in third fragment");
                }
            });
            if(i == usersArray.size()-1){   //when finished loading users into array, init the dropdownMenu
                initDropdownMenu();
            }
        }
    }

    private void initDropdownMenu() {
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(),R.layout.drop_down_list_item,usersArray);
        map_ACTV_followers.setAdapter(arrayAdapter);
        map_ACTV_followers.setText(arrayAdapter.getItem(0).toString(), false);      //sets the default drop menu starting value to "My Car"
    }

    private void showSelectedParking(){
        String selected = map_ACTV_followers.getText().toString();
        String id = userIds.get(usersArray.indexOf(selected));      //get index of userID in arraylist
        selectedUser = id;
        load(id);
    }


    //save both on sharedPref and realtime database firebase
    private void save() {
        if(photoUri != null) {  //save image  on firebase storage
            StorageReference ref = storage.getReferenceFromUrl("gs://parkit-93ad8.appspot.com/images/users/"+firebaseUser.getUid()+".jpg");
            ref.putFile(photoUri).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    highSnack("Image upload failed.");
                }
            });
        }

        outdoorParkingLocation = new OutdoorParkingLocation(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                map_TIL_address.getEditText().getText().toString(),
                photoUri.toString(),firebaseUser.getEmail());

        usersRef.child(firebaseUser.getUid()).child("OutdoorParkingLocation").setValue(outdoorParkingLocation);

        MySP.getInstance().putFloat(MySP.SP_LATITUDE_KEY, (float) currentLocation.getLatitude());
        MySP.getInstance().putFloat(MySP.SP_LONGITUDE_KEY, (float) currentLocation.getLongitude());
        MySP.getInstance().putString(MySP.SP_DESCR_KEY, map_TIL_address.getEditText().getText().toString());

        if (photoUri != null)
            MySP.getInstance().putString(MySP.SP_PHOTO_KEY, photoUri.toString());

        highSnack("Parking location saved!");
    }

    //loads location of the passed in userID (only followers id's will be passed in)
    private void load(String userID) {
        if(isNetworkAvailable()) {
            usersRef.child(userID).child("OutdoorParkingLocation").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    outdoorParkingLocation = dataSnapshot.getValue(OutdoorParkingLocation.class);
                    updateUI();
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.d("pttt", "Failed to read value.", error.toException());
                }
            });
        }else {
            outdoorParkingLocation = new OutdoorParkingLocation(
                    (double) MySP.getInstance().getFloat(MySP.SP_LATITUDE_KEY, 0),
                    (double) MySP.getInstance().getFloat(MySP.SP_LONGITUDE_KEY, 0),
                    MySP.getInstance().getString(MySP.SP_DESCR_KEY, "No description available."),
                    MySP.getInstance().getString(MySP.SP_PHOTO_KEY, ""),
                    firebaseUser.getEmail());
            updateUI();
        }
    }

    private void updateUI() {
        currentLocation = new Location(LocationManager.GPS_PROVIDER);   //initialize variable

        try{    //in case some of the variables are null (nothing saved yet)
            currentLocation.setLongitude(outdoorParkingLocation.getLongitude());
            currentLocation.setLatitude(outdoorParkingLocation.getLatitude());
            map_TIL_address.getEditText().setText(outdoorParkingLocation.getDescription());
            photoUri = Uri.parse(outdoorParkingLocation.getUriString());
            moveCameraOnMap(currentLocation);
        }catch(Exception e){
            highSnack("Nothing saved yet!");
        }
    }

    private void moveCameraOnMap(Location location) {
        if(marker != null)          //remove previous marker,to show only one at a time.
            marker.remove();

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Your car!"));
        marker.showInfoWindow();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private void findViews(View view) {
        map_BTN_refresh = view.findViewById(R.id.map_BTN_refresh);
        map_BTN_save    = view.findViewById(R.id.map_BTN_save);
        map_BTN_capture = view.findViewById(R.id.map_BTN_capture);
        map_BTN_load    = view.findViewById(R.id.map_BTN_load);
        map_TIL_address = view.findViewById(R.id.map_TIL_address);
        map_BTN_picture = view.findViewById(R.id.map_BTN_picture);
        map_ACTV_followers = view.findViewById(R.id.map_ACTV_followers);
        map_BTN_info       = view.findViewById(R.id.map_BTN_info);
    }

    private void initViews() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (supportMapFragment != null)
            supportMapFragment.getMapAsync(this);

        map_BTN_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
        map_BTN_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        map_BTN_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load(firebaseUser.getUid());
            }
        });
        map_BTN_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        map_BTN_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePopUp();
            }
        });
        map_ACTV_followers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showSelectedParking();
            }
        });
        map_BTN_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPopup();
            }
        });
    }

    private void infoPopup() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setTitle("Saving parking location outdoors:")
                .setMessage("-If someone added you as a follower, you may view his parking info by selecting in the dropdown menu.\n\n-" +
                        "-In the description box it will show you the estimated address but you may write what ever you'd like.\n\n" +
                        "-You may take a picture of the area by clicking the camera icon and view it in Captured.\n\n" +
                        "-Refresh will show you your current location and you have to press SAVE in order to save the location.\n\n" +
                        "-Load will show you your last saved location").show();
    }

}