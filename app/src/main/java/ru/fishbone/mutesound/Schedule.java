package ru.fishbone.mutesound;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to manipulate with schedule
 *
 * Created by fishbone on 27.10.2016.
 */

class Schedule implements Serializable {
    final static int START_TIME = 0; // time type of startTime to index in the times array
    final static int END_TIME = 1; // time type of endTime to index in the times array

    private final static int HOUR = 0; // needs to index in the times array
    private final static int MINUTE = 1; // needs to index in the times array
    private final int id; // to store different schedules into different shared preferences file, see loadState/saveState
    private final char[] days = new char[7]; // array if chars is easy to store into Shared Preferences
    private final int[][] times = new int[2][2]; // keeps start/end hour and minute
    private final boolean[] isTimeSet = new boolean[2]; // is start/end time defined
    private boolean enabled; // is whole schedule enabled

    Schedule(int id) {
        this.id = id;
        System.arraycopy("0111110".toCharArray(), 0, days, 0, 7); // default days of week
    }
    /**
     * Set time. Define particular time.
     *
     * @param type 0/1 - start or end time
     * @param hour hour of day
     * @param minute minute of hour
     */
    void setTime(int type, int hour, int minute) {
        times[type][HOUR] = hour;
        times[type][MINUTE] = minute;
        isTimeSet[type] = true;
    }
    /**
     * Is time set. Is start or end time defined.
     *
     * @param type 0/1 - start or end time
     * @return true if time defined, otherwise false
     */
    boolean isTimeSet(int type) {
        return isTimeSet[type];
    }

    int getHour(int type) {
        return times[type][HOUR];
    }

    int getMinute(int type) {
        return times[type][MINUTE];
    }
    /**
     * Get time. Returns next available date for schedule, considering disabled days
     *
     * @param type 0/1 - start or end time
     * @return corrected start or end time for task
     */
    Calendar getNextTime(int type, Calendar when) {
//        assert(isTimeSet[type]);
        Calendar c = (Calendar) when.clone();
        c.set(Calendar.HOUR_OF_DAY, times[type][HOUR]);
        c.set(Calendar.MINUTE, times[type][MINUTE]);
        c.set(Calendar.SECOND, 0);
        if (c.before(when)) { // If date before current time, we need to schedule at tomorrow
            c.add(Calendar.DATE, 1);
        }
        if (type == END_TIME) {
            return c;
        }
        for (int i = 0; i < 7; i++) { // Check if date is enabled
            if (isDayEnabled(c.get(Calendar.DAY_OF_WEEK))) {
                return c;
            }
            c.add(Calendar.DATE, 1);
        }
        return null; // All days are disabled
    }

    Calendar getNextTime(int type) {
        return getNextTime(type, Calendar.getInstance());
    }

    /**
     * Get previous time. Return previous available date for schedule, considering disabled days
     *
     * @param type 0 or 1 (start or end time)
     * @param when time to search before
     * @return possible previous time before given
     */
    Calendar getPrevTime(int type, Calendar when) {
        Calendar c = (Calendar) when.clone();
        c.set(Calendar.HOUR_OF_DAY, times[type][HOUR]);
        c.set(Calendar.MINUTE, times[type][MINUTE]);
        c.set(Calendar.SECOND, 0);
        if (c.after(when)) { // If date before current time, we need to schedule at tomorrow
            c.add(Calendar.DATE, -1);
        }
        if (type == END_TIME) {
            return c;
        }
        for (int i = 0; i < 7; i++) { // Check if date is enabled
            if (isDayEnabled(c.get(Calendar.DAY_OF_WEEK))) {
                return c;
            }
            c.add(Calendar.DATE, -1);
        }
        return null; // All days are disabled
    }

    Calendar getPrevTime(int type) {
        return getPrevTime(type, Calendar.getInstance());
    }
    /**
     * Set day. Set particular day as enabled or disabled.
     *
     * @param day 1-based day of week (Sun is 1...Sat is 7)
     * @param enabled true/false
     */
    void setDay(int day, boolean enabled) {
        days[day - 1] = enabled ? '1' : '0';
    }
    /**
     * Is day enabled.
     *
     * @param day 1-based day of week
     * @return true/false
     */
    boolean isDayEnabled(int day) {
        return days[day - 1] == '1';
    }

    boolean isDayEnabled(Calendar day) {
        return isDayEnabled(day.get(Calendar.DAY_OF_WEEK));
    }
    /**
     * Set enabled. Enable or disable whole schedule.
     *
     * @param enabled true - enable, false - disable
     */
    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    /**
     * Is enabled. Is whole schedule enabled.
     *
     * @return true if enabled, otherwise false
     */
    boolean isEnabled() {
        return enabled;
    }
    /**
     * Get time difference. Return difference between particular time and
     * current time as a formatted string.
     *
     * @param type 0/1 - start or end time
     * @return difference as a string
     */
    String getTimeDiff(int type, Context context) {
        Calendar targetTime = getNextTime(type);
        Calendar now = Calendar.getInstance();
        if (targetTime == null || targetTime.before(now)) {
            return "-";
        }
        StringBuilder result = new StringBuilder();
        long diff = targetTime.getTimeInMillis() - now.getTimeInMillis();
        // Calc and append to result days
        int d = (int)(diff / TimeUnit.DAYS.toMillis(1));
        result.append(d > 0 ? String.format(context.getString(R.string.diff_days), d) : "");
        diff -= d * TimeUnit.DAYS.toMillis(1);
        // Calc and append to result hours
        int h = (int)(diff / TimeUnit.HOURS.toMillis(1));
        result.append(h > 0 && result.length() > 0 ? " " : "")
                .append(h > 0 ? String.format(context.getString(R.string.diff_hours), h) : "");
        diff -= h * TimeUnit.HOURS.toMillis(1);
        // Calc and append to result minutes
        int m = (int)(diff / TimeUnit.MINUTES.toMillis(1));
        result.append(m > 0 && result.length() > 0 ? " " : "")
                .append(m > 0 ? String.format(context.getString(R.string.diff_minutes), m) : "");
        return (result.length() > 0
                ? result.toString() : context.getString(R.string.less_then_a_minute));
    }

    /**
     * Save state. Save all fields into shared preferences
     *
     * @param context reference to activity for obtain shared preferences
     */
    void saveState(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                Cons.SHARED_PREF_NAME + id,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        // Sound off time
        editor.putInt(Cons.CFG_START_HOUR, times[START_TIME][HOUR]);
        editor.putInt(Cons.CFG_START_MINUTE, times[START_TIME][MINUTE]);
        // Sound on time
        editor.putInt(Cons.CFG_END_HOUR, times[END_TIME][HOUR]);
        editor.putInt(Cons.CFG_END_MINUTE, times[END_TIME][MINUTE]);
        // Is schedule enabled
        editor.putBoolean(Cons.CFG_ENABLED, enabled);
        // Array of days
        editor.putString(Cons.CFG_DAYS, String.valueOf(days));
        editor.apply();
    }

    /**
     * Load state. Loads all fields from shared preferences
     *
     * @param context reference to activity for obtain shared preferences
     */
    void loadState(Context context) {
        Log.i("MuteSound", "context=" + context);
        Log.i("MuteSound", "pref_name=" + Cons.SHARED_PREF_NAME + id);

        SharedPreferences sharedPref = context.getSharedPreferences(
                Cons.SHARED_PREF_NAME + id,
                Context.MODE_PRIVATE);
        int hour;
        int minute;
        // Sound off time
        hour = sharedPref.getInt(Cons.CFG_START_HOUR, -1);
        minute = sharedPref.getInt(Cons.CFG_START_MINUTE, -1);
        if (hour > 0 && minute > 0) {
            times[START_TIME][HOUR] = hour;
            times[START_TIME][MINUTE] = minute;
            isTimeSet[START_TIME] = true;
        }
        // Sound on time
        hour = sharedPref.getInt(Cons.CFG_END_HOUR, -1);
        minute = sharedPref.getInt(Cons.CFG_END_MINUTE, -1);
        if (hour > 0 && minute > 0) {
            times[END_TIME][HOUR] = hour;
            times[END_TIME][MINUTE] = minute;
            isTimeSet[END_TIME] = true;
        }
        // Is schedule enabled
        enabled = sharedPref.getBoolean(Cons.CFG_ENABLED, false);
        // Array of days
        String d = sharedPref.getString(Cons.CFG_DAYS, "");
        if (d.length() == 7) {
            System.arraycopy(d.toCharArray(), 0, days, 0, 7);
        }
    }
}
