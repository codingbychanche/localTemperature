package com.example.bluetoothconnector;
/*
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 *  Last modified 12/18/18 11:16 PM
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;


public class ConnectThread extends Thread {

    // Debug
    private String tag;

    // BT
    private BluetoothSocket mSocket = null;
    private BluetoothDevice mDevice = null;
    private ConnectedThreadReadWriteData connectedThreadReadWriteData;

    // UI
    private TextView connectionStatus;
    private TextView connectionStrength;
    private TextView connectionAnimation;
    private WebView tempAndHumDisplay;
    private Handler h;
    private Context c;

    // For the HC- 05 we have to use this UUID:
    private UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String devName;

    //
    //
    //
    private BTConnectedInterface connectedInterface;


    /**
     * Connect Thread
     * <p>
     * This try's to establish a connection to a BT device. The device must already have been bound.
     * If successful this thread starts the connected thread which receives data.
     *
     * @see berthold.localtemperature.ConnectedThreadReadWriteData
     */
    public ConnectThread(BluetoothDevice mDevice, Context c, TextView connectionStatus, TextView connectionStrength,TextView connectionAnimation, WebView tempAndHumDisplay, Handler h, BTConnectedInterface connectedInterface) {

        this.mDevice = mDevice;
        this.tempAndHumDisplay = tempAndHumDisplay;
        this.connectionStatus = connectionStatus;
        this.connectionStrength=connectionStrength;
        this.connectionAnimation=connectionAnimation;
        this.h = h;
        mSocket = null;
        this.connectedInterface = connectedInterface;
        tag = getClass().getSimpleName();
        Log.v(tag, "MYDEBUG>Constructor: Hole Socket");

        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            Log.v(tag, "MYDEBUG>Constructor: Socket konnte nicht geholt werden. Grund:" + e.toString());
        }

        this.c=c;
    }

    /*
     * Run!
     */

    public void run() {
        connectedThreadReadWriteData = null;
        try {
            connectionStatusOut("\nEs wird versucht eine Verbindung mit dem Gerät herzustellen.......");
            Log.v(tag, "MYDEBUG>run: Starte thread zur Datenübertragung");
            mSocket.connect();

            // This reads incoming data from the conneted device...
            connectedThreadReadWriteData = new ConnectedThreadReadWriteData(mSocket,h,connectionStatus, connectionStrength,connectionAnimation, connectedInterface);
            connectedThreadReadWriteData.start();

        } catch (IOException e) {
            connectedInterface.receiveErrorMessage("\nKonnte Datenverbindung nicht herstellen. Grund:\n"+e.toString());
            if (connectedThreadReadWriteData != null) connectedThreadReadWriteData.cancel();
        }
        return;
    }

    /*
     * Output to connectionStatus
     */

    private void connectionStatusOut(String message) {
        final String m;
        m = message;
        h.post(new Runnable() {
            @Override
            public void run() {
                connectionStatus.append(m);
            }
        });
    }

    /*
     * Cancel
     */

    public void cancel() {
       connectedThreadReadWriteData.cancel();
    }
}