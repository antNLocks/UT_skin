package model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SkinProcessor
{
	public static class ProcessingConfiguration implements Serializable{
		private static final long serialVersionUID = 1L;

		public int MinThreshold = 0;
		public int MaxThreshold = 255;
		public int Noise_averageAlgo = 0;
		public int Noise_framesForAverage = 3;
		public float Noise_interpolationFactor = 0.1f;

		public  int ResizeFactor = 20;

		public int RawBufferCol = 6;
		public int RawBufferRow = 6;
		public int ProcessedBufferCol() { return RawBufferCol * ResizeFactor; }
		public int ProcessedBufferRow() { return RawBufferRow * ResizeFactor; }

		public ProcessingConfiguration() {}

		public ProcessingConfiguration(ProcessingConfiguration p) {
			MinThreshold = p.MinThreshold;
			MaxThreshold = p.MaxThreshold;
			Noise_averageAlgo = p.Noise_averageAlgo;
			Noise_framesForAverage = p.Noise_framesForAverage;
			Noise_interpolationFactor = p.Noise_interpolationFactor;
			ResizeFactor = p.ResizeFactor;
			RawBufferCol = p.RawBufferCol;
			RawBufferRow = p.RawBufferRow;

		}


	}

	public ProcessingConfiguration ProcessingConfig = new ProcessingConfiguration();

	private List<ISkinListener> _skinListeners = new ArrayList<ISkinListener>();



	//Public access because I want to be close to the C# version which has { get; private set; }
	public AtomicReference<float[]> RawBuffer = new AtomicReference<>();

	public AtomicReference<float[]> ProcessedBuffer = new AtomicReference<>();

	private FPSAnalyser _fpsRawAnalyser = new FPSAnalyser();
	private FPSAnalyser _fpsProcessedAnalyser = new FPSAnalyser();


	public SkinProcessor() {
		Thread processingThread = new Thread(() -> {
			while(true) 
				ProcessBuffer();
		});

		processingThread.setDaemon(true);
		processingThread.start();
	}

	public void Register(ISkinListener skinListener)
	{
		_skinListeners.add(skinListener);
	}

	public float GetRawFPS() {
		return _fpsRawAnalyser.GetFPS();
	}

	public float GetProcessedFPS() {
		return _fpsProcessedAnalyser.GetFPS();
	}

	//Called by _skinSerialPort
	public void RawBufferUpdate(float[] rawBuffer)
	{
		RawBuffer.set(rawBuffer);;

		_fpsRawAnalyser.Tick();
	}

	private void ProcessBuffer()
	{
		if(RawBuffer.get() != null) {
			float[] averageBuffer = ProcessingConfig.Noise_averageAlgo == 0 ? 
					AverageBufferOverTime_rollingAverage(RawBuffer.get(), ProcessingConfig.Noise_framesForAverage) :
						AverageBufferOverTime_interpolationPreviousFrames(RawBuffer.get(), ProcessingConfig.Noise_interpolationFactor);
			float[] resizedBuffer = NaiveInterpolation.ResizeBufferBilinear(averageBuffer, ProcessingConfig.ResizeFactor, ProcessingConfig.RawBufferCol, ProcessingConfig.RawBufferRow);
			float[] thresholdMappedBuffer = ThresholdMapping(resizedBuffer, ProcessingConfig.MinThreshold, ProcessingConfig.MaxThreshold);

			ProcessedBuffer.set(thresholdMappedBuffer);
			
			for (ISkinListener skinListener : _skinListeners)
				skinListener.BufferUpdate();

			_fpsProcessedAnalyser.Tick();

		}

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

