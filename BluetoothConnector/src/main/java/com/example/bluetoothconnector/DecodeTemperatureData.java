package com.example.bluetoothconnector;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Berthold on 1/10/19.
 *
 * Bluetooth ic_launcher sensor encodes data into a json structure.
 * This class decodes this data and returns it's contents.
 * If json could not be decoded, it returns an error.
 *
 * Results are written to:
 * @see TemperatureData
 *
 */

public class DecodeTemperatureData {

    public static TemperatureData decodeJson(String jsonRecieievedDataFromBluetoothDevice)
    {
        String tag=null;
        TemperatureData temperatureData=new TemperatureData();

        try {
            JSONObject jo = new JSONObject(jsonRecieievedDataFromBluetoothDevice);
            if (jo.has("firmware_version")) temperatureData.setFirmwareVersion(jo.getString("firmware_version"));
            if (jo.has("hardware_status")) temperatureData.setHardwareStatus(jo.getString("hardware_status"));
            if (jo.has("voltage_status")) temperatureData.setVoltageStatus(jo.getString("voltage_status"));
            if (jo.has("temperature_degrees")) temperatureData.setTemperatureDegrees(jo.getDouble("temperature_degrees"));
            if (jo.has("temperature_farenheit")) temperatureData.setTemperatureFarenheit(jo.getDouble("temperature_farenheit"));
            if (jo.has("huminidity")) temperatureData.setHumidity(jo.getDouble("huminidity"));
            if (jo.has("pressure")) temperatureData.setPressure(jo.getDouble("pressure"));
            if (jo.has("altitude")) temperatureData.setAltitude(jo.getDouble("altitude"));
            Log.v("CONVERTED","Temp:"+jo.getDouble("temperature_degrees"));
        }catch (JSONException e) {
            Log.v(tag,e.toString());
            Log.v("JSON::",e.toString());
            temperatureData.setError(true);
        }
        return temperatureData;
    }
}
