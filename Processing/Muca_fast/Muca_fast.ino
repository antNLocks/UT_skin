#include <Muca.h>

#define SKIN_CELLS NUM_TX * NUM_RX
#define framesToDrop 20
#define framesForCalib 20



Muca muca;
int rawBufferCalibration[252];
byte rawBuffer[252];

void setup() {
  Serial.begin(230400);

  muca.init();
  muca.useRawData(true); // If you use the raw data, the interrupt is not working

  delay(50);
  muca.setGain(2);

  for (int _ = 0; _ < framesToDrop; _++)
    while (!muca.updated());

  calib();

}


void loop() {
  GetRaw();
}


void GetRaw() {
  if (muca.updated()) {

    for (int i = 0; i < SKIN_CELLS; i++) {
      byte b = constrain((signed int) muca.grid[i] - rawBufferCalibration[i], 1, 255);
      Serial.write(b);
    }

    Serial.write(0x00);
  }

  //Serial.flush();
}

void calib() {
  Serial.println("[MUCA] Doing calibration");

  for (int i = 0; i < SKIN_CELLS; i++)
    rawBufferCalibration[i] = 0;

  for (int _ = 0; _ < framesForCalib; _++) {
    while (!muca.updated());

    for (int i = 0; i < SKIN_CELLS; i++)
      rawBufferCalibration[i] += round(muca.grid[i] / (float) framesForCalib);
  }

  Serial.println("[MUCA] Calibration successfull");
}

char incomingMsg[20];

void serialEvent() {
  int charsRead;
  while (Serial.available() > 0) {
    charsRead = Serial.readBytesUntil('\n', incomingMsg, sizeof(incomingMsg) - 1);
    incomingMsg[charsRead] = '\0';  // Make it a string
    if (incomingMsg[0] == 'g') {
      Gain();
    }
    if (incomingMsg[0] == 'c') {
      calib();
    }
  }
}


void Gain() {
  char *str;
  char *p = incomingMsg;
  byte i = 0;
  while ((str = strtok_r(p, ":", &p)) != NULL)  // Don't use \n here it fails
  {
    if (i == 1 )  {
      muca.setGain(atoi(str));
    }
    i++;
  }
  incomingMsg[0] = '\0'; // Clear array
}