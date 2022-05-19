package org.acastronovo.tesi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

class ConnectToGattServer {

    private Context mContext;
    private String TAG = "ConnectToGattServer";

    private BluetoothAdapter bluetoothAdapter;

    //GATT variables
    private BluetoothGatt gatt;
    private BluetoothGattService gattBME280Service;
    private BluetoothGattService gattHeartService;
    private List<BluetoothGattService> gattServicesList;
    private BluetoothGattCharacteristic gattCharacteristicTemp;
    private BluetoothGattCharacteristic gattCharacteristicHearth;
    private BluetoothGattCharacteristic gattCharacteristicHumidity;
    private BluetoothGattCharacteristic gattCharacteristicPressure;
    private BluetoothGattCharacteristic gattCharacteristicAltitude;
    private List<BluetoothGattCharacteristic> gattCharacteristicsList;

    //Constructor which initializes the dependencies needed for the connection to the GATT server of the remote device(ESP32)
    ConnectToGattServer(String deviceAddress, Context context) {
        mContext = context;
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        //connectToGatt(deviceAddress);
    }

    //method called whenever the "connect" button is pressed
    void connectToGatt(String deviceAddress) {
        //since I know which is the address of the device I want to connect to, I use the bluetoothAdapter object
        //in order to get to connect to the remote BLE advertiser
        try {
            BluetoothDevice bleAdvertiser = bluetoothAdapter.getRemoteDevice(deviceAddress);
            Log.d("connectToGatt", "found device with the following MAC address: " + deviceAddress);
            //this creates a bond with the remote device once the connection is set
            //bleAdvertiser.createBond();
            //this method is gonna connect to the remote GATT server and the result will be handled by the callBack method
            //the auto-connect is set to true, which means the phone will automatically connect to the remote device when nearby
            //the auto-connect only works if the device is bounded to the gatt server, hence only if there is a secure connection between the two parties
            gatt = bleAdvertiser.connectGatt(mContext, true, gattCallBack);
        } catch (IllegalArgumentException e) {
            e.getStackTrace();
            Log.e(TAG, "the address is not associated to any BLE advertiser nearby");
            Toast.makeText(mContext, "Error, the address doesn't match any BLE advertiser nearby", Toast.LENGTH_LONG).show();
        }
    }

    //this method is invoked whenever there is the necessity to disconnect from the GATT server
    void disconnectGattServer() {
        if (gatt != null) {
            Log.d("disconnectGattServer", "GATT server is disconnecting...");
            Toast.makeText(mContext, "Disconnecting ...", Toast.LENGTH_SHORT).show();
            gatt.disconnect();
            gatt.close();
        }
    }

    //anonymous inner class
    private final BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "There is a problem connecting to the remote peripheral");
                Toast.makeText(mContext, "Error occurred, please try again", Toast.LENGTH_SHORT).show();
                updateBroadcast(StaticResources.STATE_DISCONNECTED, StaticResources.ACTION_CONNECTION_STATE);
                return;
            }

            //I'm creating an intent which contains the intent-filter used to identify the connection state of the GATT server
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "connected to Bluetooth successfully");
                discoverServicesDelay(gatt);
                updateBroadcast(StaticResources.STATE_CONNECTED, StaticResources.ACTION_CONNECTION_STATE);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "disconnecting GATT server");
                disconnectGattServer();
                updateBroadcast(StaticResources.STATE_DISCONNECTED, StaticResources.ACTION_CONNECTION_STATE);
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                Log.d(TAG, "Connecting...");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "There was a problem with discovering the peripheral's services");
                Toast.makeText(mContext, "The remote device appears to be malfunctioning", Toast.LENGTH_SHORT).show();
                //Forcing to disconnect in case there is a problem with finding the services
                updateBroadcast(StaticResources.STATE_DISCONNECTED, StaticResources.ACTION_CONNECTION_STATE);
                return;
            }

            gattServicesList = gatt.getServices();
            gattBME280Service = gatt.getService(UUID.fromString(StaticResources.ESP32_BME280_SERVICE));
            gattHeartService = gatt.getService(UUID.fromString(StaticResources.ESP32_HEART_SERVICE));
            gattCharacteristicsList = gattBME280Service.getCharacteristics();
            gattCharacteristicHearth = gattHeartService.getCharacteristic(UUID.fromString(StaticResources.ESP32_HEARTH_CHARACTERISTIC));
            gattCharacteristicsList.add(gattCharacteristicHearth);

            //debug
            for (int i = 1; i < gattServicesList.size(); i++) {
                Log.i(TAG, "I'm printing the list of the services:" + gattServicesList.get(i).getUuid().toString() + '\n');
            }

            for (int i = 0; i < gattCharacteristicsList.size(); i++) {
                switch (gattCharacteristicsList.get(i).getUuid().toString()) {
                    case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                        gattCharacteristicTemp = gattCharacteristicsList.get(i);
                        Log.d(TAG, "charactersitic has been assigned correctly, " +
                                +'\n' + "UUID: " + gattCharacteristicsList.get(i).getUuid());

                        break;

                    case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                        gattCharacteristicHearth = gattCharacteristicsList.get(i);
                        Log.d(TAG, "characteristic has been assigned correctly, " +
                                +'\n' + "UUID: " + gattCharacteristicsList.get(i).getUuid());

                        break;

                    case StaticResources.ESP32_HUMIDITY_CHARACTERISTIC:
                        gattCharacteristicHumidity = gattCharacteristicsList.get(i);
                        Log.d(TAG, "characteristic has been assigned correctly, " +
                                +'\n' + "UUID: " + gattCharacteristicsList.get(i).getUuid());

                        break;

                    case StaticResources.ESP32_PRESSURE_CHARACTERISTIC:
                        gattCharacteristicPressure = gattCharacteristicsList.get(i);
                        Log.d(TAG, "characteristic has been assigned correctly, " +
                                +'\n' + "UUID: " + gattCharacteristicsList.get(i).getUuid());
                        break;

                    case StaticResources.ESP32_ALTITUDE_CHARACTERISTIC:
                        gattCharacteristicAltitude = gattCharacteristicsList.get(i);
                        Log.d(TAG, "characteristic has been assigned correctly, " +
                                +'\n' + "UUID: " + gattCharacteristicsList.get(i).getUuid());
                        break;

                    default:
                        //this happens when there is a characteristic in the service that isn't part of the predefined ones
                        Log.d(TAG, "Characteristic not listed in the predefined ones"
                                + '\n' + "UUID: " + gattCharacteristicsList.get(i).getUuid());
                        break;
                }
            }
            setCharacteristicNotification();
        }

        //this method works if the GATT server has one or more characteristic with the property NOTIFY or INDICATE
        //whenever the value of that characteristic changes, then the GATT server notifies the client through this method
        //so the new value of the characteristic can be read and the result of the reading will be caught by the callBack method onCharacteristicRead
        //this permits an asynchronous communication between the server and the client
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            //I might have to implement a thread here so that multiple sensor values can be processed asynchronously
            sensorValueBroadcast(characteristic);
        }

        //this is called whenever the descriptor of a characteristic is written
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Writing to the descriptor of the characteristic: " + descriptor.getCharacteristic().getUuid().toString() + " failed");
                Toast.makeText(mContext, "The notify property seems to not be working", Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "Characteristic: " + descriptor.getCharacteristic().getUuid().toString() + " notify() property -> ON");
            gatt.setCharacteristicNotification(descriptor.getCharacteristic(), true);
            setCharacteristicNotification();
        }
    };

    //background process which is going to cause the current thread to sleep for the specified time
    //it's used to avoid problems during connection
    private void discoverServicesDelay(BluetoothGatt gatt) {
        try {
            Thread.sleep(600);
            Log.d(TAG, "delay before discovering services...");
            gatt.discoverServices();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Could not find BLE services.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBroadcast(String value, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(StaticResources.EXTRA_STATE_CONNECTION, value);
        mContext.sendBroadcast(intent);
    }

    private void sensorValueBroadcast(BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ);
        switch (characteristic.getUuid().toString()) {
            case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                byte[] tempData = characteristic.getValue();
                String tempMessage = new String(tempData);
                intent.putExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED, characteristic.getUuid().toString());
                intent.putExtra(StaticResources.EXTRA_TEMP_VALUE, tempMessage + " °C");
                intent.putExtra(StaticResources.EXTRA_TEMP_BYTE_VALUE, tempData);
                mContext.sendBroadcast(intent);
                break;
            case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                byte[] heartData = characteristic.getValue();
                String heartMessage = new String(heartData);
                intent.putExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED, characteristic.getUuid().toString());
                intent.putExtra(StaticResources.EXTRA_HEART_VALUE, heartMessage + " bpm");
                intent.putExtra(StaticResources.EXTRA_HEART_BYTE_VALUE, heartData);
                mContext.sendBroadcast(intent);
                break;
            case StaticResources.ESP32_HUMIDITY_CHARACTERISTIC:
                byte[] humidityData = characteristic.getValue();
                String humidityMessage = new String(humidityData);
                intent.putExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED, characteristic.getUuid().toString());
                intent.putExtra(StaticResources.EXTRA_HUMIDITY_VALUE, humidityMessage + " %");
                intent.putExtra(StaticResources.EXTRA_TEMP_BYTE_VALUE, humidityData);
                intent.putExtra(StaticResources.EXTRA_HUMIDITY_BYTE_VALUE, humidityData);
                mContext.sendBroadcast(intent);
                break;
            case StaticResources.ESP32_PRESSURE_CHARACTERISTIC:
                byte[] pressureData = characteristic.getValue();
                String pressureMessage = new String(pressureData);
                intent.putExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED, characteristic.getUuid().toString());
                intent.putExtra(StaticResources.EXTRA_PRESSURE_VALUE, pressureMessage + " Pa");
                intent.putExtra(StaticResources.EXTRA_PRESSURE_BYTE_VALUE, pressureData);
                mContext.sendBroadcast(intent);
                break;
            case StaticResources.ESP32_ALTITUDE_CHARACTERISTIC:
                byte[] altitudeData = characteristic.getValue();
                String altitudeMessage = new String(altitudeData);
                intent.putExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED, characteristic.getUuid().toString());
                intent.putExtra(StaticResources.EXTRA_ALTITUDE_VALUE, altitudeMessage + " m");
                intent.putExtra(StaticResources.EXTRA_ALTITUDE_BYTE_VALUE, altitudeData);
                mContext.sendBroadcast(intent);
        }
    }

    //in order to use the notify property of the characteristic, a descriptor has been defined which lets the client
    //decide whether enabling the notify property of the GATT server characteristic or not
    // but setting CCCD value is the only way you can tell the API whether you are going to turn on notification
    private void setCharacteristicNotification(){

        for(int i=0; i<gattCharacteristicsList.size(); i++){
            int characteristicProperties = gattCharacteristicsList.get(i).getProperties();
            //this checks if the characteristic has the notify property h'0x10 -> b'10000
            if(((characteristicProperties>>4) ^ BluetoothGattCharacteristic.PROPERTY_NOTIFY>>4) == 0){
                //this checks if the specified characteristic notification property is already activated or not
                //if it's turned on then the descriptor has been written with the value h'0x01 -> b'0001
                if(!Arrays.equals(gattCharacteristicsList.get(i).getDescriptor(UUID.fromString(StaticResources.ESP32_DESCRIPTOR)).getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){
                    BluetoothGattDescriptor brightnessDescriptor = gattCharacteristicsList.get(i).getDescriptor(UUID.fromString(StaticResources.ESP32_DESCRIPTOR));
                    brightnessDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(brightnessDescriptor);
                    return;
                }
            }
        }
    }
}
