package controller;


import java.io.IOException;

import com.fazecast.jSerialComm.SerialPort;

import application.UserConfigurationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import model.Motors.MotorsConfiguration;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinProcessor;

public class ConfigController implements UserConfigurationManager.UserObserver {
	
	private UserConfigurationManager _userConfigManager = new UserConfigurationManager();

	@FXML 
	private ChoiceBox<String> COM;
	private ObservableList<String> ports;
	
	private SkinProcessor _skinProcessor;
	private RendererController _renderController;
	
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
		
		_userConfigManager.SetProcessingConfiguration(_processingConfig);
		_userConfigManager.setMotorsConfiguration(_motorsConfig);

	}
	
	@FXML
	private void onConnectAndRender() {
		connect();
		launchRenderer();
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
			Scene scene = new Scene(root,800,600);

			Stage newWindow = new Stage();
            newWindow.setTitle("Second Stage");
            newWindow.setScene(scene);
            
            newWindow.show();
    		_skinProcessor.StartProcessing();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	@Override
	public void ProcessingConfigurationUpdated(model.SkinProcessor.ProcessingConfiguration userConfig) {
		if(_skinProcessor != null)
			_skinProcessor.ProcessingConfig = userConfig;
	}

	@Override
	public void MotorsConfigurationUpdated(model.Motors.MotorsConfiguration userConfig) {}
}
