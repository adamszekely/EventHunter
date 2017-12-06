package com.example.adam.eventhunter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.adam.eventhunter.model.EventActivity;
import com.example.adam.eventhunter.model.EventAdapter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;

public class CalendarActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialCalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        setTitle("");
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNavigationMenuView = (BottomNavigationView) findViewById(R.id.menu);
        bottomNavigationMenuView.setItemIconTintList(null);
        bottomNavigationMenuView.setSelectedItemId(R.id.nav_map);
        bottomNavigationMenuView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_list:
                        Toast.makeText(CalendarActivity.this, "Implementation later", Toast.LENGTH_SHORT).show();
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


        final ArrayList<EventActivity> eventArrayList = new ArrayList<EventActivity>();
        eventArrayList.add(new EventActivity("Android Event","OCT 28","12Am","Horsens Campus",null,null));
        eventArrayList.add(new EventActivity("Adam EVENTOOO","OCT 28","12Am","Horsens Campus",null,null));
        eventArrayList.add(new EventActivity("Peters dentist","OCT 28","12Am","Horsens Campus",null,null));


        EventAdapter eventAdapter = new EventAdapter(this,eventArrayList);
        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(eventAdapter);


       calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
           @Override
           public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
           //    Toast.makeText(CalendarActivity.this,"" +date,Toast.LENGTH_SHORT).show();
                listView.setVisibility(View.VISIBLE);
           }
       });



    }

}
