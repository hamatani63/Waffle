// NOTICE: Whether this program can run depends on the power supply capacity of the Android smartphone.
// 注意： このプログラムを実行できるかどうかは、Androidスマートフォンの給電能力に依存します。

#include <ArduinoJson.h>
#define heaterSelPin 15

// Memory pool for JSON object tree.
// Inside the brackets, 200 is the size of the pool in bytes.
// If the JSON object is more complex, you need to increase that value.
StaticJsonBuffer<200> jsonBuffer;
JsonObject& json = jsonBuffer.createObject();

int ledPin = 13;      // select the pin for the LED
int sensorValue = 0;  // variable to store the value coming from the sensor
boolean flag = true;

void setup() {
  Serial.begin(9600);
  pinMode(heaterSelPin,OUTPUT);   // set the heaterSelPin as digital output.
  digitalWrite(heaterSelPin,LOW); // Start to heat the sensor
}

void loop() {
  sensorValue = 1024 - analogRead(A0);
    
  json["A0"] = sensorValue;
  json.printTo(Serial);
  Serial.println();
    
  blink(ledPin);
}

void blink(int pin){
  if(flag){
    digitalWrite(pin, HIGH);
    flag = false;
  } else {
    digitalWrite(pin, LOW);
    flag = true;
  }
  delay(16);
}
