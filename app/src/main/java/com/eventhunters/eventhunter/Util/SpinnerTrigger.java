package com.eventhunters.eventhunter.Util;
import android.content.Context;
import android.util.AttributeSet;

/** Spinner extension that calls onItemSelected even when the selection is the same as its previous value */
public class SpinnerTrigger extends android.support.v7.widget.AppCompatSpinner {

    public SpinnerTrigger(Context context)
    { super(context); }

    public SpinnerTrigger(Context context, AttributeSet attrs)
    { super(context, attrs); }

    public SpinnerTrigger(Context context, AttributeSet attrs, int defStyle)
    { super(context, attrs, defStyle); }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

}
