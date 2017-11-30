package com.example.adam.eventhunter;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap map;
    private TextView textView;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
    }

    public void click(View v)
    {
        switch(v.getId()){
            case R.id.normal:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.satellite:
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.hybrid:
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
       mLocationRequest =LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},42);
        }
        else
        {
            Location mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation!=null)
            {
                LatLng lastLocation=new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraPosition target= CameraPosition.builder().target(lastLocation).zoom(18).build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                map.addMarker(new MarkerOptions()
                        .position(lastLocation)
                        .title("Here you are"));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
       mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
