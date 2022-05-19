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

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class MqttService extends Service {

    String TAG = "MqttService";
    MqttAsyncClient client;
    //test variables
    private final String serverUri = "tcp://192.168.1.61:1883";
    private final String user = "cPanel";
    private final String pwd = "test";
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

        //client = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);


        try {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setUserName(user);
            mqttConnectOptions.setPassword(pwd.toCharArray());
            client = new MqttAsyncClient(serverUri, clientId, persistance);
            client.connect(mqttConnectOptions);
            try {
                Thread.sleep(5000);
            } catch (Exception e){
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

            client.connect(mqttConnectOptions);

        }  catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean sos_on = intent.getBooleanExtra(StaticResources.EXTRA_SOS_FLAG, false);
        String position = intent.getStringExtra(StaticResources.EXTRA_LOCATION);
        //this is checking if the user has fired the sos
        if(sos_on){
            try {
                pub(StaticResources.SOS_TOPIC, jsonSoS(position).toString(), StaticResources.QOS_2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
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