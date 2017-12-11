package com.example.adam.eventhunter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;

import com.example.adam.eventhunter.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Mio on 11-Dec-17.
 */

public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year,month,day);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(c.getTime());


    }
}
