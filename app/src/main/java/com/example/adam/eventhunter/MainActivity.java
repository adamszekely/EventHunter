package com.example.adam.eventhunter;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    protected LocationRequest mLoactionRequest;

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
  /*LatLng horsens=new LatLng(55.871805, 9.886148);
  CameraPosition target= CameraPosition.builder().target(horsens).zoom(16).build();
  map.moveCamera(CameraUpdateFactory.newCameraPosition(target));*/
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
        textView.setText("Longitude: "+location.getLongitude()+" Latitude: "+location.getLatitude() );
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        mLoactionRequest=LocationRequest.create();
        mLoactionRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLoactionRequest.setInterval(1000);
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},42);
        }
        else
        {
            // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLoactionRequest,this);
            Location   mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation!=null)
            {
                textView.setText("Longitude: "+mLastLocation.getLongitude()+"\nLatitude: "+mLastLocation.getLatitude() );
                LatLng horsens=new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraPosition target= CameraPosition.builder().target(horsens).zoom(18).build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                map.addMarker(new MarkerOptions()
                        .position(horsens)
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
