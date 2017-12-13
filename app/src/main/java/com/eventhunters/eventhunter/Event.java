package com.eventhunters.eventhunter;

import java.util.Date;

/**
 * Created by Mio on 05-Dec-17.
 */

public class Event {
    String id, name;
    Date startDate;

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

    Date endDate;
    double lat, lng;

}
