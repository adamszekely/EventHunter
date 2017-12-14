package com.eventhunters.eventhunter.model;

import java.util.Date;

/**
 * Created by Mio on 05-Dec-17.
 */

public class Event {
    public String id, name;
    public Date startDate;
    public Date endDate;
    public double lat, lng;

    public Event(String id, String name, Date startDate,Date endDate, double lat, double lng) {
        this.startDate=startDate;
        this.endDate=endDate;
        this.id=id;
        this.name=name;
        this.lat=lat;
        this.lng=lng;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }



}
