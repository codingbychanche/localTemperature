package com.example.bluetooththermometer;

/**
 * Connected Thread
 * <p>
 * When a bluetooth connection has been established, this thread
 * receives a data- stream from the device connected and invokes the
 * callback instance {@link berthold.localtemperature.ConnectedInterface}
 * to send the received data to the instance(s) implementing this interface.
 * <p>
 * The connected device is represented by it's socket. Socked is obtained and passed by:
 *
 * @see berthold.localtemperature.ConnectThread
 * <p>
 * Data from the bluetooth device is parsed by:
 * @see berthold.localtemperature.DecodeTemperatureData
 */

/*
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 *  Last modified 12/18/18 11:16 PM
 */

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThreadReadWriteData extends Thread {

    // Debug
    private String tag = getClass().getSimpleName();

    // BT
    private final BluetoothSocket mSocket;
    private final InputStream mIs;
    private final OutputStream mOs;
    private float dataChunksActuallySend = 1;
    private float dataChunksArrived = 1;

    // UI
    private Handler h;
    private TextView connectionStatus;
    private TextView connectionStrength;
    private TextView connectionAnimation;
    private BTConnectedInterface connectedInterface;

    // Just for fun, a nice animation when device is exchanging data
    private int[] beatingHeart = {128150, 128151};
    private int animationPhase;

    /**
     * Connected thread
     *
     * @param socket
     * @param h
     * @param connectionStatus
     */
    public ConnectedThreadReadWriteData(BluetoothSocket socket, Handler h, TextView connectionStatus, TextView connectionStrength, TextView connectionAnimation, BTConnectedInterface connectedInterface) {
        mSocket = socket;
        this.h = h;
        this.connectionStatus = connectionStatus;
        this.connectionStrength = connectionStrength;
        this.connectionAnimation = connectionAnimation;
        this.connectedInterface = connectedInterface;

        InputStream tmpIs = null;
        OutputStream tmpOs = null;

        try {
            tmpIs = mSocket.getInputStream();
            tmpOs = mSocket.getOutputStream();
        } catch (IOException e) {
            connectionStatusOut("Fehler. Datenübertragung zum gekoppelten Gerät nicht möglich.");
        }
        mOs = tmpOs;
        mIs = tmpIs;

        // Icon showing the connection is alive...
        animationPhase = 0;
    }

    /*
     * Read incoming
     */
    public void run() {
        // Read Data
        byte[] inBuffer = new byte[1024];

        connectedInterface.sucess(this);
        int length;

        try {
            while (true) {
                String jsonStringWithTempAndHumData = null;
                int readBytes = 0;
                length = mIs.read(inBuffer);
                while (readBytes != length) {
                    jsonStringWithTempAndHumData = new String(inBuffer, 0, length);
                    readBytes++;
                }

                // Animate to show, connection is alive!
                animate();

                // Wait before next data chunk arrives.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // Animate to show, connection is alive!
                animate();

                // This part is only for the animation.....
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // OK update display
                dataChunksActuallySend++;
                TemperatureData temperatureData;
                temperatureData = DecodeTemperatureData.decodeJson(jsonStringWithTempAndHumData);
                updateTempAndHumidityDisplay(temperatureData);
            }

        } catch (IOException e) {
            connectionStatusOut("Fehler während der Datenübertragung. Verbindung wurde getrennt!");
            connectedInterface.receiveErrorMessage("Fehler" + e.toString());
        }
    }

    /**
     * Send something to the device
     */
    public void send(String dataToSend) {
        try {
            mOs.write(dataToSend.getBytes());
        } catch (IOException e) {
            connectionStatusOut("Konnte nicht senden:" + e.toString());
        }
    }

    /**
     * Update temp and humidity display
     */

    public void updateTempAndHumidityDisplay(final TemperatureData temperatureData) {
        final StringBuilder dataToDisplay = new StringBuilder();
        final float dataRateInPercent = (dataChunksArrived / dataChunksActuallySend) * 100;

        if (!temperatureData.isError()) {

            h.post(new Runnable() {
                @Override
                public void run() {
                    connectionStrength.setText((int) dataRateInPercent + "%");
                    connectionStatusOut(temperatureData.getFirmwareVersion() + "\n" + temperatureData.getHardwareStatus());
                }
            });

            connectedInterface.receiveTempData(temperatureData.getTemperatureDegrees(),
                    temperatureData.getTemperatureFarenheit(),
                    temperatureData.getHumidity(),
                    +temperatureData.getPressure(),
                    +temperatureData.getAltitude(),
                    temperatureData.getVoltageStatus());
            dataChunksArrived++;

        }
    }

    /*
     * Output to connectionStatus
     */
    private void connectionStatusOut(final String message) {
        final String m;
        m = message;
        h.post(new Runnable() {
            @Override
            public void run() {
                connectionStatus.setText(m.toString());
            }
        });
    }

    /*
     * This show a animation in order to give a positive feedback to the user....
     */
    private void animate() {
        h.post(new Runnable() {
            @Override
            public void run() {
                String html = "&#" + beatingHeart[animationPhase];

                Spanned htmlText = Html.fromHtml(html);
                connectionAnimation.setText(htmlText);
                if (animationPhase < 1)
                    animationPhase++;
                else
                    animationPhase = 0;
            }
        });
    }

    /*
     * Cancel..
     */

    public void cancel() {
        try {
            if (mIs != null)
                mIs.close();
            if (mOs != null)
                mOs.close();
            if (mSocket != null)
                mSocket.close();

            //saveToSharedPreferences();

        } catch (IOException e) {
            connectedInterface.receiveErrorMessage("Fehler beim schließen der Verbindung. Grund: " + e.toString());
        } finally {
            this.interrupt();
        }
    }
}
