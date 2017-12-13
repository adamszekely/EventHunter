package com.eventhunters.eventhunter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;



import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Mio on 11-Dec-17.
 */

public class DatePickerDialogFragment extends DatePickerDialog {

    private CharSequence title;

    public DatePickerDialogFragment(@NonNull Context context, int style, @Nullable OnDateSetListener listener, int year, int month, int dayOfMonth) {
        super(context, style,listener, year, month, dayOfMonth);
    }

    public DatePickerDialogFragment(@NonNull Context context, @Nullable OnDateSetListener listener, int year, int month, int dayOfMonth) {
        super(context, listener, year, month, dayOfMonth);
    }

    public void setPermanentTitle(CharSequence title) {
        this.title = title;
        setTitle(title);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        setTitle(title);
    }
}
