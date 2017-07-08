#include <Servo.h> 
#include <SoftwareSerial.h>

SoftwareSerial bt_pin(10,11);

Servo myservo;
int pos = 0;
char prev = '0';

void setup()
{
  Serial.begin(9600);
  pinMode(LED_BUILTIN,OUTPUT);
  myservo.attach(9);
  bt_pin.begin(9600);
  bt_pin.write("AT\r\n");
  delay(500);
  while(bt_pin.available()){
    Serial.write(bt_pin.read()); 
  }
  bt_pin.write("AT+NAME=SleepCatcher\r\n");
  delay(500);
  while(bt_pin.available()){
    Serial.write(bt_pin.read()); 
  }
  pinMode(11,OUTPUT);
}

void loop()
{
  myservo.write(0);
  if(Serial.available())
  {
    char in = Serial.read();
    if(in == '1' && prev == '0')
    { 
      prev=in;
      digitalWrite(LED_BUILTIN,HIGH);
      myservo.write(180);
      bt_pin.write('1');
      Serial.print("On");
    }
    else if(in == '0' && prev == '1')
    {
      prev=in;
      myservo.write(90);
      digitalWrite(LED_BUILTIN,LOW);
      bt_pin.write('0');
      Serial.print("Off");
    }
  }
}
  
