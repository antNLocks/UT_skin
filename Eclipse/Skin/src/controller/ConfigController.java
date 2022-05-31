package controller;



import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fazecast.jSerialComm.SerialPort;

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
import javafx.scene.control.Toggle;
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
    private ChoiceBox<Integer> frameSeperatorByteCB;

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


	private ChangeListener<Number> processingConfigSliderListener = new ChangeListener<Number>() {

		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			ProcessingConfiguration config = _userConfigManager.GetProcessingConfiguration();
			
			config.MinThreshold = (int)	minThresholdSlider.getValue();
			config.MaxThreshold = (int)	maxThresholdSlider.getValue();
			config.Noise_framesForAverage = (int) framesForAverageSlider.getValue();
			config.Noise_interpolationFactor = (float) interpolationFactorSlider.getValue();
			
			_userConfigManager.SetProcessingConfiguration(config);
		}
	};

	private ChangeListener<Number> motorsConfigSliderListener = new ChangeListener<Number>() {
		
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			MotorsConfiguration config = _userConfigManager.GetMotorsConfiguration();
			
			config.DeviationGaussian = (int) gaussianDeviationSlider.getValue();
			config.DeviationUniform = (int) uniformDeviationSlider.getValue();
			config.NormalisationFactorGaussian = (float) gaussianNormalisationFactorSlider.getValue();
			config.NormalisationFactorUniform = (float) uniformNormalisationFactorSlider.getValue();
			
			_userConfigManager.setMotorsConfiguration(config);
		}
	};
	
	private ChangeListener<Number> serialConfigChoiceBoxListener = new ChangeListener<Number>() {

		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			SerialConfiguration config = _userConfigManager.GetSerialConfiguration();
			
			config.ByteSeparator = frameSeperatorByteCB.getValue();
			config.Baudrate = baudrateCB.getValue();
			
			_userConfigManager.SetSerialConfiguration(config);
		}
	};


	@FXML
	private void initialize() {
		ProcessingConfiguration _processingConfig = new ProcessingConfiguration();
		MotorsConfiguration _motorsConfig = new MotorsConfiguration();
		_motorsConfig.InputCol = _processingConfig.ProcessedBufferCol;
		_motorsConfig.InputRow = _processingConfig.ProcessedBufferRow;
		SerialConfiguration serialConfig = new SerialConfiguration();

		_userConfigManager.AddObserver(this);
		_userConfigManager.SetProcessingConfiguration(_processingConfig);
		_userConfigManager.setMotorsConfiguration(_motorsConfig);
		_userConfigManager.SetSerialConfiguration(serialConfig);
		
		_ports = FXCollections.observableArrayList();

		for (SerialPort port: SerialPort.getCommPorts()) 
			_ports.add(port.getSystemPortName());

		COM.setItems(_ports);
		COM.setValue(_ports.get(0));
		
		
		frameSeperatorByteCB.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(0, 255).boxed().collect(Collectors.toList())));
		
		baudrateCB.setItems(FXCollections.observableArrayList(9600, 19200, 38400, 57600, 74880, 115200, 230400, 250000));


		Bind();
	}



	private void Bind() {
		minThresholdSlider.valueProperty().addListener(processingConfigSliderListener);
		maxThresholdSlider.valueProperty().addListener(processingConfigSliderListener);
		framesForAverageSlider.valueProperty().addListener(processingConfigSliderListener);
		interpolationFactorSlider.valueProperty().addListener(processingConfigSliderListener);
		
		averageAlgo.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				ProcessingConfiguration config = _userConfigManager.GetProcessingConfiguration();
				config.Noise_averageAlgo = rollingAverageT.isSelected() ? 0 : 1;
				
				_userConfigManager.SetProcessingConfiguration(config);
			}
		});
		
		gaussianDeviationSlider.valueProperty().addListener(motorsConfigSliderListener);
		uniformDeviationSlider.valueProperty().addListener(motorsConfigSliderListener);
		gaussianNormalisationFactorSlider.valueProperty().addListener(motorsConfigSliderListener);
		uniformNormalisationFactorSlider.valueProperty().addListener(motorsConfigSliderListener);
		
		frameSeperatorByteCB.valueProperty().addListener(serialConfigChoiceBoxListener);
		baudrateCB.valueProperty().addListener(serialConfigChoiceBoxListener);
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
	}



	@Override
	public void SerialConfiguration(model.SkinSerialPort.SerialConfiguration userConfig) {
		frameSeperatorByteCB.setValue(userConfig.ByteSeparator);
		baudrateCB.setValue(userConfig.Baudrate);
	}
}
