# Arduino

This folder contains the source code needed to program an arduino board at the interface of a Muca board and a computer.
This is an adaptation of the [work of Marc Teyssier](https://github.com/muca-board/).
The communication protocol between the arduino board and the Muca board is I2C and the link with the computer is a UART link

## How to use

1. Copy/Paste the `Muca-master-modified` library folder into the arduino library directory
2. Modify the `MucaReader/TxRxPinout.h` file according to the Tx Rx pinout of the muca board
3. Upload `MucaReader/MucaReader.ino` to the arduino board

## Issues

The scale calibration functionality is not yet properly implemented. A first draft implementation `MucaReader_ScaleCalib` shows the overall idea.

## Notes

The changes to the Muca library are as follows:
* all strings are saved not in RAM but in flash memory using the *F()* macro.
* all code related to *TouchPoint* management has been commented out to offload RAM

