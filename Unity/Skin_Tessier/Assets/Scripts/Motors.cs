using UnityEngine;

public class Motors
{

    public float[] OutputBuffer { get; private set; }
    private int _outputCol, _outputRow;

    public float[] InputBuffer { get; set; }
    private int _inputCol , _inputRow;

    public float[][] GaussianConvolBuffers { get; private set; }
    public float[][] UniformAverageConvolBuffers { get; private set; }

    public float RayGaussian { get; set; } = 60;
    public float RayUniformAverage { get; set; } = 60;



    public Motors(int inputCol, int inputRow, int outputCol, int outputRow)
    {
        this._inputCol = inputCol;
        this._inputRow = inputRow;
        this._outputCol = outputCol;
        this._outputRow = outputRow;

        OutputBuffer = new float[outputCol * outputRow];
        ComputeGaussianConvolBuffers();
        ComputeUniformAverageConvolBuffers();
    }

    public void ComputeGaussianConvolBuffers()
    {
        GaussianConvolBuffers = MUtils.CreateArray<float>(_outputCol * _outputRow, _inputCol * _inputRow);

        for (int i = 0; i < GaussianConvolBuffers.Length; i++)
        {
            float centerX = (i % _outputCol) / (float)_outputCol * _inputCol + _inputCol / (float)_outputCol / 2;
            float centerY = (i / _outputCol) / (float)_outputRow * _inputRow + _inputRow / (float)_outputRow / 2;
            GaussianConvolBuffers[i] = GaussBuffer(centerX, centerY, RayGaussian);
        }
    }

    private float[] GaussBuffer(float centerX, float centerY, float sigma)
    {
        float[][] result = MUtils.CreateArray<float>(_inputCol, _inputRow);

        for (int i = 0; i < result.Length; i++)
            for (int j = 0; j < result[i].Length; j++)
                result[i][j] = 1 / (2 * Mathf.PI * sigma * sigma) * Mathf.Exp(-((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)) / (2 * sigma * sigma));

        return MUtils.TwoDToOneD(result);
    }

    public void ComputeUniformAverageConvolBuffers()
    {
        UniformAverageConvolBuffers = MUtils.CreateArray<float>(_outputCol * _outputRow, _inputCol * _inputRow);

        for (int i = 0; i < UniformAverageConvolBuffers.Length; i++)
        {
            float centerX = (i % _outputCol) / (float)_outputCol * _inputCol + _inputCol / (float)_outputCol / 2;
            float centerY = (i / _outputCol) / (float)_outputRow * _inputRow + _inputRow / (float)_outputRow / 2;
            UniformAverageConvolBuffers[i] = UniformAverageBuffer(centerX, centerY, RayUniformAverage, RayUniformAverage);
        }
    }

    private float[] UniformAverageBuffer(float centerX, float centerY, float rayonX, float rayonY)
    {
        float[][] result = MUtils.CreateArray<float>(_inputCol, _inputRow);

        for (int i = 0; i < result.Length; i++)
            for (int j = 0; j < result[i].Length; j++)
                if (Mathf.Abs(i - centerX) < rayonX && Mathf.Abs(j - centerY) < rayonY)
                    result[i][j] = 1 / (4f * rayonX * rayonY);
                else
                    result[i][j] = 0;

        return MUtils.TwoDToOneD(result);
    }



    public void CalculateGaussianOutput()
    {
        for (int i = 0; i < GaussianConvolBuffers.Length; i++)
        {
            float sum = 0;
            for (int j = 0; j < _inputCol * _inputRow; j++)
                sum += GaussianConvolBuffers[i][j] * InputBuffer[j];

            OutputBuffer[i] = sum;
        }
    }

    public void CalculateUniformAverageOutput()
    {
        for (int i = 0; i < UniformAverageConvolBuffers.Length; i++)
        {
            float sum = 0;
            for (int j = 0; j < _inputCol * _inputRow; j++)
                sum += UniformAverageConvolBuffers[i][j] * InputBuffer[j];

            OutputBuffer[i] = sum;
        }
    }

    public float[] GetOutputBuffer()
    {
        return OutputBuffer;
    }
    
}