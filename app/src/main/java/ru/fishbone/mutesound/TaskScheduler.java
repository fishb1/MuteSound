package ru.fishbone.mutesound;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
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

public class TaskScheduler extends BroadcastReceiver {
    private static final String TAG = "MuteSound";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: action=" + intent.getAction());
        switch (intent.getAction()) {
            case Cons.SOUND_OFF:
                soundOff(context);
                Toast.makeText(context, "Ringer mode sets to silent", Toast.LENGTH_SHORT).show();
                break;
            case Cons.SOUND_ON:
                soundOn(context);
                Toast.makeText(context, "Ringer mode sets to normal", Toast.LENGTH_SHORT).show();
                break;
            case Cons.RINGER_MODE_CHANGED:
//                TaskManager.cancelTask(context, Cons.SOUND_ON); // If user change ringer mode manually, we need to cancel current task if present
                break;
            case Cons.BOOT_COMPLETE:
                // Load settings from storage
                Bundle bundle = Main.loadState(context);
                Calendar startTime = (Calendar) bundle.getSerializable(Cons.BUNDLE_START_TIME);
                Calendar endTime = (Calendar) bundle.getSerializable(Cons.BUNDLE_END_TIME);
                boolean scheduleEnabled = bundle.getBoolean(Cons.BUNDLE_ENABLED);
                if (scheduleEnabled && startTime != null && endTime != null) {
                    createTask(context, startTime, Cons.SOUND_OFF);
                    createTask(context, endTime, Cons.SOUND_ON);
                } else {
                    Log.i(TAG, "onReceive: scheduler disabled or start/end times doesn't defined");
                }
                break;
            default:
                Log.d(TAG, "onReceive: Huh?");
        }
    }
    /**
     * Sound on. Sets the ringer mode to the normal mode.
     *
     * @param context
     */
    static void soundOn(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.i(TAG, "soundOn: old audio mode=" + am.getRingerMode());
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
    /**
     * Sound off. Sets the ringer mode to the silent mode.
     *
     * @param context
     */
    static void soundOff(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.i(TAG, "soundOff: old audio mode=" + am.getRingerMode());
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    static void createTask(Context context, Calendar time, String action) {
        if (time == null) {
            Log.i(TAG, "createTask: end or start are not defined!");
            return;
        }
        // Create new intent and give it end time, to schedule sound on in Broadcast Listener in the future
        Intent intent = new Intent(context, TaskScheduler.class);
        intent.setAction(action);
        // Create pending intent to send in Broadcast Receiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(
                AlarmManager.RTC,
                correctTime(time).getTimeInMillis(),
                TimeUnit.DAYS.toMillis(1),
                pendingIntent);
        Log.i(TAG, "createTask: New task action=" + action + " scheduled at time " + new Date(correctTime(time).getTimeInMillis()));
    }

    static void cancelTask(Context context, String action) {
        Intent intent = new Intent(context, TaskScheduler.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();
        Log.i(TAG, "cancelStartTask: task probably canceled, action=" + action);
    }

    static Calendar correctTime(Calendar calendar) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(calendar.getTimeInMillis());
        c.set(Calendar.SECOND, 0);
        Calendar now = Calendar.getInstance();
        if (c.before(now)) { // If time less than current time, task should be scheduled at tomorrow
            now.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            now.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            now.set(Calendar.SECOND, c.get(Calendar.SECOND));
            now.add(Calendar.DATE, 1);
            c = now;
        }
        return c;
    }
}
