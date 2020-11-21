package com.example.locationlibrary;
/**
 * A collection of utilities for formating time and date.
 *
 * @author Berthold
 */

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EnvironmentSunriseSunsetTimeDateCalc {

    /**
     * Hours and minutes.
     *
     * @param timeIn_h
     * @return A String containing the hours and minutes from a double
     * precission value prividing the time in decimal format.
     */
    public static String getTimeInTwentyFourHourFormat(double timeIn_h) {
        int hours = (int) timeIn_h;
        int minutes = (int) (timeIn_h % 1 * 60);

        return hours + ":" + minutes;
    }

    /**
     * Day of current year
     *
     * @return Day of the current year for the current date. 1st of January=1.
     */
    public static int getDayOfCurrentYear() {
        Calendar cal = Calendar.getInstance();

        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        // January=0, december=11
        int month = cal.get(Calendar.MONTH) + 1;

        return month * 30 + dayOfMonth;
    }

    /**
     * Current time zone.
     * @return E.g. "Central Europeean Time".
     */
    public static String getCurrentTimezoneName(){
        TimeZone timeZone=TimeZone.getDefault();
        return timeZone.getDisplayName();
    }

    /**
     * Current time zone.
     * @return Region/ City. E.g. "Europe/ Berlin"
     */
    public static String getCurrentTimezoneOffset(){
        TimeZone timeZone=TimeZone.getDefault();
        return timeZone.getID();
    }

    /**
     * Offset in hours relative to GMT (Greenwich mean time).
     *
     * @return Offset in hours.
     */
    public static int getTimeZoneOffsetIn_h(){
        TimeZone timeZone=TimeZone.getDefault();
        return timeZone.getOffset(new Date().getTime()) /1000/60/60 	;
    }

}

