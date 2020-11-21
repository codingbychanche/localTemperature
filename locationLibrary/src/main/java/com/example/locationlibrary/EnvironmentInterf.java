package com.example.locationlibrary;

public interface EnvironmentInterf {
    void getEnviromentalData(EnvironmentAddress address, float height, float speed,float avrSpeedIn_kmH, float distanceTraveled, Double longtitude, Double latitude);
    void getStatusData(Long timestampIn_ms, float lastUpdateIn_s, String statusMessage);
}
