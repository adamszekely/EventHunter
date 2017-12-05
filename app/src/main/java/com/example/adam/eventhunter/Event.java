package com.example.adam.eventhunter;

import java.util.Date;

/**
 * Created by Mio on 05-Dec-17.
 */

public class Event {
    String id, name;
    Date date;
    double lat, lng;



    public Event(String id, String name, Date date, double lat, double lng) {
        this.date=date;
        this.id=id;
        this.name=name;
        this.lat=lat;
        this.lng=lng;
    }


}
