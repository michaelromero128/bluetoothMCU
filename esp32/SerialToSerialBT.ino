//This example code is in the Public Domain (or CC0 licensed, at your option.)
//By Evandro Copercini - 2018
//
//This example creates a bridge between Serial and Classical Bluetooth (SPP)
//and also demonstrate that SerialBT have the same functionalities of a normal Serial

#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

#define LED 32
#define BUFFER_LENGTH 1024
BluetoothSerial SerialBT;


long referenceTime = 0;
char buffer[BUFFER_LENGTH];
char (*bufferPtr)[BUFFER_LENGTH] = &buffer;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32test"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");
  long referenceTime = millis();
  pinMode(LED,OUTPUT);
  digitalWrite(LED,HIGH);
}

void loop() {
  
  delay(1000);
  bool flag = true;
  if (flag && SerialBT.available() > 14 && referenceTime +1000 < millis()) {

    int index = SerialBT.readBytesUntil('\n',buffer,BUFFER_LENGTH );
    for(int i = 0; i < index; i++){
      char val = buffer[i];
      if(val!= '\t' && val != '\n'){
        Serial.print(val);
      }
    }
    Serial.println('\n');
    flag = false;

  }
  
  if(referenceTime+1001 < millis()){
    referenceTime = millis();
    String out = String(millis())+'\n';
    //out = "Hello Joe\n";
    while(out.length() < 15){
      out+= '\t';
    }
    out+='\0';
    char cstr[out.length()+1];
    char *ptr = cstr;
    strcpy(cstr,out.c_str());  
    int bound = out.length();
    
    while(*ptr!='\0'){
      SerialBT.print(*ptr);
      ptr++;
    }
    
    Serial.print(out);
    flag= true;
    
  } 
  
  
}