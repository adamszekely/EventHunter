package com.example.adam.eventhunter.model;

import android.graphics.drawable.Drawable;
import android.widget.Button;

/**
 * Created by Tomas on 05/12/2017.
 */

public class EventActivity {


    private String title;
    private String date;
    private String address;
    private String interested;
    private String going;
    private Drawable image;

    public EventActivity(String title, String date, String address, Drawable image, String going, String interested) {
        this.image = image;
        this.title = title;
        this.date = date;
        this.address = address;
        this.interested = interested;
        this.going = going;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getInterested() {
        return interested;
    }

    public String getGoing() {
        return going;
    }

    public void setInterested(String interested) {
        this.interested = interested;
    }

    public void setGoing(String going) {
        this.going = going;
    }

}
