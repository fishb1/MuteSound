package ru.fishbone.mutesound;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * My broadcast receiver. Handles some system events and main action intents to schedule tasks.
 *
 * Created by fishbone on 18.10.2016.
 */

public class TaskManager extends BroadcastReceiver {
    private static final String TAG = "MuteSound";
    private static boolean doNotWatchRingerMode = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: action=" + intent.getAction());
        // Load schedule from storage
        Schedule schedule = new Schedule(Main.SCHEDULE_ID);
        schedule.loadState(context);
        switch (intent.getAction()) {
            case Cons.SOUND_OFF:
                if (schedule.isDayEnabled(Calendar.getInstance())) {
                    changeMode(context, AudioManager.RINGER_MODE_SILENT);
                    createTask(context, schedule.getNextTime(Schedule.END_TIME), Cons.SOUND_ON);
                    doNotWatchRingerMode = true;
                    Toast.makeText(context, context.getString(R.string.msg_sound_set_to_silent),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case Cons.SOUND_ON:
                changeMode(context, AudioManager.RINGER_MODE_NORMAL);
                Toast.makeText(context, context.getString(R.string.msg_sound_set_to_normal),
                        Toast.LENGTH_SHORT).show();
                break;
            case Cons.RINGER_MODE_CHANGED:
                if (doNotWatchRingerMode) {
                    doNotWatchRingerMode = false;
                    Log.i(TAG, "onReceive: ringer mode change skipped");
                } else {
                    TaskManager.cancelTask(context, Cons.SOUND_ON); // If user change ringer mode manually, we need to cancel current task if present
                    Log.i(TAG, "onReceive: ringer mode change handled");
                }
                break;
            case Cons.BOOT_COMPLETE:
                // Restore start task
                if (schedule.isEnabled()) {
                    createTask(context, schedule.getNextTime(Schedule.START_TIME), Cons.SOUND_OFF);
                }
                // Restore end task
                Calendar lastStartTime = schedule.getPrevTime(Schedule.START_TIME);
                if (lastStartTime != null) {
                    Calendar lastEndTime = schedule.getNextTime(Schedule.END_TIME, lastStartTime);
                    Calendar now = Calendar.getInstance();
                    if (now.after(lastStartTime) && now.before(lastEndTime)) { // If reboot between enabled start and end time, register task to restore normal mode
                        createTask(context, lastEndTime, Cons.SOUND_ON);
                    }
                }
                // TODO: If phone shutdown between start and end time, and booted after end time (sound mode is still silent) -- I don't know what to do :)
                break;
            default:
                Log.d(TAG, "onReceive: Huh?");
        }
    }

    /**
     * Change mode. Sets the ringer mode to specified
     *
     * @param context main activity, need to obtain AudioManager
     */
    static void changeMode(Context context, int mode) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.i(TAG, "changeMode: current mode=" + am.getRingerMode() + ", change to mode=" + mode);
        am.setRingerMode(mode);
    }

    static void createTask(Context context, Calendar time, String action) {
        if (time == null) {
            return;
        }
        // Create new intent and give it end time, to schedule sound on in Broadcast Listener in the future
        Intent intent = new Intent(context, TaskManager.class);
        intent.setAction(action);
        // Create pending intent to send in Broadcast Receiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(
                AlarmManager.RTC,
                time.getTimeInMillis(),
                TimeUnit.DAYS.toMillis(1),
                pendingIntent);
        Log.i(TAG, "createTask: New task action=" + action + " scheduled at time " + new Date(time.getTimeInMillis()));
    }

    static void cancelTask(Context context, String action) {
        Intent intent = new Intent(context, TaskManager.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();
        Log.i(TAG, "cancelTask: task probably canceled, action=" + action);
    }
}