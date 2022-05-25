package controller;


import java.io.IOException;

import com.fazecast.jSerialComm.SerialPort;

import application.UserConfigurationManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import model.Motors.MotorsConfiguration;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinProcessor;

public class ConfigController implements UserConfigurationManager.UserObserver {

	private UserConfigurationManager _userConfigManager = new UserConfigurationManager();
	private SkinProcessor _skinProcessor;
	private RendererController _renderController;
	
	@FXML 
	private ChoiceBox<String> COM;
	private ObservableList<String> ports;


	@FXML
    private ToggleGroup averageAlgo;

    @FXML
    private ChoiceBox<?> baudrateCB;

    @FXML
    private Button connectAndRender;

    @FXML
    private ChoiceBox<?> frameSeperatorByteCB;

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


	@FXML
	private void onConnectAndRender() {
		connect();
		launchRenderer();
	}



	@FXML
	private void initialize() {
		ports = FXCollections.observableArrayList();

		for (SerialPort port: SerialPort.getCommPorts()) 
			ports.add(port.getSystemPortName());

		COM.setItems(ports);
		COM.setValue(ports.get(0));

		ProcessingConfiguration _processingConfig = new ProcessingConfiguration();
		MotorsConfiguration _motorsConfig = new MotorsConfiguration();
		_motorsConfig.InputCol = _processingConfig.ProcessedBufferCol;
		_motorsConfig.InputRow = _processingConfig.ProcessedBufferRow;

		_userConfigManager.AddObserver(this);
		_userConfigManager.SetProcessingConfiguration(_processingConfig);
		_userConfigManager.setMotorsConfiguration(_motorsConfig);

		bind();

	}



	private void bind() {
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
	}



	private void connect() {
		_skinProcessor = new SkinProcessor(ports.indexOf(COM.getValue()));
	}

	private void launchRenderer() {	
		try {
			_renderController = new RendererController(_skinProcessor);
			_renderController.MotorsConfigurationUpdated(_userConfigManager.GetMotorsConfiguration());
			_userConfigManager.AddObserver(_renderController);
			_skinProcessor.Register(_renderController);

			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Renderer.fxml"));
			loader.setController(_renderController);
			Parent root = loader.load();
			Scene scene = new Scene(root,1000,600);

			Stage newWindow = new Stage();
			newWindow.setTitle("Renderer");
			newWindow.setScene(scene);

			newWindow.show();
			_skinProcessor.StartProcessing();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	@Override
	public void ProcessingConfigurationUpdated(model.SkinProcessor.ProcessingConfiguration userConfig) {
		if(_skinProcessor != null) {
			_skinProcessor.ProcessingConfig = userConfig;

		}
		
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
	public void MotorsConfigurationUpdated(model.Motors.MotorsConfiguration userConfig) {}
}
