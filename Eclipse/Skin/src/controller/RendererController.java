package controller;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.ISkinListener;
import model.Motors;
import model.Motors.MotorsConfiguration;
import model.NaiveInterpolation;
import model.SkinProcessor;
import model.UserConfigurationManager;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinSerialPort;
import model.SkinSerialPort.SerialConfiguration;

public class RendererController implements UserConfigurationManager.UserObserver, ISkinListener{
	@FXML
	private ImageView inputRaw;
	@FXML
	private ImageView inputProcessed;
	@FXML
	private ImageView outputGaussian;
	@FXML
	private ImageView outputUniformAverage;

	private SkinSerialPort _skinSerialPort;
	private SkinProcessor _skinProcessor;
	private Motors _motors;

	private AtomicReference<float[]> _inputRawBufferRef = new AtomicReference<>();
	private AtomicReference<float[]> _inputProcessedBufferRef = new AtomicReference<>();
	private AtomicReference<float[]> _outputGaussianBufferRef = new AtomicReference<>();
	private AtomicReference<float[]> _outputUniformAverageBufferRef = new AtomicReference<>();



	private int _outputMotorsImageCol;
	private int _outputMotorsImageRow;


	private int _resizeFactorMotorsImageCol;
	private int _resizeFactorMotorsImageRow;


	private ProcessingConfiguration _processingConfig;
	private MotorsConfiguration _motorsConfig;

	public RendererController(int COM, ProcessingConfiguration processingConfig, MotorsConfiguration motorsConfig, SerialConfiguration serialConfig) {
		_skinProcessor = new SkinProcessor();
		_skinProcessor.Register(this);
		_skinSerialPort = new SkinSerialPort(_skinProcessor, COM, serialConfig);
		
		ProcessingConfigurationUpdated(processingConfig);
		MotorsConfigurationUpdated(motorsConfig);

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				TryPrintBuffer(_inputRawBufferRef, inputRaw, _processingConfig.ProcessedBufferCol(), _processingConfig.ProcessedBufferRow());
				TryPrintBuffer(_inputProcessedBufferRef, inputProcessed, _processingConfig.ProcessedBufferCol(), _processingConfig.ProcessedBufferRow());
				TryPrintBuffer(_outputGaussianBufferRef, outputGaussian, _outputMotorsImageCol, _outputMotorsImageRow);
				TryPrintBuffer(_outputUniformAverageBufferRef, outputUniformAverage, _outputMotorsImageCol, _outputMotorsImageRow);
			}

		}.start();
		

		_skinSerialPort.StartReading();
		LaunchWindow();
	}



	private void LaunchWindow() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Renderer.fxml"));
			loader.setController(this);
			Parent root = loader.load();

			Scene scene = new Scene(root,1000,600);

			Stage newWindow = new Stage();
			newWindow.setTitle("Renderer");
			newWindow.setScene(scene);
			
			newWindow.setOnCloseRequest(e -> _skinSerialPort.StopReading());


			newWindow.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Called by SkinProcessor
	@Override
	public void BufferUpdate() {
		try {

		float[] inputRawBuffer = NaiveInterpolation.ResizeBufferNearest(_skinProcessor.RawBuffer, _processingConfig.ResizeFactor, _processingConfig.ResizeFactor,  _processingConfig.RawBufferCol, _processingConfig.RawBufferRow); 
		_inputRawBufferRef.set(inputRawBuffer);

		_inputProcessedBufferRef.set(_skinProcessor.ProcessedBuffer);

		_motors.InputBuffer = _skinProcessor.ProcessedBuffer;

			_motors.CalculateGaussianOutput();

			float[] outputGaussianBuffer = NaiveInterpolation.ResizeBufferNearest(_motors.OutputBuffer, _resizeFactorMotorsImageCol, _resizeFactorMotorsImageRow,  _motorsConfig.OutputCol, _motorsConfig.OutputRow);
			_outputGaussianBufferRef.set(outputGaussianBuffer);

			_motors.CalculateUniformAverageOutput();
			float[] outputUniformAverageBuffer = NaiveInterpolation.ResizeBufferNearest(_motors.OutputBuffer, _resizeFactorMotorsImageCol, _resizeFactorMotorsImageRow,  _motorsConfig.OutputCol, _motorsConfig.OutputRow);
			_outputUniformAverageBufferRef.set(outputUniformAverageBuffer);

		} catch (Exception e) {/*The user changed the motor configuration while we were calculated motors output*/}


	}

	private Image BufferToImage(float[] buffer, int col, int row)  throws Exception {
		WritableImage result = new WritableImage(col, row);
		PixelWriter pixelWriter = result.getPixelWriter();
		for(int i = 0; i < col; i++)
			for(int j = 0; j < row; j++) {
				float c = buffer[j*col + i] / 255.0f;
				pixelWriter.setColor(i, j, new Color(c, c, c, 1));
			}
		return result;
	}

	private void TryPrintBuffer(AtomicReference<float[]> bufferRef, ImageView imgView, int col, int row) {
		float[] buffer = bufferRef.getAndSet(null);
		if(buffer != null)
			try {
				imgView.setImage(BufferToImage(buffer, col, row));
			} catch (Exception e) {/* Memory issue because the sized of the buffer was changed during the print operation*/}
	}


	//Called by UserConfigurationManager
	@Override
	public void MotorsConfigurationUpdated(MotorsConfiguration userConfig) {
		_motorsConfig = userConfig;
		_resizeFactorMotorsImageCol =  userConfig.InputCol / userConfig.OutputCol;
		_resizeFactorMotorsImageRow = userConfig.InputRow / userConfig.OutputRow;

		_outputMotorsImageCol = userConfig.OutputCol * _resizeFactorMotorsImageCol ;
		_outputMotorsImageRow = userConfig.OutputRow * _resizeFactorMotorsImageRow;


		new Thread(()-> _motors = new Motors(userConfig)).start();
	}

	//Called by UserConfigurationManager
	@Override
	public void ProcessingConfigurationUpdated(ProcessingConfiguration userConfig) {
		_processingConfig = userConfig;
		_skinProcessor.ProcessingConfig = userConfig;
	}

	//Called by UserConfigurationManager
	@Override
	public void SerialConfigurationUpdated(SerialConfiguration userConfig) {
		_skinSerialPort.SetSerialConfiguration(userConfig);
		
	}


}
