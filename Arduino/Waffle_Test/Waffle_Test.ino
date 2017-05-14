#include <ArduinoJson.h>

// Memory pool for JSON object tree.
// Inside the brackets, 200 is the size of the pool in bytes.
// If the JSON object is more complex, you need to increase that value.
StaticJsonBuffer<200> jsonBuffer;
JsonObject& json = jsonBuffer.createObject();

int ledPin = 13;      // select the pin for the LED
int sensorValue = 0;  // variable to store the value coming from the sensor
boolean flag = true;

void setup() {
  pinMode(ledPin, OUTPUT);
  // initialize the serial communication:
  Serial.begin(9600);
}

void loop() {
  // read the value from the sensor:
  sensorValue = analogRead(A0);
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

