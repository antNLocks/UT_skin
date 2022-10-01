package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.MotorsSpatial.MotorsSpatialConfiguration;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinSerialPort.SerialConfiguration;
import model.MotorsTime.MotorsTimeConfiguration;

public class UserConfigurationManager {

	public static interface UserObserver {
		public void ProcessingConfigurationUpdated(SkinProcessor.ProcessingConfiguration userConfig);
		public void MotorsSpatialConfigurationUpdated(MotorsSpatial.MotorsSpatialConfiguration userConfig);
		public void SerialConfigurationUpdated(SkinSerialPort.SerialConfiguration userConfig);
		public void MotorsTimeConfigurationUpdated(MotorsTime.MotorsTimeConfiguration userConfig);
	}

	public static class Configuration implements Serializable{
		private static final long serialVersionUID = 1L;

		public SerialConfiguration Serial = new SerialConfiguration();
		public ProcessingConfiguration Processing = new ProcessingConfiguration();
		public MotorsSpatialConfiguration MotorsSpatial = new MotorsSpatialConfiguration();
		public MotorsTimeConfiguration MotorsTime = new MotorsTimeConfiguration();
		
		public Configuration() {}
		
		public Configuration(Configuration c) {
			Serial = new SerialConfiguration(c.Serial);
			Processing = new ProcessingConfiguration(c.Processing);
			MotorsSpatial = new MotorsSpatialConfiguration(c.MotorsSpatial);
			MotorsTime = new MotorsTimeConfiguration(c.MotorsTime);
		}
	}


	private List<UserObserver> _observers = new ArrayList<UserObserver>();

	private Configuration _config = new Configuration();

	public UserConfigurationManager() {}

	public SkinProcessor.ProcessingConfiguration GetProcessingConfiguration() {
		return new ProcessingConfiguration(_config.Processing);
	}

	public MotorsSpatial.MotorsSpatialConfiguration GetMotorsSpatialConfiguration() {
		return new MotorsSpatialConfiguration(_config.MotorsSpatial);
	}

	public SkinSerialPort.SerialConfiguration GetSerialConfiguration(){
		return new SerialConfiguration(_config.Serial);
	}
	
	public MotorsTime.MotorsTimeConfiguration GetMotorsTimeConfiguration() {
		return new MotorsTimeConfiguration(_config.MotorsTime);
	}

	public void SetProcessingConfiguration(SkinProcessor.ProcessingConfiguration conf) {
		_config.Processing = conf;

		for(UserObserver obs : _observers)
			obs.ProcessingConfigurationUpdated(new ProcessingConfiguration(_config.Processing));
	}

	public void SetMotorsSpatialConfiguration(MotorsSpatial.MotorsSpatialConfiguration conf) {
		_config.MotorsSpatial = conf;

		for(UserObserver obs : _observers)
			obs.MotorsSpatialConfigurationUpdated(new MotorsSpatialConfiguration(_config.MotorsSpatial));
	}

	public void SetSerialConfiguration(SkinSerialPort.SerialConfiguration conf) {
		_config.Serial = conf;

		for(UserObserver obs : _observers)
			obs.SerialConfigurationUpdated(new SerialConfiguration(_config.Serial));
	}

	public void SetMotorsTimeConfiguration(MotorsTime.MotorsTimeConfiguration conf) {
		_config.MotorsTime = conf;
		
		for(UserObserver obs : _observers)
			obs.MotorsTimeConfigurationUpdated(new MotorsTimeConfiguration(_config.MotorsTime));
	}
	
	public Configuration GetConfiguration() {
		return new Configuration(_config);
	}
	
	public void SetConfiguration(Configuration config) {
		SetSerialConfiguration(config.Serial != null ? config.Serial : new SerialConfiguration());
		SetMotorsSpatialConfiguration(config.MotorsSpatial != null ? config.MotorsSpatial : new MotorsSpatialConfiguration());
		SetProcessingConfiguration(config.Processing != null ? config.Processing : new ProcessingConfiguration());
		SetMotorsTimeConfiguration(config.MotorsTime != null ? config.MotorsTime : new MotorsTimeConfiguration());
	}

	public void AddObserver(UserObserver obs) {
		_observers.add(obs);
	}

	public void Save(File f) throws IOException{
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(f));
			out.writeObject(_config);
			out.flush();
		}
		finally {
			if(out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void Load(File f) throws IOException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(f));
			SetConfiguration((Configuration) ois.readObject());
			
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			if(ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
