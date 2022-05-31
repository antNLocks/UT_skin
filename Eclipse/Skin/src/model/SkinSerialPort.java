package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;



public class SkinSerialPort
{

	public static class SerialConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public int BufferSize = 252;
		public int ByteSeparator = 0x00;
		public int Baudrate = 230400;
		
		public SerialConfiguration(SerialConfiguration s) {
			BufferSize = s.BufferSize;
			ByteSeparator = s.ByteSeparator;
			Baudrate = s.Baudrate;
			
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

		_readThread = new Thread(new Runnable() {
			public void run() {
				Read();
			}
		});
		_readThread.setDaemon(true); //Don't want to have this thread run when the app is closed

		_bufferWanter = bufferWanter;
	}

	public void StartReading()
	{
		_readingLoop = true;
		_readThread.start();
	}


	public void StopReading()
	{
		_readingLoop = false;

	}

	private void Read() {
		_serialPort.openPort();
		_serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		InputStream in = _serialPort.getInputStream();
		while (_readingLoop)
		{

			int b;
			List<Float> buffer = new ArrayList<Float>();

			try {
				while ((b = in.read()) != _serialConfig.ByteSeparator)
					buffer.add((float)b);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (buffer.size() == _serialConfig.BufferSize)
				_bufferWanter.RawBufferUpdate(MUtils.ToArray(buffer));



		}
		_serialPort.closePort();	
	}
}