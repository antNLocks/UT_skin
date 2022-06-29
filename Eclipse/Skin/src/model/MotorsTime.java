package model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;

public class MotorsTime extends ThreadProcess {

	public static class MotorsTimeConfiguration implements Serializable{
		private static final long serialVersionUID = 1L;

		public int TrailingFrames;
		public long SleepingTime;

		public MotorsTimeConfiguration() {}

		public MotorsTimeConfiguration(MotorsTimeConfiguration m) {
			TrailingFrames = m.TrailingFrames;
			SleepingTime = m.SleepingTime;
		}


	}

	public MotorsTimeConfiguration MotorsTimeConfig = new MotorsTimeConfiguration();

	public AtomicReference<float[]> TimeOutputBuffer = new AtomicReference<>();
	public AtomicReference<float[]> SpatialInputBuffer = new AtomicReference<>();

	private ArrayDeque<float[]> _spatialBuffers = new ArrayDeque<float[]>();


	@Override
	protected void Process() {
		try {
			float[] input = SpatialInputBuffer.get();

			float[] averageBuffer = MUtils.RollingAverage(input, MotorsTimeConfig.TrailingFrames, _spatialBuffers);

			float[] output = new float[averageBuffer.length];
			for(int i = 0; i < averageBuffer.length; i++)
				output[i] = Math.max(input[i], averageBuffer[i]);

			TimeOutputBuffer.set(output);
		}catch(Exception e) {}
	}

	@Override
	protected void Sleep() throws InterruptedException {
		Thread.sleep(MotorsTimeConfig.SleepingTime);
		super.Sleep();
	}

}
