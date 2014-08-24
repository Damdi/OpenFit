package de.damdi.hiittimer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class AltTimePicker extends FrameLayout {
    public class TimeFormatter implements NumberPicker.Formatter {
        public String format(int value) {
            return String.format("%02d", value);
        }
    }

    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(AltTimePicker view, int minute, int second) {
        }
    };

    private final NumberPicker mMinuteSpinner;
    private final NumberPicker mSecondSpinner;
    private final TextView mDivider;
    private final TimeFormatter timeFormatter;


    private OnTimeChangedListener mOnTimeChangedListener;
    private Calendar mTempCalendar;
    private Locale mCurrentLocale;

    private static final boolean DEFAULT_ENABLED_STATE = true;
    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    public interface OnTimeChangedListener {

        void onTimeChanged(AltTimePicker view, int minute, int second);
    }

    public AltTimePicker(Context context) {
        this(context, null);
    }

    public AltTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.AltTimePicker);
    }

    public AltTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        // process style attributes

        TypedArray attributesArray = context.obtainStyledAttributes(
                attrs, R.styleable.AltTimePicker, defStyle, 0);
/*
        int layoutResourceId = attributesArray.getResourceId(
                R.style.AltTimePicker, R.layout.alt_time_picker);*/
        attributesArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.alt_time_picker, this, true);

        timeFormatter = new TimeFormatter();

        // hour
        mMinuteSpinner = (NumberPicker) findViewById(R.id.minute);
        mMinuteSpinner.setMinValue(0);
//        mMinuteSpinner.setMaxValue(5);
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setFormatter(timeFormatter);
        mMinuteSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
//                updateInputState();
                onTimeChanged();
            }
        });
   //     mMinuteSpinnerInput = (EditText) mMinuteSpinner.findViewById(R.id.numberpicker_input);
   //     mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // divider (only for the new widget style)
        mDivider = (TextView) findViewById(R.id.divider);
        if (mDivider != null) {
            mDivider.setText(R.string.alt_time_picker_separator);
        }

        // minute
        mSecondSpinner = (NumberPicker) findViewById(R.id.second);
        mSecondSpinner.setMinValue(0);
        mSecondSpinner.setMaxValue(59);
        mSecondSpinner.setOnLongPressUpdateInterval(100);
        mSecondSpinner.setFormatter(timeFormatter);
        mSecondSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
//                updateInputState();
                int minValue = mSecondSpinner.getMinValue();
                int maxValue = mSecondSpinner.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mMinuteSpinner.getValue() + 1;
                    mMinuteSpinner.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mMinuteSpinner.getValue() - 1;
                    mMinuteSpinner.setValue(newHour);
                }
                onTimeChanged();
            }
        });
//        mSecondSpinnerInput = (EditText) mSecondSpinner.findViewById(R.id.numberpicker_input);
//        mSecondSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);

        // set to current time
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));
        setCurrentSecond(mTempCalendar.get(Calendar.SECOND));

        if (!isEnabled()) {
            setEnabled(false);
        }

        // set the content descriptions
        //setContentDescriptions();
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mSecondSpinner.setEnabled(enabled);
        if (mDivider != null) {
            mDivider.setEnabled(enabled);
        }
        mMinuteSpinner.setEnabled(enabled);
        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    public Integer getCurrentMinute() {
        return mMinuteSpinner.getValue();
    }

    public Integer getCurrentSecond() {
        return mSecondSpinner.getValue();
    }

    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == null || currentMinute == getCurrentMinute()) {
            return;
        }

        mMinuteSpinner.setValue(currentMinute);
        onTimeChanged();
    }

    public void setCurrentSecond(Integer currentSecond) {
        if (currentSecond == null || currentSecond == getCurrentSecond()) {
            return;
        }

        mMinuteSpinner.setValue(currentSecond);
        onTimeChanged();
    }

    public int getMaxMinute() {
        return mMinuteSpinner.getMaxValue();
    }

    public void setMaxMinute(int maxMin) {
        mMinuteSpinner.setMaxValue(maxMin);
    }

    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        mTempCalendar = Calendar.getInstance(locale);
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentMinute(), getCurrentSecond());
        }
    }

    @Override
    public int getBaseline() {
        return mMinuteSpinner.getBaseline();
    }
/*
    private void updateInputState() {
        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mMinuteSpinnerInput)) {
                mMinuteSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mSecondSpinnerInput)) {
                mSecondSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }
*/

    private static class SavedState extends BaseSavedState {

        private final int mMinute;
        private final int mSecond;

        private SavedState(Parcelable superState, int minute, int second) {
            super(superState);

            mMinute = minute;
            mSecond = second;
        }

        private SavedState(Parcel in) {
            super(in);

            mMinute = in.readInt();
            mSecond = in.readInt();
        }

        public int getMinute() {
            return mMinute;
        }


        public int getSecond() {
            return mSecond;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mMinute);
            dest.writeInt(mSecond);
        }

        @SuppressWarnings({"unused", "hiding"})
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentMinute(), getCurrentSecond());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentMinute(ss.getMinute());
        setCurrentSecond(ss.getSecond());
    }
}


