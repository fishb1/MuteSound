package ru.fishbone.mutesound;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.Locale;

/**
 * Main Activity.
 */
public class Main extends Activity {

    private final static String TAG = "MuteSound";
    private Calendar startTime; // When we should do sound off
    private Calendar endTime; // When we should do sound on
    private Button startTimeBtn;
    private Button endTimeBtn;
    private ToggleButton toggleBtn;
    private boolean scheduleEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Main activity created");
        setContentView(R.layout.activity_main);
        startTimeBtn = (Button) findViewById(R.id.timeStartBtn);
        endTimeBtn = (Button) findViewById(R.id.timeEndBtn);
        toggleBtn = (ToggleButton) findViewById(R.id.toggleButton);
        if (savedInstanceState == null) { // If it's first start or user close app before, we need to load values from shared pref.
            savedInstanceState = loadState(this);
        }
        startTime = (Calendar) savedInstanceState.getSerializable(Cons.BUNDLE_START_TIME);
        endTime = (Calendar) savedInstanceState.getSerializable(Cons.BUNDLE_END_TIME);
        scheduleEnabled = savedInstanceState.getBoolean(Cons.BUNDLE_ENABLED);
        updateViews();
    }
    /**
     * Edit the one of two times. OnClick handler for buttons.
     *
     * @param view
     */
    public void editTime(View view) {
        Calendar c = null;
        switch (view.getId()) {
            case R.id.timeStartBtn:
                c = startTime;
                break;
            case R.id.timeEndBtn:
                c = endTime;
                break;
            default:
                Log.i(TAG, "editTime: Huh?");
        }
        if (c == null) {
            c = Calendar.getInstance();
        }
        DialogFragment newFragment = TimePickerFragment.newInstance(view.getId(),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        newFragment.show(getFragmentManager(), "edit_time");
    }
    public void changeMode(View view) {
        scheduleEnabled = toggleBtn.isChecked();
        if (scheduleEnabled) {
            Log.i(TAG, "changeMode: enabled");
            if (startTime == null || endTime == null) {
                Toast.makeText(this, "Установите время", Toast.LENGTH_SHORT).show();
                return;
            }
            long totalTime = TaskScheduler.correctTime(startTime).getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
            Toast.makeText(this, "Звук будет отключен через " + totalTime + " мс", Toast.LENGTH_SHORT).show();
            TaskScheduler.createTask(this, startTime, Cons.SOUND_OFF);
            TaskScheduler.createTask(this, endTime, Cons.SOUND_ON);
        } else {
            Log.i(TAG, "changeMode: disabled");
            TaskScheduler.cancelTask(this, Cons.SOUND_OFF);
            TaskScheduler.cancelTask(this, Cons.SOUND_ON);
            Toast.makeText(this, "Отключение звука отменено", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Set up time. Calls from TimePickerDialog when user has finished to edit the time
     *
     * @param id
     * @param hours
     * @param minutes
     */
    public void setUpTime(int id, int hours, int minutes) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        switch (id) {
            case R.id.timeStartBtn:
                startTime = c;
                break;
            case R.id.timeEndBtn:
                endTime = c;
                break;
            default:
                Log.i(TAG, "setUpTime: Huh?");
        }
        updateViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Main activity paused");
        // If user close app we need to save our values into the storage
        Bundle bundle = new Bundle();
        bundle.putSerializable(Cons.BUNDLE_START_TIME, startTime);
        bundle.putSerializable(Cons.BUNDLE_END_TIME, endTime);
        bundle.putBoolean(Cons.BUNDLE_ENABLED, scheduleEnabled);
        saveState(this, bundle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Place our fields into the Bundle, to get it in onCreate in future
        outState.putSerializable(Cons.BUNDLE_START_TIME, startTime);
        outState.putSerializable(Cons.BUNDLE_END_TIME, endTime);
        outState.putBoolean(Cons.BUNDLE_ENABLED, scheduleEnabled);
    }
    /**
     * Update views. Updates caption on the buttons when time was edited.
     */
    private void updateViews() {
        if (startTime != null) {
            startTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE)));
        }
        if (endTime != null) {
            endTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE)));
        }
        toggleBtn.setChecked(scheduleEnabled);
    }
    /**
     * Save state. Saves all values to the shared pref.
     *
     * @param context
     * @param bundle
     */
    public static void saveState(Context context, Bundle bundle) {
        SharedPreferences sharedPref = context.getSharedPreferences(Cons.SHARED_PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Calendar startTime = (Calendar) bundle.getSerializable(Cons.BUNDLE_START_TIME);
        if (startTime != null) {
            editor.putLong(Cons.CFG_START_TIME, startTime.getTimeInMillis());
        }
        Calendar endTime = (Calendar) bundle.getSerializable(Cons.BUNDLE_END_TIME);
        if (endTime != null) {
            editor.putLong(Cons.CFG_END_TIME, endTime.getTimeInMillis());
        }
        editor.putBoolean(Cons.CFG_ENABLED, bundle.getBoolean(Cons.BUNDLE_ENABLED));
        editor.apply();
    }
    /**
     * Load state. Reads all need data from shared pref into the Bundle and return it.
     *
     * @param context
     * @return
     */
    public static Bundle loadState(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Cons.SHARED_PREF_NAME,
                Context.MODE_PRIVATE);
        Bundle bundle = new Bundle();
        long time;
        time = sharedPref.getLong(Cons.CFG_START_TIME, -1);
        if (time > 0) {
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(time);
            bundle.putSerializable(Cons.BUNDLE_START_TIME, startTime);
        }
        time = sharedPref.getLong(Cons.CFG_END_TIME, -1);
        if (time > 0) {
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(time);
            bundle.putSerializable(Cons.BUNDLE_END_TIME, endTime);
        }
        bundle.putBoolean(Cons.BUNDLE_ENABLED, sharedPref.getBoolean(Cons.CFG_ENABLED, false));
        return bundle;
    }

}
