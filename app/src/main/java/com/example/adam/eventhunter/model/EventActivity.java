package com.example.adam.eventhunter.model;

import android.graphics.drawable.Drawable;
import android.widget.Button;

/**
 * Created by Tomas on 05/12/2017.
 */

public class EventActivity {


   private String title;
   private String date;
   private String time;
   private String address;
    private  Button detailsButton;
    private Drawable image;

   public EventActivity(String title, String date, String time, String address, Button detailsButton, Drawable image){
       this.image = image;
       this.title = title;
       this.date = date;
       this.time = time;
       this.address = address;
       this.detailsButton = detailsButton;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Button getDetailsButton() {
        return detailsButton;
    }

    public void setDetailsButton(Button detailsButton) {
        this.detailsButton = detailsButton;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }
}
