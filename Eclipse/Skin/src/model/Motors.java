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
		public int RayGaussian = 60;
		public int RayUniformAverage = 60;
		
	}
	
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
            GaussianConvolBuffers[i] = GaussBuffer(centerX, centerY, _motorsConfig.RayGaussian);
        }
    }

    private float[] GaussBuffer(float centerX, float centerY, float sigma)
    {
        float[][] result = new float[_motorsConfig.InputCol][ _motorsConfig.InputRow];

        for (int i = 0; i < result.length; i++)
            for (int j = 0; j < result[i].length; j++)
                result[i][j] = (float) (1 / (2 * Math.PI * sigma * sigma) * Math.exp(-((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)) / (2 * sigma * sigma)));

        return MUtils.TwoDToOneD(result);
    }

    public void ComputeUniformAverageConvolBuffers()
    {
        UniformAverageConvolBuffers = new float[_motorsConfig.OutputCol * _motorsConfig.OutputRow][ _motorsConfig.InputCol * _motorsConfig.InputRow];

        for (int i = 0; i < UniformAverageConvolBuffers.length; i++)
        {
            float centerX = (i % _motorsConfig.OutputCol) / (float)_motorsConfig.OutputCol * _motorsConfig.InputCol + _motorsConfig.InputCol / (float)_motorsConfig.OutputCol / 2;
            float centerY = (i / _motorsConfig.OutputCol) / (float)_motorsConfig.OutputRow * _motorsConfig.InputRow + _motorsConfig.InputRow / (float)_motorsConfig.OutputRow / 2;
            UniformAverageConvolBuffers[i] = UniformAverageBuffer(centerX, centerY, _motorsConfig.RayUniformAverage, _motorsConfig.RayUniformAverage);
        }
    }

    private float[] UniformAverageBuffer(float centerX, float centerY, float rayonX, float rayonY)
    {
        float[][] result = new float[_motorsConfig.InputCol][ _motorsConfig.InputRow];

        for (int i = 0; i < result.length; i++)
            for (int j = 0; j < result[i].length; j++)
                if (Math.abs(i - centerX) < rayonX && Math.abs(j - centerY) < rayonY)
                    result[i][j] = 1 / (4f * rayonX * rayonY);
                else
                    result[i][j] = 0;

        return MUtils.TwoDToOneD(result);
    }



    public void CalculateGaussianOutput()
    {
        for (int i = 0; i < GaussianConvolBuffers.length; i++)
        {
            float sum = 0;
            
            for (int j = 0; j < _motorsConfig.InputCol * _motorsConfig.InputRow; j++)
                sum += GaussianConvolBuffers[i][j] * InputBuffer[j];

            OutputBuffer[i] = (int) sum;
        }
    }

    public void CalculateUniformAverageOutput()
    {
        for (int i = 0; i < UniformAverageConvolBuffers.length; i++)
        {
            float sum = 0;
            for (int j = 0; j < _motorsConfig.InputCol * _motorsConfig.InputRow; j++)
                sum += UniformAverageConvolBuffers[i][j] * InputBuffer[j];

            OutputBuffer[i] = sum;
        }
    }

    public float[] GetOutputBuffer()
    {
        return OutputBuffer;
    }
    
}