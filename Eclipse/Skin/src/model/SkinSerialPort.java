package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;



public class SkinSerialPort
{

	public static class SerialConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public int BufferSize = 36;
		public int ByteSeparator = 0x00;
		public int Baudrate = 230400;
		public int HardwareGain = 2;
		
		public SerialConfiguration(SerialConfiguration s) {
			BufferSize = s.BufferSize;
			ByteSeparator = s.ByteSeparator;
			Baudrate = s.Baudrate;
			HardwareGain = s.HardwareGain;
		}
		
		public SerialConfiguration() {}
	}
	
	


	private SerialConfiguration _serialConfig;
	
	private SerialPort _serialPort;
	private boolean _readingLoop;
	private SkinProcessor _bufferWanter;
	private Thread _readThread;

	public SkinSerialPort(SkinProcessor bufferWanter, int COM_index, SerialConfiguration serialConfig)
	{
		_serialConfig = serialConfig;
		
		_serialPort = SerialPort.getCommPorts()[COM_index];
		_serialPort.setBaudRate(_serialConfig.Baudrate);
		_serialPort.openPort();

		SetGain(_serialConfig.HardwareGain);

		_bufferWanter = bufferWanter;
	}
	
	public void SetSerialConfiguration(SerialConfiguration config) {
		if(config.Baudrate != _serialConfig.Baudrate) {
			new Thread(() -> {
				StopReading();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				_serialPort.setBaudRate(config.Baudrate);
				StartReading();
			}).start();
		}
		
		if(config.HardwareGain != _serialConfig.HardwareGain)
			SetGain(config.HardwareGain);
		
		_serialConfig = config;
	}

	public void StartReading()
	{
		_readingLoop = true;
		_serialPort.openPort();
		_readThread = new Thread(()-> Read());
		_readThread.setDaemon(true); //Don't want to have this thread run when the app is closed
		_readThread.start();
	}


	public void StopReading()
	{
		_readingLoop = false;
		_serialPort.closePort();	//Will likely be the cause of an exception which will be caught
	}
	
	public void Send(String s) {
		new PrintStream(_serialPort.getOutputStream()).println(s);
	}

	private void Read() {
		_serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		InputStream in = _serialPort.getInputStreamWithSuppressedTimeoutExceptions();
		while (_readingLoop)
		{
			int b;
			List<Float> buffer = new ArrayList<Float>();

			try {
				while ((b = in.read()) != _serialConfig.ByteSeparator && _readingLoop)
					buffer.add((float)b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			

			if (buffer.size() == _serialConfig.BufferSize)
				_bufferWanter.RawBufferUpdate(MUtils.ToArray(buffer));
		}
	}
	
	private void SetGain(int gain) {
		Send("g:" + gain);
	}
}