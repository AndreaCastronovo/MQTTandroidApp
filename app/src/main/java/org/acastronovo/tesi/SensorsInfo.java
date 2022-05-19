package org.acastronovo.tesi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *@author Cristian D'Ortona / Andrea Castronovo / Alberto Iantorni
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */


public class SensorsInfo extends AppCompatActivity implements SensorEventListener {

    private final String TAG = "SensorInfo";

    //ask for phone call permissions
    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 2;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    //I initialize an object from the class ConnectToGattServer which handles the connection to the GATT server of the ESP32
    ConnectToGattServer connectToGattServer;

    //GATT
    private String deviceAddress;
    private String deviceName;
    boolean connectedToGatt = false;
    private String stateConnection = null;
    private boolean flagDeviceFound = false;

    //TextView initialization
    ImageView profilePic;
    TextView userName;
    TextView addressInfo;
    TextView nameInfo;
    TextView connectionState;
    TextView tempValue;
    TextView heartValue;
    TextView humidityValue;
    TextView gpsValue;
    TextView pressureValue;
    TextView pedometerValue;
    TextView altitudeValue;
    TextView calories;

    //sensors
    SensorManager sensorManager;
    Sensor pedometer;
    Sensor temperature;
    Sensor pressureSensor;
    Sensor humiditySensor;
    boolean isStepSensorPresent = false;
    boolean isAmbientTempPresent = false;
    boolean isPressureSensorPresent = false;
    boolean isHumiditySensorPresent = false; //Init to false state to don't register/unreg listener if bluethoot activated
    boolean locationPermission = false;
    int stepDetect = 0; //To count step
    float tempValueSensor = 0;
    float pressureValueSensor = 0;
    float humidityValueSensor = 0;
    //float pressureAtSeaLevel = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
    float altitudeValueSensor = 0;
    float latitude;
    float longitude;


    //Toolbar
    Toolbar toolbar;

    //flags
    boolean emergencyBoolean;

    //UserModel object
    UserModel user;

    //preferences
    SharedPreferences preferences;

    String temp;
    String heartBeat;
    String humidity;
    String position;
    String pressure;
    String altitude;
    Boolean sos;

    //MQTT
    Intent mqttService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_info);

        //UI setup
        userName = findViewById(R.id.user_name);
        profilePic = findViewById(R.id.profile_image);
        addressInfo = findViewById(R.id.address_textView);
        nameInfo = findViewById(R.id.name_textView);
        connectionState = findViewById(R.id.connection_state_textView);
        tempValue = findViewById(R.id.textView_temp);
        heartValue = findViewById(R.id.textView_heart);
        humidityValue = findViewById(R.id.textView_brightness);
        gpsValue = findViewById(R.id.textView_position);
        pressureValue = findViewById(R.id.pressure_text_view);
        pedometerValue = findViewById(R.id.pedometer_text_view);
        altitudeValue = findViewById(R.id.altitude_text_view);
        calories = findViewById(R.id.calories_text_view);

        //hard coded, I must change it later
        connectionState.setTextColor(Color.RED);
        connectionState.setText("Disconnected");

        //calling the constructor in order to build a BluetoothAdaptor object
        connectToGattServer = new ConnectToGattServer(deviceAddress, this);

        //built-in sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        /*  TO PRINT SENSOR LIST OF PHONE
        String SensorList;
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i = 1; i < deviceSensors.size(); i++){
            SensorList = ("Sensor number: " + i + "\n"
                    + "Sensor name: " + deviceSensors.get(i).getName() + "\n"
                    + "Sensor type: " + deviceSensors.get(i).getStringType() + "\n"
                    + "Sensor vendor: " + deviceSensors.get(i).getVendor() + "\n"
                    + "Sensor version: "+ deviceSensors.get(i).getVersion()+ "\n" + "\n");
            System.out.println(SensorList);
        }*/

        //Set sensor
        if ((sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)) != null) {
            //StepDetector sensor is present
            pedometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isStepSensorPresent = true;
            pedometerValue.setText("0.0");  //Initialization of pedometerValue
            //Start listen event for pedometer
            sensorManager.registerListener(this, pedometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            //StepDetector sensor is not present
            pedometerValue.setText("Not Present");
            isStepSensorPresent = false;
        }
        setSensor();
        heartValue.setText("Not Present");//Init value of heart sensor, if connectedToGatt become true reset at value


        //here I'm specifying the intent filters I want to subscribe to in order to get their updates
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticResources.ACTION_CONNECTION_STATE);
        intentFilter.addAction(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ);
        registerReceiver(bleBroadcastReceiver, intentFilter);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_sensors);
        setSupportActionBar(toolbar);

        user = new UserModel();

        //preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //I need this so that when the app starts it knows which is the stored value of this preference
        emergencyBoolean = preferences.getBoolean("emergency_checkbox", true);
        userName.setText(preferences.getString("user_name", "set your username"));
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                switch (s) {
                    case "emergency_checkbox":
                        emergencyBoolean = sharedPreferences.getBoolean("emergency_checkbox", false);
                        break;
                    case "user_name":
                        user.setName(sharedPreferences.getString("user_name", "User"));
                        userName.setText(user.getName());
                        break;
                }
            }
        });

        user.setGender(preferences.getString("user_gender", "gender"));
        user.setAge(preferences.getString("user_age", "age"));
        user.setWeight(preferences.getString("user_weight", "weight"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        //userName.setText(user.getName());

        //Location
        accessLocation();

        //Restart register
        if (!connectedToGatt) {
            registerListenerSensor();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!connectedToGatt) {
            unregisterListenerSensor();
        }
    }

    //Function to set sensor
    private void setSensor() {
        //Ambient temperature sensor
        if ((sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)) != null) {
            //AmbientTemperature sensor is present
            temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            isAmbientTempPresent = true;
            /*  //Start listen event for temperature
                //sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);
            --> enable on resume to disable on pause*/
        } else {
            //AmbientTemperature sensor is not present
            tempValue.setText("Not Present");
            isAmbientTempPresent = false;
        }

        //Pressure sensor
        if ((sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)) != null) {
            //Pressure sensor is present
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            isPressureSensorPresent = true;
        } else {
            //Pressure sensor is not present
            pressureValue.setText("Not Present");
            isPressureSensorPresent = false;
        }

        //Humidity sensor
        if ((sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)) != null) {
            //Humidity sensor is present
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            isHumiditySensorPresent = true;
        } else {
            //Humidity sensor is not present
            humidityValue.setText("Not Present");
            isHumiditySensorPresent = false;
        }
    }

    public void changeProfilePic(View view) {
        Intent choosePic = new Intent(Intent.ACTION_PICK);
        choosePic.setType("image/*");
        startActivityForResult(choosePic, StaticResources.REQUEST_CODE_CHANGE_PROFILE_PIC);
    }

    //Toolbar set up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu_sensors, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_connect_to_peripheral):
                Intent scanDevices = new Intent(SensorsInfo.this, ScanningActivity.class);
                try {
                    startActivityForResult(scanDevices, StaticResources.REQUEST_CODE_SCAN_ACTIVITY);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    finish();
                }
                return true;

            case (R.id.action_emergency):
                if (emergencyBoolean) {
                    mqttService = new Intent(this, MqttService.class);
                    mqttService.putExtra(StaticResources.EXTRA_SOS_FLAG, true);
                    mqttService.putExtra(StaticResources.EXTRA_LOCATION, position);
                    startService(mqttService);
                    //I have to make sure the user agrees with the phone permissions at run-time
                    if (phoneCallPermissions()) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + preferences.getString("relative_phone", "331")));
                        startActivity(callIntent);
                    }
                } else {
                    Toast.makeText(this, "Please enable the emergency button in the settings", Toast.LENGTH_LONG).show();
                    return true;
                }
                return true;
            case (R.id.action_connect):
                connectToGatt();
                return true;
            case (R.id.action_disconnect):
                invalidateOptionsMenu();
                disconnectFromGatt();
                return true;

            case (R.id.action_graph_rssi):
                Intent rssiGraph = new Intent(SensorsInfo.this, GraphRssi.class);
                rssiGraph.putExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS, deviceAddress);
                try {
                    startActivity(rssiGraph);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    finish();
                }
                return true;

            case (R.id.action_mqtt):
                mqttService = new Intent(this, MqttService.class);
                //Pass object at MqttService activity
                mqttService.putExtra(StaticResources.EXTRA_LOCATION, position);
                mqttService.putExtra(StaticResources.EXTRA_CONNECTED_TO_GATT, connectedToGatt);
                mqttService.putExtra(StaticResources.EXTRA_LOCATION_PERMISSION, locationPermission);
                if(isStepSensorPresent){
                    mqttService.putExtra(StaticResources.EXTRA_PEDOMETER_VALUE_SENSOR, stepDetect);
                }
                if(!connectedToGatt){
                    if(isAmbientTempPresent){
                        mqttService.putExtra(StaticResources.EXTRA_TEMP_VALUE_SENSOR, tempValueSensor);
                    }
                    if(isHumiditySensorPresent){
                        mqttService.putExtra(StaticResources.EXTRA_HUMIDITY_VALUE_SENSOR, humidityValueSensor);
                    }
                    if(locationPermission){
                        mqttService.putExtra(StaticResources.EXTRA_LATITUDE_VALUE_SENSOR, latitude);
                        mqttService.putExtra(StaticResources.EXTRA_LONGITUDE_VALUE_SENSOR, longitude);
                        mqttService.putExtra(StaticResources.EXTRA_ALTITUDE_VALUE_SENSOR, altitudeValueSensor);
                    }
                    if(isPressureSensorPresent){
                        mqttService.putExtra(StaticResources.EXTRA_PRESSURE_VALUE_SENSOR, pressureValueSensor);
                    }
                }
                try {
                    startService(mqttService);
                } catch (IllegalStateException | SecurityException e) {
                    e.printStackTrace();
                }
                return true;

            case (R.id.action_connected_devices):
                startActivity(new Intent(this, ConnectedDevices.class));
                return true;

            case (R.id.action_settings):
                startActivity(new Intent(this, Settings.class));
                return true;

            case (R.id.action_about):
                Intent aboutWebView = new Intent(SensorsInfo.this, AboutWebView.class);
                aboutWebView.putExtra(StaticResources.WEB_PAGE, "https://github.com/AndreaCastronovo/TESI");
                startActivity(aboutWebView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean phoneCallPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);

            return false;
        } else {
            return true;
        }
    }

    //result of the phone call permissions or any other permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {
            // If request is cancelled, the result arrays are empty.
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                //Toast.makeText(this, "Emergency Service won't work without permissions", Toast.LENGTH_SHORT).show();
                emergencyBoolean = false;
            }
        }
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                altitudeValue.setText("Not Present \n without location");
                //Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(SensorsInfo.this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(SensorsInfo.this).removeLocationUpdates(this);
                if(locationResult != null && locationResult.getLocations().size() > 0){
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    latitude = (float) locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    longitude = (float) locationResult.getLocations().get(latestLocationIndex).getLongitude();
                    gpsValue.setText(String.format("Latitu: %.2f\nLongit: %.2f", latitude, longitude));
                    altitudeValueSensor = (float) locationResult.getLocations().get(latestLocationIndex).getAltitude();
                    altitudeValue.setText(String.format("%.2f m", altitudeValueSensor));

                }
            }
        }, Looper.getMainLooper());
    }

        @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (connectedToGatt) {
            menu.findItem(R.id.action_connect).setVisible(false);
            menu.findItem(R.id.action_disconnect).setVisible(true);
            menu.findItem(R.id.action_mqtt).setEnabled(true);
            //pedometerValue.setText(Integer.toString(0));
            return true;
        } else if (!connectedToGatt) {
            menu.findItem(R.id.action_connect).setVisible(true);
            menu.findItem(R.id.action_disconnect).setVisible(false);
            menu.findItem(R.id.action_mqtt).setEnabled(true);
            return true;
        } else {
            return super.onPrepareOptionsMenu(menu);
        }
    }

    //I override this method to make sure that the GATT server is disconnected if the users goes back to the previous activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectFromGatt();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == StaticResources.REQUEST_CODE_SCAN_ACTIVITY) {
                deviceAddress = data.getStringExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS);
                deviceName = data.getStringExtra(StaticResources.EXTRA_CHOOSEN_NAME);
                //Setting the values of the TextViews objects
                addressInfo.setTypeface(Typeface.SANS_SERIF);
                addressInfo.setText(deviceAddress);
                nameInfo.setTypeface(Typeface.SANS_SERIF);
                nameInfo.setText(deviceName);
                flagDeviceFound = true;
            }

            else if (requestCode == StaticResources.REQUEST_CODE_CHANGE_PROFILE_PIC) {
                final Uri imageUri = data.getData();
                InputStream imageStream = null;
                try {
                     imageStream = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profilePic.setImageBitmap(selectedImage);
            }
        }

        else if (resultCode == Activity.RESULT_CANCELED)
            flagDeviceFound = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == pedometer){
            stepDetect = (int) (stepDetect + sensorEvent.values[0]);
            pedometerValue.setText(String.valueOf(stepDetect));
        }
        else if(sensorEvent.sensor == temperature){
            tempValueSensor = sensorEvent.values[0];
            tempValue.setText(String.format("%.2f °C", tempValueSensor));
        }
        else if(sensorEvent.sensor == pressureSensor){
            pressureValueSensor = sensorEvent.values[0];
            pressureValue.setText(String.format("%.2f hPa", pressureValueSensor));
        }
        else if(sensorEvent.sensor == humiditySensor){
            humidityValueSensor = sensorEvent.values[0];
            humidityValue.setText(String.format("%.2f %", humidityValueSensor));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //this is used to receive the broadcast announcements that are being sent from the class ConnectToGattServer()
    //the received broadcasters depend on the intent filters declared above
    final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String broadcastReceived = intent.getAction();
            Log.d(TAG, "The received Broadcast is: " + broadcastReceived);

            switch (Objects.requireNonNull(broadcastReceived)) {

                case StaticResources.ACTION_CONNECTION_STATE:
                    stateConnection = intent.getStringExtra(StaticResources.EXTRA_STATE_CONNECTION);
                    if (stateConnection.equals(StaticResources.STATE_CONNECTED)) {
                        connectedToGatt = true;

                        //Stop listening from device sensor
                        unregisterListenerSensor();

                        //Set Gatt
                        invalidateOptionsMenu();
                        connectionStateString(StaticResources.STATE_CONNECTED);
                        //new Sensor object which will be passed to the MqttConnection constructor
                    } else if (stateConnection.equals(StaticResources.STATE_DISCONNECTED)) {
                        connectedToGatt = false;
                        invalidateOptionsMenu();
                        connectionStateString(StaticResources.STATE_DISCONNECTED);

                        //ReStart listening of device sensors
                        registerListenerSensor();
                    }
                    break;
                //this received broadcast lets the activity that subbed to this intent filter know which is the characteristic that has changed
                case StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ:
                    Log.d("whichCharChanged", StaticResources.EXTRA_CHARACTERISTIC_CHANGED);
                    switch (intent.getStringExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED)) {
                        case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                            temp = intent.getStringExtra(StaticResources.EXTRA_TEMP_VALUE);
                            tempValue.setText(temp);
                            break;
                        case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                            heartBeat = intent.getStringExtra(StaticResources.EXTRA_HEART_VALUE);
                            heartValue.setText(heartBeat);
                            break;
                        case StaticResources.ESP32_HUMIDITY_CHARACTERISTIC:
                            humidity = intent.getStringExtra(StaticResources.EXTRA_HUMIDITY_VALUE);
                            humidityValue.setText(humidity);
                            break;
                        case StaticResources.ESP32_PRESSURE_CHARACTERISTIC:
                            pressure = intent.getStringExtra(StaticResources.EXTRA_PRESSURE_VALUE);
                            pressureValue.setText(pressure);
                            break;
                        case StaticResources.ESP32_ALTITUDE_CHARACTERISTIC:
                            altitude = intent.getStringExtra(StaticResources.EXTRA_ALTITUDE_VALUE);
                            altitudeValue.setText(altitude);
                            break;
                    }
            }
        }
    };

    boolean fitnessActivityStarted = false;

    //pop up menu to choose fitness activity
    public void calculateCalories(View view) {
        if (!fitnessActivityStarted) {
            fitnessActivityStarted = true;
            calories.setText("0 Kcal");
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_calories, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    FitnessTracker.calculateCalories(item.getTitle().toString());
                    return true;
                }
            });
            popupMenu.show();
        } else {
            String text = FitnessTracker.stopFitnessActivity(user) + "KCal";
            calories.setText(text);
            fitnessActivityStarted = false;
        }
    }

    private void accessLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermission = false;
            ActivityCompat.requestPermissions(SensorsInfo.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            locationPermission = true;
            getCurrentLocation();
        }
    }

    //add an if that checks if the adaptor is connected to the GATT server already
    public void connectToGatt() {
        if (flagDeviceFound) {
            connectToGattServer.connectToGatt(deviceAddress);
            //this makes sure that there is a time out error if it takes more than 10 seconds to connect to the remote peripheral
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (stateConnection == null) {
                        Log.w(TAG, "Timeout connection to remote peripheral");
                        Toast.makeText(getApplicationContext(), "The connection has timed out, try again", Toast.LENGTH_SHORT).show();
                        connectionStateString(StaticResources.STATE_DISCONNECTED);
                        disconnectFromGatt();
                    }
                }
            }, 10000);
            connectionStateString(StaticResources.STATE_CONNECTING);
        } else {
            Toast.makeText(this, "Please, connect to remote device first", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectFromGatt() {
        stateConnection = null;
        connectToGattServer.disconnectGattServer();
        connectedToGatt = false;
        connectionStateString(StaticResources.STATE_DISCONNECTED);
        //destroy the MQTT service
        //stopService(mqttService);

        //ReStart listening of device sensor
        setSensor();
        registerListenerSensor();
    }

    //this dynamically changes the string color and text of the string that shows on screen the connection state
    private void connectionStateString(String state) {
        switch (state) {
            case StaticResources.STATE_CONNECTED:
                connectionState.setTextColor(Color.GREEN);
                connectionState.setText(StaticResources.STATE_CONNECTED);
                break;
            case StaticResources.STATE_CONNECTING:
                connectionState.setTextColor(Color.YELLOW);
                connectionState.setText(StaticResources.STATE_CONNECTING);
                break;
            case StaticResources.STATE_DISCONNECTED:
                connectionState.setTextColor(Color.RED);
                connectionState.setText(StaticResources.STATE_DISCONNECTED);
                break;
        }
    }

    //Function to unregister listener of sensor that i choose
    private void unregisterListenerSensor(){
        if (isAmbientTempPresent) {
            sensorManager.unregisterListener(this, temperature);
        }
        if (isPressureSensorPresent) {
            sensorManager.unregisterListener(this, pressureSensor);
        }
        if (isHumiditySensorPresent) {
            sensorManager.unregisterListener(this, humiditySensor);
        }
    }

    //Function to unregister listener of sensor that i choose
    private void registerListenerSensor(){
        if(isAmbientTempPresent){
            //Start listen event for temperature
            sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(isPressureSensorPresent){
            //Start listen event for pressure
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(isHumiditySensorPresent){
            //Start listen event for humidity
            sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

}