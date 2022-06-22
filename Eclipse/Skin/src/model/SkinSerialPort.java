package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.fazecast.jSerialComm.SerialPort;



public class SkinSerialPort extends ThreadProcess
{

	public static class SerialConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;

		public int BufferSize = 200;
		public int ByteSeparator = 0x00;
		public int Baudrate = 250000;
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
	private InputStream _in;

	public AtomicReference<float[]> RawOutputBuffer = new AtomicReference<>();


	public SkinSerialPort(int COM_index, SerialConfiguration serialConfig)
	{
		_serialConfig = serialConfig;

		_serialPort = SerialPort.getCommPorts()[COM_index];
		_serialPort.setBaudRate(_serialConfig.Baudrate);

		SetGain(_serialConfig.HardwareGain);

	}

	@Override
	protected void Process() {
		List<Float> buffer;

		do {
			buffer = new ArrayList<Float>();

			try {
				int b;
				while ((b = _in.read()) != _serialConfig.ByteSeparator && _processLoopThread._threadRunning)
					buffer.add((float)b);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}while (buffer.size() != _serialConfig.BufferSize && _processLoopThread._threadRunning);

		if(_processLoopThread._threadRunning)
			RawOutputBuffer.set(MUtils.ToArray(buffer));
	}

	@Override
	public void StartThread() {
		_serialPort.openPort();
		_serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		_in = _serialPort.getInputStreamWithSuppressedTimeoutExceptions();
		super.StartThread();
	}

	@Override
	public void StopThread() {
		super.StopThread();
		_serialPort.closePort();
	}

	public void SetSerialConfiguration(SerialConfiguration config) {
		if(config.Baudrate != _serialConfig.Baudrate) {
				_processLoopThread._onExit = () -> {
					StopThread();
					_serialPort.setBaudRate(config.Baudrate);
					StartThread();
				};
				super.StopThread();
		}


		if(config.HardwareGain != _serialConfig.HardwareGain)
			SetGain(config.HardwareGain);

		_serialConfig = config;
	}

	public void AskCalibration() {
		try {
			_serialPort.getOutputStream().write(0x01);
		} catch (IOException e) { e.printStackTrace(); }
	}

	public void AskScaleCalibration() {
		try {
			_serialPort.getOutputStream().write(0x02);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	private void SetGain(int gain) {
		try {
			_serialPort.getOutputStream().write(0x03);
			_serialPort.getOutputStream().write((byte) gain);
			_serialPort.getOutputStream().flush();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	
}