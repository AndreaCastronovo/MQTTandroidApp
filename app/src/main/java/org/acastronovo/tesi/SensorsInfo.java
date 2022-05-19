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
import java.util.Objects;

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */


public class SensorsInfo extends AppCompatActivity implements SensorEventListener {

    private final String TAG = "SensorInfo";

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

    //location
    FusedLocationProviderClient locationProviderClient;
    ArrayList<Location> locationList;

    //sensors
    SensorManager sensorManager;
    Sensor pedometer;

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

        //location
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationList = new ArrayList<>();

        //built-in sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pedometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);


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
        userName.setText(preferences.getString("user_name", "set you username"));
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                switch (s) {
                    case "emergency_checkbox":
                        emergencyBoolean = sharedPreferences.getBoolean("emergency_checkbox", false);
                        break;
                    case "user_name":
                        user.setName(sharedPreferences.getString("user_name", "Cristian"));
                        userName.setText(user.getName());
                        break;
                }
            }
        });

        user.setGender(preferences.getString("user_gender", "Male"));
        user.setAge(preferences.getString("user_age", "23"));
        user.setWeight(preferences.getString("user_weight", "85"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        //userName.setText(user.getName());
        locationUpdate();
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
                Toast.makeText(this, "Sending data to remote clients", Toast.LENGTH_SHORT).show();
                mqttService = new Intent(this, MqttService.class);
                mqttService.putExtra(StaticResources.EXTRA_LOCATION, position);
                try {
                    startService(mqttService);
                } catch (IllegalStateException | SecurityException e) {
                    e.printStackTrace();
                }
                return true;

            case (R.id.action_settings):
                startActivity(new Intent(this, Settings.class));
                return true;

            case (R.id.action_about):
                Intent aboutWebView = new Intent(SensorsInfo.this, AboutWebView.class);
                aboutWebView.putExtra(StaticResources.WEB_PAGE, "https://github.com/CDOrtona/Tesi");
                startActivity(aboutWebView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //ask for phone call permissions
    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    public boolean phoneCallPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);

            return false;
        } else {
            return true;
        }
    }

    //result of the phone call permissions or any other permission
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Emergency Service won't work without permissions", Toast.LENGTH_SHORT).show();
                    emergencyBoolean = false;
                }
                return;
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (connectedToGatt) {
            menu.findItem(R.id.action_connect).setVisible(false);
            menu.findItem(R.id.action_disconnect).setVisible(true);
            menu.findItem(R.id.action_mqtt).setEnabled(true);
            accessLocation();
            pedometerValue.setText(Integer.toString(0));
            return true;
        } else if (!connectedToGatt) {
            menu.findItem(R.id.action_connect).setVisible(true);
            menu.findItem(R.id.action_disconnect).setVisible(false);
            menu.findItem(R.id.action_mqtt).setEnabled(false);
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
    public void onSensorChanged(SensorEvent event) {
        Sensor sensorChanged = event.sensor;
        float[] values = event.values;
        if (values.length > 0) {
            switch (sensorChanged.getType()) {
                case Sensor.TYPE_STEP_COUNTER:
                    if (connectedToGatt)
                        pedometerValue.setText(Float.toString(values[0]));
            }
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
                        invalidateOptionsMenu();
                        connectionStateString(StaticResources.STATE_CONNECTED);
                        //new Sensor object which will be passed to the MqttConnection constructor
                    } else if (stateConnection.equals(StaticResources.STATE_DISCONNECTED)) {
                        connectedToGatt = false;
                        invalidateOptionsMenu();
                        connectionStateString(StaticResources.STATE_DISCONNECTED);
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

    public void locationUpdate() {
        //this creates a location request with default parameters
        LocationRequest locationRequest = LocationRequest.create();
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
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
    }

    private final LocationCallback locationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            //this is similar to a for each loop that stores all the locations found in the location arrayList
            locationList.addAll(locationResult.getLocations());
        }
    };

    private void accessLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            Log.d(TAG, "Location permission disabled, sent request permission activation dialog");
            accessLocation();
        } else {
            Log.d(TAG, "Location permission enabled");
            locationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //location is null if there is no known location found
                    position = Math.round(location.getLatitude() * 100d) / 100d + "," +
                            +Math.round(location.getLongitude() * 100d) / 100d;
                    gpsValue.setText(position);
                }
            });
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
        //this is gonna flush the location stored in the location variable
        locationProviderClient.flushLocations();
        locationProviderClient.removeLocationUpdates(locationCallBack);
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

}