#include <Muca.h>

#define SKIN_CELLS NUM_TX * NUM_RX
#define framesToDrop 20
#define framesForCalib 20

#define Tx 20
#define Rx 10

byte Rx_index[] = {0, 6, 1, 7, 2, 8, 3, 4, 9, 10};
byte Tx_index[] = {19, 9, 18, 8, 17, 7, 16, 6, 15, 5, 14, 4, 13, 3, 12, 2, 11, 1, 10, 0}; 


Muca muca;
int rawBufferCalibration[252];
byte rawBuffer[252];

float scaleFactor = 3;

void setup() {
  Serial.begin(250000);

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

    for (int i = 0; i < Tx; i++) {
      for(int j = 0; j < Rx; j++){
        int index = Tx_index[i] * NUM_RX + Rx_index[j];
        byte b = constrain((signed int) (muca.grid[index] - rawBufferCalibration[index]) / scaleFactor, 1, 255);
        Serial.write(b);
      }
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
