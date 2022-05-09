using System.Collections.Generic;
using UnityEngine;
using System.IO.Ports;
using System.Threading;

public class SkinSerialPort
{
    private SerialPort _serialPort;
    private bool _readingLoop;
    private SkinProcessor _bufferWanter;
    private Thread _readThread;
    [SerializeField] private int COM_index = 0;

    public SkinSerialPort(SkinProcessor bufferWanter)
    {
        Debug.Log("COM available :");
        foreach (var s in SerialPort.GetPortNames())
            Debug.Log(s);

        _serialPort = new SerialPort(SerialPort.GetPortNames()[COM_index], 230400);
                
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

            while ((b = _serialPort.ReadByte()) != 0x00)
                buffer.Add(b);

            if (buffer.Count == 252)
                _bufferWanter.RawBufferUpdate(buffer.ToArray());

        }
        _serialPort.Close();
    }
}