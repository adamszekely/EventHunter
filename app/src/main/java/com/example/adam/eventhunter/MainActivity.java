package com.example.adam.eventhunter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements android.location.LocationListener, OnMapReadyCallback {
    private GoogleMap map;
    protected LocationRequest mLocationRequest;
    private LatLng newLocation;
    private Location currentLocation;
    private Toolbar toolbar;
    private Context mContext;
    private FirebaseAuth mAuth;
    ConnectivityManager cm;
    AccessToken accessToken;
    private String userId;
    private List<String> pagesList;
    private List<Event> eventsList;
    private boolean arePagesDone = false;
    private int start;
    private double listLength;
    private ThreadPoolExecutor executor;
    private Date now;
    private BottomNavigationView bottomNavigationMenuView;
    // Flag for GPS status
    boolean isGPSEnabled = false;
    // Flag for network status
    boolean isNetworkEnabled = false;
    // Flag for GPS status
    boolean canGetLocation = false;

    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 minute
    // Declaring a Location Manager
    protected LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initializations
        createLocationRequest();
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        currentLocation = getLocation();
        bottomNavigationMenuView = (BottomNavigationView) findViewById(R.id.menu);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        Spinner dynamicSpinner = (Spinner) findViewById(R.id.dynamic_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.map_type_array));
        accessToken = AccessToken.getCurrentAccessToken();
        pagesList = new ArrayList<String>();
        eventsList = new ArrayList<Event>();
        executor = new ThreadPoolExecutor(10, 15, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2));
        now = new Date(System.currentTimeMillis());
        //End of initializations

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Bottom navigation menu
        bottomNavigationMenuView.setItemIconTintList(null);
        bottomNavigationMenuView.setSelectedItemId(R.id.nav_map);
        bottomNavigationMenuView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_list:
                        Toast.makeText(MainActivity.this, "Implementation later", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_map:
                        break;
                    case R.id.nav_calendar:
                        Intent intent3 = new Intent(MainActivity.this, CalendarActivity.class);
                        startActivity(intent3);
                        finish();
                        break;
                }
                return true;
            }
        });
        //End of bottom navigation menu

        //Map type drop-down list
        dynamicSpinner.setAdapter(adapter);
        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorPrimary));
                //((TextView) parent.getChildAt(0)).setTextSize(20);
                switch ((String) parent.getItemAtPosition(position)) {
                    case "Normal":
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case "Satellite":
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case "Hybrid":
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //End of map type drop-down list
    }

    private void getPages() {
        final String[] afterString = {""};  // will contain the next page cursor
        final Boolean[] noData = {false};   // stop when there is no after cursor
        do {
            Bundle params = new Bundle();
            params.putString("after", afterString[0]);
            new GraphRequest(
                    accessToken,
                    "/me/likes?fields=id",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject jsonObject = response.getJSONObject();
                            //Add all the ids of the pages a user likes into an arraylist
                            try {
                                if (response != null) {
                                    //Get JSON array from "data" object from Facebook JSON
                                    JSONArray pagesIdArray = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < pagesIdArray.length(); i++) {
                                        //Create new page object and add it to the list of pages
                                        JSONObject page = pagesIdArray.getJSONObject(i);
                                        pagesList.add(page.getString("id"));
                                    }

                                    //Get the next page of ids from JSONObject
                                    if (!jsonObject.isNull("paging")) {
                                        JSONObject paging = jsonObject.getJSONObject("paging");
                                        JSONObject cursors = paging.getJSONObject("cursors");
                                        if (!cursors.isNull("after"))
                                            afterString[0] = cursors.getString("after");
                                        else
                                            noData[0] = true;
                                    } else
                                        noData[0] = true;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).executeAndWait();
        }
        while (!noData[0] == true);
        //If the user has more than 25 liked pages divide the list of pages into 4 parts and start a thread for each one which
        //retrieves the list of events
        //Using the ThreadPoolExecutor, the threads run simultaneously so retrieving the events is fast
        if (pagesList.size() > 25) {
            start = 0;
            for(int i=0;i<10;i++)
            {
                listLength=pagesList.size()*((i+1)*0.1);
                new getEventsAsync().executeOnExecutor(executor, (int) listLength, start);
                start=(int) listLength;
            }
        } else {
            //Use this if the user has less than 25 liked pages
            new getAllEventsAsync().execute();
        }

    }

    private void getEvents(final String pageId) {
        final String[] afterString = {""};  // will contain the next page cursor
        final Boolean[] noData = {false};// stop when there is no after cursor
        final Boolean[] isEventOld = {false};// stop when an event is older than today

        do {

            Bundle params = new Bundle();
            params.putString("after", afterString[0]);
            new GraphRequest(
                    accessToken,
                    pageId + "/events?fields=id,place,name,start_time",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject jsonObject = response.getJSONObject();
                            Log.d("PAGEID2", pageId + "");
                            //Add all the ids of the pages a user likes into an arraylist
                            try {
                                if (response != null) {
                                    JSONArray eventsArray = jsonObject.getJSONArray("data");
                                    if (eventsArray.length() == 0) {
                                        Log.d("Pagelist", "Page list is empty");
                                        noData[0] = true;
                                    } else {
                                        for (int i = 0; i < eventsArray.length(); i++) {
                                            JSONObject event = eventsArray.getJSONObject(i);

                                            //Parsing a date in a string to a Date type
                                            String strDate = event.getString("start_time");
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                                            Date date = dateFormat.parse(strDate);

                                            //Only save events from today on
                                            if (date.after(now)) {
                                                //Only save events if it has a place object in the JSON
                                                if (event.has("place")) {
                                                    //Only save events if it has a location object inside the place object in the JSON
                                                    if (event.getJSONObject("place").has("location")) {
                                                        //Retrieving the location of an event
                                                        JSONObject locationObj = event.getJSONObject("place").getJSONObject("location");
                                                        double lat = locationObj.getDouble("latitude");
                                                        double lng = locationObj.getDouble("longitude");

                                                        //Create new event object
                                                        Event eventObj = new Event(event.getString("id"), event.getString("name"), date, lat, lng);
                                                        //Add the newly created event object to the list of events
                                                        eventsList.add(eventObj);
                                                        Log.d("JSONEvent", eventObj.name.toString());
                                                    } else noData[0] = true;
                                                } else noData[0] = true;
                                            } else isEventOld[0] = true;
                                        }


                                        //Get the next page of the user's liked pages ids from JSONObject
                                        if (!jsonObject.isNull("paging")) {
                                            JSONObject paging = jsonObject.getJSONObject("paging");
                                            JSONObject cursors = paging.getJSONObject("cursors");
                                            if (!cursors.isNull("after"))
                                                afterString[0] = cursors.getString("after");
                                            else
                                                noData[0] = true;
                                        } else
                                            noData[0] = true;
                                    }
                                } else
                                    noData[0] = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            ).executeAndWait();
        }
        while (noData[0] == false && isEventOld[0] == false);

    }

    public void updateUI() {
        //If the user's location is detected the view is zoomed in to the user's position
        if (currentLocation != null) {
            newLocation = new LatLng(getLocation().getLatitude(), getLocation().getLongitude());
            CameraPosition target = CameraPosition.builder().target(newLocation).zoom(14).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request the missing permissions, and then overriding
            return;
        }
        //Enables the user's precise location and updates the UI
        map.setMyLocationEnabled(true);
        updateUI();
        //Retrieves the pages the user liked
        new getPagesAsync().execute();

    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // Getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
            } else {
                this.canGetLocation = true;
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //request the missing permissions, and then overriding
                    return null;
                }
                //Request location updates with the given parameters
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                //Set the "location" to the last known location if the location manager is not null
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

//    public LatLng getLocationFromAddress(Context context, String strAddress) {
//
//        Geocoder coder = new Geocoder(context);
//        List<Address> address;
//        LatLng p1 = null;
//
//        try {
//            // May throw an IOException
//            address = coder.getFromLocationName(strAddress, 5);
//            if (address == null) {
//                /*Toast.makeText(this,"Wait a second",Toast.LENGTH_LONG).show();
//                Thread.sleep(3000);*/
//                return null;
//            }
//            Address location = address.get(0);
//            location.getLatitude();
//            location.getLongitude();
//
//            p1 = new LatLng(location.getLatitude(), location.getLongitude());
//
//        } catch (IOException ex) {
//
//            ex.printStackTrace();
//        } /*catch (InterruptedException e) {
//            e.printStackTrace();
//        }*/
//
//        return p1;
//    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(MainActivity.this, FacebookActivity.class);
                startActivity(intent);
                finish();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private class setPinsOnMap extends AsyncTask<Void, Void, Void> {
        boolean connected = false;

        @Override
        protected Void doInBackground(Void... voids) {
            connected = true;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = calendar.getTime();
            if (connected) {
                //Go through the list of events and add a pin to the map for each one
                for (int i = 0; i < eventsList.size(); i++) {
                    if (eventsList.get(i).date != null) {
                        if (eventsList.get(i).date.before(tomorrow)) {
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                    .title(eventsList.get(i).name));
                        }
                    }
                }
            }
        }
    }

    private class getPagesAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getPages();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("JSON", pagesList.size() + "");
            Log.d("JSONEvent", eventsList.size() + "");
        }
    }

    private class getEventsAsync extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... pageArrayLength) {
            Log.d("Pagelist", pageArrayLength[1] + ", " + pageArrayLength[0] + " here");
            for (int i = pageArrayLength[1]; i < pageArrayLength[0]; i++) {
                getEvents(pagesList.get(i));
            }
            new setPinsOnMap().execute();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    private class getAllEventsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            for (int i = 0; i < pagesList.size(); i++) {
                getEvents(pagesList.get(i));
            }
            new setPinsOnMap().execute();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("JSONEvent", eventsList.size() + "");
        }
    }


}
