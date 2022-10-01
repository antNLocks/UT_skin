using System.Collections.Generic;
using UnityEngine;

/*
 *  This is the main script that can only be instantiated once.
 *  The instance of this script is accessible from the whole project through SkinProcessor.Instance (singleton pattern)
 *  This script instantiates SkinSerialPort and receives the raw buffers it processes.
 *  When it receives raw data, it processes it and then announces to all registered ISkinListener that new data is available
 */
public class SkinProcessor : MonoBehaviour
{
    public static SkinProcessor Instance { get; private set; }

    private SkinSerialPort _skinPort;
    private List<ISkinListener> _skinListeners = new List<ISkinListener>();

    private readonly int _resizeFactor = 30;

    [Range(0, 1)] public float noise_interpolation_factor = 0.5f;
    [Range(0, 254)] public int minThreshold = 90;
    [Range(0, 254)] public int maxThreshold = 254;



    public int[] RawBuffer { get; private set; }
    public int RawBufferCol { get; private set; } = 12;
    public int RawBufferRow { get; private set; } = 21;
    public int[,] RawBuffer2d { get; private set; }

    public float[] ProcessedBuffer { get; private set; }
    public int ProcessedBufferCol { get; private set; }
    public int ProcessedBufferRow { get; private set; }
    public float[,] ProcessedBuffer2d { get; private set; }



    private void Awake()
    {
        if (Instance != null)
        {
            DestroyImmediate(this.gameObject);
            return;
        }

        Instance = this;

        DontDestroyOnLoad(this);

        ProcessedBufferCol = (RawBufferCol - 1) * _resizeFactor + 1;
        ProcessedBufferRow = (RawBufferRow - 1) * _resizeFactor + 1;
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
        RawBuffer = rawBuffer;
        RawBuffer2d = MUtils.OneDToTwoD(RawBuffer, RawBufferCol, RawBufferRow);

        ProcessBuffer(rawBuffer);
    }

    private void ProcessBuffer(int[] buffer)
    {
        float[] averageBuffer = AverageBufferOverTime_interpolationPreviousFrames(buffer, noise_interpolation_factor);
        float[] resizedBuffer = ResizeBufferBilinear(averageBuffer, _resizeFactor, RawBufferCol, RawBufferRow);
        float[] thresholdMappedBuffer = ThresholdMapping(resizedBuffer, minThreshold, maxThreshold);

        ProcessedBuffer = thresholdMappedBuffer;
        ProcessedBuffer2d = MUtils.OneDToTwoD(ProcessedBuffer, ProcessedBufferCol, ProcessedBufferRow);

        foreach (var skinListener in _skinListeners)
            skinListener.BufferUpdate();
    }

    private float[] ResizeBufferBilinear(float[] rawBuffer, int resizeFactor, int col, int row)
    {
        float[] result = new float[((col - 1) * resizeFactor + 1) * ((row - 1) * resizeFactor + 1)];

        float[][] result2d = MUtils.CreateArray<float>((row - 1) * resizeFactor + 1, (col - 1) * resizeFactor + 1);

        float[][] colInter = MUtils.CreateArray<float>(col, (row - 1) * resizeFactor + 1);

        float[][] rawBuffer2d = MUtils.CreateArray<float>(col, row);

        for (int i = 0; i < col * row; i++)
            rawBuffer2d[i % col][i / col] = rawBuffer[i];

        for (int i = 0; i < col; i++)
            colInter[i] = InterLinear(rawBuffer2d[i], resizeFactor);

        float[][] colInterT = MUtils.CreateArray<float>((row - 1) * resizeFactor + 1, col);
        for (int i = 0; i < colInterT.Length; i++)
            for (int j = 0; j < colInterT[i].Length; j++)
                colInterT[i][j] = colInter[j][i];

        for (int i = 0; i < (row - 1) * resizeFactor + 1; i++)
            result2d[i] = InterLinear(colInterT[i], resizeFactor);

        for (int i = 0; i < result2d.Length; i++)
            for (int j = 0; j < result2d[i].Length; j++)
                result[i * result2d[i].Length + j] = result2d[i][j];

        return result;
    }

    // Linear interpolation
    private float[] InterLinear(float[] column, int resizeFactor)
    {
        float[] result = new float[(column.Length - 1) * resizeFactor + 1];

        for (int i = 0; i < column.Length - 1; i++)
            for (int j = 0; j < resizeFactor; j++)
            {
                float t = j / (float)resizeFactor;
                result[i * resizeFactor + j] = (1 - t) * column[i] + t * column[i + 1];
            }

        result[(column.Length - 1) * resizeFactor] = column[column.Length - 1];
        return result;
    }

    
    private float[] previousRawBuffer = null;
    private float[] AverageBufferOverTime_interpolationPreviousFrames(int[] actualRawBuffer, float k)
    {
        float[] result = new float[actualRawBuffer.Length];

        if (previousRawBuffer == null)
        {
            previousRawBuffer = new float[actualRawBuffer.Length];
            for (int i = 0; i < actualRawBuffer.Length; i++)
                previousRawBuffer[i] = actualRawBuffer[i];
        }

        for (int i = 0; i < actualRawBuffer.Length; i++)
            result[i] = (1 - k) * actualRawBuffer[i] + k * previousRawBuffer[i];

        previousRawBuffer = result;

        return result;
    }


    private float[] ThresholdMapping(float[] buffer, float min, float max)
    {
        float[] result = new float[buffer.Length];

        for (int i = 0; i < buffer.Length; i++)
            result[i] = MUtils.Map(Mathf.Clamp(buffer[i], min, max), min, max, 0, 255);

        return result;
    }
}