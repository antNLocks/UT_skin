package model;

import java.io.Serializable;

public class Motors
{
	public static class MotorsConfiguration implements Serializable{
		private static final long serialVersionUID = 1L;

		public int InputCol;
		public int InputRow;
		public int OutputCol = 2;
		public int OutputRow = 4;
		public int DeviationGaussian = 60;
		public int DeviationUniform = 60;
		public float NormalisationFactorGaussian = 1;
		public float NormalisationFactorUniform = 1;

		public MotorsConfiguration() {}

		public MotorsConfiguration(MotorsConfiguration m) {
			InputCol = m.InputCol;
			InputRow = m.InputRow;
			OutputCol = m.OutputCol;
			OutputRow = m.OutputRow;
			DeviationGaussian = m.DeviationGaussian;
			DeviationUniform = m.DeviationUniform;
			NormalisationFactorGaussian = m.NormalisationFactorGaussian;
			NormalisationFactorUniform = m.NormalisationFactorUniform;
		}



	}

	@SuppressWarnings("serial")
	public static class MemoryException extends Exception{	};

	private MotorsConfiguration _motorsConfig;

	public float[] OutputBuffer;

	public float[] InputBuffer;

	public float[][] GaussianConvolBuffers;
	public float[][] UniformAverageConvolBuffers;



	public Motors(MotorsConfiguration motorsConfig)
	{
		_motorsConfig = motorsConfig;

		OutputBuffer = new float[_motorsConfig.OutputCol * _motorsConfig.OutputRow];
		ComputeGaussianConvolBuffers();
		ComputeUniformAverageConvolBuffers();
	}

	public void ComputeGaussianConvolBuffers()
	{
		GaussianConvolBuffers = new float[_motorsConfig.OutputCol * _motorsConfig.OutputRow][ _motorsConfig.InputCol * _motorsConfig.InputRow];

		for (int i = 0; i < GaussianConvolBuffers.length; i++)
		{
			float centerX = (i % _motorsConfig.OutputCol) / (float)_motorsConfig.OutputCol * _motorsConfig.InputCol + _motorsConfig.InputCol / (float)_motorsConfig.OutputCol / 2;
			float centerY = (i / _motorsConfig.OutputCol) / (float)_motorsConfig.OutputRow * _motorsConfig.InputRow + _motorsConfig.InputRow / (float)_motorsConfig.OutputRow / 2;
			GaussianConvolBuffers[i] = GaussBuffer(centerX, centerY, _motorsConfig.DeviationGaussian);
		}
	}

	private float[] GaussBuffer(float centerX, float centerY, float deviation)
	{
		float[][] result = new float[_motorsConfig.InputCol][ _motorsConfig.InputRow];

		for (int i = 0; i < result.length; i++)
			for (int j = 0; j < result[i].length; j++)
				result[i][j] = (float) (_motorsConfig.NormalisationFactorGaussian / (2 * Math.PI * deviation * deviation) * Math.exp(-((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)) / (2 * deviation * deviation)));

		return MUtils.TwoDToOneD(result);
	}

	public void ComputeUniformAverageConvolBuffers()
	{
		UniformAverageConvolBuffers = new float[_motorsConfig.OutputCol * _motorsConfig.OutputRow][ _motorsConfig.InputCol * _motorsConfig.InputRow];

		for (int i = 0; i < UniformAverageConvolBuffers.length; i++)
		{
			float centerX = (i % _motorsConfig.OutputCol) / (float)_motorsConfig.OutputCol * _motorsConfig.InputCol + _motorsConfig.InputCol / (float)_motorsConfig.OutputCol / 2;
			float centerY = (i / _motorsConfig.OutputCol) / (float)_motorsConfig.OutputRow * _motorsConfig.InputRow + _motorsConfig.InputRow / (float)_motorsConfig.OutputRow / 2;
			UniformAverageConvolBuffers[i] = UniformAverageBuffer(centerX,  centerY, 1.732f* _motorsConfig.DeviationUniform, 1.732f * _motorsConfig.DeviationUniform);
		}
	}

	private float[] UniformAverageBuffer(float centerX, float centerY, float supportRayX, float supportRayY)
	{
		float[][] result = new float[_motorsConfig.InputCol][ _motorsConfig.InputRow];

		for (int i = 0; i < result.length; i++)
			for (int j = 0; j < result[i].length; j++)
				if (Math.abs(i - centerX) < supportRayX && Math.abs(j - centerY) < supportRayY)
					result[i][j] = _motorsConfig.NormalisationFactorUniform / (4f * supportRayX * supportRayY);
				else
					result[i][j] = 0;

		return MUtils.TwoDToOneD(result);
	}



	public void CalculateGaussianOutput() throws MemoryException
	{

		try {
			for (int i = 0; i < GaussianConvolBuffers.length; i++)
			{
				float sum = 0;

				for (int j = 0; j < _motorsConfig.InputCol * _motorsConfig.InputRow; j++)
					sum += GaussianConvolBuffers[i][j] * InputBuffer[j];

				OutputBuffer[i] = Math.min(sum, 255);
			}
		}catch(Exception e) {throw new MemoryException();}

	}

	public void CalculateUniformAverageOutput() throws MemoryException
	{
		try {			
			for (int i = 0; i < UniformAverageConvolBuffers.length; i++)
			{
				float sum = 0;
				for (int j = 0; j < _motorsConfig.InputCol * _motorsConfig.InputRow; j++)
					sum += UniformAverageConvolBuffers[i][j] * InputBuffer[j];

				OutputBuffer[i] = Math.min(sum, 255);
			}
		}catch(Exception e) {throw new MemoryException();}
	}

	public float[] GetOutputBuffer()
	{
		return OutputBuffer;
	}

}