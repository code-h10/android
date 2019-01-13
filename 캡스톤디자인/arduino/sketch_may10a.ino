#include <ESP8266.h>
#include <DHT.h>
#include <SoftwareSerial.h>
#define ID        "dlink-806a-z"  
#define PASSWORD  ""  
#define SOIL A0
#define WTMP 7
#define LED 13
#define DHTP 12

SoftwareSerial mySerial = SoftwareSerial(2, 3);
ESP8266 wifi = ESP8266(mySerial);
DHT dht(DHTP,DHT11);

void setup(void)  
{  
    Serial.begin(9600);
    mySerial.begin(9600);

    
    Serial.print("setup begin\r\n");  
    
    Serial.print("FW Version:");  
    Serial.println(wifi.getVersion().c_str());  

        
    if (wifi.setOprToStationSoftAP()) {  
        Serial.print("to station + softap ok\r\n");  
    } else {  
        Serial.print("to station + softap err\r\n");  
    }  
   
    if (wifi.joinAP(ID, PASSWORD)) {  
        Serial.print("Join AP success\r\n");  
        Serial.print("IP: ");  
        Serial.println(wifi.getLocalIP().c_str());      
    } else {  
        Serial.print("Join AP failure\r\n");  
    }  
      
    if (wifi.enableMUX()) {  
        Serial.print("multiple ok\r\n");  
    } else {  
        Serial.print("multiple err\r\n");  
    }  

    if (wifi.startTCPServer(80)) {  
        Serial.print("start tcp server ok\r\n");  
    } else {  
        Serial.print("start tcp server err\r\n");  
    }  
      
    if (wifi.setTCPServerTimeout(10)) {   
        Serial.print("set tcp server timout 10 seconds\r\n");  
    } else {  
        Serial.print("set tcp server timout err\r\n");  
    }  

    Serial.print("setup end\r\n");  

    pinMode(LED, OUTPUT);
    pinMode(SOIL, INPUT);
    pinMode(WTMP, OUTPUT);
    
}  


void loop(void)  
{ 
    delay(200);
    int soilvalue = analogRead(SOIL);
    int tem = dht.readTemperature();
    int hum = dht.readHumidity();

    if(soilvalue < 800){
      digitalWrite(LED,HIGH);
    }
    else{
      digitalWrite(LED, LOW);
    }
    uint8_t SenData[128];
    sprintf(SenData,"%d-%d-%d\n",soilvalue, tem, hum);

    uint8_t buffer[128] = {0};
    uint8_t mux_id;
    uint32_t len = wifi.recv(&mux_id, buffer, sizeof(buffer), 100);
    
    if (len >= 0) {
        
        Serial.print("Status:[");
        Serial.print(wifi.getIPStatus().c_str());
        Serial.print("] : ");
        
        Serial.print("Received from :");
        Serial.print(mux_id);
        Serial.print("[");
        
        Serial.print(buffer[0]);
       
        Serial.println("]");
        
        if ( buffer[0] == 49 ){
          digitalWrite( WTMP, HIGH );  
        }
        else if ( buffer[0] == 50 ){
          digitalWrite( WTMP, LOW ); 
        }
        
        if(wifi.send(mux_id, SenData, strlen(SenData))) {
            SenData[256] = {0};
            Serial.print("Send Data to Android ok\r\n");
        } else {
            Serial.print("Send Data to Android err\r\n");
        }
    } 
}
