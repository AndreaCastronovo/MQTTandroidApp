package org.acastronovo.tesi;

import java.nio.charset.StandardCharsets;

/**
 *@author Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class SensorModel {

    byte[] temp;
    byte[] humidity;
    byte[] pressure;
    byte[] altitude;
    byte[] gps;
    byte[] heart;
    byte[] sos;

    SensorModel(String temp, String humidity, String pressure, String altitude){
        this.temp = temp.getBytes(StandardCharsets.UTF_8);
        this.humidity = temp.getBytes(StandardCharsets.UTF_8);
        this.pressure = temp.getBytes(StandardCharsets.UTF_8);
        this.altitude = temp.getBytes(StandardCharsets.UTF_8);
    }

    public void setGps(byte[] gps) {
        this.gps = gps;
    }

    public void setHeart(byte[] heart) {
        this.heart = heart;
    }

    public void setSos(byte[] sos) {
        this.sos = sos;
    }

    public byte[] getTemp() {
        return this.temp;
    }

    public byte[] getHumidity() {
        return this.humidity;
    }

    public byte[] getPressure() {
        return this.pressure;
    }

    public byte[] getAltitude() {
        return this.altitude;
    }

    public byte[] getGps() {
        return this.gps;
    }

    public byte[] getHeart() {
        return this.heart;
    }

    public byte[] getSos() {
        return this.sos;
    }
}
