package com.example.adam.eventhunter;

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.adam.eventhunter.PriorityQueue.PriorityQueueEvent;
import com.example.adam.eventhunter.model.EventActivity;
import com.example.adam.eventhunter.model.EventAdapter;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.DayFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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
        listView = (ListView) findViewById(R.id.listView);
        mContext = this.getApplicationContext();
        accessToken = AccessToken.getCurrentAccessToken();
        events = MainActivity.getListOfEvents();
        priorityQueueEvent = new PriorityQueueEvent();
        eventArrayList = new ArrayList<EventActivity>();
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_calendar);
        int colorCodeDark = Color.parseColor("#FF4052B5");
        progressBar.getIndeterminateDrawable().setColorFilter(colorCodeDark, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.INVISIBLE);
        setTitle("");
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        eventAdapter = new EventAdapter(CalendarActivity.this, eventArrayList);

        dates = new ArrayList<CalendarDay>();

        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        BottomNavigationView bottomNavigationMenuView = (BottomNavigationView) findViewById(R.id.menu);
        bottomNavigationMenuView.setItemIconTintList(null);
        bottomNavigationMenuView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationMenuView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_list:
                        Intent intent = new Intent(CalendarActivity.this, ListActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_map:
                        Intent intent2 = new Intent(CalendarActivity.this, MainActivity.class);
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.nav_calendar:

                        break;
                }
                return true;
            }
        });


        for (int i = 0; i < events.size(); i++) {
            date = events.get(i).getStartDate();
            dates.add(CalendarDay.from(date));
        }

        mDecorator = new DatesDecorator(dates, mContext);
        calendarView.addDecorator(mDecorator);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(CalendarActivity.this,DetailedEvent.class);
                intent.putExtra("pageId",eventArrayList.get(position).getId());
                startActivity(intent);
            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                listSize=0;
                executed=0;

                if (eventAdapter.getCount() != 0) {
                    listView.setAdapter(null);
                    eventArrayList.removeAll(eventArrayList);
                }
                for (int i = 0; i < events.size(); i++) {
                    if (CalendarDay.from(events.get(i).getStartDate()).equals(date)) {
                        priorityQueueEvent.add(events.get(i));
                        listSize++;
                    }
                }
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
            new GraphRequest(
                    accessToken,
                    strings[0] + "?fields=name,place,start_time,picture.type(large),attending_count,maybe_count,id",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject jsonObject = response.getJSONObject();
                            Log.d("JSONRESPONSE", response.getRawResponse() + "");
                            //Add all the ids of the pages a user likes into an arraylist
                            if (!response.equals("null")) {
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
}
