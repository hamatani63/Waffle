#include <ArduinoJson.h>
#define DHT11_PIN 0      // ADC0

// Memory pool for JSON object tree.
// Inside the brackets, 200 is the size of the pool in bytes.
// If the JSON object is more complex, you need to increase that value.
StaticJsonBuffer<200> jsonBuffer;
JsonObject& json = jsonBuffer.createObject();

int sensorValue = 0;

byte read_dht11_dat()
{
    byte i = 0;
    byte result=0;
    for(i=0; i< 8; i++){

        while(!(PINC & _BV(DHT11_PIN)));  // wait for 50us
        delayMicroseconds(30);

        if(PINC & _BV(DHT11_PIN))
        result |=(1<<(7-i));
        while((PINC & _BV(DHT11_PIN)));  // wait '1' finish
    }
    return result;
}

void setup()
{
    DDRC |= _BV(DHT11_PIN);
    PORTC |= _BV(DHT11_PIN);

    Serial.begin(9600);
    Serial.println("Ready");
}

void loop()
{
    byte dht11_dat[5];
    byte dht11_in;
    byte i;
    // start condition
    // 1. pull-down i/o pin from 18ms
    PORTC &= ~_BV(DHT11_PIN);
    delay(18);
    PORTC |= _BV(DHT11_PIN);
    delayMicroseconds(40);

    DDRC &= ~_BV(DHT11_PIN);
    delayMicroseconds(40);

    dht11_in = PINC & _BV(DHT11_PIN);

    if(dht11_in){
        Serial.println("dht11 start condition 1 not met");
        return;
    }
    delayMicroseconds(80);

    dht11_in = PINC & _BV(DHT11_PIN);

    if(!dht11_in){
        Serial.println("dht11 start condition 2 not met");
        return;
    }
    delayMicroseconds(80);
    // now ready for data reception
    for (i=0; i<5; i++)
    dht11_dat[i] = read_dht11_dat();

    DDRC |= _BV(DHT11_PIN);
    PORTC |= _BV(DHT11_PIN);

    byte dht11_check_sum = dht11_dat[0]+dht11_dat[1]+dht11_dat[2]+dht11_dat[3];
    // check check_sum
    if(dht11_dat[4]!= dht11_check_sum)
    {
        Serial.println("DHT11 checksum error");
    }

    sensorValue = dht11_dat[0]*10.24;
    
    for(i=0; i<30; i++){
      json["humdity"] = dht11_dat[0];
      json["temperature"] = dht11_dat[2];
      json["A0"] = sensorValue;
      json.printTo(Serial);
      Serial.println();
    }

    //delay(2000);
}
