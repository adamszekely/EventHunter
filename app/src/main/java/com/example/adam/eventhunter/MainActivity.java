package com.example.adam.eventhunter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.adam.eventhunter.PriorityQueue.PriorityQueueEvent;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements android.location.LocationListener, OnMapReadyCallback {
    private GoogleMap map;
    protected LocationRequest mLocationRequest;
    private LatLng newLocation;
    private Location currentLocation;
    private Toolbar toolbar;
    private static Context mContext;
    private FirebaseAuth mAuth;
    ConnectivityManager cm;
    AccessToken accessToken;
    private String userId;
    private List<String> pagesList;
    private static List<Event> eventsList;
    private boolean today, weekend, threedays, chooseDates, downloaded;
    private int start;
    private double listLength;
    private ThreadPoolExecutor executor;
    private Date now, pickedStartDate, pickedEndDate;
    private BottomNavigationView bottomNavigationMenuView;
    private ProgressBar progressBar;
    private TextView title, address, date, going, interested;
    private ImageView image, clockIcon, locationIcon, checkIcon, starIcon;
    private String mTitle, mAddress, mDate, mGoing, mInterested;
    private Drawable drawable;
    private Marker mMarker;
    private int colorCodeDark;
    private int firstTime = 0;
    private SpinnerTrigger spinnerTrigger;


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

        Spinner dynamicSpinnerMap = (Spinner) findViewById(R.id.dynamic_spinner_map_type);
        ArrayAdapter<String> adapterMap = new ArrayAdapter<String>(this,
                R.layout.dropdown_item_layout, getResources().getStringArray(R.array.map_type_array));

        SpinnerTrigger dynamicSpinnerDates = (SpinnerTrigger) findViewById(R.id.dynamic_spinner_dates);
        ArrayAdapter<String> adapterDates = new ArrayAdapter<String>(this,
                R.layout.dropdown_item_layout, getResources().getStringArray(R.array.dates_array));

        accessToken = AccessToken.getCurrentAccessToken();
        pagesList = new ArrayList<String>();
        eventsList = new ArrayList<Event>();
        executor = new ThreadPoolExecutor(12, 20, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2));
        now = new Date(System.currentTimeMillis());
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        colorCodeDark = Color.parseColor("#FF4052B5");
        progressBar.getIndeterminateDrawable().setColorFilter(colorCodeDark, PorterDuff.Mode.SRC_IN);
        //progressBar.setVisibility(View.VISIBLE);
        spinnerTrigger = new SpinnerTrigger(mContext);


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
                        if (executor.getActiveCount() == 0) {
                            progressBar.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(MainActivity.this, ListActivity.class);
                            startActivity(intent);
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            Toast.makeText(MainActivity.this, "Wait for downloading", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_map:
                        break;
                    case R.id.nav_calendar:
                        if (executor.getActiveCount() == 0) {
                            progressBar.setVisibility(View.VISIBLE);
                            Intent intent3 = new Intent(MainActivity.this, CalendarActivity.class);
                            startActivity(intent3);
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            Toast.makeText(MainActivity.this, "Wait for downloading", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });
        //End of bottom navigation menu

        //Map type drop-down list
        dynamicSpinnerMap.setAdapter(adapterMap);
        dynamicSpinnerMap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        dynamicSpinnerDates.setAdapter(adapterDates);

        dynamicSpinnerDates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorPrimary));
                // ((TextView) parent.getChildAt(0)).setTextSize(20);

                switch ((String) parent.getItemAtPosition(position)) {
                    case "Today":
                        today = true;
                        weekend = false;
                        threedays = false;
                        chooseDates = false;
                        progressBar.setVisibility(View.VISIBLE);
                        if (firstTime > 0) {
                            new setPinsOnMap().execute();
                        }
                        firstTime++;
                        break;
                    case "Weekend":
                        today = false;
                        weekend = true;
                        threedays = false;
                        chooseDates = false;
                        progressBar.setVisibility(View.VISIBLE);
                        new setPinsOnMap().execute();
                        break;
                    case "3 days":
                        today = false;
                        weekend = false;
                        threedays = true;
                        chooseDates = false;
                        progressBar.setVisibility(View.VISIBLE);
                        new setPinsOnMap().execute();
                        break;
                    case "Choose dates":
                        if (executor.getActiveCount() == 0) {
                            today = false;
                            weekend = false;
                            threedays = false;
                            chooseDates = true;

                            final Calendar c = Calendar.getInstance();
                            int year = c.get(Calendar.YEAR);
                            int month = c.get(Calendar.MONTH);
                            int day = c.get(Calendar.DAY_OF_MONTH);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                DatePickerDialogFragment datePickerDialogTo = new DatePickerDialogFragment(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert, new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                        if ((new LocalDate(year, month + 1, day).toDate()).before(pickedStartDate)) {
                                            Toast.makeText(MainActivity.this, "End date cannot be before the start date\nChoose another date", Toast.LENGTH_LONG).show();
                                        } else {
                                            pickedEndDate = new LocalDate(year, month + 1, day).toDate();
                                            new setPinsOnMap().execute();
                                        }
                                    }
                                }, year, month, day);
                                datePickerDialogTo.setPermanentTitle("To");
                                datePickerDialogTo.show();

                                DatePickerDialogFragment datePickerDialogFrom = new DatePickerDialogFragment(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert, new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                        pickedStartDate = new LocalDate(year, month + 1, day).toDate();
                                        Log.d("INSIDE", "Start");

                                    }
                                }, year, month, day);
                                datePickerDialogFrom.setPermanentTitle("From");
                                datePickerDialogFrom.show();
                            } else {
                                DatePickerDialogFragment datePickerDialogTo = new DatePickerDialogFragment(MainActivity.this, new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                        if ((new LocalDate(year, month + 1, day).toDate()).before(pickedStartDate)) {
                                            Toast.makeText(MainActivity.this, "End date cannot be before the start date\nChoose another date", Toast.LENGTH_LONG).show();
                                        } else {
                                            pickedEndDate = new LocalDate(year, month + 1, day).toDate();
                                            new setPinsOnMap().execute();
                                        }
                                    }
                                }, year, month, day);
                                datePickerDialogTo.setPermanentTitle("To");
                                datePickerDialogTo.show();

                                DatePickerDialogFragment datePickerDialogFrom = new DatePickerDialogFragment(MainActivity.this, new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                        pickedStartDate = new LocalDate(year, month + 1, day).toDate();
                                        Log.d("INSIDE", "Start");

                                    }
                                }, year, month, day);
                                datePickerDialogFrom.setPermanentTitle("From");
                                datePickerDialogFrom.show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Wait for downloading", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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
                                if (response != null && jsonObject != null) {
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
                                } else {
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
            for (int i = 0; i < 10; i++) {
                listLength = pagesList.size() * ((i + 1) * 0.1);
                new getPageEventsAsync().executeOnExecutor(executor, (int) listLength, start);
                start = (int) listLength;
            }
        } else {
            //Use this if the user has less than 25 liked pages
            new getAllPageEventsAsync().execute();
        }
        new getUserEventsAsync().executeOnExecutor(executor);
        new checkForBackgroundTasks().execute();
        executor.shutdown();
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
                    pageId + "/events?fields=id,place,name,start_time,end_time",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject jsonObject = response.getJSONObject();
                            Log.d("PAGEID2", pageId + "");
                            //Add all the ids of the pages a user likes into an arraylist
                            try {
                                if (response != null && jsonObject != null) {
                                    JSONArray eventsArray = jsonObject.getJSONArray("data");
                                    if (eventsArray.length() == 0) {
                                        Log.d("Pagelist", "Page list is empty");
                                        noData[0] = true;
                                    } else {
                                        for (int i = 0; i < eventsArray.length(); i++) {
                                            JSONObject event = eventsArray.getJSONObject(i);

                                            if (event.has("start_time") && event.has("end_time")) {
                                                //Parsing a date in a string to a Date type
                                                String strDate = event.getString("start_time");
                                                String eDate = event.getString("end_time");
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                                                Date startDate = dateFormat.parse(strDate);
                                                Date endDate = dateFormat.parse(eDate);


                                                //Only save events from today on
                                                if (startDate.after(now) || (startDate.before(now) && endDate.after(now))) {
                                                    //Only save events if it has a place object in the JSON
                                                    if (event.has("place")) {
                                                        //Only save events if it has a location object inside the place object in the JSON
                                                        if (event.getJSONObject("place").has("location")) {
                                                            Log.d("ALLEVENTS", event.getString("name"));
                                                            //Retrieving the location of an event
                                                            JSONObject locationObj = event.getJSONObject("place").getJSONObject("location");
                                                            double lat = locationObj.getDouble("latitude");
                                                            double lng = locationObj.getDouble("longitude");

                                                            //Create new event object
                                                            Event eventObj = new Event(event.getString("id"), event.getString("name"), startDate, endDate, lat, lng);
                                                            //Add the newly created event object to the list of events
                                                            eventsList.add(eventObj);
                                                            Log.d("JSONEvent", eventObj.name.toString());
                                                        } else noData[0] = true;
                                                    } else noData[0] = true;
                                                } else isEventOld[0] = true;
                                            } else noData[0] = true;
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
                                } else {
                                    noData[0] = true;
                                }
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

    private void getUserEvents(final String graphPath) {
        final String[] afterString = {""};  // will contain the next page cursor
        final Boolean[] noData = {false};// stop when there is no after cursor
        final Boolean[] isEventOld = {false};// stop when an event is older than today

        do {

            Bundle params = new Bundle();
            params.putString("after", afterString[0]);
            new GraphRequest(
                    accessToken,
                    graphPath,
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject jsonObject = response.getJSONObject();

                            //Add all the ids of the pages a user likes into an arraylist
                            try {
                                if (response != null && jsonObject != null) {
                                    JSONArray eventsArray = jsonObject.getJSONArray("data");
                                    if (eventsArray.length() == 0) {
                                        Log.d("Pagelist", "Page list is empty");
                                        noData[0] = true;
                                    } else {
                                        for (int i = 0; i < eventsArray.length(); i++) {
                                            JSONObject event = eventsArray.getJSONObject(i);

                                            if (event.has("start_time") && event.has("end_time")) {
                                                //Parsing a date in a string to a Date type
                                                String strDate = event.getString("start_time");
                                                String eDate = event.getString("end_time");
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                                                Date startDate = dateFormat.parse(strDate);
                                                Date endDate = dateFormat.parse(eDate);


                                                //Only save events from today on
                                                if (startDate.after(now) || (startDate.before(now) && endDate.after(now))) {
                                                    Log.d("JSONEvent", event.getString("name"));
                                                    //Only save events if it has a place object in the JSON
                                                    if (event.has("place")) {
                                                        //Only save events if it has a location object inside the place object in the JSON
                                                        if (event.getJSONObject("place").has("location")) {
                                                            //Retrieving the location of an event
                                                            JSONObject locationObj = event.getJSONObject("place").getJSONObject("location");
                                                            double lat = locationObj.getDouble("latitude");
                                                            double lng = locationObj.getDouble("longitude");

                                                            //Create new event object
                                                            Event eventObj = new Event(event.getString("id"), event.getString("name"), startDate, endDate, lat, lng);
                                                            //Add the newly created event object to the list of events
                                                            eventsList.add(eventObj);

                                                        } else if (event.getJSONObject("place").has("name")) {
                                                            LatLng address = getLocationFromAddress(mContext, event.getJSONObject("place").getString("name"));
                                                            if (address != null) {
                                                                double lat = address.latitude;
                                                                double lng = address.longitude;

                                                                //Create new event object
                                                                Event eventObj = new Event(event.getString("id"), event.getString("name"), startDate, endDate, lat, lng);
                                                                //Add the newly created event object to the list of events
                                                                eventsList.add(eventObj);

                                                            }
                                                        } else noData[0] = true;
                                                    } else noData[0] = true;
                                                } else isEventOld[0] = true;
                                            } else if (event.has("start_time") && !event.has("end_time")) {
                                                //Parsing a date in a string to a Date type
                                                String strDate = event.getString("start_time");
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                                                Date startDate = dateFormat.parse(strDate);
                                                DateTime endTime = new DateTime(startDate);
                                                Date endDate = endTime.plusHours(6).toDate();

                                                //Only save events from today on
                                                if (startDate.after(now) || (startDate.before(now) && endDate.after(now))) {
                                                    Log.d("ENDDATE", endDate + "...." + event.getString("name"));
                                                    Log.d("JSONEvent", event.getString("name"));
                                                    //Only save events if it has a place object in the JSON
                                                    if (event.has("place")) {
                                                        //Only save events if it has a location object inside the place object in the JSON
                                                        if (event.getJSONObject("place").has("location")) {
                                                            //Retrieving the location of an event
                                                            JSONObject locationObj = event.getJSONObject("place").getJSONObject("location");
                                                            double lat = locationObj.getDouble("latitude");
                                                            double lng = locationObj.getDouble("longitude");

                                                            //Create new event object
                                                            Event eventObj = new Event(event.getString("id"), event.getString("name"), startDate, endDate, lat, lng);
                                                            //Add the newly created event object to the list of events
                                                            eventsList.add(eventObj);

                                                        } else if (event.getJSONObject("place").has("name")) {
                                                            LatLng address = getLocationFromAddress(mContext, event.getJSONObject("place").getString("name"));
                                                            if (address != null) {
                                                                double lat = address.latitude;
                                                                double lng = address.longitude;

                                                                //Create new event object
                                                                Event eventObj = new Event(event.getString("id"), event.getString("name"), startDate, endDate, lat, lng);
                                                                //Add the newly created event object to the list of events
                                                                eventsList.add(eventObj);
                                                            }
                                                        } else noData[0] = true;
                                                    } else noData[0] = true;
                                                } else isEventOld[0] = true;
                                            } else noData[0] = true;
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
                                } else {
                                    noData[0] = true;
                                }
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
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {

                mMarker = marker;
                if (downloaded == false) {
                    new getEventDetailsAsync().execute(mMarker.getTitle());
                }
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.activity_preview, null);
                /*ProgressBar progressBarInfo = (ProgressBar) v.findViewById(R.id.progressBar4);
                progressBarInfo.getIndeterminateDrawable().setColorFilter(colorCodeDark, PorterDuff.Mode.SRC_IN);
                progressBarInfo.setVisibility(View.VISIBLE);*/
                title = (TextView) v.findViewById(R.id.title);
                address = (TextView) v.findViewById(R.id.address);
                date = (TextView) v.findViewById(R.id.date);
                image = (ImageView) v.findViewById(R.id.image);
                going = (TextView) v.findViewById(R.id.going);
                interested = (TextView) v.findViewById(R.id.interested);
                clockIcon = (ImageView) v.findViewById(R.id.clock);
                locationIcon = (ImageView) v.findViewById(R.id.location);
                checkIcon = (ImageView) v.findViewById(R.id.check);
                starIcon = (ImageView) v.findViewById(R.id.star);

                if (downloaded == true) {
                    title.setText(mTitle);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    try {
                        Date myDate = dateFormat.parse(mDate);
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String finalDate = dateFormat.format(myDate);
                        date.setText(finalDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    address.setText(mAddress);
                    image.setImageDrawable(drawable);
                    going.setText(mGoing);
                    interested.setText(mInterested);
                    clockIcon.setVisibility(View.VISIBLE);
                    locationIcon.setVisibility(View.VISIBLE);
                    checkIcon.setVisibility(View.VISIBLE);
                    starIcon.setVisibility(View.VISIBLE);
                    downloaded = false;
                    // progressBarInfo.setVisibility(View.INVISIBLE);
                }
                return v;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //Toast.makeText(MainActivity.this, "Implementation later", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, DetailedEvent.class);
                intent.putExtra("pageId", marker.getTitle());
                startActivity(intent);

            }
        });

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request the missing permissions, and then overriding
            return;
        }
        //Enables the user's precise location and updates the UI
        map.setMyLocationEnabled(true);
        updateUI();
        //Retrieves the pages the user liked
        new getPagesAsync().executeOnExecutor(executor);

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

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            } else if (address.size() > 0) {
                Address location = address.get(0);
                location.getLatitude();
                location.getLongitude();

                p1 = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                return null;
            }
        } catch (IOException ex) {

            ex.printStackTrace();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        return p1;
    }

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
            case R.id.nav_refresh:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;


            case R.id.nav_logout:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent2 = new Intent(MainActivity.this, FacebookActivity.class);
                startActivity(intent2);
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
            LocalDate nowTime = new LocalDate();
            DateTime dateTime = new DateTime();
            map.clear();
            if (connected) {
                if (today == true) {
                    Date tomorrow = nowTime.plusDays(1).toDate();
                    //Go through the list of events and add a pin to the map for each one
                    for (int i = 0; i < eventsList.size(); i++) {
                        if (eventsList.get(i).startDate != null) {
                            if (eventsList.get(i).startDate.before(tomorrow)) {
                                if (eventsList.get(i).startDate.before(now) && eventsList.get(i).endDate.after(now)) {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags_green",64,64))));
                                } else {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags",64,64))));
                                }
                            }
                        }
                    }
                } else if (weekend == true) {
                    int old = nowTime.getDayOfWeek();
                    LocalDate next = nowTime.plusDays(8 - old);
                    Date monday = next.toDate();
                    Date friday = nowTime.withDayOfWeek(DateTimeConstants.FRIDAY).toDate();

                    //Go through the list of events and add a pin to the map for each one
                    for (int i = 0; i < eventsList.size(); i++) {
                        if (eventsList.get(i).startDate != null && eventsList.get(i).endDate != null) {
                            if (eventsList.get(i).startDate.before(monday) && ((eventsList.get(i).startDate.before(friday) && eventsList.get(i).endDate.after(friday)) || eventsList.get(i).startDate.after(friday))) {
                                Log.d("WEEKEND", eventsList.get(i).startDate + ",    " + eventsList.get(i).endDate);
                                if (eventsList.get(i).startDate.before(now) && eventsList.get(i).endDate.after(now)) {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags_green",64,64))));
                                } else {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags",64,64))));
                                }
                            }
                        } else if (eventsList.get(i).endDate == null) {
                            if (eventsList.get(i).startDate.before(monday) && eventsList.get(i).startDate.after(friday)) {
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                        .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags",64,64))));
                            }
                        }
                    }
                } else if (threedays == true) {

                    Date three = nowTime.plusDays(3).toDate();
                    Log.d("TIMES2", three + " THREE");
                    //Go through the list of events and add a pin to the map for each one
                    for (int i = 0; i < eventsList.size(); i++) {
                        if (eventsList.get(i).startDate != null) {
                            if (eventsList.get(i).startDate.before(three)) {
                                Log.d("TIMES", eventsList.get(i).startDate + ", " + now);
                                if (eventsList.get(i).startDate.before(now) && eventsList.get(i).endDate.after(now)) {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags_green",64,64))));
                                } else {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags",64,64))));
                                }
                            }
                        }
                    }
                } else if (chooseDates == true) {
                    //Go through the list of events and add a pin to the map for each one
                    for (int i = 0; i < eventsList.size(); i++) {
                        if (eventsList.get(i).startDate != null && eventsList.get(i).endDate != null) {
                            if (eventsList.get(i).startDate.before(pickedEndDate) && ((eventsList.get(i).startDate.before(pickedStartDate) && eventsList.get(i).endDate.after(pickedStartDate)) || eventsList.get(i).startDate.after(pickedStartDate))) {
                                Log.d("TIMES", eventsList.get(i).startDate + ", " + now);
                                if (eventsList.get(i).startDate.before(now) && eventsList.get(i).endDate.after(now)) {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags_green",64,64))));
                                } else {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(eventsList.get(i).lat, eventsList.get(i).lng))
                                            .title(eventsList.get(i).id).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("maps_and_flags",64,64))));
                                }
                            }
                        }
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);

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
            progressBar.setVisibility(View.VISIBLE);
            Log.d("JSON", pagesList.size() + "");
            Log.d("JSONEvent", eventsList.size() + "");
        }
    }

    private class getPageEventsAsync extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... pageArrayLength) {
            Log.d("Pagelist", pageArrayLength[1] + ", " + pageArrayLength[0] + " here");
            for (int i = pageArrayLength[1]; i < pageArrayLength[0]; i++) {
                getEvents(pagesList.get(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //new setPinsOnMap().execute();
        }
    }

    private class getAllPageEventsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            for (int i = 0; i < pagesList.size(); i++) {
                getEvents(pagesList.get(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // new setPinsOnMap().execute();
            Log.d("JSONEvent", eventsList.size() + "");
        }
    }

    private class getUserEventsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            getUserEvents("me/events/maybe?fields=id,place,name,start_time,end_time");
            getUserEvents("me/events/attending?fields=id,place,name,start_time,end_time");
            getUserEvents("me/events/not_replied?fields=id,place,name,start_time,end_time");


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //new setPinsOnMap().execute();
            Log.d("JSONEvent", eventsList.size() + "");
        }
    }

    private class getEventDetailsAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... strings) {
            Bundle params = new Bundle();
            new GraphRequest(
                    accessToken,
                    strings[0] + "?fields=name,place,start_time,picture.type(large),attending_count,maybe_count",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject jsonObject = response.getJSONObject();

                            //Add all the ids of the pages a user likes into an arraylist
                            if (response != null) {
                                try {
                                    mTitle = (jsonObject.getString("name"));
                                    mDate = (jsonObject.getString("start_time"));
                                    mAddress = (jsonObject.getJSONObject("place").getString("name").toString());
                                    drawable = drawableFromUrl(jsonObject.getJSONObject("picture").getJSONObject("data")
                                            .getString("url"));
                                    mGoing = "Going: " + (jsonObject.getString("attending_count"));
                                    mInterested = "Interested: " + (jsonObject.getString("maybe_count"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).executeAndWait();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            downloaded = true;
            if (mMarker != null && mMarker.isInfoWindowShown()) {
                mMarker.showInfoWindow();
            }
        }
    }

    private class checkForBackgroundTasks extends AsyncTask<Void, Void, Void> {
        boolean running = true;

        @Override
        protected Void doInBackground(Void... voids) {
            while (executor.getActiveCount() != 0) {
                running = true;
            }
            if (executor.getActiveCount() == 0) {
                running = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!running) {
                new setPinsOnMap().execute();
            }
        }
    }

    public Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(mContext.getResources(), x);
    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public static List<Event> getListOfEvents() {
        //Log.i("LastINQUEUE", eventsList.size() + "");
        return eventsList;
    }
}
