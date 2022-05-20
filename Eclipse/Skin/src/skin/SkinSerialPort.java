package skin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;



public class SkinSerialPort
{

	static final int SERIAL_RATE = 230400;
	


	private SerialPort _serialPort;
	private boolean _readingLoop;
	private SkinProcessor _bufferWanter;
	private Thread _readThread;
	private int _skin_cells;

	public SkinSerialPort(SkinProcessor bufferWanter, int COM_index, int skin_cells)
	{
		System.out.println("COM available :");

		SerialPort[] Device_Ports = SerialPort.getCommPorts();

		for (SerialPort port: Device_Ports) 
			System.out.println(port.getSystemPortName());

		_serialPort = SerialPort.getCommPorts()[COM_index];
		_serialPort.setBaudRate(SERIAL_RATE);

		_readThread = new Thread(new Runnable() {
			public void run() {
				Read();
			}
		});

		_bufferWanter = bufferWanter;
		_skin_cells = skin_cells;
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
			List<Integer> buffer = new ArrayList<Integer>();

			try {
				while ((b = in.read()) != 0x00)
					buffer.add(b);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (buffer.size() == _skin_cells)
				_bufferWanter.RawBufferUpdate(MUtils.ToArray(buffer));



		}
		_serialPort.closePort();	
	}
}