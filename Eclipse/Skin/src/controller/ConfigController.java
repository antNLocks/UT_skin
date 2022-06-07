package controller;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.fazecast.jSerialComm.SerialPort;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import model.UserConfigurationManager;
import model.Motors.MotorsConfiguration;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinSerialPort.SerialConfiguration;

public class ConfigController implements UserConfigurationManager.UserObserver {

	private UserConfigurationManager _userConfigManager = new UserConfigurationManager();

	@FXML 
	private ChoiceBox<String> COM;
	private ObservableList<String> _ports;


	@FXML
	private ToggleGroup averageAlgo;

	@FXML
	private ChoiceBox<Integer> baudrateCB;

	@FXML
	private Button connectAndRender;

	@FXML
	private TextField frameSeparatorByteTF;

	@FXML
	private Slider framesForAverageSlider;

	@FXML
	private Label framesForAverageView;

	@FXML
	private Slider gaussianDeviationSlider;

	@FXML
	private Label gaussianDeviationView;

	@FXML
	private Slider gaussianNormalisationFactorSlider;

	@FXML
	private Label gaussianNormalisationFactorView;

	@FXML
	private Slider interpolationFactorSlider;

	@FXML
	private RadioButton interpolationFactorT;

	@FXML
	private Label interpolationFactorView;

	@FXML
	private Slider maxThresholdSlider;

	@FXML
	private Label maxThresholdView;

	@FXML
	private Slider minThresholdSlider;

	@FXML
	private Label minThresholdView;

	@FXML
	private TextField motorsColTF;

	@FXML
	private TextField motorsRowTF;

	@FXML
	private Label processedResolutionView;

	@FXML
	private TextField rawColTF;

	@FXML
	private Label rawResolutionView;

	@FXML
	private TextField rawRowTF;

	@FXML
	private Slider resizeFactorSLider;

	@FXML
	private Label resizeFactorView;

	@FXML
	private RadioButton rollingAverageT;

	@FXML
	private Slider uniformDeviationSlider;

	@FXML
	private Label uniformDeviationView;

	@FXML
	private Slider uniformNormalisationFactorSlider;

	@FXML
	private Label uniformNormalisationFactorView;

	private final String INVALID_STYLE = "-fx-text-box-border: #FF0000; -fx-focus-color: #FF0000;";
	private final String VALID_STYLE = "-fx-text-box-border: #00FF00; -fx-focus-color: #00FF00;";


	private ChangeListener<Number> processingConfigSliderListener = (observable, oldValue, newValue) -> {
		ProcessingConfiguration config = _userConfigManager.GetProcessingConfiguration();

		config.MinThreshold = (int)	minThresholdSlider.getValue();
		config.MaxThreshold = (int)	maxThresholdSlider.getValue();
		config.Noise_framesForAverage = (int) framesForAverageSlider.getValue();
		config.Noise_interpolationFactor = (float) interpolationFactorSlider.getValue();

		_userConfigManager.SetProcessingConfiguration(config);
	};

	private ChangeListener<Number> motorsConfigSliderListener = (observable, oldValue, newValue) -> {
		MotorsConfiguration config = _userConfigManager.GetMotorsConfiguration();

		config.DeviationGaussian = (int) gaussianDeviationSlider.getValue();
		config.DeviationUniform = (int) uniformDeviationSlider.getValue();
		config.NormalisationFactorGaussian = (float) gaussianNormalisationFactorSlider.getValue();
		config.NormalisationFactorUniform = (float) uniformNormalisationFactorSlider.getValue();

		_userConfigManager.SetMotorsConfiguration(config);
	};

	private class NumericTFListener implements ChangeListener<String> {

		private TextField _tf;
		private String _authorizedPattern, _completePattern;
		private Consumer<String> _onComplete;

		public NumericTFListener(TextField tf, String authorizedPattern, String completePattern, Consumer<String> onComplete) {
			_tf = tf;
			_authorizedPattern = authorizedPattern;
			_completePattern = completePattern;
			_onComplete = onComplete;			
		}

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			if (!newValue.matches(_authorizedPattern)) //We don't allow unauthorized chars
				Platform.runLater(() -> {
					_tf.setText(oldValue);
					_tf.positionCaret(oldValue.length());
				}); 
			else if(newValue.matches(_completePattern)) { //Complete string
				_onComplete.accept(newValue);
				Platform.runLater(()-> _tf.positionCaret(newValue.length()));
			}else //In progress string
				Platform.runLater(() -> _tf.setStyle(INVALID_STYLE));		
		}
	};

	@FXML
	private void initialize() {
		ProcessingConfiguration _processingConfig = new ProcessingConfiguration();
		MotorsConfiguration _motorsConfig = new MotorsConfiguration();
		_motorsConfig.InputCol = _processingConfig.ProcessedBufferCol();
		_motorsConfig.InputRow = _processingConfig.ProcessedBufferRow();
		SerialConfiguration serialConfig = new SerialConfiguration();

		_userConfigManager.AddObserver(this);
		_userConfigManager.SetProcessingConfiguration(_processingConfig);
		_userConfigManager.SetMotorsConfiguration(_motorsConfig);
		_userConfigManager.SetSerialConfiguration(serialConfig);

		_ports = FXCollections.observableArrayList();

		for (SerialPort port: SerialPort.getCommPorts()) 
			_ports.add(port.getSystemPortName());

		COM.setItems(_ports);
		COM.setValue(_ports.get(0));

		baudrateCB.setItems(FXCollections.observableArrayList(9600, 19200, 38400, 57600, 74880, 115200, 230400, 250000));

		Bind();
	}



	private void Bind() {
		minThresholdSlider.valueProperty().addListener(processingConfigSliderListener);
		maxThresholdSlider.valueProperty().addListener(processingConfigSliderListener);
		framesForAverageSlider.valueProperty().addListener(processingConfigSliderListener);
		interpolationFactorSlider.valueProperty().addListener(processingConfigSliderListener);

		averageAlgo.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			ProcessingConfiguration config = _userConfigManager.GetProcessingConfiguration();
			config.Noise_averageAlgo = rollingAverageT.isSelected() ? 0 : 1;
			_userConfigManager.SetProcessingConfiguration(config);
		});

		resizeFactorSLider.valueProperty().addListener((observable, oldValue, newValue) -> {
			ProcessingConfiguration processingConfig = _userConfigManager.GetProcessingConfiguration();
			MotorsConfiguration motorsConfig = _userConfigManager.GetMotorsConfiguration();

			processingConfig.ResizeFactor = (int) resizeFactorSLider.getValue();
			motorsConfig.InputCol = processingConfig.ProcessedBufferCol();
			motorsConfig.InputRow = processingConfig.ProcessedBufferRow();

			_userConfigManager.SetProcessingConfiguration(processingConfig);
			_userConfigManager.SetMotorsConfiguration(motorsConfig);			
		});

		gaussianDeviationSlider.valueProperty().addListener(motorsConfigSliderListener);
		uniformDeviationSlider.valueProperty().addListener(motorsConfigSliderListener);
		gaussianNormalisationFactorSlider.valueProperty().addListener(motorsConfigSliderListener);
		uniformNormalisationFactorSlider.valueProperty().addListener(motorsConfigSliderListener);

		frameSeparatorByteTF.textProperty().addListener(new NumericTFListener(
				frameSeparatorByteTF, "^(0x)?((\\d|[a-f]|[A-F])?){2}$", "^(0x)?(\\d|[a-f]|[A-F]){2}$", (newValue) -> {
					SerialConfiguration serialConfig = _userConfigManager.GetSerialConfiguration();
					serialConfig.ByteSeparator = Integer.parseInt(newValue.subSequence(newValue.length()-2, newValue.length()).toString(), 16);
					Platform.runLater(() -> _userConfigManager.SetSerialConfiguration(serialConfig));
				}));

		frameSeparatorByteTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				frameSeparatorByteTF.setText(String.format("0x%02X", _userConfigManager.GetSerialConfiguration().ByteSeparator));
				frameSeparatorByteTF.setStyle(VALID_STYLE);
			}
		});

		baudrateCB.valueProperty().addListener((observable, oldValue, newValue) -> {
			SerialConfiguration config = _userConfigManager.GetSerialConfiguration();
			config.Baudrate = baudrateCB.getValue();
			_userConfigManager.SetSerialConfiguration(config);
		});

		rawColTF.textProperty().addListener(new NumericTFListener(
				rawColTF, "^(\\d?){3}$", "^\\d{1,3}$", (newValue) -> {
					SerialConfiguration serialConfig = _userConfigManager.GetSerialConfiguration();
					ProcessingConfiguration processingConfig = _userConfigManager.GetProcessingConfiguration();
					MotorsConfiguration motorsConfig = _userConfigManager.GetMotorsConfiguration();

					processingConfig.RawBufferCol = Integer.parseInt(newValue);
					serialConfig.BufferSize = processingConfig.RawBufferCol*processingConfig.RawBufferRow;
					motorsConfig.InputCol = processingConfig.ProcessedBufferCol();

					Platform.runLater(() -> {
						_userConfigManager.SetSerialConfiguration(serialConfig);
						_userConfigManager.SetProcessingConfiguration(processingConfig);
						_userConfigManager.SetMotorsConfiguration(motorsConfig);
					});
				}));

		rawColTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				rawColTF.setText(Integer.toString(_userConfigManager.GetProcessingConfiguration().RawBufferCol));
				rawColTF.setStyle(VALID_STYLE);
			}
		});

		rawRowTF.textProperty().addListener(new NumericTFListener(
				rawRowTF, "^(\\d?){3}$", "^\\d{1,3}$", (newValue) -> {
					SerialConfiguration serialConfig = _userConfigManager.GetSerialConfiguration();
					ProcessingConfiguration processingConfig = _userConfigManager.GetProcessingConfiguration();
					MotorsConfiguration motorsConfig = _userConfigManager.GetMotorsConfiguration();

					processingConfig.RawBufferRow = Integer.parseInt(newValue);
					serialConfig.BufferSize = processingConfig.RawBufferCol*processingConfig.RawBufferRow;
					motorsConfig.InputRow = processingConfig.ProcessedBufferRow();

					Platform.runLater(() -> {
						_userConfigManager.SetSerialConfiguration(serialConfig);
						_userConfigManager.SetProcessingConfiguration(processingConfig);
						_userConfigManager.SetMotorsConfiguration(motorsConfig);
					});
				}));

		rawRowTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				rawRowTF.setText(Integer.toString(_userConfigManager.GetProcessingConfiguration().RawBufferRow));
				rawRowTF.setStyle(VALID_STYLE);
			}
		});

		motorsColTF.textProperty().addListener(new NumericTFListener(
				motorsColTF, "^(\\d?){2}$", "^\\d{1,2}$", (newValue) -> {
					MotorsConfiguration motorsConfig = _userConfigManager.GetMotorsConfiguration();
					motorsConfig.OutputCol = Integer.parseInt(newValue);
					Platform.runLater(() -> _userConfigManager.SetMotorsConfiguration(motorsConfig));
				}));
		
		motorsColTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				motorsColTF.setText(Integer.toString(_userConfigManager.GetMotorsConfiguration().OutputCol));
				motorsColTF.setStyle(VALID_STYLE);
			}
		});
		
		motorsRowTF.textProperty().addListener(new NumericTFListener(
				motorsRowTF, "^(\\d?){2}$", "^\\d{1,2}$", (newValue) -> {
					MotorsConfiguration motorsConfig = _userConfigManager.GetMotorsConfiguration();
					motorsConfig.OutputRow = Integer.parseInt(newValue);
					Platform.runLater(() -> _userConfigManager.SetMotorsConfiguration(motorsConfig));
				}));
		
		motorsRowTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				motorsRowTF.setText(Integer.toString(_userConfigManager.GetMotorsConfiguration().OutputRow));
				motorsRowTF.setStyle(VALID_STYLE);
			}
		});

	}


	@FXML
	private void onConnectAndRender() {
		RendererController _renderController = new RendererController(_ports.indexOf(COM.getValue()),
				_userConfigManager.GetProcessingConfiguration(),
				_userConfigManager.GetMotorsConfiguration(),
				_userConfigManager.GetSerialConfiguration());

		_userConfigManager.AddObserver(_renderController);	
	}



	@Override
	public void ProcessingConfigurationUpdated(model.SkinProcessor.ProcessingConfiguration userConfig) {		
		minThresholdView.setText(Integer.toString(userConfig.MinThreshold));
		minThresholdSlider.setValue(userConfig.MinThreshold);

		maxThresholdView.setText(Integer.toString(userConfig.MaxThreshold));
		maxThresholdSlider.setValue(userConfig.MaxThreshold);

		averageAlgo.selectToggle(userConfig.Noise_averageAlgo == 0 ? rollingAverageT : interpolationFactorT);

		framesForAverageView.setText(Integer.toString(userConfig.Noise_framesForAverage));
		framesForAverageSlider.setValue(userConfig.Noise_framesForAverage);

		interpolationFactorView.setText(String.format("%1.2f", userConfig.Noise_interpolationFactor));
		interpolationFactorSlider.setValue(userConfig.Noise_interpolationFactor);

		resizeFactorView.setText(Integer.toString(userConfig.ResizeFactor));
		resizeFactorSLider.setValue(userConfig.ResizeFactor);

		rawResolutionView.setText(String.format("%dx%d", userConfig.RawBufferCol, userConfig.RawBufferRow));
		processedResolutionView.setText(String.format("%dx%d", userConfig.ProcessedBufferCol(), userConfig.ProcessedBufferRow()));

		rawColTF.setStyle(VALID_STYLE);
		rawRowTF.setStyle(VALID_STYLE);


		rawColTF.setText(Integer.toString(userConfig.RawBufferCol));
		rawRowTF.setText(Integer.toString(userConfig.RawBufferRow));


	}

	@Override
	public void MotorsConfigurationUpdated(model.Motors.MotorsConfiguration userConfig) {
		gaussianDeviationView.setText(Integer.toString(userConfig.DeviationGaussian));
		gaussianDeviationSlider.setValue(userConfig.DeviationGaussian);

		uniformDeviationView.setText(Integer.toString(userConfig.DeviationUniform));
		uniformDeviationSlider.setValue(userConfig.DeviationUniform);

		gaussianNormalisationFactorView.setText(String.format("%1.1f", userConfig.NormalisationFactorGaussian));
		gaussianNormalisationFactorSlider.setValue(userConfig.NormalisationFactorGaussian);

		uniformNormalisationFactorView.setText(String.format("%1.1f", userConfig.NormalisationFactorUniform));
		uniformNormalisationFactorSlider.setValue(userConfig.NormalisationFactorUniform);
		
		motorsColTF.setText(Integer.toString(userConfig.OutputCol));
		motorsColTF.setStyle(VALID_STYLE);
		
		motorsRowTF.setText(Integer.toString(userConfig.OutputRow));
		motorsRowTF.setStyle(VALID_STYLE);
	}



	@Override
	public void SerialConfigurationUpdated(model.SkinSerialPort.SerialConfiguration userConfig) {
		frameSeparatorByteTF.setText(String.format("0x%02X", userConfig.ByteSeparator));
		frameSeparatorByteTF.setStyle(VALID_STYLE);
		baudrateCB.setValue(userConfig.Baudrate);
	}
}
