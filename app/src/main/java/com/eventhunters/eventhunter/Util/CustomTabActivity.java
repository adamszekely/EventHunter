package com.eventhunters.eventhunter.Util;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eventhunters.eventhunter.R;

//Used by Facebook login
public class CustomTabActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_tab);
    }
}
