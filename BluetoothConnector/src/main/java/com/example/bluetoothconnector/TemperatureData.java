package com.example.bluetoothconnector;

/**
 * Created by Berthold on 1/10/19.
 * <p>
 * Holds data send from bluetooth ic_launcher sensor.
 */

/*
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 *  Last modified 12/18/18 11:16 PM
 */

public class TemperatureData {
    private String firmwareVersion;
    private String hardwareStatus;

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getHardwareStatus() {
        return hardwareStatus;
    }

    public void setHardwareStatus(String hardwareStatus) {
        this.hardwareStatus = hardwareStatus;
    }

    private String voltageStatus;
    private double temperatureDegrees;
    private double temperatureFarenheit;
    private double humidity;
    private double pressure;
    private double altitude;
    private boolean error = false;

    public void setVoltageStatus(String voltageStatus){
        this.voltageStatus=voltageStatus;
    }

    public String getVoltageStatus(){
        return voltageStatus;

    }
    public void setTemperatureDegrees(double temperatureDegrees) {
        this.temperatureDegrees = temperatureDegrees;
    }

    public double getTemperatureDegrees() {
        if (error) temperatureDegrees = 999;
        return temperatureDegrees;
    }

    public void setTemperatureFarenheit(double temperatureFarenheit) {
        this.temperatureFarenheit = temperatureFarenheit;
    }

    public double getTemperatureFarenheit() {
        if (error) temperatureFarenheit = 999;
        return temperatureFarenheit;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getHumidity() {
        if (error) humidity = 999;
        return humidity;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getPressure() {
        return pressure;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isError() {
        return error;
    }
}

