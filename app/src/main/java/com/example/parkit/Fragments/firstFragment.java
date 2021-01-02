package com.example.parkit.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.parkit.R;
import com.example.parkit.Utils.BaseActivity;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class firstFragment extends Fragment implements OnMapReadyCallback {
    public final static String SP_LATITUDE_KEY = "latitudeKay";
    public final static String SP_LONGITUDE_KEY = "longitudeKey";
    public final static String SP_DESCRIPTION_KEY = "descriptionKey";


    private GoogleMap googleMap;
    private Location currentLocation;
    private FusedLocationProviderClient client;

    private MaterialButton map_BTN_save,map_BTN_refresh,map_BTN_picture,map_BTN_load;
    private TextInputLayout map_TIL_address;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        client = LocationServices.getFusedLocationProviderClient(getActivity());
        MySP.init(container.getContext());


        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (supportMapFragment != null)
            supportMapFragment.getMapAsync(this);

        findViews(view);
        initViews();
        //check location permissions and set current location on map
        //press Load to show last saved parking with description
        checkPermissionsGetLocation();

        return view;
    }

    private void findViews(View view) {
        map_BTN_refresh = view.findViewById(R.id.map_BTN_refresh);
        map_BTN_save    = view.findViewById(R.id.map_BTN_save);
        map_BTN_picture = view.findViewById(R.id.map_BTN_picture);
        map_BTN_load    = view.findViewById(R.id.map_BTN_load);
        map_TIL_address = view.findViewById(R.id.map_TIL_address);
    }

    private void initViews() {
        map_BTN_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsGetLocation();
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
                load();
                moveCameraOnMap(currentLocation);
            }
        });
    }

    private void save() {
        MySP.getInstance().putFloat(SP_LATITUDE_KEY,(float)currentLocation.getLatitude());
        MySP.getInstance().putFloat(SP_LONGITUDE_KEY,(float)currentLocation.getLongitude());
        MySP.getInstance().putString(SP_DESCRIPTION_KEY,map_TIL_address.getEditText().getText().toString());
    }
    private void load() {
        currentLocation = new Location(LocationManager.GPS_PROVIDER);
        currentLocation.setLatitude((double)MySP.getInstance().getFloat(SP_LATITUDE_KEY,0));
        currentLocation.setLongitude((double)MySP.getInstance().getFloat(SP_LONGITUDE_KEY,0));
        map_TIL_address.getEditText().setText(MySP.getInstance().getString(SP_DESCRIPTION_KEY,"No description available."));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if((requestCode == 100 &&(grantResults.length > 0 ) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED))){
            getCurrentLocation();
        }else {
            Toast.makeText(getActivity(),"Permission denied",Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermissionsGetLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            //when permission is not granted, request permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
            checkPermissionsGetLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)){
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    currentLocation = task.getResult();

                    if(currentLocation != null){
                        moveCameraOnMap(currentLocation);
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

    private void moveCameraOnMap(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I AM HERE!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        googleMap.addMarker(markerOptions);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

}