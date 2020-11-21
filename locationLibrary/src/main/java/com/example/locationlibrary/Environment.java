package com.example.locationlibrary;
/***
 * Simplifies the usage of the geocoder class and provides
 * some utility methods.
 *
 * @Author Berthold Fritz
 *
 */

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import berthold.trendAnalyzer.DataToAnalyze;


public class Environment extends Thread {

    //
    Handler handler;

    // Geocoder
    private Geocoder geocoder;
    private List<Address> addresses;
    private LocationListener locationListener=null;

    // GPS
    private LocationManager locationManager;
    private String provider;

    // Calc
    private Location lastLoc;
    private double totalDistanceTraveledIn_m, speedIn_ms, speedIn_kmH, heightIn_m;
    private DataToAnalyze speedData;

    // Timing
    private long nowIn_ms, lastUpdateIn_ms, lastUpdateIn_SEC;

    //
    EnvironmentInterf receiver;

    private final static float MIN_SPEED_IN_KMH_FOR_DISTANCE_CALC = 5;

    /**
     * The sole constructor.
     *
     * @param receiver
     * @param locationManager
     * @param geocoder
     */
    public Environment(EnvironmentInterf receiver, LocationManager locationManager, Geocoder geocoder) {
        this.receiver = receiver;
        this.locationManager = locationManager;
        this.geocoder = geocoder;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, true);

        speedData = new DataToAnalyze(100);

        handler = new Handler();

        Log.v("LOCATIONLOCATION","Created");
    }

    /**
     * Updates position data, current speed and distance traveled...
     */
    public void run() {
        // Debug
        final String tag = this.getClass().getSimpleName();

        // Location listener
        locationListener = new LocationListener() {


            @Override
            public void onProviderEnabled(String provider) {
                Log.v(tag, "Enabeled. Provider:" + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.v(tag, "Disabeled. Provider:" + provider);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.v(tag, "Updated " + provider);
            }

            @Override
            public void onLocationChanged(Location currentLocation) {

                String location = "-";
                EnvironmentAddress environmentAddress;

                // calc time between position updates
                nowIn_ms = System.currentTimeMillis();
                Long waitTime = nowIn_ms - lastUpdateIn_ms;
                lastUpdateIn_SEC = waitTime / 1000;

                // Get latitude, longitude, name and address of location....
                if (currentLocation != null) {

                    // Speed
                    speedIn_ms = currentLocation.getSpeed();
                    speedIn_kmH = (speedIn_ms / 1000) * 3600;
                    speedData.add(speedIn_kmH);
                    double avrSpeedIn_kmH = speedData.getAverageSlope();

                    // Distance traveled since last location update
                    if (lastLoc != null && avrSpeedIn_kmH > MIN_SPEED_IN_KMH_FOR_DISTANCE_CALC)
                        totalDistanceTraveledIn_m = totalDistanceTraveledIn_m + currentLocation.distanceTo(lastLoc);

                    try {
                        addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);

                        if (addresses.size() > 0) {
                            String addressLine1 = addresses.get(0).getAddressLine(0);
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String postalCode = addresses.get(0).getPostalCode();
                            environmentAddress = new EnvironmentAddress(addressLine1, city, state, postalCode);
                        } else
                            environmentAddress = new EnvironmentAddress("-", "-", "-", "-");

                        // Height
                        heightIn_m = (float) currentLocation.getAltitude();

                        // Save current location for distance
                        lastLoc = new Location("");
                        lastLoc.setLongitude(currentLocation.getLongitude());
                        lastLoc.setLatitude(currentLocation.getLatitude());

                        // Save current time for the calculation of the update frequency.
                        lastUpdateIn_ms = nowIn_ms;

                        receiver.getEnviromentalData(environmentAddress, (float) heightIn_m, (float) speedIn_kmH, (float)avrSpeedIn_kmH,(float) totalDistanceTraveledIn_m, currentLocation.getLongitude(), currentLocation.getLatitude());
                        receiver.getStatusData(nowIn_ms, lastUpdateIn_SEC, "OK");

                    } catch (IOException e) {
                        Log.v("ERROR", e.toString());
                        receiver.getStatusData(nowIn_ms, lastUpdateIn_SEC, e.toString());
                    }
                }
            }
        };

        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    locationManager.requestLocationUpdates(provider, 3000, 0, locationListener);
                }
            });
        } catch (SecurityException e) {
            receiver.getStatusData(nowIn_ms, lastUpdateIn_SEC, e.toString());
        }

        // Give the UI thread a little bit time....
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

    }

    /**
     * Reset distance traveled to zero...
     */
    public void resetDiestanceTraveled() {
        totalDistanceTraveledIn_m = 0;
    }

    /**
     * Stop location updates
     */
    public void cancel(){
        locationManager.removeUpdates(locationListener);
    }
}



