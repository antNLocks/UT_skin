#include <Muca.h>
#include <EEPROM.h>

#include "TxRxLoop.h"

#define SKIN_CELLS NUM_TX * NUM_RX
#define framesToDrop 20
#define framesForCalib 20



Muca muca;
unsigned int rawBufferCalibration[252];
byte scaleBufferCalibration[252];
#define scaleBufferAdress 0

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

  EEPROM.get(scaleBufferAdress, scaleBufferCalibration);
}


void loop() {
  GetRaw();
}


void GetRaw() {
  if (muca.updated()) {

    forEachTxRx([](int Tx_index, int Rx_index){
        int index = Tx_index * NUM_RX + Rx_index;
        byte b = constrain((signed int) (muca.grid[index] - rawBufferCalibration[index]) / scaleFactor, 1, 255);
        Serial.write(b);
    });

    Serial.write(0x00);
  }
}

void calib() {

  for (int i = 0; i < SKIN_CELLS; i++)
    rawBufferCalibration[i] = 0;

  for (int _ = 0; _ < framesForCalib; _++) {
    while (!muca.updated());

    for (int i = 0; i < SKIN_CELLS; i++)
      rawBufferCalibration[i] += round(muca.grid[i] / (float) framesForCalib);
  }

}


void scaleCalib(){
  



  EEPROM.put(scaleBufferAdress, scaleBufferCalibration);
}

void serialEvent() {
  if(Serial.available() > 0){
    byte b = Serial.read();

    if(b == 0x01)
      calib();

    if(b == 0x02)
      scaleCalib();

    if(b == 0x03){
      while(Serial.available() <= 0);
      muca.setGain(Serial.read());
    }
  }
}

/*
 * 
 * 
 *       byte b = constrain((signed int) (muca.grid[index] - rawBufferCalibration[index]) * scaleBufferCalibration[index] /  scaleFactor / scaleBufferCalibration[scalePivot], 1, 255);


void scaleCalib() {
  Serial.println("[MUCA] Doing Scale calibration");
  calib();


  for (int _ = 0; _ < framesForCalib / 2; _++) {
    while (!muca.updated());

    for (int i = 0; i < SKIN_CELLS; i++)
      scaleBufferCalibration[i] += round((muca.grid[i] - rawBufferCalibration[i]) / scaleFactor / (framesForCalib / 2));
  }

  byte imax = 0;
  for (int i = 0; i < SKIN_CELLS; i++)
    if (scaleBufferCalibration[imax] < scaleBufferCalibration[i])
      imax = i;

  float scale = scaleFactor * scaleBufferCalibration[imax] / 255;
  for (int _ = 0; _ < framesForCalib / 2; _++) {
    while (!muca.updated());

    for (int i = 0; i < SKIN_CELLS; i++)
      scaleBufferCalibration[i] += round((muca.grid[i] - rawBufferCalibration[i]) / scale / (framesForCalib / 2));
  }


  Serial.println("[MUCA] Scale Calibration successfull");

  EEPROM.put(scaleBufferAdress, scaleBufferCalibration);
}aleBufferCalibration[i] += round((muca.grid[i] - rawBufferCalibration[i]) / scale / (framesForCalib / 2));
  }
 */
