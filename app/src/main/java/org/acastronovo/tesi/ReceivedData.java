package org.acastronovo.tesi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.Token;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.lang.String;


public class ReceivedData extends AppCompatActivity {

    //UI
    TextView temperature;
    TextView heartbeat;
    TextView humidity;
    TextView position;
    TextView altitude;
    TextView pressure;
    TextView pedometer;
    TextView calories;

    MqttAndroidClient client;
    String TAG = "ReceivedData";
    private final String serverUri = "tcp://192.168.1.2:1883";
    private final String user = "andrea";
    private final String pwd = "1234";
    private MemoryPersistence persistance;

    private String latitude = "";
    private String longitude = "";

    private String topic;
    int qos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_data);

        //UI
        temperature = findViewById(R.id.temperature);
        heartbeat = findViewById(R.id.heartbeat);
        humidity = findViewById(R.id.humidity);
        position = findViewById(R.id.position);
        altitude = findViewById(R.id.altitude);
        pressure = findViewById(R.id.pressure);
        pedometer = findViewById(R.id.pedometer);
        calories = findViewById(R.id.calories);

        connect();

    }

    private void connect () {
        String clientId = MqttClient.generateClientId();

        try {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setUserName(user);
            mqttConnectOptions.setPassword(pwd.toCharArray());
            client = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
            IMqttToken token = client.connect(mqttConnectOptions);

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    //Subscribe all topic to view messages
                    createSub(client);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                }
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void createSub(MqttAndroidClient client){
        try {

            client.subscribe(StaticResources.PEDOMETER_SENSOR_TOPIC, qos);
            //Subscribe topic from bluetooth
            client.subscribe(StaticResources.TEMP_TOPIC,qos);
            client.subscribe(StaticResources.HUMIDITY_TOPIC,qos);
            client.subscribe(StaticResources.PRESSURE_TOPIC, qos);
            client.subscribe(StaticResources.ALTITUDE_TOPIC,qos);
            client.subscribe(StaticResources.GPS_TOPIC,qos);
            //Subscribe topic from sensorsDevice
            client.subscribe(StaticResources.TEMP_SENSOR_TOPIC,qos);
            client.subscribe(StaticResources.HUMIDITY_SENSOR_TOPIC,qos);
            client.subscribe(StaticResources.PRESSURE_SENSOR_TOPIC, qos);
            client.subscribe(StaticResources.ALTITUDE_SENSOR_TOPIC,qos);
            client.subscribe(StaticResources.LATITUDE_TOPIC,qos);
            client.subscribe(StaticResources.LONGITUDE_TOPIC,qos);

            //Start setCallback for client
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "Connection Lost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "Message Arrived");
                    String messageString = new String(message.getPayload());
                    messageHandler(topic, messageString);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Delivery Complete");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void messageHandler(String topic, String message){
        switch(topic){
            case StaticResources.PEDOMETER_SENSOR_TOPIC:
                pedometer.setText("Pedometer: " + message + " Steps");
                return;

            case StaticResources.TEMP_TOPIC:
                temperature.setText("Temperature: " + message + "°C");
                return;

            case StaticResources.HUMIDITY_TOPIC:
                humidity.setText("Humidity: " + message + "%");
                return;

            case StaticResources.PRESSURE_TOPIC:
                pressure.setText("Pressure: " + message + " hPa");
                return;

            case StaticResources.ALTITUDE_TOPIC:
                altitude.setText("Altitude: " + message + " m");
                return;

            case StaticResources.GPS_TOPIC:
                position.setText("Position: " + message);
                return;

            case StaticResources.TEMP_SENSOR_TOPIC:
                temperature.setText("Temperature: " + message + "°C");
                return;

            case StaticResources.HUMIDITY_SENSOR_TOPIC:
                humidity.setText("Humidity: " + message + "%");
                return;

            case StaticResources.PRESSURE_SENSOR_TOPIC:
                pressure.setText("Pressure: " + message + " hPa");
                return;

            case StaticResources.ALTITUDE_SENSOR_TOPIC:
                altitude.setText("Altitude: " + message + " m");
                return;

            case StaticResources.LATITUDE_TOPIC:
                if(!longitude.equals("")){
                    position.setText("Position: " + message + ", " + longitude);
                    longitude = "";
                }else{
                    latitude = message;
                }
                return;

            case StaticResources.LONGITUDE_TOPIC:
                if(!latitude.equals("")){
                    position.setText("Position: " + latitude + ", " + message);
                    latitude = "";
                }else{
                    longitude = message;
                }
                return;

        }
    }

}