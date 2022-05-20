package skin;

import processing.core.PApplet;
import processing.core.PImage;


import com.fazecast.jSerialComm.SerialPort;

import controlP5.*;


public class Skin extends PApplet {


	int     DISPLAY_W          = 1800;
	int     DISPLAY_H          = 700;


	// =========== VARIABLES ==================

	SkinProcessor _skinProcessor;
	Motors motors;


	boolean showImage = false;
	PImage finalImage;
	PImage rawFinalImage;
	PImage motorFinalImage;
	PImage motorFinalAverageImage;


	public void settings () {
		//fullScreen();
		size( DISPLAY_W, DISPLAY_H );
		noSmooth();
	}

	public void setup () {
		noStroke( );
		InterfaceSetup();
	}

	public void CreateSkinProcessor(int COM_index)
	{
		_skinProcessor = new SkinProcessor(serial_port);
		motors = new Motors(_skinProcessor.ProcessedBufferCol, _skinProcessor.ProcessedBufferRow, 2, 4);


		_skinProcessor.Register(new ISkinListener() {

			@Override
			public void BufferUpdate() {
				finalImage = bufferToImage(_skinProcessor.ProcessedBuffer, _skinProcessor.ProcessedBufferCol, _skinProcessor.ProcessedBufferRow);


				rawFinalImage = bufferToImage(NaiveInterpolation.ResizeBufferNearest(_skinProcessor.RawBuffer, _skinProcessor.getResizeFactor(), _skinProcessor.getResizeFactor(),  _skinProcessor.RawBufferCol, _skinProcessor.RawBufferRow),
						_skinProcessor.ProcessedBufferCol, _skinProcessor.ProcessedBufferRow);


				int resizeFactorMotorsCol = _skinProcessor.ProcessedBufferCol / 2;
				int resizeFactorMotorsRow = _skinProcessor.ProcessedBufferRow / 4;
				int motorImageCol = 2 * resizeFactorMotorsCol ;
				int motorImageRow = 4 * resizeFactorMotorsRow;

				motors.InputBuffer = _skinProcessor.ProcessedBuffer;
				motors.CalculateGaussianOutput();

				motorFinalImage = bufferToImage(NaiveInterpolation.ResizeBufferNearest(motors.OutputBuffer, resizeFactorMotorsCol, resizeFactorMotorsRow,  2, 4),
						motorImageCol, motorImageRow);

				motors.CalculateUniformAverageOutput();

				motorFinalAverageImage = bufferToImage(NaiveInterpolation.ResizeBufferNearest(motors.OutputBuffer, resizeFactorMotorsCol, resizeFactorMotorsRow,  2, 4),
						motorImageCol, motorImageRow);


				IncrementFPS();
				showImage = true;
			}
		});

		_skinProcessor.StartProcessing();
	}


	public void draw() {
		background(200);

		fill(0);
		text("Skin FPS: " + fps, 10, 15);

		if(_skinProcessor != null && showImage)
		{
			pushMatrix();
			translate(30, 30);
			image(rawFinalImage, 0, 0);
			popMatrix();

			pushMatrix();
			translate((_skinProcessor.ProcessedBufferCol + 30) + 30, 30);
			image(finalImage, 0, 0);
			popMatrix();

			pushMatrix();
			translate(2 * (_skinProcessor.ProcessedBufferCol + 30) +30, 30);
			image(motorFinalImage, 0, 0);
			popMatrix();

			pushMatrix();
			translate(3 * (_skinProcessor.ProcessedBufferCol + 30)+30, 30);
			image(motorFinalAverageImage, 0, 0);
			popMatrix();

			updateValues();
		}

	}


	PImage bufferToImage(int[] buffer, int col, int row) {
		PImage result = createImage( col, row, RGB );
		result.loadPixels();
		for ( int i = 0; i < col*row; i++ ) {
			char b = (char) buffer[i];
			result.pixels[i] = b<<16 | b<<8 | b ;
		}

		result.updatePixels( );

		return result;
	}

	PImage bufferToImage(float[] buffer, int col, int row) {
		PImage result = createImage( col, row, RGB );
		result.loadPixels();
		for ( int i = 0; i < col*row; i++ ) {
			char b = (char) buffer[i];
			result.pixels[i] = b<<16 | b<<8 | b ;
		}

		result.updatePixels( );

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


	int maxImageWidth = 300;

	int min_threshold = 90;
	int max_threshold = 255;

	int serial_port = 0;


	int averageAlgo = 1; //0 : rolling average  |  1 : interpolation with previous frames
	int frames_for_average = 3;
	float interpolation_factor = 0.5f; //0.5 seems to be a good deal


	ControlP5 cp5;
	Accordion accordion;

	RadioButton rCon, rAve;

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

		for(int i = 0; i < SerialPort.getCommPorts().length; i++){
			rCon.addItem(SerialPort.getCommPorts()[i].toString(), i);
		}


		rCon.activate(serial_port);

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


		////////////////////////////////
		//      ACCORDION
		///////////////////////////////

		accordion = cp5.addAccordion("acc")
				.setPosition(4 *(maxImageWidth + 30) + 30, 30)
				.setWidth(280)
				.addItem(g0)
				.addItem(g1)
				.addItem(g2)
				;

		accordion.open(0, 1, 2, 3);
		accordion.setCollapseMode(Accordion.MULTI);
	}

	public void connect(){
		println("Connect to " + serial_port);
		CreateSkinProcessor(serial_port);
	}

	public void launch_calibration() {
		String t= "c\n";
		println("Sending: " + t);
		//skinPort.write(t);
	}


	public void controlEvent(ControlEvent theEvent) {
		if (theEvent.isFrom(rCon)) 
			if ((int)theEvent.getGroup().getValue() != -1)
				serial_port = (int)theEvent.getGroup().getValue();

		if (theEvent.isFrom(rAve)) 
			if ((int)theEvent.getGroup().getValue() != -1)
				averageAlgo = (int)theEvent.getGroup().getValue();



	}

	int _updateValuesCounter = 0;
	public void updateValues() 
	{
		if(_updateValuesCounter++ % 20 == 0) {
			_skinProcessor.MinThreshold = min_threshold;
			_skinProcessor.MaxThreshold = max_threshold;
			_skinProcessor.Noise_averageAlgo = averageAlgo;
			_skinProcessor.Noise_framesForAverage = frames_for_average;
			_skinProcessor.Noise_interpolationFactor = interpolation_factor;

		}
	}
}


