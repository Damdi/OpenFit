package de.damdi.hiittimer;

import de.damdi.hiittimer.AltTimePicker;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {
    private int lastMinute = 0;
    private int lastSecond = 0;
    private AltTimePicker picker = null;
    private static final int SECS_IN_MIN = 60;
    private int maxMinute = 5;

    public static int getMinute(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[0]));
    }

    public static int getSecond(String time) {
        String[] pieces=time.split(":");

        return(Integer.parseInt(pieces[1]));
    }

    public static int getTime(String time) {

        if(time.contains(":")) {
            String[] pieces=time.split(":");

            return((Integer.parseInt(pieces[0]) * SECS_IN_MIN) + Integer.parseInt(pieces[1]));
        }
        else {
            return Integer.parseInt(time);
        }
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        TypedArray attributesArray = ctxt.obtainStyledAttributes(
                attrs, R.styleable.TimePreference);
        String maxMin = attributesArray.getString(R.styleable.TimePreference_maxMinute);
        if (maxMin != null) {
            maxMinute = Integer.parseInt(maxMin);
        }
        attributesArray.recycle();

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        picker = new AltTimePicker(getContext());
        picker.setMaxMinute(maxMinute);
        return(picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentMinute(lastMinute);
        picker.setCurrentSecond(lastSecond);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastMinute = picker.getCurrentMinute();
            lastSecond = picker.getCurrentSecond();

            String time=String.valueOf(lastMinute)+":"+String.valueOf(lastSecond);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            if (defaultValue == null) {
                time=getPersistedString("00:00");
            }
            else {
                time=getPersistedString(defaultValue.toString());
            }
        }
        else {
            time=defaultValue.toString();
        }

        lastMinute=getMinute(time);
        lastSecond=getSecond(time);
    }

    protected void setMaxMinute(int maxMin) {
        picker.setMaxMinute(maxMin);
    }

    protected int getMaxMinute() {
        return picker.getMaxMinute();
    }
}
