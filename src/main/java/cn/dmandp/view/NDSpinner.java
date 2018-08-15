package cn.dmandp.view;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;

public class NDSpinner extends AppCompatSpinner {
    public NDSpinner(Context context) {
        super(context);
    }

    public NDSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NDSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
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
