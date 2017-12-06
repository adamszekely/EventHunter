package com.example.adam.eventhunter.model;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.adam.eventhunter.R;

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

        TextView time = (TextView) listItemView.findViewById(R.id.time);
        time.setText(currentEvent.getTime());

        TextView address = (TextView) listItemView.findViewById(R.id.address);
        address.setText(currentEvent.getAddress());


        return listItemView;
    }
}
