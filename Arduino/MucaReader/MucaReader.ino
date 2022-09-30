#include <Muca.h>
#include "TxRxPinout.h"

#define SKIN_CELLS NUM_TX * NUM_RX

// Before calibration, when the Muca board has just been powered up
#define framesToDrop 20
#define framesForCalib 20

#define SERIAL_RATE 250000

Muca muca;
unsigned int rawBufferCalibration[252];

float scaleFactor = 3;

void setup() {
  Serial.begin(SERIAL_RATE);

  muca.init();
  muca.useRawData(true); // If you use the raw data, the interrupt is not working

  delay(50);
  muca.setGain(2);

  for (int _ = 0; _ < framesToDrop; _++)
    while (!muca.updated());

  calib();

}


void loop() {
  if (muca.updated()) {

    //Just looking at the data we're intersted in (cf pinout)
    for (int i = 0; i < Tx; i++) {
      for(int j = 0; j < Rx; j++) {
        int index = Tx_index[i] * NUM_RX + Rx_index[j];
        byte b = constrain((signed int) (muca.grid[index] - rawBufferCalibration[index]) / scaleFactor, 0, 0xff - 1); //0xff is reserved
        Serial.write(b);
      }
    }

    Serial.write(0xff); //End of frame
  }
}

void calib() {
  Serial.println(F("[MUCA_READER] Doing calibration"));

  for (int i = 0; i < SKIN_CELLS; i++)
    rawBufferCalibration[i] = 0;

  for (int _ = 0; _ < framesForCalib; _++) {
    while (!muca.updated());

    for (int i = 0; i < SKIN_CELLS; i++)
      rawBufferCalibration[i] += round(muca.grid[i] / (float) framesForCalib);
  }

  Serial.println(F("[MUCA_READER] Calibration successfull"));
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

//Not implemented yet -- see MucaReader_ScaleCalib for a first implementation draft
void scaleCalib() {
    Serial.println(F("[MUCA_READER] Scale calibration not yet available"));
}
