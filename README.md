# Bluetooth Ultrasonic Distance Measurement

This project connects an Android app to an Arduino via Bluetooth to measure distances using an ultrasonic sensor. The distance data is displayed on the Android app, and if an object is detected within 15 cm, the app uses Text-to-Speech (TTS) to notify the user.

## Requirements

- Arduino with Bluetooth module (e.g., HC-05)
- Ultrasonic sensor (e.g., HC-SR04)
- Android device
- Android Studio
- Kotlin

## Arduino Code

```cpp
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
