package org.acastronovo.tesi;

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

final class StaticResources {

    private static final String PACKAGE_NAME = "com.acastronovo.tesi";

    //ESP32 info
    static final String ESP32_ADDRESS = "30:AE:A4:F5:88:6E";
    static final String ESP32_BME280_SERVICE = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    static final String ESP32_HEART_SERVICE = "9c100603-509a-42ae-a22a-f5ba9e9c9d94";
    static final String ESP32_TEMP_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    static final String ESP32_HEARTH_CHARACTERISTIC = "5ebad8b8-8128-11eb-8dcd-0242ac130003";
    static final String ESP32_HUMIDITY_CHARACTERISTIC = "3935a44c-81c3-11eb-8dcd-0242ac130003";
    static final String ESP32_PRESSURE_CHARACTERISTIC = "605958dd-bf4b-4d0b-a237-3875db31466c";
    static final String ESP32_ALTITUDE_CHARACTERISTIC = "f28c3ced-d0ce-41ab-87d4-23cc4f0dc9df";
    //client characteristic configuration which is used to by the client in order to configure the
    //Indicate or Notify property of that specified characteristic
    static final String ESP32_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    //Broadcaster Intent Actions what will be received by the broadcast receiver
    static final String ACTION_CONNECTION_STATE = PACKAGE_NAME + ".connectionState";
    //static final String ACTION_CHARACTERISTIC_CHANGED = PACKAGE_NAME + ".characteristicChanged";
    static final String ACTION_CHARACTERISTIC_CHANGED_READ = PACKAGE_NAME + "characteristicChangedRead";

    //Keys for the putExtra method
    static final String EXTRA_STATE_CONNECTION = PACKAGE_NAME + "keyConnection";
    static final String EXTRA_TEMP_VALUE = PACKAGE_NAME + "tempValue";
    static final String EXTRA_HEART_VALUE = PACKAGE_NAME + "heartValue";
    static final String EXTRA_HUMIDITY_VALUE = PACKAGE_NAME + "humidityValue";
    static final String EXTRA_PRESSURE_VALUE = PACKAGE_NAME + "pressureValue";
    static final String EXTRA_ALTITUDE_VALUE = PACKAGE_NAME + "altitudeValue";
    static final String EXTRA_TEMP_BYTE_VALUE = PACKAGE_NAME + "tempValueByte";
    static final String EXTRA_HEART_BYTE_VALUE = PACKAGE_NAME + "heartValueByte";
    static final String EXTRA_HUMIDITY_BYTE_VALUE = PACKAGE_NAME + "humidityValueByte";
    static final String EXTRA_PRESSURE_BYTE_VALUE = PACKAGE_NAME + "pressureValueByte";
    static final String EXTRA_ALTITUDE_BYTE_VALUE = PACKAGE_NAME + "altitudeValueByte";
    static final String EXTRA_CHARACTERISTIC_CHANGED = PACKAGE_NAME + "characteristicToBeNotified";

    //values for the putExtra method for BLE connectivity
    static final String STATE_CONNECTED = "Connected";
    static final String STATE_DISCONNECTED = "Disconnected";
    static final String STATE_CONNECTING = "Connecting...";

    //MQTT Connection
    static final String LWT_MESSAGE = "Last Will and Testament from " + ESP32_ADDRESS;
    static final String LWT_TOPIC = ESP32_ADDRESS + "/lastWill";
    static final String TEMP_TOPIC = ESP32_ADDRESS + "/temp";
    static final String HUMIDITY_TOPIC = ESP32_ADDRESS + "/humidity";
    static final String PRESSURE_TOPIC = ESP32_ADDRESS + "/pressure";
    static final String ALTITUDE_TOPIC = ESP32_ADDRESS + "/altitude";
    static final String HEART_TOPIC = ESP32_ADDRESS + "/heart";
    static final String SOS_TOPIC =  "emergency/sos";
    static final String GPS_TOPIC = ESP32_ADDRESS + "/gps";
    static final String EXTRA_SOS_FLAG = PACKAGE_NAME + "sos_flag";
    static final String EXTRA_LOCATION = PACKAGE_NAME + "location_mqtt";
    static final int QOS_0 = 0;
    static final int QOS_1 = 1;
    static final int QOS_2 = 2;

    //General static Resources
    static final String WEB_PAGE = PACKAGE_NAME + ".webUrl";
    static final String EXTRA_CHOOSEN_ADDRESS = PACKAGE_NAME + ".address";
    static final String EXTRA_CHOOSEN_NAME = PACKAGE_NAME + ".name";
    static final int REQUEST_CODE_SCAN_ACTIVITY = 1;
    static final int REQUEST_CODE_CHANGE_PROFILE_PIC = 2;
}