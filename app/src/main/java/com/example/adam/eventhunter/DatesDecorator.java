package com.example.adam.eventhunter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.List;

/**
 * Created by Adam on 11/12/2017.
 */

public class DatesDecorator implements DayViewDecorator {

    private List<CalendarDay> dates;
    private Context mContext;

    public DatesDecorator(List<CalendarDay> dates,Context context)
    {
        this.dates=dates;
        mContext = context;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        Log.d("Coloreddates",day+"");
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(
                ContextCompat.getColor(mContext, R.color.colorAccent)));
    }
}
