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
    String CFG_START_TIME = "start_time";
    String CFG_END_TIME = "end_time";
    String CFG_ENABLED = "enabled";
    String SHARED_PREF_NAME = "default";
    String BUNDLE_START_TIME = "startTime";
    String BUNDLE_END_TIME = "endTime";
    String BUNDLE_ENABLED = "scheduleEnabled";
}
