#include <Muca.h>
#include <EEPROM.h>


#define SKIN_CELLS NUM_TX * NUM_RX
#define framesToDrop 20
#define framesForCalib 20

const byte Rx_index[] = { 0, 6, 1, 7, 2, 8, 3, 4, 9, 10 }; //10 Rx
const byte Tx_index[] = { 19, 9, 18, 8, 17, 7, 16, 6, 15, 5, 14, 4, 13, 3, 12, 2, 11, 1, 10, 0 }; //20 Tx



Muca muca;
unsigned int rawBufferCalibration[252];
byte scaleBufferCalibration[252];
byte scalePivot;
#define scaleBufferAddress 1
#define scalePivotAddress 0

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

  EEPROM.get(scalePivotAddress, scalePivot);
  EEPROM.get(scaleBufferAddress, scaleBufferCalibration);

 
}


void loop() {
  GetRaw();
}


void GetRaw() {
  if (muca.updated()) {

    for (byte i = 0; i < sizeof(Tx_index); i++) {
      for (byte j = 0; j < sizeof(Rx_index); j++) {
        int index = Tx_index[i] * NUM_RX + Rx_index[j];
        byte b = constrain(((unsigned int) constrain(((signed int) (muca.grid[index] - rawBufferCalibration[index]))/ scaleFactor,1, 255)) * scaleBufferCalibration[scalePivot] / scaleBufferCalibration[index], 1, 255);
        Serial.write(b);
      }
    }

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


void scaleCalib() {
  Serial.println(F("[MUCA] Doing Scale calibration"));

  for (int _ = 0; _ < framesForCalib / 2; _++) {
    while (!muca.updated());

    for (int i = 0; i < SKIN_CELLS; i++)
      scaleBufferCalibration[i] += round(((signed int) (muca.grid[i] - rawBufferCalibration[i])) / scaleFactor / (framesForCalib / 2));
  }

  for (int i = 0; i < SKIN_CELLS; i++)
    if (scaleBufferCalibration[scalePivot] < scaleBufferCalibration[i])
      scalePivot = i;

  float scale = scaleFactor * scaleBufferCalibration[scalePivot] / 255;
  for (int _ = 0; _ < framesForCalib / 2; _++) {
    while (!muca.updated());

    for (int i = 0; i < SKIN_CELLS; i++)
      scaleBufferCalibration[i] += round(((signed int) (muca.grid[i] - rawBufferCalibration[i])) / scale / (framesForCalib / 2));
  }

  for (byte k = 0; k < 150; k++) {
    for (byte i = 0; i < sizeof(Tx_index); i++) {
      for (byte j = 0; j < sizeof(Rx_index); j++) {
        int index = Tx_index[i] * NUM_RX + Rx_index[j];
        Serial.write(scaleBufferCalibration[index]);
      }
    }

    Serial.write(0x00);
  }


  Serial.println(F("[MUCA] Scale Calibration successfull"));

  EEPROM.put(scalePivotAddress, scalePivot);
  EEPROM.put(scaleBufferAddress, scaleBufferCalibration);


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

/*


         byte b = constrain((signed int) (muca.grid[index] - rawBufferCalibration[index]) * scaleBufferCalibration[index] /  scaleFactor / scaleBufferCalibration[scalePivot], 1, 255);


  void scaleCalib() {

  }aleBufferCalibration[i] += round((muca.grid[i] - rawBufferCalibration[i]) / scale / (framesForCalib / 2));
  }
*/
