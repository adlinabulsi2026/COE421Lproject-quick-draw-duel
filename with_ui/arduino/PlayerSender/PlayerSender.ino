/*
 * Wild West Quick-Draw Duel - Player gun (LilyPad Arduino)
 *
 * The same code runs on both guns. Only playerId changes (1 or 2).
 * The gun reads its sensors and sends a line to the Java program
 * every time a value changes:
 *     player,holster,trigger,hit      e.g. "1,0,1,0"
 * It also receives one-byte commands from Java:
 *     'P' = send your values again, 'D' = DRAW,
 *     '1' / '2' = round winner, '0' = tie
 */

#include <SoftwareSerial.h>

// XBee connected to pin 10 (RX) and pin 11 (TX)
SoftwareSerial mySerial(10, 11);

const int playerId = 1;              // 1 for player 1, 2 for player 2
const bool selfTest = false;         // true = gun can detect its own shot (testing only)

// pins
const int irReceiver = A0;   // IR receiver output (LOW when it sees 38 kHz IR)
const int irLed = A1;        // IR emitter (the "bullet")
const int trigger = 4;       // trigger button (LOW when pressed)
const int holster = 5;       // holster sensor (HIGH when the gun is holstered)
const int motor = 9;         // vibration motor (HIGH = on)

// last values sent, so we only send when something changes
int lastHolster = -1;
int lastTrigger = -1;
int lastHit = -1;

// hit stays 1 for half a second after the receiver sees IR
int hit = 0;
unsigned long hitTime = 0;

void setup() {
  Serial.begin(9600);      // USB (Serial Monitor, or Java for the USB gun)
  mySerial.begin(9600);    // XBee

  pinMode(irReceiver, INPUT);
  pinMode(irLed, OUTPUT);
  pinMode(trigger, INPUT_PULLUP);   // button to GND, no resistor needed
  pinMode(holster, INPUT);
  pinMode(motor, OUTPUT);
  digitalWrite(motor, LOW);         // motor off at start
}

// turns the vibration motor on for ms milliseconds
void vibrate(int ms) {
  digitalWrite(motor, HIGH);
  delay(ms);
  digitalWrite(motor, LOW);
}

// one burst of IR light flickering at about 38 kHz
// (13 us on + 13 us off), the frequency the IR receiver reacts to
void burst() {
  for (int i = 0; i < 20; i++) {
    digitalWrite(irLed, HIGH);
    delayMicroseconds(13);
    digitalWrite(irLed, LOW);
    delayMicroseconds(13);
  }
}

// one shot = 10 bursts with short pauses so the opponent's receiver catches it
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

// reads one command byte from the XBee (or from USB) and reacts to it
void readCommand() {
  char c = 0;
  if (mySerial.available()) {
    c = mySerial.read();
  }
  if (Serial.available()) {
    c = Serial.read();
  }

  if (c != 0) {

    // Java asks for our values: forget the last ones so they are sent again
    if (c == 'P') {
      lastHolster = -1;
    }

    // DRAW signal: short buzz
    if (c == 'D') {
      vibrate(200);
    }

    // tie: very short buzz
    if (c == '0') {
      vibrate(100);
    }

    // round result: two short buzzes for the winner, one long buzz for the loser
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

  // holster: 1 = gun in the holster, 0 = drawn
  int h = digitalRead(holster);

  // trigger: 1 = pressed
  int t = 0;
  if (digitalRead(trigger) == LOW) {
    t = 1;
  }

  // fire only at the moment the trigger is pressed (not while held)
  if (t == 1 && lastTrigger == 0) {
    fire();
  }

  // the IR receiver sees the opponent's shot
  if (digitalRead(irReceiver) == LOW) {
    hit = 1;
    hitTime = millis();
  }

  // hit goes back to 0 after half a second
  if (hit == 1 && millis() - hitTime > 500) {
    hit = 0;
  }

  // send a line to Java only when a value changed
  if (h != lastHolster || t != lastTrigger || hit != lastHit) {
    lastHolster = h;
    lastTrigger = t;
    lastHit = hit;

    // to Java through the XBee
    mySerial.print(playerId);
    mySerial.print(",");
    mySerial.print(h);
    mySerial.print(",");
    mySerial.print(t);
    mySerial.print(",");
    mySerial.println(hit);

    // same line on USB (Serial Monitor, or Java for the gun connected by USB)
    Serial.print(playerId);
    Serial.print(",");
    Serial.print(h);
    Serial.print(",");
    Serial.print(t);
    Serial.print(",");
    Serial.println(hit);
  }
}
