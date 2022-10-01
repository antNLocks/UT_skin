using System.Collections.Generic;
using UnityEngine;
using System.IO.Ports;
using System.Threading;

/*
 * This script is instantiated by SkinProcessor.
 * It reads the raw data from the serial port and sends it to SkinProcessor.
 * 
 * Please configure the COM_Index correctly for the PC
 */
public class SkinSerialPort
{
    private SerialPort _serialPort;
    private bool _readingLoop;
    private SkinProcessor _bufferWanter;
    private Thread _readThread;
    [SerializeField] private int COM_index = 0;
    [SerializeField] private int serialRate = 250000;


    public SkinSerialPort(SkinProcessor bufferWanter)
    {
        Debug.Log("COM available :");
        foreach (var s in SerialPort.GetPortNames())
            Debug.Log(s);

        _serialPort = new SerialPort(SerialPort.GetPortNames()[COM_index], serialRate);
                
        _readThread = new Thread(Read);
        _bufferWanter = bufferWanter;
    }

    public void StartReading()
    {
        _readingLoop = true;
        _readThread.Start();
    }


    public void StopReading()
    {
        _readingLoop = false;
    }

    private void Read()
    {
        _serialPort.Open();
        while (_readingLoop)
        {
            int b;
            List<int> buffer = new List<int>();

            while ((b = _serialPort.ReadByte()) != 0xff) // 0xff is end of frame byte
                buffer.Add(b);

            if (buffer.Count == SkinProcessor.Instance.RawBufferCol* SkinProcessor.Instance.RawBufferRow)
                _bufferWanter.RawBufferUpdate(buffer.ToArray());

        }
        _serialPort.Close();
    }
}