package com.example.bluetooththermometer;

/**
 * Local ic_launcher.
 * <p>
 * Build for use with a DIY- ic_launcher/ Humidity Bluetooth Sensor.
 * Connects to HC- 05 Bluetooth module, receives data and displays it.
 *
 * @toDo When bluetooth on is turned of, the app crashes.....
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.locationlibrary.Environment;
import com.example.locationlibrary.EnvironmentAddress;
import com.example.locationlibrary.EnvironmentInterf;
import com.example.locationlibrary.EnvironmentSunriseSunsetTimeDateCalc;
import com.example.locationlibrary.EnvironmentSunsetSunriseCalc;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.berthold.physicsFormulas.*;

import berthold.trendAnalyzer.DataToAnalyze;

/**
 * Main
 */

public class MainActivity extends AppCompatActivity
        implements BTConnectedInterface, FragmentSelectDevice.getDataFromFragment, EnvironmentInterf {

    // Debug
    private String tag;

    // UI
    private Handler handler;
    private WebView tempAndHumDisplay;
    private TextView tempMainUnit, tempAltUnit, humidity, dewPoint, pressure, pressureTimeRecorded, currentPressure, basePressure, avrPressure, altitude;
    private ImageButton switchBasePressureForAlt;

    private TextView connectionStatus, nameOfConnectedDevice, deviceBatStatus, connectionStrength, connectionAnimation;
    private FloatingActionButton reconnect, connectToAnotherDevice;

    private TextView timeZoneView, sunriseView, sunsetView, addressView;

    // Bluetooth
    private BluetoothAdapter blueToothAdapter;
    private Set<BluetoothDevice> btBondedDevices;
    private BluetoothDevice bluetoothDeviceCurentlyConnectedTo;
    private ConnectThread connectThread;
    private String adressOfCurrentDevice;

    // Statistics
    private long startTimeOfThread_ms, lastTimeValueWasSaved_ms, timestamp;
    private static final int TIMESTAMP_TO_OLD_MINUTES = 10;
    private static final int MILISEC_PER_MINUTE = 60000;
    private static final float SAVE_FREQUENCY_IN_MS = 60000;
    private static final float QNH = 1013.25f;

    // Record air pressure for 24 hours....
    private static final int SIZE_OF_DATA_SET = (int) ((24 * 60) / (SAVE_FREQUENCY_IN_MS / (1000 * 60)));

    private DataToAnalyze airPressureData_HP;
    private double currentAirPressure_HP;
    private float lastAirPressureSaved;
    private float currentBasePressureForAlt;
    private String currentBasePressureForAltFormated;
    private boolean basePressureForAltIs_QNH;

    // Connected thread
    private ConnectedThreadReadWriteData connectedThreadReadWriteData;
    private final static int BROKEN_HEART = 128148;
    private final static int ANTENNA = 128225;

    // Unicode
    private int fog = 127787;
    private int clear = 9728;

    private int tentencySymbolAirPressure;
    private final static int QUESTIONMARK = 63;
    private final static int ARROW_CONSTANT = 8658;
    private final static int ARROW_RISING_MODERATE = 8663;
    private final static int ARROW_RISING_FAST = 8657;
    private final static int ARROW_FALLING_MODERATE = 8664;
    private final static int ARROW_FALLING_FAST = 8659;

    // HTML
    private final static String DEGREE_SYMBOL = "&#176";
    private final static String DELTA_SYMBOL = "&#206";

    // Geocoder
    private Geocoder geocoder;
    private List<Address> addresses;

    //
    private Environment env;

    // GPS
    private LocationManager locationManager;
    private LocationListener locationListener;

    // Shared
    SharedPreferences sharedPreferences;

    /**
     * Main
     *
     * @param savedInstanceState
     */

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_alt);

        // Debug
        tag = getClass().getSimpleName();

        // UI
        handler = new Handler();

        // UI- elements displaying temperature data received....
        tempAndHumDisplay = (WebView) findViewById(R.id.temp_and_humidity_display);

        tempMainUnit = findViewById(R.id.temp_main_unit);
        tempAltUnit = findViewById(R.id.temp_alt_unit);
        humidity = findViewById(R.id.humidity);
        dewPoint = findViewById(R.id.dew_point);
        pressure = findViewById(R.id.pressure);
        avrPressure = findViewById(R.id.avr_press_slope);
        pressureTimeRecorded = findViewById(R.id.pressure_time_recorded);
        currentPressure = findViewById(R.id.current_press);
        basePressure = findViewById(R.id.base_pressure);
        altitude = findViewById(R.id.alt);

        // UI elements displaying the connection status
        nameOfConnectedDevice = (TextView) findViewById(R.id.name_of_connected_device);
        deviceBatStatus = (TextView) findViewById(R.id.sensor_bat_status);
        connectionStatus = (TextView) findViewById(R.id.connection_status);
        connectionStrength = (TextView) findViewById(R.id.connection_signal_strength);
        connectionAnimation = (TextView) findViewById(R.id.connection_animation);

        reconnect = (FloatingActionButton) findViewById(R.id.reconnect);
        connectToAnotherDevice = (FloatingActionButton) findViewById(R.id.select_device);

        switchBasePressureForAlt = findViewById(R.id.switch_bas_pressure);

        // UI elements showing enviromental data
        timeZoneView = findViewById(R.id.time_zone);
        sunriseView = findViewById(R.id.sunrise_time);
        sunsetView = findViewById(R.id.sunset_time);
        addressView = findViewById(R.id.address);

        // Check if bluetooth is enabeled
        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bm.getAdapter();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(new BTEventReceiver(), filter);

        if (!bluetoothAdapter.isEnabled()) {
            // Turn bt on....
            /*
            FragmentManager fm = getSupportFragmentManager();
            FragmentSelectDevice fragmentSelectDevice = FragmentSelectDevice.newInstance("Titel");
            fragmentSelectDevice.show(fm, "fragment_ask_for_bt_connection");
            */
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, 1);
        }

        // todo do this inside the callback of the fragment asking to enable bt....
        connectToDeviceLogic();

        // Statistics
        airPressureData_HP = new DataToAnalyze(SIZE_OF_DATA_SET);
        tentencySymbolAirPressure = ARROW_CONSTANT;

        // Try to get last air pressure.
        //
        // If pressure was saved, get it and set timestamp accordingly.
        // So, if 10 min ago the app was closed, we can evaluate the
        // pressure change over that period of time...
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        lastAirPressureSaved = sharedPreferences.getFloat("lastAirPressureSaved", 0);
        timestamp = sharedPreferences.getLong("timestamp", 0);

        if (timestamp > 0) {
            startTimeOfThread_ms = timestamp;
            airPressureData_HP.add((double) lastAirPressureSaved);

        } else
            startTimeOfThread_ms = System.currentTimeMillis();

        lastTimeValueWasSaved_ms = startTimeOfThread_ms;

        // Settings for altitude either QNH or QFE
        basePressureForAltIs_QNH = false;
        toggleBasePressure();

        // GPS
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());

        Log.v("LOCATIONLOCATION", "ENV" + env);
        if (env == null) {
            env = new Environment(this, locationManager, geocoder);
            env.start();
        }
    }

    /**
     * On Resume
     */
    @Override
    protected void onResume() {
        super.onResume();

        // UI
        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToDevice(bluetoothDeviceCurentlyConnectedTo);
            }
        });
        connectToAnotherDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeviceList();
            }
        });

        switchBasePressureForAlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBasePressure();
            }
        });
    }

    /**
     * Callback for {@link berthold.localtemperature.FragmentSelectDevice}
     */
    @Override
    public void getDialogInput(String buttonPressed, String deviceName, BluetoothDevice bluetoothDeviceToConnectTo) {
        Log.v(tag, "Button pressed" + buttonPressed + " Device:" + deviceName);

        if (buttonPressed.equals("CANCEL")) {
            connectionStatus.append(getResources().getString(R.string.no_device_selected));
        } else {
            connectionStatus.append(getResources().getString(R.string.name_of_device_selected) + deviceName);
            connectToDevice(bluetoothDeviceToConnectTo);
            adressOfCurrentDevice = bluetoothDeviceToConnectTo.getAddress();
        }
    }

    /**
     * Shows a list of bonded devices to choose from and
     * to connect to.
     */
    private void showDeviceList() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentSelectDevice fragmentSelectDevice = FragmentSelectDevice.newInstance("Titel");
        fragmentSelectDevice.show(fm, "fragment_select_device");
    }

    /**
     * When bt is turned on or it was turned on, this checks if a
     * device adress was stored in shred prefs and if so, get the
     * asociated device and try's to connect it. If not, show a
     * list of bonded dev's allowing the user to select one.....
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connectToDeviceLogic() {
        // Device already selected and saved after leaving previous session?
        // If so, try to reconnect... If not, show list of devices
        // to connect to.
        adressOfCurrentDevice = currentStateRestoreFromSharedPref();
        if (adressOfCurrentDevice.equals("NO_DEVICE")) {
            showDeviceList();
        } else {
            bluetoothDeviceCurentlyConnectedTo = getBlueToothDeviceByAdress(adressOfCurrentDevice);
            if (bluetoothDeviceCurentlyConnectedTo != null)
                connectToDevice(bluetoothDeviceCurentlyConnectedTo);
        }
    }

    /**
     * Connect to device
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connectToDevice(BluetoothDevice bluetoothDevice) {

        if (bluetoothDevice != null) {
            String name = bluetoothDevice.getName();
            String address = bluetoothDevice.getAddress();

            String html = "&#" + ANTENNA;
            Spanned htmlText = Html.fromHtml(html);
            connectionAnimation.setText(htmlText);

            connectionStatus.setText(getResources().getString(R.string.info_connecting) + " Name:" + " " + name + "    Adress:" + address);

            BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            blueToothAdapter = bm.getAdapter();

            // todo Test
            Context c = getApplicationContext();


            connectThread = new ConnectThread(bluetoothDevice, c, connectionStatus, connectionStrength, connectionAnimation, tempAndHumDisplay, handler, this);
            connectThread.start();
        } else {
            connectionStatus.append(getResources().getString(R.string.error_no_bonded_devices_found));
        }
    }

    /**
     * Get Bluetooth device by it's address
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothDevice getBlueToothDeviceByAdress(String adressOfBluetoothDevice) {
        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        BluetoothAdapter bluetoothAdapter = bm.getAdapter();

        btBondedDevices = bluetoothAdapter.getBondedDevices();

        if (btBondedDevices.size() > 0) {
            connectionStatus.append(getResources().getString(R.string.found_bomded_devices_looking_for_hc05) + ":" + adressOfBluetoothDevice);

            for (BluetoothDevice dev : btBondedDevices) {
                if (dev.getAddress().equals(adressOfBluetoothDevice)) {
                    bluetoothDeviceCurentlyConnectedTo = dev;
                    connectionStatus.append(getResources().getString(R.string.found_hc05) + " " + bluetoothDeviceCurentlyConnectedTo.getName() + " " + bluetoothDeviceCurentlyConnectedTo.getAddress());
                    Log.v(tag, "MYDEBUG>Bonded hc05 adress is:" + bluetoothDeviceCurentlyConnectedTo.getAddress().toString() + " Name:" + bluetoothDeviceCurentlyConnectedTo.getName().toString());
                }
            }
        } else {
            connectionStatus.append(getResources().getString(R.string.error_no_dev_with_this_address_found));
            showDeviceList();
        }
        return bluetoothDeviceCurentlyConnectedTo;
    }

    /**
     * Callback when a connection was established.
     * <p>
     * Receives an instance of the {@link berthold.localtemperature.ConnectedThreadReadWriteData} over which
     * data can be send to the connected device.
     */
    @Override
    public void sucess(ConnectedThreadReadWriteData connectedThreadReadWriteData) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //nameOfConnectedDevice.setText(bluetoothDeviceCurentlyConnectedTo.getName());

                reconnect.hide();
                connectToAnotherDevice.hide();
            }
        });
        this.connectedThreadReadWriteData = connectedThreadReadWriteData;
        //connectedThreadReadWriteData.send("Sending test, test....");

    }

    /**
     * Callback receiving temperature data from {@link berthold.localtemperature.ConnectedThreadReadWriteData}
     * and updating the display...
     */
    @Override
    public void receiveTempData(final double tempMain, final double tempAlt, final double hum, final double press, final double alt, final String batStatus) {

        // Air temperature
        final String tempMainFormated = String.format("%.1f", tempMain);
        final Spanned tempMainHTML = Html.fromHtml(tempMainFormated + DEGREE_SYMBOL + "C");
        final String tempAltFormated = String.format("%.1f", tempAlt);
        final Spanned tempAltHTML = Html.fromHtml(tempAltFormated + DEGREE_SYMBOL + "F");

        // Dew point...
        final float dewPointUnformated = Calculate.dewPointTemperature_C((float) tempMain, (float) hum);
        final String dewPointFormated = String.format("%.0f", dewPointUnformated);
        String dewPointSymbol = evaluateDewPoint((float) tempMain, dewPointUnformated);
        final Spanned dewPointHTML = Html.fromHtml(dewPointFormated + DEGREE_SYMBOL + "C " + dewPointSymbol);

        // Air pressure related...
        PressureEvalResult pressureEvalResult = evalAirPressure(press);
        String pressureSymbol = "&#" + pressureEvalResult.getTendencySymbol();
        final float totalTimeAirPressureIsRecorded_MIN = pressureEvalResult.getTotalTimePressureIsRecorded();
        final String totalTimeAirPressureIsRecordedFormated_MIN = String.format("%.0f", totalTimeAirPressureIsRecorded_MIN);
        final String pressureFormated = String.format("%.2f", press / 100);
        final Spanned pressureHTML = Html.fromHtml(pressureFormated + " hPa " + pressureSymbol);

        final float sumOfPressChanges = (float) airPressureData_HP.getSumOfDifferences();
        final String sumOfPressChangesFormated = String.format("%.2f", sumOfPressChanges);

        // Set altitude according to current base pressure
        final double calculatedAlt;
        if (basePressureForAltIs_QNH)
            calculatedAlt = alt;
        else
            calculatedAlt = Calculate.altitude((float) currentAirPressure_HP / 100, (float) currentBasePressureForAlt);

        final String calculatedAltFormated = String.format("%.3f", calculatedAlt);

        // Display
        handler.post(new Runnable() {
            @Override
            public void run() {
                tempMainUnit.setText(tempMainHTML);
                tempAltUnit.setText(tempAltHTML);

                humidity.setText(hum + " %");
                dewPoint.setText(dewPointHTML);

                pressure.setText(pressureHTML);
                currentPressure.setText(pressureHTML);
                pressureTimeRecorded.setText(totalTimeAirPressureIsRecordedFormated_MIN + " Min.");
                avrPressure.setText(sumOfPressChangesFormated + " hPa ");

                //basePressure.setText(currentBasePressureForAlt + " hPa");
                altitude.setText(calculatedAltFormated + " m");

                deviceBatStatus.setText(batStatus);
            }
        });
    }

    /**
     * Evaluate dew point
     * <p>
     * toDo: set wallpaper accordingly......
     */
    private String evaluateDewPoint(float temperature_C, float dewPoint_C) {
        String dewPointSymbol;
        float dewPointDelta_C = temperature_C - dewPoint_C;
        if (dewPointDelta_C <= 4)
            dewPointSymbol = "&#" + fog;
        else
            dewPointSymbol = "&#" + clear;

        return dewPointSymbol;
    }

    /**
     * Toggle base pressure between QFE (pressure at the current devices position) and
     * and QNH (pressure at main sea level = 1013.15 hPa).
     */
    private void toggleBasePressure() {
        if (basePressureForAltIs_QNH) {
            currentBasePressureForAlt = (float) currentAirPressure_HP / 100;
            currentBasePressureForAltFormated = String.format("%.2f", currentBasePressureForAlt);
            switchBasePressureForAlt.setImageResource(R.drawable.qfe_icon);
            basePressureForAltIs_QNH = false;
        } else {
            currentBasePressureForAlt = QNH;
            currentBasePressureForAltFormated = String.format("%.2f", currentBasePressureForAlt);
            switchBasePressureForAlt.setImageResource(R.drawable.qnh_icon);
            basePressureForAltIs_QNH = true;
        }
        basePressure.setText(currentBasePressureForAltFormated + " hPa");
    }

    /**
     * Evaluate air pressure
     */
    private PressureEvalResult evalAirPressure(Double airPresure_HP) {

        currentAirPressure_HP = airPresure_HP; // Update global var...

        long now_ms = System.currentTimeMillis();
        long timeEleapsedSinceThreadWasStarted_MIN = (now_ms - startTimeOfThread_ms) / 60000;

        if ((now_ms - lastTimeValueWasSaved_ms) > SAVE_FREQUENCY_IN_MS) {
            airPressureData_HP.add(airPresure_HP / 100);

            lastTimeValueWasSaved_ms = now_ms;

            // Evaluate air pressure as integer (this way the avarage calculated is less erratic)....
            saveToSharedPreferences((int) (airPresure_HP / 100), now_ms);
        }

        // Get avr slope of air pressure
        double avrSlope_HP = (airPressureData_HP.getAverageSlope());
        Log.v(tag, "Pressure Slope:" + avrSlope_HP + " over the last:" + timeEleapsedSinceThreadWasStarted_MIN + " Min");

        if (avrSlope_HP < 0) tentencySymbolAirPressure = ARROW_FALLING_MODERATE;
        if (avrSlope_HP == 0) tentencySymbolAirPressure = ARROW_CONSTANT;
        if (avrSlope_HP > 0) tentencySymbolAirPressure = ARROW_RISING_MODERATE;


        PressureEvalResult pressureEvalResult = new PressureEvalResult(tentencySymbolAirPressure, timeEleapsedSinceThreadWasStarted_MIN);
        return pressureEvalResult;
    }

    /**
     * Connection error callback.
     * <p>
     * Simply try to reconnect....
     */
    @Override
    public void receiveErrorMessage(final String errorDescription) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connectedThreadReadWriteData != null)
                    connectedThreadReadWriteData.cancel();

                connectionStatus.setText("\n\n" + errorDescription);
                String html = "&#" + BROKEN_HEART;
                Spanned htmlText = Html.fromHtml(html);
                connectionAnimation.setText(htmlText);

                reconnect.show();
                connectToAnotherDevice.show();
            }
        });
    }

    /**
     * Get location data and update display.
     *
     * @param heightIn_m
     * @param getSpeedIn_kmH
     * @param distanceTraveledIn_m
     * @param lon
     * @param lat
     */
    @Override
    public void getEnviromentalData(final EnvironmentAddress environmentAddress, final float heightIn_m, final float getSpeedIn_kmH, final float avrSpeedIn_kmH, final float distanceTraveledIn_m, final Double lon, final Double lat) {

        handler.post(new Runnable() {
            @Override
            public void run() {

                //waitingForGpsView.setVisibility(View.GONE);

                String fullAddress = environmentAddress.getAddress();
                String city = environmentAddress.getCity();
                String state = environmentAddress.getState();
                String postalCode = environmentAddress.getPostalCode();
                //String address = fullAddress+"\n\n"+postalCode + "\n" + city + "\n" + state;
/*
                locationNameAndAdressView.setText(address);

                heighView.setText(heightIn_m + " m");

                speedView.setText(getSpeedIn_kmH + " km/h");

                avrSpeedView.setText("Avr:"+avrSpeedIn_kmH);

                distanceView.setText(distanceTraveledIn_m + " m");

                String statusText = "Long:" + lon + " Lat:" + lat;
                gpsCoordinatesView.setText(statusText);
*/


                int dayOfYear = EnvironmentSunriseSunsetTimeDateCalc.getDayOfCurrentYear();
                int offsetToGMT = EnvironmentSunriseSunsetTimeDateCalc.getTimeZoneOffsetIn_h();

                String nameOfTimeZone = EnvironmentSunriseSunsetTimeDateCalc.getCurrentTimezoneName() + " GMT+" + offsetToGMT;
                timeZoneView.setText(nameOfTimeZone);

                double sunriseTimeIn_h = EnvironmentSunsetSunriseCalc.getSunriseTimeAtObserversLocationIn_h(lon, lat, dayOfYear, offsetToGMT);
                sunriseView.setText(EnvironmentSunriseSunsetTimeDateCalc.getTimeInTwentyFourHourFormat(sunriseTimeIn_h));

                double sunsetTimeIn_h = EnvironmentSunsetSunriseCalc.getSunsetTimeAtObserversLocationIn_h(lon, lat, dayOfYear, offsetToGMT);
                sunsetView.setText(EnvironmentSunriseSunsetTimeDateCalc.getTimeInTwentyFourHourFormat(sunsetTimeIn_h));

                addressView.setText(fullAddress);
            }
        });

    }

    /**
     * Get status info like update time and/ or error messages...
     *
     * @param timestamp
     * @param lastUpdateIn_s
     * @param statusMessage
     */
    @Override
    public void getStatusData(Long timestamp, float lastUpdateIn_s, String statusMessage) {
        String status = "Last update:" + lastUpdateIn_s + "s // Status:" + statusMessage;
        //gpsStatusView.setText(status);
    }

    /*
     * Save Instance State
     *
     * This method is called, when the activity was destroyed by the
     * system. E.g. screen orientation changed.
     */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        currentStateSaveToSharedPref(adressOfCurrentDevice);
    }

    /*
     * Save to shared prefs...
     */

    private void saveToSharedPreferences(double airPressure_HP, long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("lastAirPressureSaved", (float) airPressure_HP);
        editor.putLong("timestamp", timestamp);
        editor.commit();
    }

    /*
     * Save current state to sharedPreferences.
     */
    private void currentStateSaveToSharedPref(String adressOfCurrentDevice) {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // ToDo
        editor.putString("adressOfCurrentDevice", adressOfCurrentDevice);
        editor.commit();
    }

    /*
     * Restore from shared pref's..
     */
    private String currentStateRestoreFromSharedPref() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        String adressOfCurrentDevice = sharedPreferences.getString("adressOfCurrentDevice", "NO_DEVICE");
        return adressOfCurrentDevice;
    }

    /*
     * When app gets destroyed by the system...
     */
    @Override
    public void onDestroy() {
        Log.v(tag, "On destroy");
        super.onDestroy();
        connectedThreadReadWriteData.cancel();
        env.cancel();
    }

    /*
     * When back button was pressed....
     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.v(tag, "On back pressed");
        connectedThreadReadWriteData.cancel();
        env.cancel();
    }
}
