

#include <WiFi.h>
#include <PubSubClient.h>
#include<BluetoothSerial.h>
#include<ESP32Servo.h>

const char* ssid     = "Dann_of_Thursday";     // your network SSID (name of wifi network)
const char* password = "jimjamjim"; // your network password

const char*  server = "broker.hivemq.com";  // Server URL
unsigned long lastMsg = 0;
const int MSG_BUFFER_SIZE = 500;
char msgBuffer[MSG_BUFFER_SIZE];
long int value = 0L;
int servoPin =13;
int GPIOPin = 25;
Servo myservo;
String status = "a";// status a is unlocked, status b is locked
BluetoothSerial SerialBT;

WiFiClient client;
PubSubClient clientPubSub;

void updateStatus(String change){
  clientPubSub.publish("MichaelRFAU-AfterLock",change.c_str());
  status = change;
}
void IRAM_ATTR buttonPush(){
  Serial.println("button pushed");
}
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  String zeMessage = "";
  for (int i = 0; i < length; i++) {
    zeMessage+= (char)payload[i];
  }
  Serial.println("\nMessage:"+zeMessage);
  if(zeMessage == "a" && status == "b"){
    myservo.write(0);
    delay(100);
    updateStatus(zeMessage);
    Serial.println("unlock to lock");
  }else if(zeMessage=="b" && status =="a"){
    myservo.write(180);
    delay(100);
    updateStatus(zeMessage);
    Serial.println("unlock to lock");
  }else{
    updateStatus(zeMessage);
    Serial.println("confused state");
  }
  
}

void setup() {
  //Initialize serial and wait for port to open:
  Serial.begin(115200);
  delay(100);
  SerialBT.begin("ESP32test");
  pinMode(GPIOPin, INPUT_PULLUP);
  attachInterrupt(GPIOPin,buttonPush,FALLING);
  Serial.print("Attempting to connect to SSID: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  // attempt to connect to Wifi network:
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    // wait 1 second for re-trying
    delay(1000);
  }
  myservo.setPeriodHertz(50);
  myservo.attach(servoPin, 500, 2500);
  delay(100);
  myservo.write(0);
  delay(100);
  Serial.print("Connected to ");
  Serial.println(ssid);
  clientPubSub = PubSubClient(client);
  clientPubSub.setServer(server,1883);
  clientPubSub.setCallback(callback);

  
}

void reconnect() {
  // Loop until we’re reconnected
  while (!clientPubSub.connected()) {
    Serial.print("Attempting MQTT connection…");
    String clientId = "ESP8266Client - MyClient";
    String userName = "arduino";
    String password = "powerArduino1";
    // Attempt to connect
    // Insert your password
   
    boolean va;
    try{
      va = clientPubSub.connect(clientId.c_str());
    delay(500);
    }
    catch(int e){
      va = false;
      Serial.println(e);
      Serial.println("connect bad");
    }
    if (va) {
      Serial.println("connected");
      // Once connected, publish an announcement…
     // clientPubSub.publish("testTopic", "hello world");
      // … and resubscribe
      clientPubSub.subscribe("MichaelRFAU-SendLock");
    } else {
      Serial.print("failed, rc = ");
      Serial.print(clientPubSub.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}
void loop() {

  if(!clientPubSub.connected()){
    reconnect();
    }
  clientPubSub.loop();

  //unsigned long time = millis();
  //if(time -lastMsg > 2000){
  //  lastMsg = time;
  //  ++value;
  //  snprintf(msgBuffer, MSG_BUFFER_SIZE, "hello world #%ld",value);
  //  Serial.print("Publish message: ");
  //  Serial.println(msgBuffer);
  //  clientPubSub.publish("testTopic",msgBuffer);

  }
