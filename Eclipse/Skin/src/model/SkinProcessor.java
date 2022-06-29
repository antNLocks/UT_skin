package model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;

public class SkinProcessor extends ThreadProcess
{
	public static class ProcessingConfiguration implements Serializable{
		private static final long serialVersionUID = 1L;

		public int MinThreshold;
		public int MaxThreshold;
		public int Noise_averageAlgo;
		public int Noise_framesForAverage;
		public float Noise_interpolationFactor;
		public long SleepingTime;

		public  int ResizeFactorCol;
		public  int ResizeFactorRow;

		public int RawBufferCol;
		public int RawBufferRow;
		public int ProcessedBufferCol() { return RawBufferCol * ResizeFactorCol; }
		public int ProcessedBufferRow() { return RawBufferRow * ResizeFactorRow; }

		public ProcessingConfiguration() {}

		public ProcessingConfiguration(ProcessingConfiguration p) {
			MinThreshold = p.MinThreshold;
			MaxThreshold = p.MaxThreshold;
			Noise_averageAlgo = p.Noise_averageAlgo;
			Noise_framesForAverage = p.Noise_framesForAverage;
			Noise_interpolationFactor = p.Noise_interpolationFactor;
			ResizeFactorCol = p.ResizeFactorCol;
			ResizeFactorRow = p.ResizeFactorRow;
			RawBufferCol = p.RawBufferCol;
			RawBufferRow = p.RawBufferRow;
			SleepingTime = p.SleepingTime;
		}
	}

	public ProcessingConfiguration ProcessingConfig = new ProcessingConfiguration();


	//Public access because I want to be close to the C# version which has { get; private set; }

	public AtomicReference<float[]> ProcessedOutputBuffer = new AtomicReference<>();

	public AtomicReference<float[]> RawInputBuffer = new AtomicReference<>();
	
	private ArrayDeque<float[]> _rawBuffers = new ArrayDeque<float[]>();



	@Override
	protected void Process() {
		try {
			float[] averageBuffer = ProcessingConfig.Noise_averageAlgo == 0 ? 
					MUtils.RollingAverage(RawInputBuffer.get(), ProcessingConfig.Noise_framesForAverage, _rawBuffers) :
						AverageBufferOverTime_interpolationPreviousFrames(RawInputBuffer.get(), ProcessingConfig.Noise_interpolationFactor);
			float[] resizedBuffer = NaiveInterpolation.ResizeBufferBilinear(averageBuffer, ProcessingConfig.ResizeFactorCol, ProcessingConfig.ResizeFactorRow, ProcessingConfig.RawBufferCol, ProcessingConfig.RawBufferRow);
			float[] thresholdMappedBuffer = ThresholdMapping(resizedBuffer, ProcessingConfig.MinThreshold, ProcessingConfig.MaxThreshold);

			ProcessedOutputBuffer.set(thresholdMappedBuffer);

		}catch(Exception e) {/*The user changed the motor configuration while we were calculated motors output*/}
	}

	@Override
	protected void Sleep() {
		try {
			Thread.sleep(ProcessingConfig.SleepingTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.Sleep();
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

