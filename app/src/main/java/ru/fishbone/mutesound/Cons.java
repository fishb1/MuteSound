package ru.fishbone.mutesound;

/**
 * Some constants live here.
 *
 * Created by fishbone on 19.10.2016.
 */

interface Cons {
    String SOUND_ON = "ru.fishbone.mutesound.SOUND_ON";
    String SOUND_OFF = "ru.fishbone.mutesound.SOUND_OFF";
    String RINGER_MODE_CHANGED = "android.media.RINGER_MODE_CHANGED";
    String BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
    String CFG_START_HOUR = "start_hour";
    String CFG_START_MINUTE = "start_minute";
    String CFG_END_HOUR = "end_hour";
    String CFG_END_MINUTE = "end_minute";
    String CFG_ENABLED = "enabled";
    String CFG_DAYS = "days";
    String SHARED_PREF_NAME = "default";
    String SCHEDULE = "schedule";
}
