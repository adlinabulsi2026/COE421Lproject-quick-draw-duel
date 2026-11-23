#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11);

const int playerId = 1;
const bool selfTest = false;

const int irReceiver = A0;
const int irLed = A1;
const int trigger = 4;
const int holster = 5;
const int motor = 9;

int lastHolster = -1;
int lastTrigger = -1;
int lastHit = -1;

int hit = 0;
unsigned long hitTime = 0;

void setup() {
  Serial.begin(9600);
  mySerial.begin(9600);

  pinMode(irReceiver, INPUT);
  pinMode(irLed, OUTPUT);
  pinMode(trigger, INPUT_PULLUP);
  pinMode(holster, INPUT);
  pinMode(motor, OUTPUT);
  digitalWrite(motor, LOW);
}

void vibrate(int ms) {
  digitalWrite(motor, HIGH);
  delay(ms);
  digitalWrite(motor, LOW);
}

void burst() {
  for (int i = 0; i < 20; i++) {
    digitalWrite(irLed, HIGH);
    delayMicroseconds(13);
    digitalWrite(irLed, LOW);
    delayMicroseconds(13);
  }
}

void fire() {
  for (int j = 0; j < 10; j++) {
    burst();
    if (selfTest && digitalRead(irReceiver) == LOW) {
      hit = 1;
      hitTime = millis();
    }
    delay(20);
  }
  delay(50);
}

void readCommand() {
  char c = 0;
  if (mySerial.available()) {
    c = mySerial.read();
  }
  if (Serial.available()) {
    c = Serial.read();
  }

  if (c != 0) {

    if (c == 'P') {
      lastHolster = -1;
    }

    if (c == 'D') {
      vibrate(200);
    }

    if (c == '0') {
      vibrate(100);
    }

    if (c == '1' || c == '2') {
      if (c - '0' == playerId) {
        vibrate(100);
        delay(100);
        vibrate(100);
      } else {
        vibrate(1000);
      }
    }
  }
}

void loop() {
  readCommand();

  int h = digitalRead(holster);

  int t = 0;
  if (digitalRead(trigger) == LOW) {
    t = 1;
  }

  if (t == 1 && lastTrigger == 0) {
    fire();
  }

  if (digitalRead(irReceiver) == LOW) {
    hit = 1;
    hitTime = millis();
  }

  if (hit == 1 && millis() - hitTime > 500) {
    hit = 0;
  }

  if (h != lastHolster || t != lastTrigger || hit != lastHit) {
    lastHolster = h;
    lastTrigger = t;
    lastHit = hit;

    mySerial.print(playerId);
    mySerial.print(",");
    mySerial.print(h);
    mySerial.print(",");
    mySerial.print(t);
    mySerial.print(",");
    mySerial.println(hit);

    Serial.print(playerId);
    Serial.print(",");
    Serial.print(h);
    Serial.print(",");
    Serial.print(t);
    Serial.print(",");
    Serial.println(hit);
  }
}
