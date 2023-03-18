package com.zohausman.current_location_tracker_mobilie_appfbdb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.zohausman.current_location_tracker_mobilie_appfbdb.databinding.ActivityMapsBinding;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
       GoogleApiClient.OnConnectionFailedListener,
      com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener Listener;
    private long UPDATE_INTERVAL = 2000;
    private long FASTEST_INTERVAL = 2000;
    private  LocationManager locationManager;
    private LatLng latLng;
    private boolean isPermission;


    private ActivityMapsBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


   if(requestSinglePermission()){
       // Obtain the SupportMapFragment and get notified when the map is ready to be used.
       SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
               .findFragmentById(R.id.map);
       mapFragment.getMapAsync(this);

     mGoogleApiClient = new GoogleApiClient.Builder(this)
             .addConnectionCallbacks(this)
             .addOnConnectionFailedListener(this)
             .addApi(LocationServices.API)
             .build();
     mLocationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
      checkLoction();

   }








    }

    private boolean checkLoction() {
        if(!isLocationEnable()){
            showAlert();
        }
        return isLocationEnable();
    }

    private void showAlert() {
         final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
         dialog.setTitle("Enable Location")
                 .setMessage("Your Location Settings is set to Off' .\n Please Enable Location to " +
                         " use this app")
                 .setPositiveButton("Loction Settings", new DialogInterface.OnClickListener(){

                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                         startActivity(myIntent);
                     }
                 })
                 .setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {

                     }
                 });
         dialog.show();
    }

    private boolean isLocationEnable() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


    }

    private boolean requestSinglePermission() {

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        isPermission=true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                      if(response.isPermanentlyDenied()){
                            isPermission= false;
                      }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

        return  isPermission;

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
      if(latLng!=null){
          mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Current Location"));
          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));

     }




    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            return;
        }
        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
         if(mLocation == null){
             startLocationUpdates();

         }
         else
         {
             Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show(); 
         }

    }

    private void startLocationUpdates() {

       mLocationRequest = LocationRequest.create()
               .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
               .setInterval(UPDATE_INTERVAL)
               .setFastestInterval(FASTEST_INTERVAL);
       if(ActivityCompat.checkSelfPermission(this,
               Manifest.permission.ACCESS_FINE_LOCATION)!=
       PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(this,
                       Manifest.permission.ACCESS_COARSE_LOCATION)!=
       PackageManager.PERMISSION_GRANTED){
           return;
       }
       // cod ehere for on resume and paused
       LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient,
               mLocationRequest, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
    }
    // on Resume and Pasused

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {


         String msg = "UpdatedLocation:" +
         Double.toString(location.getLatitude())+","+
        Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        LocationHelper helper = new LocationHelper(
                location.getLongitude(),
                location.getLatitude()
        );

        FirebaseDatabase.getInstance()
                .getReference("Current Location")
                .setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MapsActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(MapsActivity.this, "Location Not Saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                });




        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
}