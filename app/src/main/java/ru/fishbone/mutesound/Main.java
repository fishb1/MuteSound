package ru.fishbone.mutesound;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;

/**
 * Main Activity.
 */
public class Main extends Activity {

    private final static String TAG = "MuteSound";
    private Button startTimeBtn;
    private Button endTimeBtn;
    private ToggleButton toggleBtn;
    private SparseArray<CheckBox> daysCheckBoxes = new SparseArray<>();
    protected Schedule schedule; // Encapsulate form data
    final static int SCHEDULE_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: main activity");
        setContentView(R.layout.activity_main);
        // Init some useful fields
        startTimeBtn = (Button) findViewById(R.id.timeStartBtn);
        endTimeBtn = (Button) findViewById(R.id.timeEndBtn);
        toggleBtn = (ToggleButton) findViewById(R.id.toggleButton);
        daysCheckBoxes.put(1, (CheckBox) findViewById(R.id.day1));
        daysCheckBoxes.put(2, (CheckBox) findViewById(R.id.day2));
        daysCheckBoxes.put(3, (CheckBox) findViewById(R.id.day3));
        daysCheckBoxes.put(4, (CheckBox) findViewById(R.id.day4));
        daysCheckBoxes.put(5, (CheckBox) findViewById(R.id.day5));
        daysCheckBoxes.put(6, (CheckBox) findViewById(R.id.day6));
        daysCheckBoxes.put(7, (CheckBox) findViewById(R.id.day7));
        // Try to restore state and fill values in the views
        if (savedInstanceState != null) { // If it's first start or user close app before, we need to load values from shared pref.
            schedule = (Schedule) savedInstanceState.getSerializable(Cons.SCHEDULE);
        } else {
            schedule = new Schedule(SCHEDULE_ID);
            schedule.loadState(this);
        }
        updateViews();
    }
    /**
     * Edit time. OnClick handler for buttons.
     *
     * @param view clicked button
     */
    public void editTime(View view) {
        int timeType = 0;
        switch (view.getId()) {
            case R.id.timeStartBtn:
                timeType = Schedule.START_TIME;
                break;
            case R.id.timeEndBtn:
                timeType = Schedule.END_TIME;
                break;
            default:
                Log.i(TAG, "editTime: Huh?");
        }
        DialogFragment newFragment = TimePickerFragment.newInstance(
                view.getId(),
                schedule.getHour(timeType),
                schedule.getMinute(timeType)
        );
        newFragment.show(getFragmentManager(), "edit_time");
    }

    public void changeMode(View view) {
        schedule.setEnabled(toggleBtn.isChecked());
        if (schedule.isEnabled()) {
            Log.i(TAG, "changeMode: enabled");
            if (!schedule.isTimeSet(Schedule.START_TIME) || !schedule.isTimeSet(Schedule.END_TIME)) {
                Toast.makeText(this, getString(R.string.msg_time_not_defined), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, String.format(getString(R.string.msg_remain_time),
                    schedule.getTimeDiff(Schedule.START_TIME, this)), Toast.LENGTH_SHORT).show();

            TaskManager.createTask(this, schedule.getNextTime(Schedule.START_TIME), Cons.SOUND_OFF);
        } else {
            Log.i(TAG, "changeMode: disabled");
            TaskManager.cancelTask(this, Cons.SOUND_OFF);
            TaskManager.cancelTask(this, Cons.SOUND_ON);
            Toast.makeText(this, getString(R.string.msg_cancel_task), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Set up time. Calls from TimePickerDialog when user has finished to edit the time
     *
     * @param id is id of clicked button
     * @param hours selected hour
     * @param minutes selected minute
     */
    public void setUpTime(int id, int hours, int minutes) {
        switch (id) {
            case R.id.timeStartBtn:
                schedule.setTime(Schedule.START_TIME, hours, minutes);
                break;
            case R.id.timeEndBtn:
                schedule.setTime(Schedule.END_TIME, hours, minutes);
                break;
            default:
                Log.i(TAG, "setUpTime: Huh?");
        }
        updateViews();
    }
    /**
     * Check day. Calls when one of checkboxes was selected
     *
     * @param view selected checkbox
     */
    public void checkDay(View view) {
        for (int i = 0; i < daysCheckBoxes.size(); i++) {
            int key = daysCheckBoxes.keyAt(i);
            if (daysCheckBoxes.get(key) == view) {
                schedule.setDay(key, daysCheckBoxes.get(key).isChecked());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Main activity paused");
        // If user close app we need to save schedule into the Shared Pref.
        if (schedule != null) {
            schedule.saveState(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Place schedule into the Bundle, to get it back in onCreate in future
        outState.putSerializable(Cons.SCHEDULE, schedule);
    }
    /**
     * Update views. Updates caption on the buttons when time was edited.
     */
    private void updateViews() {
        if (schedule.isTimeSet(Schedule.START_TIME)) {
            startTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    schedule.getHour(Schedule.START_TIME),
                    schedule.getMinute(Schedule.START_TIME)));
        }
        if (schedule.isTimeSet(Schedule.END_TIME)) {
            endTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    schedule.getHour(Schedule.END_TIME),
                    schedule.getMinute(Schedule.END_TIME)));
        }
        toggleBtn.setChecked(schedule.isEnabled());
        for (int i = 0; i < daysCheckBoxes.size(); i++) {
            int key = daysCheckBoxes.keyAt(i);
            daysCheckBoxes.get(key).setChecked(schedule.isDayEnabled(key));
        }
    }
}
