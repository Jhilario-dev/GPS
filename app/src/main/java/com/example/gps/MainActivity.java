package com.example.gps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int default_update_interval = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_wayPointCounts;

    Button btn_newWaypoint, btn_showWayPointList,  btn_showMap;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_locationupdates, sw_gps;

    boolean updateOn = false;

    Location currentLocation;

    List<Location> savedLocations;

    LocationRequest locationRequest;

    LocationCallback locationCallback;


    FusedLocationProviderClient fusedLocationProviderClient;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);

        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        btn_newWaypoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPoint);
        btn_showMap = findViewById(R.id.btn_showMap);


        locationRequest = new LocationRequest();


        locationRequest.setInterval(1000 * default_update_interval);


        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                updateUIValues(locationResult.getLastLocation());
            }
        };

        btn_newWaypoint.setOnClickListener(v -> {

            Myapplication myapplication = (Myapplication)getApplicationContext();
            savedLocations = myapplication.getMyLocations();
            savedLocations.add(currentLocation);

        });

        btn_showWayPointList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowSavedLocationList.class);
            startActivity(intent);
        });

        btn_showMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });




        sw_gps.setOnClickListener(v -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                tv_sensor.setText("Using GPS sensor");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                tv_sensor.setText("Using towers + wifi");
            }
        });


        sw_locationupdates.setOnClickListener(v -> {
            if (sw_locationupdates.isChecked()) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        });


        updateGPS();


    }

    @SuppressLint("SetTextI18n")
    private void stopLocationUpdates() {
        tv_updates.setText("Location is not being tracked");
        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not tracking location");
        tv_address.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("SetTextI18n")
    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(this, "this app requires permission", Toast.LENGTH_SHORT).show();
                finish();

            }
        }


    }

    private void updateGPS(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == getPackageManager().PERMISSION_GRANTED){

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                updateUIValues(location);
                currentLocation = location;
            });
        }else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUIValues(Location location) {

        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else {
            tv_altitude.setText("Not available");
        }


        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }else{
            tv_speed.setText("Not available");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1 );
            tv_address.setText(addresses.get(0).getAddressLine(0));


        }catch (IOException e) {

            tv_address.setText("Unable to get street address");

        }

        Myapplication myapplication = (Myapplication)getApplicationContext();
        savedLocations = myapplication.getMyLocations();

        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));

    }
}






















