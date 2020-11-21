package com.example.locationlibrary;
/**
 * Calculates the time of sunrise or sunset
 * at a given longitude and latititude.<p>
 *
 * Formulas are taken from: https://lexikon.astronomie.info/zeitgleichung/index.html
 *
 * @author Berthold Fritz
 *
 */
public class EnvironmentSunsetSunriseCalc {

    private static final double sunsetDecIn_Minutes=-0.0145; // Dec of sun below local lat for sunset
    private static final double NOON=12;

    /**
     * Convert degrees to radians.
     *
     * @param grad
     * @return Given angle in degrees converted to radians.
     */
    public static double toRad (double grad){
        return grad*Math.PI/180;
    }

    /**
     * Convert radians to degrees.
     *
     * @param rad
     * @return Given angle in radians converted to degrees.
     */
    public static double toGrad (double rad){
        return rad*180/Math.PI;
    }

    /**
     * Time of sunrise at an observers location.
     * This is a convenient way to get a quick result whithout
     * calling all methods step by step.
     *
     * @param lng	Longitude in degrees.
     * @param lat	Latitude in degrees.
     * @param dayOfYear	Day of year (1st of January is 1).
     * @param timeZone	+/- relative to Greenwich Mean time (east is +, west is -)
     * @return Time of sunrise at the given location and timezone.
     */
    public static double getSunriseTimeAtObserversLocationIn_h(double lng,double lat,int dayOfYear,int timeZone){
        double latIn_rad=toRad(lat);
        double longIn_rad=toRad(lng);

        double declinationIn_rad=getSunDeclinationIn_Rad(dayOfYear);
        double deltaTimeIn_h=getDeltaTimeIn_h(declinationIn_rad,latIn_rad);
        double trueSunsetTimeIn_h=getTrueSunriseTimeIn_h(deltaTimeIn_h,dayOfYear);
        return getTimeAtLatitudeAndTimeZoneIn_h(trueSunsetTimeIn_h, longIn_rad, timeZone);
    }

    /**
     * Time of sunset at an observers location.
     * This is a convenient way to get a quick result whithout
     * calling all methods step by step.
     *
     * @param lng	Longitude in degrees.
     * @param lat	Latitude in degrees.
     * @param dayOfYear	Day of year (1st of January is 1).
     * @param timeZone	+/- relative to Greenwich Mean time (east is +, west is -)
     * @return Time of sunset at the given location and timezone.
     */
    public static double getSunsetTimeAtObserversLocationIn_h(double lng,double lat,int dayOfYear,int timeZone){
        double latIn_rad=toRad(lat);
        double longIn_rad=toRad(lng);

        double declinationIn_rad=getSunDeclinationIn_Rad(dayOfYear);
        double deltaTimeIn_h=getDeltaTimeIn_h(declinationIn_rad,latIn_rad);
        double trueSunsetTimeIn_h=getTrueSunsetTimeIn_h(deltaTimeIn_h,dayOfYear);
        return getTimeAtLatitudeAndTimeZoneIn_h(trueSunsetTimeIn_h, longIn_rad, timeZone);
    }

    /**
     * Declination.
     *
     * @param dayOfYear
     * @return Declination of sun in radians at a given day of year (1=1st of january).
     */
    public static  double getSunDeclinationIn_Rad(int dayOfYear){
        return 0.4095*Math.sin(0.016906*(dayOfYear-80.086));
    }

    /**
     * Difference between true local time and avarge local time (time equation).
     *
     * @param dayOfYear
     * @return Time difference in hours.
     */
    public static double timeEquation(int dayOfYear){
        return -0.171*Math.sin(0.0337*dayOfYear+0.465)-0.1299*Math.sin(0.01787*dayOfYear-0.168);
    }

    /**
     * Time difference.
     *
     * @param decIn_Rad
     * @return Time differnece in hours until sun reaches a given latitude from
     * it's zenit.
     */
    public static double getDeltaTimeIn_h(double decIn_Rad,double latitudeIn_rad){
        return 12*Math.acos((Math.sin(sunsetDecIn_Minutes)-Math.sin(latitudeIn_rad)*Math.sin(decIn_Rad))/(Math.cos(0.9163)*Math.cos(decIn_Rad)))/Math.PI;
    }

    /**
     * True Sunrise time.
     *
     * @param deltaTimeIn_h
     * @return Time of sunrise in hours not compensated for average time at a given location
     * (time displayed on an observers watch).
     */
    public static double getTrueSunriseTimeIn_h(double deltaTimeIn_h, int dayOfYear){
        return NOON-deltaTimeIn_h-timeEquation(dayOfYear);
    }

    /**
     * True Sunset time.
     *
     * @param deltaTimeIn_h
     * @return Time of sunset in hours not compensated for average time at a given location
     * (time displayed on an observers watch).
     */
    public static double getTrueSunsetTimeIn_h(double deltaTimeIn_h,int dayOfYear){
        return NOON+deltaTimeIn_h-timeEquation(dayOfYear);
    }

    /**
     * Time at the latitude and timezone of the observer.
     *
     * @param gmtTimeIn_h
     * @param latitude
     * @param timeZoneIn_DeltaToGMTin_h
     * @return Time at a given latitude and timezone.
     */
    public static double getTimeAtLatitudeAndTimeZoneIn_h(double gmtTimeIn_h,double latitude,int timeZoneIn_DeltaToGMTin_h){
        return gmtTimeIn_h-latitude/15+timeZoneIn_DeltaToGMTin_h;

    }
}
