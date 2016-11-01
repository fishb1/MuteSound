package ru.fishbone.mutesound;

import android.app.Dialog;
import android.app.DialogFragment;
import android.support.annotation.Nullable;
import android.app.TimePickerDialog;
import android.os.Bundle;

import android.text.format.DateFormat;
import android.widget.TimePicker;


/**
 * Time Picker Dialog
 *
 * Created by fishbone on 06.10.2016.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private int viewId;
    private int hour;
    private int minute;


    public static TimePickerFragment newInstance(int viewId, int hour, int minute) {
        TimePickerFragment d = new TimePickerFragment();
        Bundle b = new Bundle();
        b.putInt("viewId", viewId);
        b.putInt("hour", hour);
        b.putInt("minute", minute);
        d.setArguments(b);
        return d;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewId = getArguments().getInt("viewId");
        hour = getArguments().getInt("hour");
        minute = getArguments().getInt("minute");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker picker, int hour, int minute) {
        Main activity = (Main) getActivity();
        activity.setUpTime(viewId, hour, minute);
    }
}


