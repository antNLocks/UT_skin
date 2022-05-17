import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

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
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MucaRendererLog extends PApplet {













// =========== CONSTANTS ==================
int     SKIN_COLS          = 12;
int     SKIN_ROWS          = 21;
int     SKIN_CELLS         = SKIN_COLS * SKIN_ROWS;

int     SERIAL_PORT        = 1; 
int     SERIAL_RATE        = 230400;

int     DISPLAY_W          = 1200;
int     DISPLAY_H          = 700;


// =========== VARIABLES ==================
Serial  skinPort;
OpenCV opencv; // openCV object for interpolation


int resizeFactor = 30;
Size finalSize = new Size(resizeFactor * SKIN_COLS, resizeFactor * SKIN_ROWS);


int min_threshold = 90;
int max_threshold = 255;


int averageAlgo = 1; //0 : rolling average  |  1 : interpolation with previous frames
int frames_for_average = 3;
float interpolation_factor = 0.5f; //0.5 seems to be a good deal


int binary_threshold = 153;
boolean binary = false;

PImage finalImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);
PImage rawFinalImage = createImage((int) finalSize.width, (int) finalSize.height, RGB);

PrintWriter logFile;
String logFilePath = "Log_" + month() + " _" + day() +"/" +hour()+"-"+minute()+"-"+second()+".hx";

public void settings () { 
  size( DISPLAY_W, DISPLAY_H );
  noSmooth();
}

public void setup () { 
  noStroke( );
  printArray(Serial.list());

  opencv = new OpenCV(this, SKIN_COLS, SKIN_ROWS);
  logFile = createWriter(logFilePath);

  InterfaceSetup();

  for ( int i = 0; i < finalSize.width * finalSize.height; i++ ) {
    finalImage.pixels[i] = color(0, 153, 0); //A green image until we receive data from the arduino board
    rawFinalImage.pixels[i] = color(0, 153, 0); //A green image until we receive data from the arduino board
  }

  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    public void run() {
      for (int i = 0; i < index; i++) {
        logFile.print(data[i]);
        logFile.flush();
      }

      logFile.close();
    }
  }));
}



public void draw() {
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


int counter = 0;
char[] data = new char[4554000];
int index = 0;
public int[] readRawBuffer() {
  int[] result = null;
  if ( skinPort != null && skinPort.available( ) > 0 ) {
    byte[] skinBuffer = skinPort.readBytesUntil(0x00);

    if (skinBuffer != null && skinBuffer.length == SKIN_CELLS+1) {
      result = new int[SKIN_CELLS];
      for (int i = 0; i < SKIN_CELLS; i++) {
        result[i] = skinBuffer[i] & 0xFF;
        data[index] = (char) skinBuffer[i];
        index++;
      }
      data[index] = (char) 0x00;
      index++;
    }
  }
  return result;
}




ArrayDeque<int[]> rawBuffers = new ArrayDeque<int[]>();

public float[] averageBufferOverTime_rollingAverage(int[] actualRawBuffer, int nbFrames) {
  float[] result = new float[SKIN_CELLS];

  rawBuffers.add(actualRawBuffer);

  while (rawBuffers.size() > nbFrames)
  rawBuffers.poll();


  Iterator<int[]> it = rawBuffers.iterator();
  while (it.hasNext()) {
    int[] rwB = it.next();
    for (int i = 0; i < SKIN_CELLS; i++)
    result[i] += rwB[i] / (float) rawBuffers.size();
  }

  return result;
}


float[] previousRawBuffer = null;

public float[] averageBufferOverTime_interpolationPreviousFrames(int[] actualRawBuffer, float k) {
  float[] result = new float[SKIN_CELLS];

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

public PImage bufferToImage(int[] buffer) {
  PImage result = createImage( SKIN_COLS, SKIN_ROWS, RGB );
  for ( int i = 0; i < SKIN_CELLS; i++ ) 
  result.pixels[i] = color(buffer[i]);

  result.updatePixels( );

  return result;
}

public PImage bufferToImage(float[] buffer) {
  PImage result = createImage( SKIN_COLS, SKIN_ROWS, RGB );
  for ( int i = 0; i < SKIN_CELLS; i++ ) 
  result.pixels[i] = color(buffer[i]);

  result.updatePixels( );

  return result;
}

public Mat imageToMat(PImage img) {
  opencv.loadImage(img);
  Mat result = opencv.getGray();
  return result;
}

public Mat resizeMat(Mat img, Size destSize, int algo) {
  Mat skinImageResized = new Mat(destSize, img.type()); // new matrix to store resize image
  Imgproc.resize(img, skinImageResized, destSize, 0, 0, algo); // resize // INTER_LANCZOS4
  return skinImageResized;
}

public Mat thresholdMapping(Mat img, float min, float max) {
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

public Mat toBin(Mat img, int threshold) {
  Mat result = new Mat(img.size(), img.type());
  Imgproc.threshold(img, result, threshold, 255, Imgproc.THRESH_BINARY);
  return result;
}



int countFrame = 0;
float fps = 0.0F;
float t = 0.0F;
float prevtt = 0.0F;

public void IncrementFPS()
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



ControlP5 cp5;
Accordion accordion;

RadioButton rCon, rAve, rBin;

public void InterfaceSetup() {
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
    .setPosition((int) (2.0f*finalSize.width + 90), 30)
    .setWidth(280)
    .addItem(g0)
    .addItem(g1)
    .addItem(g2)
    .addItem(g3)
    ;

  accordion.open(0, 1, 2, 3);
  accordion.setCollapseMode(Accordion.MULTI);
}

public void connect(){
  skinPort = new Serial( this, Serial.list( )[ SERIAL_PORT ], SERIAL_RATE );
}

public void launch_calibration() {
  String t= "c\n";
  println("Sending: " + t);
  skinPort.write(t);
}


public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isFrom(rCon)) 
    if (PApplet.parseInt(theEvent.getGroup().getValue()) != -1)
      SERIAL_PORT = PApplet.parseInt(theEvent.getGroup().getValue());
  
  if (theEvent.isFrom(rAve)) 
    if (PApplet.parseInt(theEvent.getGroup().getValue()) != -1)
      averageAlgo = PApplet.parseInt(theEvent.getGroup().getValue());

  if (theEvent.isFrom(rBin)) 
    binary = PApplet.parseInt(theEvent.getGroup().getValue()) != -1;
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "MucaRendererLog" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
