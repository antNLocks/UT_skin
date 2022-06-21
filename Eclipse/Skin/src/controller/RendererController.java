package controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import application.Main;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.FPSAnalyser;
import model.Motors;
import model.Motors.MotorsConfiguration;
import model.NaiveInterpolation;
import model.SkinProcessor;
import model.UserConfigurationManager;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinSerialPort;
import model.SkinSerialPort.SerialConfiguration;
import model.ThreadProcess.IProcessListener;

public class RendererController implements UserConfigurationManager.UserObserver{

	private Stage _window;
	private Runnable _onExit;
	private int _minWidth = 700;
	private int _minHeight = 300;

	@FXML
	private Label drawingFPS;

	@FXML
	private ImageView inputProcessed;

	@FXML
	private ImageView inputRaw;

	@FXML
	private Label motorsGaussianFPS;

	@FXML
	private Label motorsUniformFPS;

	@FXML
	private ImageView outputGaussian;

	@FXML
	private ImageView outputUniformAverage;

	@FXML
	private Label processedSignalFPS;

	@FXML
	private Label rawSignalFPS;

	@FXML
	private Button connectToBHapticsBtn;

	private SkinSerialPort _skinSerialPort;
	private SkinProcessor _skinProcessor;
	private Motors _motors;

	private Socket _bhapticsServer = null;
	private IProcessListener _bhapticsSender = null;

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

	private FPSAnalyser _fpsDrawingAnalyser = new FPSAnalyser();

	public RendererController(String title, int COM, ProcessingConfiguration processingConfig, MotorsConfiguration motorsConfig, SerialConfiguration serialConfig, Runnable onExit) {
		_onExit = onExit;
		LaunchWindow(title);

		_skinSerialPort = new SkinSerialPort(COM, serialConfig);
		_skinSerialPort.Register(() -> {
			if(_skinSerialPort.RawOutputBuffer.get() != null)
				_inputRawBufferRef.set(NaiveInterpolation.ResizeBufferNearest(
						_skinSerialPort.RawOutputBuffer.get(), 
						_processingConfig.ResizeFactorCol, _processingConfig.ResizeFactorRow,
						_processingConfig.RawBufferCol, _processingConfig.RawBufferRow)); 
		});

		_skinProcessor = new SkinProcessor();
		_skinProcessor.Register(() -> {
			if(_skinProcessor.ProcessedOutputBuffer.get() != null)
				_inputProcessedBufferRef.set(_skinProcessor.ProcessedOutputBuffer.get());

			_skinProcessor.RawInputBuffer.set(_skinSerialPort.RawOutputBuffer.get());
		});

		ProcessingConfigurationUpdated(processingConfig);
		MotorsConfigurationUpdated(motorsConfig);

		new AnimationTimer() {

			@Override
			public void handle(long now) {
				TryPrintBuffer(_inputRawBufferRef, inputRaw, _processingConfig.ProcessedBufferCol(), _processingConfig.ProcessedBufferRow());
				TryPrintBuffer(_inputProcessedBufferRef, inputProcessed, _processingConfig.ProcessedBufferCol(), _processingConfig.ProcessedBufferRow());
				TryPrintBuffer(_outputGaussianBufferRef, outputGaussian, _outputMotorsImageCol, _outputMotorsImageRow);
				TryPrintBuffer(_outputUniformAverageBufferRef, outputUniformAverage, _outputMotorsImageCol, _outputMotorsImageRow);

				try {
					rawSignalFPS.setText("Refresh rate : " + (int) _skinSerialPort.GetProcessFPS() + " Hz");
					processedSignalFPS.setText("Refresh rate : " + (int) _skinProcessor.GetProcessFPS() + " Hz");
					motorsGaussianFPS.setText("Refresh rate : " + (int) _motors.GetProcessFPS() + " Hz");
					motorsUniformFPS.setText("Refresh rate : " + (int) _motors.GetProcessFPS() + " Hz");
					drawingFPS.setText("Drawing rate : " + (int) _fpsDrawingAnalyser.GetFPS() + " fps");
				} catch(NullPointerException e) {}

				_fpsDrawingAnalyser.Tick();
			}

		}.start();

		_skinProcessor.StartThread();
		_skinSerialPort.StartThread();
	}



	private void LaunchWindow(String title) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Renderer.fxml"));
			loader.setController(this);
			Parent root = loader.load();

			Scene scene = new Scene(root,10,10);

			_window = new Stage();
			_window.setTitle(title);
			_window.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("al.png")));

			_window.setScene(scene);

			_window.setOnCloseRequest(e -> {
				_motors.StopThread();
				_skinProcessor.StopThread();
				_skinSerialPort.StopThread();
				if(_bhapticsServer != null)
					try {
						_bhapticsServer.close();
					} catch (IOException e1) { e1.printStackTrace(); }

				_onExit.run();
			});


			_window.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void instantiateMotors() {
		if(_motors != null)
			_motors.StopThread();


		_motors = new Motors(_motorsConfig);
		_motors.Register(() -> {
			try {
				_outputGaussianBufferRef.set(NaiveInterpolation.ResizeBufferNearest(
						_motors.GaussianOutputBuffer.get(), _resizeFactorMotorsImageCol, _resizeFactorMotorsImageRow,  _motorsConfig.OutputCol, _motorsConfig.OutputRow));

				_outputUniformAverageBufferRef.set(NaiveInterpolation.ResizeBufferNearest(
						_motors.UniformOutputBuffer.get(), _resizeFactorMotorsImageCol, _resizeFactorMotorsImageRow,  _motorsConfig.OutputCol, _motorsConfig.OutputRow));

			} catch(NullPointerException e) {}
			_motors.InputBuffer.set(_skinProcessor.ProcessedOutputBuffer.get());
		});

		if(_bhapticsSender != null)
			_motors.Register(_bhapticsSender);

		_motors.StartThread();		
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

		new Thread(()-> instantiateMotors()).start();

		_window.setMinWidth(Math.max(_processingConfig.ProcessedBufferCol()*2 + _outputMotorsImageCol*2 + 200, _minWidth));
		_window.setMinHeight(Math.max(Math.max(_processingConfig.ProcessedBufferRow(), _outputMotorsImageRow) + 200, _minHeight));

	}

	//Called by UserConfigurationManager
	@Override
	public void ProcessingConfigurationUpdated(ProcessingConfiguration userConfig) {
		_processingConfig = userConfig;
		_skinProcessor.ProcessingConfig = userConfig;

		_window.setMinWidth(Math.max(_processingConfig.ProcessedBufferCol()*2 + _outputMotorsImageCol*2 + 200, _minWidth));
		_window.setMinHeight(Math.max(Math.max(_processingConfig.ProcessedBufferRow(), _outputMotorsImageRow) + 200, _minHeight));
	}

	//Called by UserConfigurationManager
	@Override
	public void SerialConfigurationUpdated(SerialConfiguration userConfig) {
		_skinSerialPort.SetSerialConfiguration(userConfig);

	}


	@FXML
	private void onLaunchCalibration() {
		_skinSerialPort.Send("c");
	}

	@FXML
	private void onConnectToBHaptics() {
		try{   
			_bhapticsServer = new Socket((String) null, 51470);  

			OutputStream os = _bhapticsServer.getOutputStream();

			os.write(0xFF);
			os.write(0xFF); //Just to be sure
			os.write((byte) (_motorsConfig.SleepingTime + 8)); //Duration of a frame

			for(int j = 0; j < _motorsConfig.OutputRow; j++)
				for(int i = 0; i < _motorsConfig.OutputCol; i++)
					os.write((byte) (j*_motorsConfig.OutputCol + i));

			os.write(0xFF);
			os.flush();

			_bhapticsSender = () -> {
				float[] output = _motors.GaussianOutputBuffer.get();
				if(output != null) {
					try {
						for(int i = 0; i < output.length; i++) 
							os.write((byte) ((int) Math.min(254, output[i])));

						os.write(0xFF); //End of frame
						os.flush();
					} catch (IOException e) {
						e.printStackTrace();
						_motors.Unregister(_bhapticsSender);

						Platform.runLater(() -> {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Connection Error");
							alert.setHeaderText("Socket write error");
							alert.setContentText("The BHapticsSkinServer was closed.\nPlease start the BHaptics Player first, then launch the BHapticsSkinServer and try again.");
							alert.initOwner(Main.Stage);
							alert.showAndWait();

							connectToBHapticsBtn.setDisable(false);
						});

					}
				}

			};
			connectToBHapticsBtn.setDisable(true);
			_motors.Register(_bhapticsSender);

		}catch(Exception e){
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Connection Error");
			alert.setHeaderText("Unable to connect to the Bhaptics skin server");
			alert.setContentText("Please start the BHaptics Player first, then launch the BHapticsSkinServer and try again.");
			alert.initOwner(Main.Stage);
			alert.show();
		}  
	}
}
