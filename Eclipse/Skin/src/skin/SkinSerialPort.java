package skin;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.serial.*;



public class SkinSerialPort
{

	final int SERIAL_RATE = 230400;
	final int     SKIN_COLS          = 12;
	final int     SKIN_ROWS          = 21;
	final int     SKIN_CELLS         = SKIN_COLS * SKIN_ROWS;


	private Serial _serialPort;
	private boolean _readingLoop;
	private SkinProcessor _bufferWanter;
	private Thread _readThread;
	private int COM_index = 0;
	private PApplet _applet;

	public SkinSerialPort(PApplet applet, SkinProcessor bufferWanter)
	{
		_applet = applet;
		System.out.println("COM available :");
		for (int i = 0; i < Serial.list().length; i++)
			System.out.println(Serial.list()[i]);

		_serialPort = new Serial( applet, Serial.list( )[ COM_index ], SERIAL_RATE );

		_readThread = new Thread(new Runnable() {
			public void run() {
				Read();
			}
		});

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
		while (_readingLoop)
		{
			int[] result = null;
			if ( _serialPort != null && _serialPort.available( ) > 0 ) {
				byte[] skinBuffer = _serialPort.readBytesUntil(0x00);

				if (skinBuffer != null && skinBuffer.length == SKIN_CELLS+1) {
					result = new int[SKIN_CELLS];
					for (int i = 0; i < SKIN_CELLS; i++)
						result[i] = skinBuffer[i] & 0xFF;
				}
			}

		}
		_serialPort.stop();
	}
}