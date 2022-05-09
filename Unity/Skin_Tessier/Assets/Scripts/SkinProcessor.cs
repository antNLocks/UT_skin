using System.Collections.Generic;
using UnityEngine;

public class SkinProcessor : MonoBehaviour
{
    public static SkinProcessor Instance { get; private set; }

    private SkinSerialPort _skinPort;
    private List<ISkinListener> _skinListeners = new List<ISkinListener>();

    private void Awake()
    {
        if (Instance != null)
        {
            DestroyImmediate(this.gameObject);
            return;
        }

        Instance = this;

        DontDestroyOnLoad(this);
    }

    // Start is called before the first frame update
    void Start()
    {
        _skinPort = new SkinSerialPort(this);
        _skinPort.StartReading();
    }

    void OnApplicationQuit()
    {
        _skinPort.StopReading();
    }

    public void Register(ISkinListener skinListener)
    {
        _skinListeners.Add(skinListener);
    }

    //Called by _skinSerialPort
    public void RawBufferUpdate(int[] rawBuffer)
    {
        ProcessBuffer(rawBuffer);
    }

    private void ProcessBuffer(int[] buffer)
    {
        float[,] b = new float[12, 21];

        for (int i = 0; i < 252; i++)
        {
            b[11 - i % 12, i / 12] = (buffer[i] - 150) / (2.55f - 1.5f);
            if (b[11 - i % 12, i / 12] < 0)
                b[11 - i % 12, i / 12] = 0;
        }

        foreach (var skinListener in _skinListeners)
            skinListener.BufferUpdate(b);

    }
}
