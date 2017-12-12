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
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.adam.eventhunter.PriorityQueue.PriorityQueueEvent;
import com.example.adam.eventhunter.model.EventActivity;
import com.example.adam.eventhunter.model.EventAdapter;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

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
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ListActivity extends AppCompatActivity {

    AccessToken accessToken;
    private static Context mContext;
    ArrayList<EventActivity> eventArrayList;
    ProgressBar progressBar;
    List<Event> events;
    BottomNavigationView bottomNavigationMenuView;
    ThreadPoolExecutor executor;
    boolean addedFooter;
    View view;
    ListView listView;
    Button buttonMore;
    PriorityQueueEvent priorityQueueEvent;
    int exeNumber=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        events = MainActivity.getListOfEvents();
        accessToken = AccessToken.getCurrentAccessToken();
        priorityQueueEvent = new PriorityQueueEvent();
        mContext = this.getApplicationContext();
        eventArrayList = new ArrayList<EventActivity>();
        bottomNavigationMenuView = (BottomNavigationView) findViewById(R.id.menu);
        progressBar = (ProgressBar) findViewById(R.id.progressBarList);
        int colorCodeDark = Color.parseColor("#FF4052B5");
        progressBar.getIndeterminateDrawable().setColorFilter(colorCodeDark, PorterDuff.Mode.SRC_IN);
        listView = (ListView) findViewById(R.id.listView_list);
        view = getLayoutInflater().inflate(R.layout.more_button_layout, listView, false);
        executor = new ThreadPoolExecutor(12, 20, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2));
        for (int i = 0; i < events.size(); i++) {
            priorityQueueEvent.add(events.get(i));
        }
        progressBar.setVisibility(View.INVISIBLE);
        //Bottom navigation menu
        bottomNavigationMenuView.setItemIconTintList(null);
        bottomNavigationMenuView.setSelectedItemId(R.id.nav_list);
        bottomNavigationMenuView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_list:
                        break;
                    case R.id.nav_map:
                        progressBar.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(ListActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_calendar:
                        progressBar.setVisibility(View.VISIBLE);
                        Intent intent3 = new Intent(ListActivity.this, CalendarActivity.class);
                        startActivity(intent3);
                        break;
                }
                return true;
            }
        });
        //End of bottom navigation menu
        downloadTenMore();

        buttonMore = view.findViewById(R.id.buttonMore);

        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!priorityQueueEvent.isEmpty()) {

                    downloadTenMore();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(ListActivity.this,DetailedEvent.class);
                intent.putExtra("pageId",eventArrayList.get(position).getId());
                startActivity(intent);
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

                            //Add all the ids of the pages a user likes into an arraylist
                            if (jsonObject != null) {
                                try {
                                    drawable = drawableFromUrl(jsonObject.getJSONObject("picture").getJSONObject("data")
                                            .getString("url"));
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    try {
                                        Date myDate = dateFormat.parse(jsonObject.getString("start_time"));
                                        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        String finalDate = dateFormat.format(myDate);

                                        eventArrayList.add(new EventActivity(jsonObject.getString("id"),jsonObject.getString("name"),
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
                            }
                        }
                    }).executeAndWait();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            EventAdapter eventAdapter = new EventAdapter(ListActivity.this, eventArrayList);
            if (!addedFooter) {
                listView.addFooterView(view);
                addedFooter = true;
            }

            listView.setAdapter(eventAdapter);

            exeNumber++;
            if(exeNumber==10 || priorityQueueEvent.isEmpty())
            {
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelection(eventArrayList.size()-10);
                    }
                });
                progressBar.setVisibility(View.INVISIBLE);
                exeNumber=0;
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

    private void downloadTenMore() {
        progressBar.setVisibility(View.VISIBLE);
        for (int i = 0; i < 10; i++) {
            new getEventDetailsAsync().execute(priorityQueueEvent.poll().getId());
            if (priorityQueueEvent.isEmpty()) {
                break;
            }
        }
    }
}
