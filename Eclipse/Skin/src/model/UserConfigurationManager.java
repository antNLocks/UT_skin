package model;

import java.util.ArrayList;
import java.util.List;

import model.Motors.MotorsConfiguration;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinSerialPort.SerialConfiguration;

public class UserConfigurationManager {

	public interface UserObserver {
		public void ProcessingConfigurationUpdated(SkinProcessor.ProcessingConfiguration userConfig);
		public void MotorsConfigurationUpdated(Motors.MotorsConfiguration userConfig);
		public void SerialConfigurationUpdated(SkinSerialPort.SerialConfiguration userConfig);
	}
	
		
		
	private List<UserObserver> _observers = new ArrayList<UserObserver>();

	private SkinProcessor.ProcessingConfiguration _processingConfig = new ProcessingConfiguration();
	private Motors.MotorsConfiguration _motorsConfig = new MotorsConfiguration();
	private SkinSerialPort.SerialConfiguration _serialConfig = new SerialConfiguration();

	public UserConfigurationManager() {}

	public SkinProcessor.ProcessingConfiguration GetProcessingConfiguration() {
		return new ProcessingConfiguration(_processingConfig);
	}
	
	public Motors.MotorsConfiguration GetMotorsConfiguration() {
		return new MotorsConfiguration(_motorsConfig);
	}
	
	public SkinSerialPort.SerialConfiguration GetSerialConfiguration(){
		return new SerialConfiguration(_serialConfig);
	}

	public void SetProcessingConfiguration(SkinProcessor.ProcessingConfiguration conf) {
		_processingConfig = conf;

		for(UserObserver obs : _observers)
			obs.ProcessingConfigurationUpdated(new ProcessingConfiguration(_processingConfig));
	}
	
	public void SetMotorsConfiguration(Motors.MotorsConfiguration conf) {
		_motorsConfig = conf;
		
		for(UserObserver obs : _observers)
			obs.MotorsConfigurationUpdated(new MotorsConfiguration(_motorsConfig));
	}
	
	public void SetSerialConfiguration(SkinSerialPort.SerialConfiguration conf) {
		_serialConfig = conf;
		
		for(UserObserver obs : _observers)
			obs.SerialConfigurationUpdated(new SerialConfiguration(_serialConfig));
	}
	
	
	public void AddObserver(UserObserver obs) {
		_observers.add(obs);
	}
}
