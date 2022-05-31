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

	private SkinProcessor _skinProcessor;
	private Motors _motors;

	private AtomicReference<float[]> _inputRawBufferRef = new AtomicReference<>();
	private AtomicReference<float[]> _inputProcessedBufferRef = new AtomicReference<>();
	private AtomicReference<float[]> _outputGaussianBufferRef = new AtomicReference<>();
	private AtomicReference<float[]> _outputUniformAverageBufferRef = new AtomicReference<>();

	private int _inputCol;
	private int _inputRow;

	private int _outputCol;
	private int _outputRow;

	private int _motorsCol;
	private int _motorsRow;

	private int _resizeFactorMotorsCol;
	private int _resizeFactorMotorsRow;

	private boolean _updateMotorsConfig = false;
	private int _COM_index;


	@FXML
	private void initialize() {

	}

	public RendererController(int COM, ProcessingConfiguration processingConfig, MotorsConfiguration motorsConfig, SerialConfiguration serialConfig) {
		_COM_index = COM;
		
		_skinProcessor = new SkinProcessor(_COM_index, serialConfig);
		_skinProcessor.Register(this);

		ProcessingConfigurationUpdated(processingConfig);
		MotorsConfigurationUpdated(motorsConfig);

		AnimationTimer animation = new AnimationTimer() {

			@Override
			public void handle(long now) {
				TryPrintBuffer(_inputRawBufferRef, inputRaw, _inputCol, _inputRow);
				TryPrintBuffer(_inputProcessedBufferRef, inputProcessed, _inputCol, _inputRow);
				TryPrintBuffer(_outputGaussianBufferRef, outputGaussian, _outputCol, _outputRow);
				TryPrintBuffer(_outputUniformAverageBufferRef, outputUniformAverage, _outputCol, _outputRow);
			}

		};

		animation.start();

		_skinProcessor.StartProcessing();

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

			newWindow.show();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Called by SkinProcessor
	@Override
	public void BufferUpdate() {
		float[] inputRawBuffer = NaiveInterpolation.ResizeBufferNearest(_skinProcessor.RawBuffer, _skinProcessor.getResizeFactor(), _skinProcessor.getResizeFactor(),  12,21); 
		_inputRawBufferRef.set(inputRawBuffer);

		_inputProcessedBufferRef.set(_skinProcessor.ProcessedBuffer);

		if(!_updateMotorsConfig) {
			_motors.InputBuffer = _skinProcessor.ProcessedBuffer;

			_motors.CalculateGaussianOutput();
			float[] outputGaussianBuffer = NaiveInterpolation.ResizeBufferNearest(_motors.OutputBuffer, _resizeFactorMotorsCol, _resizeFactorMotorsRow,  _motorsCol, _motorsRow);
			_outputGaussianBufferRef.set(outputGaussianBuffer);

			_motors.CalculateUniformAverageOutput();
			float[] outputUniformAverageBuffer = NaiveInterpolation.ResizeBufferNearest(_motors.OutputBuffer, _resizeFactorMotorsCol, _resizeFactorMotorsRow,  _motorsCol, _motorsRow);
			_outputUniformAverageBufferRef.set(outputUniformAverageBuffer);
		}

	}

	private Image BufferToImage(float[] buffer, int col, int row) {
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
			imgView.setImage(BufferToImage(buffer, col, row));
	}


	//Called by UserConfigurationManager
	@Override
	public void MotorsConfigurationUpdated(MotorsConfiguration userConfig) {
		_updateMotorsConfig = true;
		
		_inputCol = userConfig.InputCol;
		_inputRow = userConfig.InputRow;
		_motorsCol = userConfig.OutputCol;
		_motorsRow = userConfig.OutputRow;

		_resizeFactorMotorsCol = _inputCol / _motorsCol;
		_resizeFactorMotorsRow = _inputRow / _motorsRow;

		_outputCol = _motorsCol * _resizeFactorMotorsCol ;
		_outputRow = _motorsRow * _resizeFactorMotorsRow;

		_motors = new Motors(userConfig);
		
		_updateMotorsConfig = false;
	}

	//Called by UserConfigurationManager
	@Override
	public void ProcessingConfigurationUpdated(ProcessingConfiguration userConfig) {
		_skinProcessor.ProcessingConfig = userConfig;
	}

	@Override
	public void SerialConfiguration(SerialConfiguration userConfig) {
		_skinProcessor.SetSerialConfiguration(userConfig);
	}


}
