/*
 * Bluetooth Termometer/ Luftfeuchtemesser
 *
 * Arbeitet mit HC05 dem DHT11 Temperatur und Luftfeuchtemesser, und dem BMP0180 Luftdruck/ Temperatursensor.
 *
 * Benötigt Androidseitig diese App:
 * ./0_Vorlagen/Networking/BluetoothConector/app/src/main/java/berthold/bluetoothconector/MainActivity.java
 *
 */
#define FIRMWARE_VERSION "V1.0.1 for Tensy2.0 MC. Chipset:DHT11, BMP0180"

#include <Wire.h>
#include <Adafruit_BMP085.h>
#include <SoftwareSerial.h>
#include <dht11.h>
 
// DHT 11
dht11 DHT11;
#define DHT11PIN 10   

#define VOLTAGE_CHECK_PIN PIN_B2
#define VOLTAGE_LOW_WARNING_PIN PIN_B1
 
// BMP 0180 Luftdruck/ Temperatur Sensor
Adafruit_BMP085 bmp;

// HC05   <->   Tensy
//  RX          TX (PIN 8)
//  TX          RX (Pin 7)
SoftwareSerial BT(7,8); 

// Da kommen die temperatur/ Luftdruck Daten hin
String temperatureData;

// Underspannung?
String voltageStatus;
int voltageStatusCurrentState,voltageStatusLastState;

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

  // Setup BMP0180
  Serial.begin(9600);
  if (!bmp.begin()){
    Serial.println("BMP0180_FAIL");
    hwStatus="BMP0180_FAIL";
  } else {
    hwStatus=("BMP0180_OK");
    Serial.println("BMP0180_OK");
  }

  // To Do: DHT11 Check...

  // Voltage check pin
  //pinMode(VOLTAGE_CHECK_PIN,INPUT_PULLUP);
  pinMode(VOLTAGE_LOW_WARNING_PIN,OUTPUT);
}

/*
 * Forever, forever and forever
 */
void loop() 
{ 
  // BMP0180 lesen
  float bmpTemp_C=bmp.readTemperature();       
  float farenheit=bmpTemp_C*1.8+32;
  float bmpPressure_MB=bmp.readPressure();   
  float bmpAltitude_m=bmp.readAltitude();       // Höhe über Normal Null auf Basis des Standartdruck auf Mehreshöhe (1013.25 MB).

  // DHT11 lesen (da holen wir uns nur die Luftfeuchte)
  int chk=DHT11.read(DHT11PIN);
  float humidity=DHT11.humidity;

  // Voltage check
  voltageStatus=digitalRead(VOLTAGE_CHECK_PIN);
  if (voltageStatusCurrentState==LOW && voltageStatusLastState==HIGH){
    voltageStatus="ok";
    digitalWrite(VOLTAGE_LOW_WARNING_PIN,LOW);
  } else {
     if (voltageStatusCurrentState==HIGH && voltageStatusLastState==LOW){
      voltageStatus="Low";
      digitalWrite(VOLTAGE_LOW_WARNING_PIN,HIGH);
     }
  }
  
  // String mit BMP0180 Daten zusammensetzen und als JSON über Bluetooth schicken.
  temperatureData=temperatureData+"{\"firmware_version\":\""+fwVersion+"\",\"hardware_status\":\""+hwStatus+"\",\"voltage_status\":\""+voltageStatus+"\",\"temperature_degrees\":"+(float)bmpTemp_C+",\"temperature_farenheit\":"+farenheit+",\"huminidity\":"+humidity+",\"pressure\":"+(float)bmpPressure_MB+",\"altitude\":"+bmpAltitude_m+"}";
  BT.println(temperatureData);
  Serial.println(temperatureData);
  temperatureData="";
  voltageStatusLastState=voltageStatusCurrentState;

 // Kurz warten
  delay(500); 
}
