package controller;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.fazecast.jSerialComm.SerialPort;

import application.Main;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;
import java.io.IOException;

import model.UserConfigurationManager;
import model.UserConfigurationManager.Configuration;
import model.MotorsSpatial.MotorsSpatialConfiguration;
import model.MotorsTime.MotorsTimeConfiguration;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinSerialPort.SerialConfiguration;

public class ConfigController implements UserConfigurationManager.UserObserver {

	private ArrayList<UserConfigurationManager> _userConfigManagers = new ArrayList<>();
	private ObservableList<String> _userConfigManagersView;

	private String _fileArg = null;

	@FXML 
	private ChoiceBox<String> COM;
	private ObservableList<String> _ports;

	@FXML
	private ToggleGroup averageAlgo;

	@FXML
	private ChoiceBox<Integer> baudrateCB;

	@FXML
	private ChoiceBox<String> configurationCB;

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
	private Slider hardwareGainSlider;

	@FXML
	private Label hardwareGainView;

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
	private Slider resizeFactorHeightSLider;

	@FXML
	private Label resizeFactorHeightView;
	
	@FXML
	private Slider resizeFactorWidthSLider;

	@FXML
	private Label resizeFactorWidthView;

	@FXML
	private RadioButton rollingAverageT;
	
	@FXML
	private Slider sleepingTimeMotorsSpatialSlider;
	
	@FXML
	private Label sleepingTimeMotorsSpatialView;
	
	@FXML
	private Slider sleepingTimeMotorsTimeSlider;
	
	@FXML
	private Label sleepingTimeMotorsTimeView;
	
	@FXML
	private Slider sleepingTimeProcessingSlider;
	
	@FXML
	private Label sleepingTimeProcessingView;
	
	@FXML
	private Slider trailingFramesSlider;
	
	@FXML
	private Label trailingFramesView;

	private final String INVALID_STYLE = "-fx-text-box-border: #FF0000; -fx-focus-color: #FF0000;";
	private final String VALID_STYLE = "-fx-text-box-border: #00FF00; -fx-focus-color: #00FF00;";


	private ChangeListener<Number> processingConfigSliderListener = (observable, oldValue, newValue) -> {
		ProcessingConfiguration config = GetUserConfigManager().GetProcessingConfiguration();

		config.MinThreshold = (int)	minThresholdSlider.getValue();
		config.MaxThreshold = (int)	maxThresholdSlider.getValue();
		config.Noise_framesForAverage = (int) framesForAverageSlider.getValue();
		config.Noise_interpolationFactor = (float) interpolationFactorSlider.getValue();
		config.SleepingTime = (long) sleepingTimeProcessingSlider.getValue();

		GetUserConfigManager().SetProcessingConfiguration(config);
	};

	private ChangeListener<Number> motorsConfigSliderListener = (observable, oldValue, newValue) -> {
		MotorsSpatialConfiguration config = GetUserConfigManager().GetMotorsSpatialConfiguration();

		config.DeviationGaussian = (int) gaussianDeviationSlider.getValue();
		config.NormalisationFactorGaussian = (float) gaussianNormalisationFactorSlider.getValue();
		config.SleepingTime = (long) sleepingTimeMotorsSpatialSlider.getValue();
		
		GetUserConfigManager().SetMotorsSpatialConfiguration(config);
	};
	
	private ChangeListener<Number> motorsTimeConfigSliderListener = (observable, oldValue, newValue) -> {
		MotorsTimeConfiguration config = GetUserConfigManager().GetMotorsTimeConfiguration();

		config.TrailingFrames = (int) trailingFramesSlider.getValue();
		config.SleepingTime = (long) sleepingTimeMotorsTimeSlider.getValue();
		
		GetUserConfigManager().SetMotorsTimeConfiguration(config);
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

	private ChangeListener<String> configChangeListener = (observable, oldValue, newValue) -> {
		UpdateUI(GetUserConfigManager());
		connectAndRender.setDisable(!configurationCB.getValue().equalsIgnoreCase("New Config"));
	};
	
	private ChangeListener<Number> resizeFacorSliderListener = (observable, oldValue, newValue) -> {
		ProcessingConfiguration processingConfig = GetUserConfigManager().GetProcessingConfiguration();
		MotorsSpatialConfiguration motorsSpatialConfig = GetUserConfigManager().GetMotorsSpatialConfiguration();

		processingConfig.ResizeFactorCol = (int) resizeFactorWidthSLider.getValue();
		processingConfig.ResizeFactorRow = (int) resizeFactorHeightSLider.getValue();
		motorsSpatialConfig.InputCol = processingConfig.ProcessedBufferCol();
		motorsSpatialConfig.InputRow = processingConfig.ProcessedBufferRow();

		GetUserConfigManager().SetProcessingConfiguration(processingConfig);
		GetUserConfigManager().SetMotorsSpatialConfiguration(motorsSpatialConfig);			
	};
	
	public ConfigController(String fileArg) { _fileArg = fileArg;	}

	@FXML
	private void initialize() {
		_userConfigManagersView = FXCollections.observableArrayList();

		CreateNewConfigurationManager();
		UpdateUI(GetUserConfigManager());

		onRefreshCOM();

		baudrateCB.setItems(FXCollections.observableArrayList(9600, 19200, 38400, 57600, 74880, 115200, 230400, 250000));

		Bind();
		
		if(_fileArg != null) 
			Platform.runLater(() -> Load(new File(_fileArg)));
	}



	private void Bind() {
		minThresholdSlider.valueProperty().addListener(processingConfigSliderListener);
		maxThresholdSlider.valueProperty().addListener(processingConfigSliderListener);
		framesForAverageSlider.valueProperty().addListener(processingConfigSliderListener);
		interpolationFactorSlider.valueProperty().addListener(processingConfigSliderListener);

		averageAlgo.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			ProcessingConfiguration config = GetUserConfigManager().GetProcessingConfiguration();
			config.Noise_averageAlgo = rollingAverageT.isSelected() ? 0 : 1;
			GetUserConfigManager().SetProcessingConfiguration(config);
		});

		resizeFactorWidthSLider.valueProperty().addListener(resizeFacorSliderListener);
		resizeFactorHeightSLider.valueProperty().addListener(resizeFacorSliderListener);

		gaussianDeviationSlider.valueProperty().addListener(motorsConfigSliderListener);
		gaussianNormalisationFactorSlider.valueProperty().addListener(motorsConfigSliderListener);
		
		trailingFramesSlider.valueProperty().addListener(motorsTimeConfigSliderListener);

		frameSeparatorByteTF.textProperty().addListener(new NumericTFListener(
				frameSeparatorByteTF, "^(0x)?((\\d|[a-f]|[A-F])?){2}$", "^(0x)?(\\d|[a-f]|[A-F]){2}$", (newValue) -> {
					SerialConfiguration serialConfig = GetUserConfigManager().GetSerialConfiguration();
					serialConfig.ByteSeparator = Integer.parseInt(newValue.subSequence(newValue.length()-2, newValue.length()).toString(), 16);
					Platform.runLater(() -> GetUserConfigManager().SetSerialConfiguration(serialConfig));
				}));

		frameSeparatorByteTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				frameSeparatorByteTF.setText(String.format("0x%02X", GetUserConfigManager().GetSerialConfiguration().ByteSeparator));
				frameSeparatorByteTF.setStyle(VALID_STYLE);
			}
		});

		baudrateCB.valueProperty().addListener((observable, oldValue, newValue) -> {
			SerialConfiguration config = GetUserConfigManager().GetSerialConfiguration();
			config.Baudrate = baudrateCB.getValue();
			GetUserConfigManager().SetSerialConfiguration(config);
		});

		rawColTF.textProperty().addListener(new NumericTFListener(
				rawColTF, "^(\\d?){3}$", "^\\d{1,3}$", (newValue) -> {
					SerialConfiguration serialConfig = GetUserConfigManager().GetSerialConfiguration();
					ProcessingConfiguration processingConfig = GetUserConfigManager().GetProcessingConfiguration();
					MotorsSpatialConfiguration motorsConfig = GetUserConfigManager().GetMotorsSpatialConfiguration();

					processingConfig.RawBufferCol = Integer.parseInt(newValue);
					serialConfig.BufferSize = processingConfig.RawBufferCol*processingConfig.RawBufferRow;
					motorsConfig.InputCol = processingConfig.ProcessedBufferCol();

					Platform.runLater(() -> {
						GetUserConfigManager().SetSerialConfiguration(serialConfig);
						GetUserConfigManager().SetProcessingConfiguration(processingConfig);
						GetUserConfigManager().SetMotorsSpatialConfiguration(motorsConfig);
					});
				}));

		rawColTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				rawColTF.setText(Integer.toString(GetUserConfigManager().GetProcessingConfiguration().RawBufferCol));
				rawColTF.setStyle(VALID_STYLE);
			}
		});

		rawRowTF.textProperty().addListener(new NumericTFListener(
				rawRowTF, "^(\\d?){3}$", "^\\d{1,3}$", (newValue) -> {
					SerialConfiguration serialConfig = GetUserConfigManager().GetSerialConfiguration();
					ProcessingConfiguration processingConfig = GetUserConfigManager().GetProcessingConfiguration();
					MotorsSpatialConfiguration motorsConfig = GetUserConfigManager().GetMotorsSpatialConfiguration();

					processingConfig.RawBufferRow = Integer.parseInt(newValue);
					serialConfig.BufferSize = processingConfig.RawBufferCol*processingConfig.RawBufferRow;
					motorsConfig.InputRow = processingConfig.ProcessedBufferRow();

					Platform.runLater(() -> {
						GetUserConfigManager().SetSerialConfiguration(serialConfig);
						GetUserConfigManager().SetProcessingConfiguration(processingConfig);
						GetUserConfigManager().SetMotorsSpatialConfiguration(motorsConfig);
					});
				}));

		rawRowTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				rawRowTF.setText(Integer.toString(GetUserConfigManager().GetProcessingConfiguration().RawBufferRow));
				rawRowTF.setStyle(VALID_STYLE);
			}
		});

		motorsColTF.textProperty().addListener(new NumericTFListener(
				motorsColTF, "^(\\d?){2}$", "^\\d{1,2}$", (newValue) -> {
					MotorsSpatialConfiguration motorsConfig = GetUserConfigManager().GetMotorsSpatialConfiguration();
					motorsConfig.OutputCol = Integer.parseInt(newValue);
					Platform.runLater(() -> GetUserConfigManager().SetMotorsSpatialConfiguration(motorsConfig));
				}));

		motorsColTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				motorsColTF.setText(Integer.toString(GetUserConfigManager().GetMotorsSpatialConfiguration().OutputCol));
				motorsColTF.setStyle(VALID_STYLE);
			}
		});

		motorsRowTF.textProperty().addListener(new NumericTFListener(
				motorsRowTF, "^(\\d?){2}$", "^\\d{1,2}$", (newValue) -> {
					MotorsSpatialConfiguration motorsConfig = GetUserConfigManager().GetMotorsSpatialConfiguration();
					motorsConfig.OutputRow = Integer.parseInt(newValue);
					Platform.runLater(() -> GetUserConfigManager().SetMotorsSpatialConfiguration(motorsConfig));
				}));

		motorsRowTF.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if(!newValue) {		
				motorsRowTF.setText(Integer.toString(GetUserConfigManager().GetMotorsSpatialConfiguration().OutputRow));
				motorsRowTF.setStyle(VALID_STYLE);
			}
		});

		hardwareGainSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			SerialConfiguration config = GetUserConfigManager().GetSerialConfiguration();
			config.HardwareGain = (int) hardwareGainSlider.getValue();
			GetUserConfigManager().SetSerialConfiguration(config);
		});

		configurationCB.valueProperty().addListener(configChangeListener);
		
		sleepingTimeMotorsSpatialSlider.valueProperty().addListener(motorsConfigSliderListener);
		sleepingTimeProcessingSlider.valueProperty().addListener(processingConfigSliderListener);
		sleepingTimeMotorsTimeSlider.valueProperty().addListener(motorsTimeConfigSliderListener);
	}

	private void CreateNewConfigurationManager() {
		Configuration config = new Configuration();
		
		if(_userConfigManagers.size() > 0) { //The user asked to render the current config and we create a new one
			_userConfigManagersView.set(_userConfigManagersView.size() - 1, "Config " + COM.getValue());
			configurationCB.setValue("Config " + COM.getValue());
			
			UserConfigurationManager _userConfigManager = GetUserConfigManager();
			
			config.Processing = new ProcessingConfiguration(_userConfigManager.GetProcessingConfiguration());
			config.MotorsSpatial = new MotorsSpatialConfiguration(_userConfigManager.GetMotorsSpatialConfiguration());
			config.Serial = new SerialConfiguration(_userConfigManager.GetSerialConfiguration());
			config.MotorsTime = new MotorsTimeConfiguration(_userConfigManager.GetMotorsTimeConfiguration());
		}
		else {
			config.Processing = new ProcessingConfiguration();
			config.MotorsSpatial = new MotorsSpatialConfiguration();
			config.MotorsSpatial.InputCol = config.Processing.ProcessedBufferCol();
			config.MotorsSpatial.InputRow = config.Processing.ProcessedBufferRow();
			config.Serial = new SerialConfiguration();
			config.MotorsTime = new MotorsTimeConfiguration();
		}
		
		

		UserConfigurationManager configManager = new UserConfigurationManager();

		configManager.SetConfiguration(config);
		configManager.AddObserver(this);

		

		_userConfigManagersView.add("New config");
		_userConfigManagers.add(configManager);

		configurationCB.setItems(_userConfigManagersView);

		if(_userConfigManagers.size() == 1)
			configurationCB.setValue(_userConfigManagersView.get(0));
	}

	private void UpdateUI(UserConfigurationManager config) {
		ProcessingConfigurationUpdated(config.GetProcessingConfiguration());
		SerialConfigurationUpdated(config.GetSerialConfiguration());
		MotorsSpatialConfigurationUpdated(config.GetMotorsSpatialConfiguration());
		MotorsTimeConfigurationUpdated(config.GetMotorsTimeConfiguration());
	}


	@FXML
	private void onRender() {
		final String configManagerName = "Config " + COM.getValue();

		RendererController _renderController = new RendererController("Renderer - "+ configManagerName,
				_ports.indexOf(COM.getValue()),
				GetUserConfigManager().GetConfiguration(),
				() -> {
					if(configurationCB.getValue().equalsIgnoreCase(configManagerName)) {
						configurationCB.setValue("New config");
						configChangeListener.changed(null, null, null); //Force update
					}

					int configManagerIndex = _userConfigManagersView.indexOf(configManagerName);
					_userConfigManagers.remove(configManagerIndex);
					_userConfigManagersView.remove(configManagerIndex);
				});

		GetUserConfigManager().AddObserver(_renderController);

		CreateNewConfigurationManager();
	}

	@FXML
	private void onRefreshCOM() {
		_ports = FXCollections.observableArrayList();

		for (SerialPort port: SerialPort.getCommPorts()) 
			_ports.add(port.getSystemPortName());

		COM.setItems(_ports);

		if(_ports.size() > 0 && COM.getValue() == null)
			COM.setValue(_ports.get(0));
	}

	@FXML
	private void onSaveConfig() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save configuration");
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Config Files", "*.noc"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showSaveDialog(Main.Stage);

		if (selectedFile != null)
			try {
				GetUserConfigManager().Save(selectedFile);
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Save Error");
				alert.setHeaderText("Unable to save this configuration");
				alert.setContentText("Maybe the location of the file no longer exists.");
				alert.initOwner(Main.Stage);
				alert.showAndWait();
				e.printStackTrace();
			}
	}

	@FXML
	private void onLoadConfig() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Config Files", "*.noc"),
				new ExtensionFilter("All Files", "*.*"));
		Load(fileChooser.showOpenDialog(Main.Stage));
	}
	
	private void Load(File f) {
		if (f != null)
			try {
				GetUserConfigManager().Load(f);
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Load Error");
				alert.setHeaderText("Unable to read this configuration file");
				alert.setContentText("Maybe this file cannot be read.\nOr it is not compatible with this version of the software.");
				alert.initOwner(Main.Stage);
				alert.showAndWait();
				e.printStackTrace();
			}
	}

	private UserConfigurationManager GetUserConfigManager() {
		return _userConfigManagers.get(_userConfigManagersView.indexOf(configurationCB.getValue()));
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

		resizeFactorWidthView.setText(Integer.toString(userConfig.ResizeFactorCol));
		resizeFactorWidthSLider.setValue(userConfig.ResizeFactorCol);
		
		resizeFactorHeightView.setText(Integer.toString(userConfig.ResizeFactorRow));
		resizeFactorHeightSLider.setValue(userConfig.ResizeFactorRow);

		rawResolutionView.setText(String.format("%dx%d", userConfig.RawBufferCol, userConfig.RawBufferRow));
		processedResolutionView.setText(String.format("%dx%d", userConfig.ProcessedBufferCol(), userConfig.ProcessedBufferRow()));

		rawColTF.setStyle(VALID_STYLE);
		rawRowTF.setStyle(VALID_STYLE);


		rawColTF.setText(Integer.toString(userConfig.RawBufferCol));
		rawRowTF.setText(Integer.toString(userConfig.RawBufferRow));

		sleepingTimeProcessingSlider.setValue(userConfig.SleepingTime);
		sleepingTimeProcessingView.setText(Long.toString(userConfig.SleepingTime));
	}

	@Override
	public void MotorsSpatialConfigurationUpdated(model.MotorsSpatial.MotorsSpatialConfiguration userConfig) {
		gaussianDeviationView.setText(Integer.toString(userConfig.DeviationGaussian));
		gaussianDeviationSlider.setValue(userConfig.DeviationGaussian);

		gaussianNormalisationFactorView.setText(String.format("%1.1f", userConfig.NormalisationFactorGaussian));
		gaussianNormalisationFactorSlider.setValue(userConfig.NormalisationFactorGaussian);

		motorsColTF.setText(Integer.toString(userConfig.OutputCol));
		motorsColTF.setStyle(VALID_STYLE);

		motorsRowTF.setText(Integer.toString(userConfig.OutputRow));
		motorsRowTF.setStyle(VALID_STYLE);
		
		sleepingTimeMotorsSpatialSlider.setValue(userConfig.SleepingTime);
		sleepingTimeMotorsSpatialView.setText(Long.toString(userConfig.SleepingTime));
	}



	@Override
	public void SerialConfigurationUpdated(model.SkinSerialPort.SerialConfiguration userConfig) {
		frameSeparatorByteTF.setText(String.format("0x%02X", userConfig.ByteSeparator));
		frameSeparatorByteTF.setStyle(VALID_STYLE);
		baudrateCB.setValue(userConfig.Baudrate);

		hardwareGainView.setText(Integer.toString(userConfig.HardwareGain));
		hardwareGainSlider.setValue(userConfig.HardwareGain);
	}

	@Override
	public void MotorsTimeConfigurationUpdated(MotorsTimeConfiguration userConfig) {
		trailingFramesSlider.setValue(userConfig.TrailingFrames);
		trailingFramesView.setText(Integer.toString(userConfig.TrailingFrames));
		
		sleepingTimeMotorsTimeSlider.setValue(userConfig.SleepingTime);
		sleepingTimeMotorsTimeView.setText(Long.toString(userConfig.SleepingTime));
	}
}
