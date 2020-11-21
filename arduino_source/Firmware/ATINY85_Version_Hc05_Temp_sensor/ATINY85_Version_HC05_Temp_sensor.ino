/* ATINY85 Version.
 *  
 * Receieves data from a DHT11 Sensor and sends
 * it via a bluetooth connection.
 * 
 * BMP0180 is not - and will not - be supported...Due to it has no more pins and memory constraits :-(
 * 
 * Lesson learned here: 8kB of Ram is not much when programming in 6502 assembler or Basic. It is
 * even less, when programming in C... => A way to save a big amount of ram is: Use the 'F' macro for
 * string literals as often as possible and do not use C's String- library.
 * 
 * Issues: Voltage check string is not shown on device.....
 * 
 */
#define FIRMWARE_VERSION "V1.0.0B for ATINY85 MC"
#define HARDWARE_STATUS  "ATIN85: Air pressure data not supported"

#include <SoftwareSerial.h>
#include <dht11.h>

// Pre defines
#define RX 3
#define TX 4
#define DHT11PIN 0

// Dht11
dht11 DHT11;
#define VOLTAGE_CHECK_PIN       1
#define VOLTAGE_LOW_WARNING_PIN 2

// Serial connection
SoftwareSerial mySerial(RX,TX);
int chk;

// Underspannung?
String voltageStatus; 

void setup() {
  
  // Nothing to do for dht11

  // Voltage check pins
  pinMode(VOLTAGE_CHECK_PIN,INPUT_PULLUP);
  pinMode(VOLTAGE_LOW_WARNING_PIN,OUTPUT);

  // Setup serial connection
  pinMode(RX,INPUT);
  pinMode(TX,OUTPUT);
  pinMode(DHT11PIN,INPUT);
  
  mySerial.begin(9600);
}

void loop() {
  // Voltage check
  int voltageCheck=digitalRead(VOLTAGE_CHECK_PIN);
  if (voltageCheck==HIGH){
    voltageStatus=F("ok");
    digitalWrite(VOLTAGE_LOW_WARNING_PIN,LOW);
  } else {
    voltageStatus=F("Low");
    digitalWrite(VOLTAGE_LOW_WARNING_PIN,HIGH);
  }
  // Bmp0180 mot supported yet
  float bmpPressure_MB,bmpAltitude_m;
    
  // Get sensor data and send serially...
  int chk = DHT11.read(DHT11PIN);
  float humidity=DHT11.humidity;
  float temp_DEGREES=DHT11.temperature;
  float farenheit=temp_DEGREES*1.8+32;

  // This string was to much for the ATINY85's 8kB, so i disapeared and was never send....
  // String temperatureData="{\"firmware_version\":\""+fwVersion+"\",\"hardware_status\":\""+hwStatus+"\",\"voltage_status\":\""+voltageStatus+"\",\"temperature_degrees\":"+(float)temp_DEGREES+",\"temperature_farenheit\":"+farenheit+",\"huminidity\":"+humidity+",\"pressure\":\"-\",\"altitude\":"+bmpAltitude_m+"}";
  //
  // This is the workaround for the problem mentioned above. Esentially one should avoid the C- String which
  // uses to much memory. When using String- literals, one should use the F- macro 
  //
  // See also: https://forum.arduino.cc/index.php?topic=349383.0
  //
  mySerial.print("{\"firmware_version\":");
  mySerial.print("\"");
  mySerial.print(FIRMWARE_VERSION);
  mySerial.print("\"");
  mySerial.print(",");


  mySerial.print("\"hardware_status\":");
  mySerial.print("\"");
  mySerial.print(HARDWARE_STATUS);
  mySerial.print("\"");
  mySerial.print(",");

  mySerial.print("\"voltage_status\":");
  mySerial.print("\"");
  mySerial.print(voltageStatus);
  mySerial.print("\"");
  mySerial.print(",");
  
  mySerial.print("\"temperature_degrees\":");
  mySerial.print(temp_DEGREES);
  mySerial.print(",");
  
  mySerial.println("\"temperature_farenheit\":");
  mySerial.print(farenheit);
  mySerial.print(",");
  
  mySerial.println("\"huminidity\":");
  mySerial.print(humidity);
  
  mySerial.println("}");
  
  delay(250);
}
