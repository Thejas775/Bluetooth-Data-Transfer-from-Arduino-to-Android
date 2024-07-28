#include <SoftwareSerial.h>

SoftwareSerial BTSerial(10, 11); // RX | TX
const int ledPin = 13; // Onboard LED pin
const int trigPin = 9;
const int echoPin = 8;

void setup() {
  pinMode(ledPin, OUTPUT);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  Serial.begin(9600);
  BTSerial.begin(9600);  // Bluetooth module baud rate
}

void loop() {
  // Read distance from the ultrasonic sensor
  long duration, distance;
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = duration * 0.034 / 2; // Calculate the distance in cm

  // Send the distance to the Bluetooth module
  BTSerial.println(distance);
  Serial.println(distance);


  delay(500); // Delay for a second before the next reading
}


The above is the arduino code.
