/*Cristian D'Ortona
    Tesi di Laurea
*/
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>
#include <Wire.h>


Adafruit_BME280 bme;

#define SEALEVELPRESSURE_HPA (1013.25)

BLEServer* pServer = NULL;
BLECharacteristic* tempCharacteristic = NULL;
BLECharacteristic* heartCharacteristic = NULL;
BLECharacteristic* brightnessCharacteristic = NULL;
BLECharacteristic* pressureCharacteristic = NULL;
BLECharacteristic* altitudeCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint32_t value = 0;

#define BME280_SERVICE        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define HEART_SERVICE "9c100603-509a-42ae-a22a-f5ba9e9c9d94"
#define CHARACTERISTIC_TEMP "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHARACTERISTIC_HEART "5ebad8b8-8128-11eb-8dcd-0242ac130003"
#define CHARACTERISTIC_BRIGHTNESS "3935a44c-81c3-11eb-8dcd-0242ac130003"
#define CHARACTERISTIC_PRESSURE "605958dd-bf4b-4d0b-a237-3875db31466c"
#define CHARACTERISTIC_ALTITUDE "f28c3ced-d0ce-41ab-87d4-23cc4f0dc9df"


class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("Connected");
      /*int i=0;
      while(i<3){
        digitalWrite(1, LOW);
        delay(500);
        digitalWrite(0, HIGH);
        delay(500);
        i++;
      }*/
    };
    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("Disconnected");
    }
};



void setup() {
  Serial.begin(115200);

  // Create the BLE Device
  BLEDevice::init("ESP32");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BME280 and heart Service
  BLEService *bme280Service = pServer->createService(BME280_SERVICE);
  BLEService *heartService = pServer->createService(HEART_SERVICE);


  // Create a BLE temp Characteristic
  tempCharacteristic = bme280Service->createCharacteristic(
                      CHARACTERISTIC_TEMP,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );


  // Create a BLE Descriptor for the temp characteristic
  tempCharacteristic->addDescriptor(new BLE2902());

  //crete BLE heart Characteristic
  heartCharacteristic = heartService->createCharacteristic(
                        CHARACTERISTIC_HEART,
                        BLECharacteristic::PROPERTY_READ |
                        BLECharacteristic::PROPERTY_NOTIFY
                        );

  //create a BLE descriptor for the heart characteristic
  //0x2902 is the standard for Client Characteristic configuration Descriptor (CCCD)
  heartCharacteristic->addDescriptor(new BLE2902());

  //create a BLE brightness characteristic
  brightnessCharacteristic = bme280Service->createCharacteristic(
                            CHARACTERISTIC_BRIGHTNESS,
                            BLECharacteristic::PROPERTY_READ |
                            BLECharacteristic::PROPERTY_NOTIFY
                            );

  brightnessCharacteristic->addDescriptor(new BLE2902());

   //crete BLE pressure Characteristic
  pressureCharacteristic = bme280Service->createCharacteristic(
                        CHARACTERISTIC_PRESSURE,
                        BLECharacteristic::PROPERTY_READ |
                        BLECharacteristic::PROPERTY_NOTIFY
                        );

  //create a BLE descriptor for the heart characteristic
  //0x2902 is the standard for Client Characteristic configuration Descriptor (CCCD)
  pressureCharacteristic->addDescriptor(new BLE2902());

 // Create a BLE altitude Characteristic
  altitudeCharacteristic = bme280Service->createCharacteristic(
                      CHARACTERISTIC_ALTITUDE,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );


  // Create a BLE Descriptor for the temp characteristic
  altitudeCharacteristic->addDescriptor(new BLE2902());


  // Start the service
  bme280Service->start();
  heartService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(BME280_SERVICE);
  pAdvertising->addServiceUUID(HEART_SERVICE);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();
  Serial.println("Waiting a client connection to notify...");

  //BME280
  bool  status = bme.begin(0x76);
  Serial.print("BME280 status is:");
  Serial.println(status);
  Serial.println(bme.sensorID(), 16);
  if(!status){
    Serial.println("Could not find a valid BME280 sensor, check wiring, address, sensor ID!");
  }
}

void loop() {
    // notify changed value

    if(deviceConnected){
      bme280Reading();
      delay(3000);
    }

    //this is used in order to make the ESP32 advertise again once it disconnects from the Central
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
        delay(500);
        pServer->startAdvertising(); // restart advertising
        Serial.println("start advertising");
        oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        oldDeviceConnected = deviceConnected;
    }
}

void bme280Reading(){

  float temp = bme.readTemperature();
  std::string tempReading = String(temp, 2).c_str();
  tempCharacteristic->setValue(tempReading);
  tempCharacteristic->notify();
  Serial.print("Temperature = ");
  Serial.print(temp);
  Serial.println(" *C");

  float humidity = bme.readHumidity();
  std::string humidityReading = String(humidity, 2).c_str();
  brightnessCharacteristic->setValue(humidityReading);
  brightnessCharacteristic->notify();
  Serial.print("Humidity = ");
  Serial.print(humidity);
  Serial.println(" %");

  float pressure = bme.readPressure();
  std::string pressureReading = String(pressure, 2).c_str();
  pressureCharacteristic->setValue(pressureReading);
  pressureCharacteristic->notify();
  Serial.print("Humidity = ");
  Serial.print(pressure);
  Serial.println("hPa");

  float altitude = getAltitude(SEALEVELPRESSURE_HPA);
  std::string altitudeReading = String(altitude, 2).c_str();
  altitudeCharacteristic->setValue(altitudeReading);
  altitudeCharacteristic->notify();
  Serial.print("Altitude = ");
  Serial.print(altitude);
  Serial.println("m");
}


//page 16
//https://cdn-shop.adafruit.com/datasheets/BST-BMP180-DS000-09.pdf
float getAltitude(float seaLevel){
  float atmospheric = bme.readPressure() / 100.0F;
  return 44330.0 * (1.0 - pow(atmospheric / seaLevel, 0.1903));
}