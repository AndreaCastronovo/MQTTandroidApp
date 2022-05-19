package org.acastronovo.tesi;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.Token;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

/**
 *@author Cristian D'Ortona / Andrea Castronovo / Alberto Iantorni
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class MqttService extends Service {

    String TAG = "MqttService";
    MqttAsyncClient client;
    //test variables
    private final String serverUri = "tcp://192.168.1.2:1883";
    private final String user = "andrea";
    private final String pwd = "1234";
    private MemoryPersistence persistance;

    String temp;
    String heartBeat;
    String humidity;
    String locations;
    String pressure;
    String altitude;
    Boolean sos;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //this runs only the first time the service is created
    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticResources.ACTION_CONNECTION_STATE);
        intentFilter.addAction(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ);
        registerReceiver(bleBroadcastReceiver, intentFilter);

        String clientId = MqttClient.generateClientId();

        try {
            client = new MqttAsyncClient(serverUri, clientId, persistance);
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setUserName(user);
            mqttConnectOptions.setPassword(pwd.toCharArray());
            IMqttToken token = client.connect(mqttConnectOptions);

            /*token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println("CONNECTED_WITH_RASPBERRY");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewal
                    Log.d(TAG, "onFailure");
                    System.out.println("FAILURE_WITH_RASPBERRY");
                }
            });*/



            try{
                Thread.sleep(5000);
            }catch (Exception e){
                e.printStackTrace();
            }

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection Lost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.e(TAG, "Message arrived");

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.e(TAG, "Delivery Complete");
                }
            });

            token = client.connect(mqttConnectOptions);

        }  catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Get object from SensorInfo
        String position = intent.getStringExtra(StaticResources.EXTRA_LOCATION);
        boolean sos_on = intent.getBooleanExtra(StaticResources.EXTRA_SOS_FLAG, false);
        boolean connectedToGatt = intent.getBooleanExtra(StaticResources.EXTRA_CONNECTED_TO_GATT, false);
        boolean locationPermission = intent.getBooleanExtra(StaticResources.EXTRA_LOCATION_PERMISSION, false);
        float latitude = 360;
        float longitude = 360;
        float altitudeValueSensor = 9999;
        if(locationPermission){
            latitude = intent.getFloatExtra(StaticResources.EXTRA_LATITUDE_VALUE_SENSOR, 360);
            longitude = intent.getFloatExtra(StaticResources.EXTRA_LONGITUDE_VALUE_SENSOR, 360);
            altitudeValueSensor = intent.getFloatExtra(StaticResources.EXTRA_ALTITUDE_VALUE_SENSOR, 9999);
        }
        float tempValueSensor = intent.getFloatExtra(StaticResources.EXTRA_TEMP_VALUE_SENSOR, -999);
        float humidityValueSensor = intent.getFloatExtra(StaticResources.EXTRA_HUMIDITY_VALUE_SENSOR, -1);
        float pressureValueSensor = intent.getFloatExtra(StaticResources.EXTRA_PRESSURE_VALUE_SENSOR, 0);
        int stepDetect = intent.getIntExtra(StaticResources.EXTRA_PEDOMETER_VALUE_SENSOR, -1);

        /*TO TEST (PRINT) VALUE
        System.out.println("CONNECTED TO GATT: " + connectedToGatt);
        System.out.println("LOCATION PERMISSION: " + locationPermission);
        System.out.println("LATITUDE: " + latitude);
        System.out.println("LONGITUDE: " + longitude);
        System.out.println("TEMPERATURE: " + tempValueSensor);
        System.out.println("HUMIDITY: " + humidityValueSensor);
        System.out.println("PRESSURE: " + pressureValueSensor);
        System.out.println("ALTITUDE: " + altitudeValueSensor);
        System.out.println("PEDOMETER: " + stepDetect);*/



        //this is checking if the user has fired the sos
        if(sos_on){
            try {
                pub(StaticResources.SOS_TOPIC, jsonSoS(position).toString(), StaticResources.QOS_2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

            if(stepDetect != -1){
                String stepDetectString = String.valueOf(stepDetect);
                pub(StaticResources.PEDOMETER_SENSOR_TOPIC, stepDetectString, StaticResources.QOS_0);
            }

            if(connectedToGatt){
                if(temp != null)
                    pub(StaticResources.TEMP_TOPIC, temp, StaticResources.QOS_0);
                if(humidity != null)
                    pub(StaticResources.HUMIDITY_TOPIC, humidity, StaticResources.QOS_0);
                if(pressure != null)
                    pub(StaticResources.PRESSURE_TOPIC, pressure, StaticResources.QOS_0);
                if(altitude != null)
                    pub(StaticResources.ALTITUDE_TOPIC, altitude, StaticResources.QOS_0);
                if(position != null)
                    pub(StaticResources.GPS_TOPIC, position, StaticResources.QOS_0);
            } else{
                if(tempValueSensor != -999){
                    String tempValueSensorString = String.valueOf(tempValueSensor);
                    pub(StaticResources.TEMP_SENSOR_TOPIC, tempValueSensorString, StaticResources.QOS_0);
                }
                if(humidityValueSensor != -1){
                    String humidityValueSensorString = String.valueOf(humidityValueSensor);
                    pub(StaticResources.HUMIDITY_SENSOR_TOPIC, humidityValueSensorString, StaticResources.QOS_0);
                }
                if(pressureValueSensor != 0){
                    String pressureValueSensorString = String.valueOf(pressureValueSensor);
                    pub(StaticResources.PRESSURE_SENSOR_TOPIC, pressureValueSensorString, StaticResources.QOS_0);
                }
                if(locationPermission && altitudeValueSensor != 9999){
                    String altitudeValueSensorString = String.valueOf(altitudeValueSensor);
                    pub(StaticResources.ALTITUDE_SENSOR_TOPIC, altitudeValueSensorString, StaticResources.QOS_0);
                }
                if(locationPermission && latitude != 360 && longitude != 360){
                    String positionString = String.valueOf(latitude);
                    pub(StaticResources.LATITUDE_TOPIC, positionString, StaticResources.QOS_0);
                    positionString = String.valueOf(longitude);
                    pub(StaticResources.LONGITUDE_TOPIC, positionString, StaticResources.QOS_0);
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String broadcastReceived = intent.getAction();
            Log.d(TAG, "The received Broadcast is: " + broadcastReceived);

            assert broadcastReceived != null;
            if(broadcastReceived.equals(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ)){
                switch (intent.getStringExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED)){
                    case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                        temp = intent.getStringExtra(StaticResources.EXTRA_TEMP_VALUE);
                        break;
                    case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                        heartBeat = intent.getStringExtra(StaticResources.EXTRA_HEART_VALUE);
                        break;
                    case  StaticResources.ESP32_HUMIDITY_CHARACTERISTIC:
                        humidity = intent.getStringExtra(StaticResources.EXTRA_HUMIDITY_VALUE);
                        break;
                    case StaticResources.ESP32_PRESSURE_CHARACTERISTIC:
                        pressure = intent.getStringExtra(StaticResources.EXTRA_PRESSURE_VALUE);
                        break;
                    case StaticResources.ESP32_ALTITUDE_CHARACTERISTIC:
                        altitude = intent.getStringExtra(StaticResources.EXTRA_ALTITUDE_VALUE);
                        break;
                }
            }
        }
    };

    //this handles the publishing of the messages
    void pub(String topic, String payload, int QoS){
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setQos(QoS);
            client.publish(topic, message);
            Toast.makeText(this.getApplicationContext(),"Data sent", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //this creates a JSON object containing SoS info to be published
    private JSONObject jsonSoS(String position) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("position", position);
        jsonObject.put("sender", StaticResources.ESP32_ADDRESS);
        return jsonObject;
    }

}