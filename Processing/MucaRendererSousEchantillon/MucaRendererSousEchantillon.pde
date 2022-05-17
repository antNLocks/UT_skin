import processing.serial.*;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.*;

import gab.opencv.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Core;

// =========== CONSTANTS ==================
int     SKIN_COLS          = 6;
int     SKIN_ROWS          = 10;
int     SKIN_CELLS         = 252;

int     SERIAL_PORT        = 1; 
int     SERIAL_RATE        = 230400;

int     DISPLAY_W          = 1200;
int     DISPLAY_H          = 700;


// =========== VARIABLES ==================
Serial  skinPort;
OpenCV opencv; // openCV object for interpolation


int resizeFactor = 60;
Size finalSize = new Size(resizeFactor * SKIN_COLS, resizeFactor * SKIN_ROWS);


int min_threshold = 90;
int max_threshold = 255;


int averageAlgo = 1; //0 : rolling average  |  1 : interpolation with previous frames
int frames_for_average = 3;
float interpolation_factor = 0.5; //0.5 seems to be a good deal


int binary_threshold = 153;
boolean binary = false;

PImage finalImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);
PImage rawFinalImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);


void settings () { 
  size( DISPLAY_W, DISPLAY_H );
  noSmooth();
}

void setup () { 
  noStroke( );
  printArray(Serial.list());

  opencv = new OpenCV(this, SKIN_COLS, SKIN_ROWS);

  InterfaceSetup();

  for ( int i = 0; i < finalSize.width * finalSize.height; i++ ) {
    finalImage.pixels[i] = color(0, 153, 0); //A green image until we receive data from the arduino board
    rawFinalImage.pixels[i] = color(0, 153, 0); //A green image until we receive data from the arduino board
  }
}



void draw() {
  int[] rawBuffer = readRawBuffer( );

  if ( rawBuffer != null ) {

    PImage skinImage = 
      bufferToImage(
      averageAlgo == 0 ? averageBufferOverTime_rollingAverage(rawBuffer, frames_for_average) : averageBufferOverTime_interpolationPreviousFrames(rawBuffer, interpolation_factor));


    Mat resizedMat = resizeMat(imageToMat(skinImage), finalSize, Imgproc.INTER_LANCZOS4);
    Mat thresholdedResizedMat = thresholdMapping(resizedMat, min_threshold, max_threshold);
    Mat binMat = toBin(thresholdedResizedMat, binary_threshold);

    Mat finalMat = binary ? binMat : thresholdedResizedMat;
    opencv.toPImage(finalMat, finalImage);

    opencv.toPImage(resizeMat(imageToMat(bufferToImage(rawBuffer)), finalSize, Imgproc.INTER_NEAREST), rawFinalImage);
    IncrementFPS();
  }

  background(200);

  pushMatrix();
  translate(30, 30);
  image(rawFinalImage, 0, 0);
  popMatrix();

  pushMatrix();
  translate((int) finalSize.width + 60, 30);
  image(finalImage, 0, 0);
  popMatrix();

  fill(0);
  text("Skin FPS: " + fps, 10, 15);
}



int[] readRawBuffer() {
  int[] result1 = null;
  ArrayList<Integer> r = new ArrayList<Integer>();
  int[] result2 = null;

  if ( skinPort != null && skinPort.available( ) > 0 ) {
    byte[] skinBuffer = skinPort.readBytesUntil(0x00);

    if (skinBuffer != null && skinBuffer.length == SKIN_CELLS+1) {
      result1 = new int[SKIN_CELLS];
      for (int i = 0; i < SKIN_CELLS; i++) {
        result1[i] = skinBuffer[i] & 0xFF;
        
      }
      
      int[][] b = new int[12][21];
      
      for (int i = 0; i < 252; i++)
        b[11 - i % 12][i / 12] = result1[i];
        
      
            
        
      
      result2 = new int[6*10];
      for(int i = 0; i < 6; i++)
        for(int j = 0; j < 10; j++)
        result2[j*6+i] = b[2*i][2*j];
    }
  }

  return result2;
}


ArrayDeque<int[]> rawBuffers = new ArrayDeque<int[]>();

float[] averageBufferOverTime_rollingAverage(int[] actualRawBuffer, int nbFrames) {
  float[] result = new float[actualRawBuffer.length];

  rawBuffers.add(actualRawBuffer);

  while (rawBuffers.size() > nbFrames)
    rawBuffers.poll();


  Iterator<int[]> it = rawBuffers.iterator();
  while (it.hasNext()) {
    int[] rwB = it.next();
    for (int i = 0; i < actualRawBuffer.length; i++)
      result[i] += rwB[i] / (float) rawBuffers.size();
  }

  return result;
}


float[] previousRawBuffer = null;

float[] averageBufferOverTime_interpolationPreviousFrames(int[] actualRawBuffer, float k) {
  float[] result = new float[actualRawBuffer.length];

  if (previousRawBuffer == null) {
    previousRawBuffer = new float[actualRawBuffer.length];
    for (int i = 0; i < actualRawBuffer.length; i++)
      previousRawBuffer[i] = actualRawBuffer[i];
  }

  for (int i = 0; i < actualRawBuffer.length; i++)
    result[i] = (1-k)*actualRawBuffer[i] + k*previousRawBuffer[i];

  previousRawBuffer = result;

  return result;
}

PImage bufferToImage(int[] buffer) {
  PImage result = createImage( SKIN_COLS, SKIN_ROWS, RGB );
  for ( int i = 0; i < buffer.length; i++ ) 
    result.pixels[i] = color(buffer[i]);

  result.updatePixels( );

  return result;
}

PImage bufferToImage(float[] buffer) {
  PImage result = createImage( SKIN_COLS, SKIN_ROWS, RGB );
  for ( int i = 0; i < buffer.length; i++ ) 
    result.pixels[i] = color(buffer[i]);

  result.updatePixels( );

  return result;
}

Mat imageToMat(PImage img) {
  opencv.loadImage(img);
  Mat result = opencv.getGray();
  return result;
}

Mat resizeMat(Mat img, Size destSize, int algo) {
  Mat skinImageResized = new Mat(destSize, img.type()); // new matrix to store resize image
  Imgproc.resize(img, skinImageResized, destSize, 0, 0, algo); // resize // INTER_LANCZOS4
  return skinImageResized;
}

Mat thresholdMapping(Mat img, float min, float max) {
  Mat result = new Mat(img.size(), img.type());

  byte[] temp = new byte[(int) (img.size().width * img.size().height)]; //For accessing each pixel
  img.get(0, 0, temp);
  for (int i = 0; i < temp.length; i++) {
    int t = temp[i] & 0xFF; //Convert byte (range: -128;127) to positive int (range: 0; 255)

    temp[i] = (byte) map(constrain(t, min, max), min, max, 0, 255);
  }

  result.put(0, 0, temp);

  return result;
}

Mat toBin(Mat img, int threshold) {
  Mat result = new Mat(img.size(), img.type());
  Imgproc.threshold(img, result, threshold, 255, Imgproc.THRESH_BINARY);
  return result;
}



int countFrame = 0;
float fps = 0.0F;
float t = 0.0F;
float prevtt = 0.0F;

void IncrementFPS()
{
  countFrame++;
  t += millis() - prevtt;
  if (t > 1000.0f)
  {
    fps = countFrame;
    countFrame = 0;
    t = 0;
  }
  prevtt = millis();
}