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

import model.Motors.MotorsConfiguration;
import model.SkinProcessor.ProcessingConfiguration;
import model.SkinSerialPort.SerialConfiguration;

public class UserConfigurationManager {

	public static interface UserObserver {
		public void ProcessingConfigurationUpdated(SkinProcessor.ProcessingConfiguration userConfig);
		public void MotorsConfigurationUpdated(Motors.MotorsConfiguration userConfig);
		public void SerialConfigurationUpdated(SkinSerialPort.SerialConfiguration userConfig);
	}

	private static class Configuration implements Serializable{
		private static final long serialVersionUID = 1L;

		public SerialConfiguration Serial = new SerialConfiguration();
		public ProcessingConfiguration Processing = new ProcessingConfiguration();
		public MotorsConfiguration Motors = new MotorsConfiguration();
	}


	private List<UserObserver> _observers = new ArrayList<UserObserver>();

	private Configuration _config = new Configuration();

	public UserConfigurationManager() {}

	public SkinProcessor.ProcessingConfiguration GetProcessingConfiguration() {
		return new ProcessingConfiguration(_config.Processing);
	}

	public Motors.MotorsConfiguration GetMotorsConfiguration() {
		return new MotorsConfiguration(_config.Motors);
	}

	public SkinSerialPort.SerialConfiguration GetSerialConfiguration(){
		return new SerialConfiguration(_config.Serial);
	}

	public void SetProcessingConfiguration(SkinProcessor.ProcessingConfiguration conf) {
		_config.Processing = conf;

		for(UserObserver obs : _observers)
			obs.ProcessingConfigurationUpdated(new ProcessingConfiguration(_config.Processing));
	}

	public void SetMotorsConfiguration(Motors.MotorsConfiguration conf) {
		_config.Motors = conf;

		for(UserObserver obs : _observers)
			obs.MotorsConfigurationUpdated(new MotorsConfiguration(_config.Motors));
	}

	public void SetSerialConfiguration(SkinSerialPort.SerialConfiguration conf) {
		_config.Serial = conf;

		for(UserObserver obs : _observers)
			obs.SerialConfigurationUpdated(new SerialConfiguration(_config.Serial));
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
			Configuration config = (Configuration) ois.readObject();
			
			SetSerialConfiguration(config.Serial);
			SetMotorsConfiguration(config.Motors);
			SetProcessingConfiguration(config.Processing);
			
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
