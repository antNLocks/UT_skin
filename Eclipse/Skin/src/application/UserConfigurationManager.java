package application;

import java.util.ArrayList;
import java.util.List;

import model.Motors;
import model.SkinProcessor;

public class UserConfigurationManager {

	public interface UserObserver {
		public void ProcessingConfigurationUpdated(SkinProcessor.ProcessingConfiguration userConfig);
		public void MotorsConfigurationUpdated(Motors.MotorsConfiguration userConfig);
	}
	
		
		
	private List<UserObserver> _observers = new ArrayList<UserObserver>();

	private SkinProcessor.ProcessingConfiguration _processingConfig;
	private Motors.MotorsConfiguration _motorsConfig;

	public UserConfigurationManager() {}

	public SkinProcessor.ProcessingConfiguration GetProcessingConfiguration() {
		return _processingConfig;
	}
	
	public Motors.MotorsConfiguration GetMotorsConfiguration() {
		return _motorsConfig;
	}

	public void SetProcessingConfiguration(SkinProcessor.ProcessingConfiguration conf) {
		_processingConfig = conf;

		for(UserObserver obs : _observers)
			obs.ProcessingConfigurationUpdated(_processingConfig);
	}
	
	public void setMotorsConfiguration(Motors.MotorsConfiguration conf) {
		_motorsConfig = conf;
		
		for(UserObserver obs : _observers)
			obs.MotorsConfigurationUpdated(_motorsConfig);
	}
	
	
	public void AddObserver(UserObserver obs) {
		_observers.add(obs);
	}
}
