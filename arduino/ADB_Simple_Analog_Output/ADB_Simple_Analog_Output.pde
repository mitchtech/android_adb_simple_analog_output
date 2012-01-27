#include <SPI.h>
#include <Adb.h>

Connection * connection;

// LED leads connected to PWM pins
const int RED_LED_PIN = 3;
const int GREEN_LED_PIN = 5;
const int BLUE_LED_PIN = 6;

// Event handler for shell connection; called whenever data sent from Android to Microcontroller
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
  int i;
  // Data packets contain three bytes, one for each led, in the range of [0..255]
  if (event == ADB_CONNECTION_RECEIVE)
  {
    analogWrite(RED_LED_PIN, data[0]);
    analogWrite(GREEN_LED_PIN, data[1]);
    analogWrite(BLUE_LED_PIN, data[2]);    
  }
}

void setup()
{
  Serial.begin(57600);

  // Init the ADB subsystem.  
  ADB::init();

  // Open an ADB stream to the phone's shell. Auto-reconnect
  connection = ADB::addConnection("tcp:4567", true, adbEventHandler);  
}

void loop() {
  // Poll the ADB subsystem.
  ADB::poll();
}

