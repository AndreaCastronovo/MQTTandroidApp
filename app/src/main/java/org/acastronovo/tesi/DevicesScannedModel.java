package org.acastronovo.tesi;

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */


public class DevicesScannedModel {

    private String deviceName;
    private String address;
    private int bondState;
    private int rssi;

    //constructor
    DevicesScannedModel(String deviceName, String address, int rssi, int bondState) {
        if(deviceName == null){
            this.deviceName = "Unknown Device";
        }
        else{
            this.deviceName = deviceName;
        }
        this.address = address;
        this.rssi = rssi;
        this.bondState = bondState;
    }

    //this associates the RSSI value to the address respectively
    DevicesScannedModel(String address, int rssi){
        this.address = address;
        this.rssi = rssi;
    }

    String getDeviceName(){
        return this.deviceName;
    }

    String getBleAddress(){
        return this.address;
    }

    int getBondState(){
        return this.bondState;
    }

    int getRssi(){
        return this.rssi;
    }

    void setDeviceName(String deviceName){
        if(deviceName == null){
            this.deviceName = "Unknown";
        }
        else{
            this.deviceName = deviceName;
        }
    }

    void setAddress(String address){
        this.address = address;
    }

    void setBondState(int bondState){
        this.bondState = bondState;
    }

    void setRssi(int rssi){
        this.rssi = rssi;
    }

    public String toString(){
        return  '\n' + "Address: " + this.getBleAddress() + '\n'
                + "Device name: " + this.getDeviceName() + '\n'
                + "Bound State: " + this.getBondState() + '\n'
                + "Rssi: " + this.getRssi();
    }


}