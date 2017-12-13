package com.eventhunters.eventhunter.model;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.eventhunters.eventhunter.R;

import java.util.ArrayList;

/**
 * Created by Tomas on 05/12/2017.
 */



public class EventAdapter extends ArrayAdapter<EventActivity> {

    public EventAdapter(Activity context, ArrayList<EventActivity> events){
        super(context,0,events);
    }

    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_preview, parent, false);
        }

        EventActivity currentEvent = getItem(position);

        ImageView image = (ImageView) listItemView.findViewById(R.id.image);
        image.setImageDrawable(currentEvent.getImage());

        TextView title = (TextView) listItemView.findViewById(R.id.title);
        title.setText(currentEvent.getTitle());

        TextView date = (TextView) listItemView.findViewById(R.id.date);
        date.setText(currentEvent.getDate());

        TextView address = (TextView) listItemView.findViewById(R.id.address);
        address.setText(currentEvent.getAddress());

        TextView going=(TextView) listItemView.findViewById(R.id.going);
        going.setText(currentEvent.getGoing());

        TextView interested=(TextView) listItemView.findViewById(R.id.interested);
        interested.setText(currentEvent.getInterested());

        ImageView clock=(ImageView) listItemView.findViewById(R.id.clock);
        clock.setImageResource(R.drawable.ic_alarm);
        clock.setVisibility(View.VISIBLE);

        ImageView location=(ImageView) listItemView.findViewById(R.id.location);
        location.setImageResource(R.drawable.ic_location);
        location.setVisibility(View.VISIBLE);

        ImageView goingImg=(ImageView) listItemView.findViewById(R.id.check);
        goingImg.setImageResource(R.drawable.ic_check_circle);
        goingImg.setVisibility(View.VISIBLE);

        ImageView interestedImg=(ImageView) listItemView.findViewById(R.id.star);
        interestedImg.setImageResource(R.drawable.ic_circle_star);
        interestedImg.setVisibility(View.VISIBLE);

        return listItemView;
    }
}
