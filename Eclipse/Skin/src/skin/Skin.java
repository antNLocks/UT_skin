package skin;

import processing.core.PApplet;
import processing.core.PImage;
import processing.serial.Serial;

import java.util.ArrayDeque;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import gab.opencv.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Core;

import controlP5.*;


public class Skin extends PApplet {
	
	

	// =========== CONSTANTS ==================
	int     SKIN_COLS          = 12;
	int     SKIN_ROWS          = 21;
	int     SKIN_CELLS         = SKIN_COLS * SKIN_ROWS;

	int     SERIAL_PORT        = 1; 
	int     SERIAL_RATE        = 230400;

	int     DISPLAY_W          = 1500;
	int     DISPLAY_H          = 700;


	// =========== VARIABLES ==================
	Serial  skinPort;
	OpenCV opencv; // openCV object for interpolation


	int resizeFactor = 25;
	Size finalSize = new Size((SKIN_COLS-1)* resizeFactor + 1, (SKIN_ROWS-1)* resizeFactor + 1);


	int min_threshold = 0;
	int max_threshold = 255;


	int averageAlgo = 1; //0 : rolling average  |  1 : interpolation with previous frames
	int frames_for_average = 3;
	float interpolation_factor = 0.5f; //0.5 seems to be a good deal


	int binary_threshold = 153;
	boolean binary = false;

	PImage finalImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);
	PImage rawFinalImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);
	PImage motorFinalImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);
	PImage motorFinalAverageImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);



	Motors motors = new Motors((int) finalSize.width, (int) finalSize.height, 2, 4);

	BufferedReader logFile;
	String logFilePath = "Log_" + month() + "_11/" +"10-31-53.hx";

	public void settings () {
	  //fullScreen();
	  size( DISPLAY_W, DISPLAY_H );
	  noSmooth();
	}

	char[] fileDataArray;
	public void setup () { 
	  noStroke( );
	  printArray(Serial.list());

	  //logFile = createReader(logFilePath);
	  opencv = new OpenCV(this, SKIN_COLS, SKIN_ROWS);

	  InterfaceSetup();

	  for ( int i = 0; i < finalSize.width * finalSize.height; i++ ) {
	    //finalImage.pixels[i] = color(0, 153, 0); //A green image until we receive data from the arduino board
	    rawFinalImage.pixels[i] = color(0, 153, 0); //A green image until we receive data from the arduino board
	  }

	  List<String> fileData = new ArrayList<String>();
	  try {
	    fileData = Files.readAllLines(Paths.get("D:\\Users\\aLocks\\Desktop\\UT\\MucaRendererReader\\Log_5 _11\\10-31-53.hx"));
	  }
	  catch(IOException e) {
	    println(e);
	  }

	  println("Fichier lu");

	  String fileAllLines = new String();

	  Iterator<String> it = fileData.iterator();
	  while (it.hasNext()) 
	    fileAllLines += it.next() + '\n';

	  println("Concatenation des lignes");
	  fileDataArray = fileAllLines.toCharArray();
	  println("Taille des donnees : " + fileData.size());
	}


	int[] rawBuffer = new int[252];
	int index = 0;

	public void draw() {
	  int[] rawBuffer = readRawBuffer( );
	  delay(20);

	  if ( rawBuffer != null ) {

	    float[] processBuffer =
	      thresholdMapping(
	      resizeBufferBilinear(
	      averageAlgo == 0 ? averageBufferOverTime_rollingAverage(rawBuffer, frames_for_average) : averageBufferOverTime_interpolationPreviousFrames(rawBuffer, interpolation_factor), 
	      resizeFactor, SKIN_COLS, SKIN_ROWS), 
	      min_threshold, max_threshold);

	    finalImage = bufferToImage(processBuffer, (int) finalSize.width, (int) finalSize.height);

	    opencv.toPImage(resizeMat(imageToMat(bufferToImage(rawBuffer, SKIN_COLS, SKIN_ROWS), SKIN_COLS, SKIN_ROWS), finalSize, Imgproc.INTER_NEAREST), rawFinalImage);

	    motors.setInputBuffer(processBuffer);
	    motors.calculateGaussianOutput();
	    PImage motorImage = bufferToImage(motors.getOutputBuffer(), 2, 4);

	    Mat resizedMotorMat = resizeMat(imageToMat(motorImage, 2, 4), finalSize, 0);
	    opencv.toPImage(resizedMotorMat, motorFinalImage);
	    
	    motors.calculateAverageOutput();
	    PImage motorAverageImage = bufferToImage(motors.getOutputBuffer(), 2, 4);

	    Mat resizedMotorAverageMat = resizeMat(imageToMat(motorAverageImage, 2, 4), finalSize, 0);
	    opencv.toPImage(resizedMotorAverageMat, motorFinalAverageImage);

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

	  pushMatrix();
	  translate( 2 * (int) finalSize.width + 90, 30);
	  image(motorFinalImage, 0, 0);
	  popMatrix();
	  
	  pushMatrix();
	  translate( 3 * (int) finalSize.width + 120, 30);
	  image(motorFinalAverageImage, 0, 0);
	  popMatrix();

	  fill(0);
	  text("Skin FPS: " + fps, 10, 15);
	}



	int indexData = 0;
	int[] readRawBuffer() {
	  int[] result = null;
	  if ( indexData < fileDataArray.length - SKIN_CELLS ) {
	    byte[] skinBuffer = new byte[SKIN_CELLS];
	    int indexBuffer = 0;
	    byte b;
	    while ((b = (byte) fileDataArray[indexData++]) != 0x00)
	      skinBuffer[indexBuffer++] = b;

	    result = new int[SKIN_CELLS];
	    for (int i = 0; i < SKIN_CELLS; i++) {
	      result[i] = skinBuffer[i] & 0xFF;
	    }
	  } else
	    indexData = 0;
	  return result;
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

	float[] resizeBufferBilinear(float[] rawBuffer, int resizeFactor, int col, int row) {
	  float[] result = new float[((col-1)*resizeFactor+1)*((row-1)*resizeFactor+1)];

	  float[][] result2d = new float[(row-1)*resizeFactor+1][(col-1)*resizeFactor+1];

	  float[][] colInter = new float[col][(row-1)*resizeFactor+1];

	  float[][] rawBuffer2d = new float[col][row];

	  for (int i = 0; i < col*row; i++)
	    rawBuffer2d[i % col][i / col] = rawBuffer[i];

	  for (int i = 0; i < col; i++)
	    colInter[i] = interLinear(rawBuffer2d[i], resizeFactor);

	  float[][] colInterT = new float[(row-1)*resizeFactor+1][col];
	  for (int i = 0; i < colInterT.length; i++)
	    for (int j = 0; j < colInterT[i].length; j++)
	      colInterT[i][j] = colInter[j][i];

	  for (int i = 0; i < (row-1)*resizeFactor+1; i++)
	    result2d[i] = interLinear(colInterT[i], resizeFactor);

	  for (int i = 0; i < result2d.length; i++)
	    for (int j = 0; j < result2d[i].length; j++)
	      result[i*result2d[i].length + j] = result2d[i][j];

	  return result;
	}

	float[] interLinear(float[] column, int resizeFactor) {
	  float[] result = new float[(column.length - 1)*resizeFactor + 1];

	  for (int i = 0; i < column.length - 1; i++)
	    for (int j = 0; j < resizeFactor; j++) {
	      float t = j / (float) resizeFactor;
	      result[i*resizeFactor+j] = (1-t) * column[i] + t * column[i+1];
	    }

	  result[(column.length-1)*resizeFactor] = column[column.length - 1];
	  return result;
	}

	PImage bufferToImage(int[] buffer, int col, int row) {
	  PImage result = createImage( col, row, RGB );
	  for ( int i = 0; i < SKIN_CELLS; i++ ) 
	    result.pixels[i] = color(buffer[i]);

	  result.updatePixels( );

	  return result;
	}

	PImage bufferToImage(float[] buffer, int col, int row) {
	  PImage result = createImage( col, row, RGB );
	  for ( int i = 0; i < col*row; i++ ) 
	    result.pixels[i] = color(buffer[i]);

	  result.updatePixels( );

	  return result;
	}

	Mat imageToMat(PImage img, int col, int row) {
	  opencv = new OpenCV(this, col, row);

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

	float[] thresholdMapping(float[] buffer, float min, float max) {
	  float[] result = new float[buffer.length];

	  for (int i = 0; i < buffer.length; i++) 
	    result[i] = map(constrain(buffer[i], min, max), min, max, 0, 255);

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
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { skin.Skin.class.getName() });
	}
	


ControlP5 cp5;
Accordion accordion;

RadioButton rCon, rAve, rBin;

void InterfaceSetup() {
  cp5 = new ControlP5(this);
  
  Group g0 = cp5.addGroup("Serial Communication")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(70)
    ;
  
   rCon = cp5.addRadioButton("Serial Port") // INTER_NEAREST // 1 INTER_LINEAR  // 2 INTER_CUBIC  3 // INTER_AREA  4 // INTER_LANCZOS4
    .setPosition(10, 15)
    .setLabel("lol")
    .setSize(10, 10)
    .setColorForeground(color(120))
    .setColorLabel(color(255))
    .setItemsPerRow(1)
    .setSpacingColumn(50)
    .moveTo(g0)
    ;
    
    for(int i = 0; i < Serial.list().length; i++){
      rCon.addItem(Serial.list()[i], i);
    }
    
    rCon.activate(SERIAL_PORT);

  for (Toggle t : rCon.getItems()) {
    t.getCaptionLabel().setColorBackground(color(255, 0));
    t.getCaptionLabel().getStyle().moveMargin(-7, 0, 0, -3);
    t.getCaptionLabel().getStyle().movePadding(7, 0, 0, 3);
    t.getCaptionLabel().getStyle().backgroundWidth = 40;
    t.getCaptionLabel().getStyle().backgroundHeight = 13;
  }
  
  
 cp5.addButton("connect")
    .setPosition(100, 15)
    .setSize(50, 20)
    .moveTo(g0)
    ;

  ////////////////////////////////
  //      DEFAULT SETTINGS
  ///////////////////////////////
  Group g1 = cp5.addGroup("Value Range")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(80)
    ;

  cp5.addButton("launch_calibration")
    .setPosition(10, 10)
    .setSize(100, 20)
    .moveTo(g1)
    ;


  cp5.addSlider("min_threshold")
    .setPosition(10, 40)
    .setRange(0, 255)
    .moveTo(g1)
    ;

  cp5.addSlider("max_threshold")
    .setPosition(10, 55)
    .setRange(0, 255)
    .moveTo(g1)
    ;


  Group g2 = cp5.addGroup("Noise reduction")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(90)
    ;

  cp5.addTextlabel("Average algorithme :")
    .setText("Average algorithme :")        
    .setPosition(10, 10)
    .moveTo(g2);
  ;

  rAve = cp5.addRadioButton("AverageAlgo") 
    .setPosition(10, 25)
    .setLabel("lol2")
    .setSize(10, 10)
    .setColorForeground(color(120))
    .setColorLabel(color(255))
    .setItemsPerRow(2)
    .setSpacingColumn(100)
    .addItem("rolling_average", 0)
    .addItem("interpolation_previous_frames", 1)
    .activate(averageAlgo)
    .moveTo(g2)
    ;

  cp5.addSlider("frames_for_average")
    .setPosition(10, 55)
    .setRange(1, 10)
    .moveTo(g2)
    ;

  cp5.addSlider("interpolation_factor")
    .setPosition(10, 70)
    .setRange(0, 1)
    .moveTo(g2)
    ;


  Group g3 = cp5.addGroup("Binary")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(60);
  ;

  rBin = cp5.addRadioButton("BinaryRadioButton")
    .setPosition(10, 10)
    .setSize(10, 10)
    .addItem("enable_binary", 1)
    .moveTo(g3)
    ;

  cp5.addSlider("binary_threshold")
    .setPosition(10, 30)
    .setRange(0, 255)
    .moveTo(g3)
    ;


  ////////////////////////////////
  //      ACCORDION
  ///////////////////////////////

  accordion = cp5.addAccordion("acc")
    .setPosition((int) (4.0*finalSize.width + 150), 30)
    .setWidth(280)
    .addItem(g0)
    .addItem(g1)
    .addItem(g2)
    .addItem(g3)
    ;

  accordion.open(0, 1, 2, 3);
  accordion.setCollapseMode(Accordion.MULTI);
}

void connect(){
  skinPort = new Serial( this, Serial.list( )[ SERIAL_PORT ], SERIAL_RATE );
}

void launch_calibration() {
  String t= "c\n";
  println("Sending: " + t);
  skinPort.write(t);
}


void controlEvent(ControlEvent theEvent) {
  if (theEvent.isFrom(rCon)) 
    if ((int)theEvent.getGroup().getValue() != -1)
      SERIAL_PORT = (int)theEvent.getGroup().getValue();
  
  if (theEvent.isFrom(rAve)) 
    if ((int)theEvent.getGroup().getValue() != -1)
      averageAlgo = (int)theEvent.getGroup().getValue();

  if (theEvent.isFrom(rBin)) 
    binary = (int)theEvent.getGroup().getValue() != -1;
}

}


