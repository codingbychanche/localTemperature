package com.example.locationlibrary;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A collection of utilities to format time and date.
 * 
 * @author Berthold
 *
 */
public class EnvironmentSunriseSunsetTimeDateCalc {

	/**
	 * Hours and minutes.
	 * 
	 * @param A double precision value representing the time in hours and minutes.
	 * @return A String containing the hours and minutes from a double precission
	 *         value providing the time in decimal format.
	 */
	public static String getTimeInTwentyFourHourFormat(double timeIn_h) {
		int hours = (int) timeIn_h;
		int minutes = (int) (timeIn_h % 1 * 60);

		return String.format("%02d", hours) + ":" + String.format("%02d", minutes);
	}

	/**
	 * Day of current year
	 * 
	 * @return Day of the current year for the current date. 1st of January=1.
	 */
	public static int getDayOfCurrentYear() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Current time zone short format.
	 * 
	 * @return E.g. "Central Europeean Time".
	 */
	public static String getCurrentTimezoneName() {
		TimeZone timeZone = TimeZone.getDefault();
		return timeZone.getDisplayName();
	}

	/**
	 * Current time zone long format with city.
	 * 
	 * @return Region/ City. E.g. "Europe/ Berlin"
	 */
	public static String getCurrentTimezoneOffset() {
		TimeZone timeZone = TimeZone.getDefault();
		return timeZone.getID();
	}

	/**
	 * Offset in hours relative to GMT (Greenwich mean time).
	 * 
	 * @return Offset in hours.
	 */
	public static int getTimeZoneOffsetIn_h() {
		TimeZone timeZone = TimeZone.getDefault();
		return timeZone.getOffset(new Date().getTime()) / 1000 / 60 / 60;
	}

	/**
	 * @return Current time in decimal format.
	 */
	public static double getCurrentTimeInDec() {
		Calendar cal = Calendar.getInstance();
		double h = cal.get(Calendar.HOUR_OF_DAY);
		double m = cal.get(Calendar.MINUTE);
		return (h + (m / 60));

	}
}
