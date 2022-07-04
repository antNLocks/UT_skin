#include <Muca.h>


#define SKIN_CELLS NUM_TX * NUM_RX
#define framesToDrop 20
#define framesForCalib 20

#define Tx 20
#define Rx 10

const byte Rx_index[] = { 6, 0, 7, 1, 8, 2, 9, 10, 3, 4};
const byte Tx_index[] = {9, 19, 8, 18, 7, 17, 6, 16, 5, 15, 4, 14, 3, 13, 2, 12, 1, 11, 0, 10}; 


Muca muca;
unsigned int rawBufferCalibration[252];

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

    for (int i = 0; i < Rx; i++) {
      for(int j = Tx-1; j >= 0; j--){
        int index = Tx_index[j] * NUM_RX + Rx_index[i];
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

void serialEvent() {
  if (Serial.available() > 0) {
    byte b = Serial.read();

    if (b == 0x01)
      calib();

    if (b == 0x02)
      scaleCalib();

    if (b == 0x03) {
      while (Serial.available() <= 0);
      muca.setGain(Serial.read());
    }
  }
}

void scaleCalib() {}