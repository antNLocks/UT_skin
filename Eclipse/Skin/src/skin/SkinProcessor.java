package skin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SkinProcessor
{

    private SkinSerialPort _skinPort;
    private List<ISkinListener> _skinListeners = new ArrayList<ISkinListener>();

    private  int _resizeFactor = 20;


    public int Noise_averageAlgo = 1; //0 : rolling average  |  1 : interpolation with previous frames
	public int Noise_framesForAverage = 3;
    public float Noise_interpolationFactor = 0.5f;
    public int MinThreshold = 90;
    public int MaxThreshold = 255;
    

    //Public access because I want to be close to the C# version which has { get; private set; }
    public int[] RawBuffer;
    public int RawBufferCol = 12;
    public int RawBufferRow = 21;
    public int[][] RawBuffer2d;

    public float[] ProcessedBuffer;
    public int ProcessedBufferCol = RawBufferCol * _resizeFactor;
    public int ProcessedBufferRow = RawBufferRow * _resizeFactor;
    public float[][] ProcessedBuffer2d;


   
    
    public SkinProcessor(int COM_index)
    {
    	_skinPort = new SkinSerialPort(this, COM_index, RawBufferCol*RawBufferRow);
    }
    
    public int getResizeFactor()
    {
    	return _resizeFactor;
    }

    public void setResizeFactor(int resizeFactor)
    {
    	_resizeFactor = resizeFactor;
    	ProcessedBufferCol = RawBufferCol * _resizeFactor;
    	ProcessedBufferRow = RawBufferRow * _resizeFactor;
    }

    
    public void StartProcessing()
    {
        _skinPort.StartReading();
    }
    
    public void StopProcessing()
    {
        _skinPort.StopReading();
    }
    

    public void Register(ISkinListener skinListener)
    {
        _skinListeners.add(skinListener);
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
        float[] averageBuffer = Noise_averageAlgo == 0 ? AverageBufferOverTime_rollingAverage(buffer, Noise_framesForAverage) : AverageBufferOverTime_interpolationPreviousFrames(buffer, Noise_interpolationFactor);
        float[] resizedBuffer = NaiveInterpolation.ResizeBufferBilinear(averageBuffer, _resizeFactor, RawBufferCol, RawBufferRow);
        float[] thresholdMappedBuffer = ThresholdMapping(resizedBuffer, MinThreshold, MaxThreshold);

        ProcessedBuffer = thresholdMappedBuffer;
        ProcessedBuffer2d = MUtils.OneDToTwoD(ProcessedBuffer, ProcessedBufferCol, ProcessedBufferRow);

        for (ISkinListener skinListener : _skinListeners)
            skinListener.BufferUpdate();

    }

    

    
    ArrayDeque<int[]> rawBuffers = new ArrayDeque<int[]>();
	private float[] AverageBufferOverTime_rollingAverage(int[] actualRawBuffer, int nbFrames) {
	  float[] result = new float[actualRawBuffer.length];

	  rawBuffers.add(actualRawBuffer);

	  while (rawBuffers.size() > nbFrames)
	    rawBuffers.poll();


	  Iterator<int[]> it = rawBuffers.iterator();
	  while (it.hasNext()) {
	    int[] rwB = it.next();
	    for (int i = 0; i < actualRawBuffer.length; i++)
	      result[i] += rwB[i] / (float) rawBuffers.size();
	  }

	  return result;
	}
    
    private float[] previousRawBuffer = null;
    private float[] AverageBufferOverTime_interpolationPreviousFrames(int[] actualRawBuffer, float k)
    {
		float[] result = new float[actualRawBuffer.length];

        if (previousRawBuffer == null)
        {
            previousRawBuffer = new float[actualRawBuffer.length];
            for (int i = 0; i < actualRawBuffer.length; i++)
                previousRawBuffer[i] = actualRawBuffer[i];
        }

        for (int i = 0; i < actualRawBuffer.length; i++)
            result[i] = (1 - k) * actualRawBuffer[i] + k * previousRawBuffer[i];

        previousRawBuffer = result;

        return result;
    }


    private float[] ThresholdMapping(float[] buffer, float min, float max)
    {
        float[] result = new float[buffer.length];

        for (int i = 0; i < buffer.length; i++)
            result[i] = MUtils.Map(MUtils.Clamp(buffer[i], min, max), min, max, 0, 255);

        return result;
    }
}

