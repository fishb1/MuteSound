package ru.fishbone.mutesound;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by fishbone on 27.10.2016.
 */

public class ScheduleTest {

    @Test
    public void startTimeTest() throws Throwable {
        Schedule schedule = new Schedule(1);
//        schedule.setStartTime(23, 40);
//        System.out.println(new Date(schedule.startTime().getTimeInMillis()));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, 1);

        System.out.println(new Date(c.getTimeInMillis()));
    }
}
