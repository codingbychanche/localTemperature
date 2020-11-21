package com.example.bluetooththermometer;

/**
 * This interfaces with the instance created it....
 */

/*
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 *  Last modified 12/18/18 11:16 PM
 */

public interface BTConnectedInterface {
    void sucess (ConnectedThreadReadWriteData connectedThreadReadWriteData);
    public void receiveTempData(double tempMainUnit,double tempAltUnit,double humidity,double pressure, double alt,String batStatus);
    void receiveErrorMessage(String error);

}
