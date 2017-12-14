package com.eventhunters.eventhunter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.eventhunters.eventhunter.PriorityQueue.*;
import com.eventhunters.eventhunter.Util.DatesDecorator;
import com.eventhunters.eventhunter.model.Event;
import com.eventhunters.eventhunter.model.EventActivity;
import com.eventhunters.eventhunter.Util.EventAdapter;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialCalendarView calendarView;
    private static Context mContext;
    private List<Event> events;
    private PriorityQueueEvent priorityQueueEvent;
    private AccessToken accessToken;
    private Date date;
    private List<CalendarDay> dates;
    private DatesDecorator mDecorator;
    private ArrayList<EventActivity> eventArrayList;
    private ListView listView;
    private EventAdapter eventAdapter;
    private ProgressBar progressBar;
    private int executed,listSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        //Start of initializations
        listView = (ListView) findViewById(R.id.listView);
        mContext = this.getApplicationContext();
        accessToken = AccessToken.getCurrentAccessToken();
        events = MainActivity.getListOfEvents();
        priorityQueueEvent = new PriorityQueueEvent();
        eventArrayList = new ArrayList<EventActivity>();
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_calendar);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        eventAdapter = new EventAdapter(CalendarActivity.this, eventArrayList);
        date = new Date();
        dates = new ArrayList<CalendarDay>();
        BottomNavigationView bottomNavigationMenuView = (BottomNavigationView) findViewById(R.id.menu);
        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        //End of initializations

        int colorCodeDark = Color.parseColor("#FF4052B5");
        progressBar.getIndeterminateDrawable().setColorFilter(colorCodeDark, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.INVISIBLE);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        setTitle("");
        //Highlights today
        calendarView.setSelectedDate(date);

        //Listener for bottom navigation bar
        bottomNavigationMenuView.setItemIconTintList(null);
        bottomNavigationMenuView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationMenuView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_list:
                        Intent intent = new Intent(CalendarActivity.this, ListActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_map:
                        Intent intent2 = new Intent(CalendarActivity.this, MainActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_calendar:

                        break;
                }
                return true;
            }
        });

        //Adds event dates to a CalendarDay array
        for (int i = 0; i < events.size(); i++) {
            date = events.get(i).getStartDate();
            dates.add(CalendarDay.from(date));
        }

        //Initialize decorator and assign it to the calendar
        mDecorator = new DatesDecorator(dates, mContext);
        calendarView.addDecorator(mDecorator);

        //Shows the today's events
        TodayShow();

        //Listener for the events list under the calendar
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(CalendarActivity.this,DetailedEventActivity.class);
                intent.putExtra("pageId",eventArrayList.get(position).getId());
                startActivity(intent);
            }
        });

        //Listener for clicking on another date
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                listSize=0;
                executed=0;

                //Clears the list under the calendar
                if (eventAdapter.getCount() != 0) {
                    listView.setAdapter(null);
                    eventArrayList.removeAll(eventArrayList);
                }

                //Adds events to the priorityQueue
                for (int i = 0; i < events.size(); i++) {
                    if (CalendarDay.from(events.get(i).getStartDate()).equals(date)) {
                        priorityQueueEvent.add(events.get(i));
                        listSize++;
                    }
                }

                //While the priorityQueue is not empty, retrieve the first event and get event details
                while (!priorityQueueEvent.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);
                    new getEventDetailsAsync().execute(priorityQueueEvent.poll().getId());
                }
            }
        });
    }


    private class getEventDetailsAsync extends AsyncTask<String, Void, Void> {
        Drawable drawable;

        @Override
        protected Void doInBackground(final String... strings) {

            Bundle params = new Bundle();

            //Request event details from Facebook
            new GraphRequest(
                    accessToken,
                    strings[0] + "?fields=name,place,start_time,picture.type(large),attending_count,maybe_count,id",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            //Receive response and add to JSON Object
                            JSONObject jsonObject = response.getJSONObject();
                            Log.d("JSONRESPONSE", response.getRawResponse() + "");

                            if (!response.equals("null") && jsonObject!=null) {
                                try {
                                    drawable = drawableFromUrl(jsonObject.getJSONObject("picture").getJSONObject("data")
                                            .getString("url"));
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    try {
                                        Date myDate = dateFormat.parse(jsonObject.getString("start_time"));
                                        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        String finalDate = dateFormat.format(myDate);

                                        eventArrayList.add(new EventActivity(jsonObject.getString("id"), jsonObject.getString("name"),
                                                finalDate, jsonObject.getJSONObject("place").getString("name"),
                                                drawable, "Going: " + (jsonObject.getString("attending_count")),
                                                "Interested: " + (jsonObject.getString("maybe_count"))));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.d("ERROR", "Response is null");
                            }
                        }
                    }).executeAndWait();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            listView.setAdapter(eventAdapter);
            listView.setVisibility(View.VISIBLE);
            executed++;
            if(executed==listSize){
                progressBar.setVisibility(View.INVISIBLE);
            }

        }
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(mContext.getResources(), x);
    }

    private void TodayShow()
    {
        listSize=0;
        executed=0;

        if (eventAdapter.getCount() != 0) {
            listView.setAdapter(null);
            eventArrayList.removeAll(eventArrayList);
        }
        date=new Date();
        for (int i = 0; i < events.size(); i++) {
            if (CalendarDay.from(events.get(i).getStartDate()).equals(CalendarDay.from(date))) {
                priorityQueueEvent.add(events.get(i));
                listSize++;
            }
        }
        while (!priorityQueueEvent.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            new getEventDetailsAsync().execute(priorityQueueEvent.poll().getId());
        }
    }
}
