#include <BlueDot_BME280.h>

/*
 * Bluetooth Termometer/ Luftfeuchtemesser
 *
 * Arbeitet mit HC05 und dem bme0280 Luftdruck/ Temperatursensor.
 * 
 * Verwendet die folgenden Bibliotheken für den bme0280:
 * 
 *    https://github.com/adafruit/Adafruit_Sensor/releases/tag/1.1.4 (Wird von der folgenden Bibliothek benötigt):
 * 
 * Benötigt Androidseitig diese App:
 * ./0_Vorlagen/Networking/BluetoothConector/app/src/main/java/berthold/bluetoothconector/MainActivity.java
 *
 */
#define FIRMWARE_VERSION "V1.0.1 for Tensy2.0 MC. Chipset:BME0280 // Long refresh"

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>
#include <SoftwareSerial.h>
 
// Serial data input pin. 
// Für beliebige Erweiterungen....
#define SERIAL_IN_PIN 10   

#define VOLTAGE_CHECK_PIN PIN_B2
#define VOLTAGE_LOW_WARNING_PIN PIN_B1
 
// BME 0280 Luftdruck/ Temperatur Sensor
Adafruit_BME280 bme;

// HC05   <->   Tensy
//  RX          TX (PIN 8)
//  TX          RX (Pin 7)
SoftwareSerial BT(7,8); 

//
int refreshRate=1500;

// Da kommen die temperatur/ Luftdruck Daten hin
String temperatureData;

// Underspannung?
String voltageStatus;

// Der Status der Bauteile steht hier beschrieben.
String hwStatus;  

// Aktuelle Version der Firmware 
String fwVersion=FIRMWARE_VERSION;

/*
 * Einmal
 */
void setup()  
{
  // Setup HC05:
  pinMode(8, OUTPUT);
  pinMode(7,INPUT);
  BT.begin(9600);

  // Setup bme0280
  Serial.begin(9600);
  if (!bme.begin(0x76)){
    Serial.println("bme0280_FAIL");
    hwStatus="BME0280_FAIL";
  } else {
    hwStatus=("BME0280_OK");
    Serial.println("BME0280_OK");
  }

  // Voltage check pin
  pinMode(VOLTAGE_CHECK_PIN,INPUT_PULLUP);
  pinMode(VOLTAGE_LOW_WARNING_PIN,OUTPUT);
}

/*
 * Forever, forever and forever
 */
void loop() 
{ 
  // BME0280 lesen
  float temp_C=bme.readTemperature();       
  float farenheit=temp_C*1.8+32;
  float pressure_MB=bme.readPressure();   
  float altitude_m=bme.readAltitude(1013.25);       // Höhe über Normal Null auf Basis des Standartdruck auf Mehreshöhe (1013.25 MB).
  float humidity=bme.readHumidity();

  // Vom seriellen Eingabe-pin lesen, hier zum beispiel, der in
  // dieser Hardware Konfiguration, übeflüssige DHT11...
  //int chk=DHT11.read(DHT11PIN);
  //float humidity=DHT11.humidity;

  // Voltage check
  int voltageCheck=digitalRead(VOLTAGE_CHECK_PIN);
  if (voltageCheck==HIGH){
    voltageStatus="ok";
    digitalWrite(VOLTAGE_LOW_WARNING_PIN,LOW);
  } else {
    voltageStatus="Low";
    digitalWrite(VOLTAGE_LOW_WARNING_PIN,HIGH);
  }
  
  // String mit bme0280 Daten zusammensetzen und als JSON über Bluetooth schicken.
  temperatureData=temperatureData+"{\"firmware_version\":\""+fwVersion+"\",\"hardware_status\":\""+hwStatus+"\",\"voltage_status\":\""+voltageStatus+"\",\"temperature_degrees\":"+(float)temp_C+",\"temperature_farenheit\":"+(float)farenheit+",\"huminidity\":"+(float)humidity+",\"pressure\":"+(float)pressure_MB+",\"altitude\":"+(float)altitude_m+"}";
  BT.println(temperatureData);
  Serial.println(temperatureData);
  temperatureData="";

 // Kurz warten
  delay(refreshRate); 
}
