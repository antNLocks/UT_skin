package model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SkinProcessor
{
	public static class ProcessingConfiguration implements Serializable{
		private static final long serialVersionUID = 1L;

		public int MinThreshold = 0;
		public int MaxThreshold = 255;
		public int Noise_averageAlgo = 1;
		public int Noise_framesForAverage = 3;
		public float Noise_interpolationFactor = 0.5f;

		private  int _resizeFactor = 20;

		public int RawBufferCol = 12;
		public int RawBufferRow = 21;
		public int ProcessedBufferCol = RawBufferCol * _resizeFactor;
		public int ProcessedBufferRow = RawBufferRow * _resizeFactor;
	}

	public ProcessingConfiguration ProcessingConfig = new ProcessingConfiguration();

	private SkinSerialPort _skinPort;
	private List<ISkinListener> _skinListeners = new ArrayList<ISkinListener>();




	//Public access because I want to be close to the C# version which has { get; private set; }
	public float[] RawBuffer;

	public float[][] RawBuffer2d;

	public float[] ProcessedBuffer;

	public float[][] ProcessedBuffer2d;




	public SkinProcessor(int COM_index)
	{
		_skinPort = new SkinSerialPort(this, COM_index, ProcessingConfig.RawBufferCol*ProcessingConfig.RawBufferRow);
	}


	public int getResizeFactor()
	{
		return ProcessingConfig._resizeFactor;
	}

	//TODO adapt
	public void setResizeFactor(int resizeFactor)
	{
		ProcessingConfig._resizeFactor = resizeFactor;
		ProcessingConfig.ProcessedBufferCol = ProcessingConfig.RawBufferCol * ProcessingConfig._resizeFactor;
		ProcessingConfig.ProcessedBufferRow = ProcessingConfig.RawBufferRow * ProcessingConfig._resizeFactor;
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
	public void RawBufferUpdate(float[] rawBuffer)
	{
		RawBuffer = rawBuffer;
		RawBuffer2d = MUtils.OneDToTwoD(RawBuffer, ProcessingConfig.RawBufferCol, ProcessingConfig.RawBufferRow);

		ProcessBuffer(rawBuffer);
	}

	private void ProcessBuffer(float[] buffer)
	{
		float[] averageBuffer = ProcessingConfig.Noise_averageAlgo == 0 ? 
				AverageBufferOverTime_rollingAverage(buffer, ProcessingConfig.Noise_framesForAverage) :
					AverageBufferOverTime_interpolationPreviousFrames(buffer, ProcessingConfig.Noise_interpolationFactor);
		float[] resizedBuffer = NaiveInterpolation.ResizeBufferBilinear(averageBuffer, ProcessingConfig._resizeFactor, ProcessingConfig.RawBufferCol, ProcessingConfig.RawBufferRow);
		float[] thresholdMappedBuffer = ThresholdMapping(resizedBuffer, ProcessingConfig.MinThreshold, ProcessingConfig.MaxThreshold);

		ProcessedBuffer = thresholdMappedBuffer;
		ProcessedBuffer2d = MUtils.OneDToTwoD(ProcessedBuffer, ProcessingConfig.ProcessedBufferCol, ProcessingConfig.ProcessedBufferRow);

		for (ISkinListener skinListener : _skinListeners)
			skinListener.BufferUpdate();

	}




	ArrayDeque<float[]> rawBuffers = new ArrayDeque<float[]>();
	private float[] AverageBufferOverTime_rollingAverage(float[] actualRawBuffer, int nbFrames) {
		float[] result = new float[actualRawBuffer.length];

		rawBuffers.add(actualRawBuffer);

		while (rawBuffers.size() > nbFrames)
			rawBuffers.poll();


		Iterator<float[]> it = rawBuffers.iterator();
		while (it.hasNext()) {
			float[] rwB = it.next();
			for (int i = 0; i < actualRawBuffer.length; i++)
				result[i] += rwB[i] / (float) rawBuffers.size();
		}

		return result;
	}

	private float[] previousRawBuffer = null;
	private float[] AverageBufferOverTime_interpolationPreviousFrames(float[] actualRawBuffer, float k)
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

