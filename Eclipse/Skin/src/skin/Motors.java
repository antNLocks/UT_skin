package skin;

public class Motors
{

    public int[] OutputBuffer;
    private int _outputCol, _outputRow;

    public float[] InputBuffer;
    private int _inputCol , _inputRow;

    public float[][] GaussianConvolBuffers;
    public float[][] UniformAverageConvolBuffers;


    public float RayGaussian = 60;
    public float RayUniformAverage = 60;



    public Motors(int inputCol, int inputRow, int outputCol, int outputRow)
    {
        this._inputCol = inputCol;
        this._inputRow = inputRow;
        this._outputCol = outputCol;
        this._outputRow = outputRow;

        OutputBuffer = new int[outputCol * outputRow];
        ComputeGaussianConvolBuffers();
        ComputeUniformAverageConvolBuffers();
    }

    public void ComputeGaussianConvolBuffers()
    {
        GaussianConvolBuffers = new float[_outputCol * _outputRow][ _inputCol * _inputRow];

        for (int i = 0; i < GaussianConvolBuffers.length; i++)
        {
            float centerX = (i % _outputCol) / (float)_outputCol * _inputCol + _inputCol / (float)_outputCol / 2;
            float centerY = (i / _outputCol) / (float)_outputRow * _inputRow + _inputRow / (float)_outputRow / 2;
            GaussianConvolBuffers[i] = GaussBuffer(centerX, centerY, RayGaussian);
        }
    }

    private float[] GaussBuffer(float centerX, float centerY, float sigma)
    {
        float[][] result = new float[_inputCol][ _inputRow];

        for (int i = 0; i < result.length; i++)
            for (int j = 0; j < result[i].length; j++)
                result[i][j] = (float) (1 / (2 * Math.PI * sigma * sigma) * Math.exp(-((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)) / (2 * sigma * sigma)));

        return MUtils.TwoDToOneD(result);
    }

    public void ComputeUniformAverageConvolBuffers()
    {
        UniformAverageConvolBuffers = new float[_outputCol * _outputRow][ _inputCol * _inputRow];

        for (int i = 0; i < UniformAverageConvolBuffers.length; i++)
        {
            float centerX = (i % _outputCol) / (float)_outputCol * _inputCol + _inputCol / (float)_outputCol / 2;
            float centerY = (i / _outputCol) / (float)_outputRow * _inputRow + _inputRow / (float)_outputRow / 2;
            UniformAverageConvolBuffers[i] = UniformAverageBuffer(centerX, centerY, RayUniformAverage, RayUniformAverage);
        }
    }

    private float[] UniformAverageBuffer(float centerX, float centerY, float rayonX, float rayonY)
    {
        float[][] result = new float[_inputCol][ _inputRow];

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
            
            for (int j = 0; j < _inputCol * _inputRow; j++)
                sum += GaussianConvolBuffers[i][j] * InputBuffer[j];

            OutputBuffer[i] = (int) sum;
        }
    }

    public void CalculateUniformAverageOutput()
    {
        for (int i = 0; i < UniformAverageConvolBuffers.length; i++)
        {
            float sum = 0;
            for (int j = 0; j < _inputCol * _inputRow; j++)
                sum += UniformAverageConvolBuffers[i][j] * InputBuffer[j];

            OutputBuffer[i] = (int) sum;
        }
    }

    public int[] GetOutputBuffer()
    {
        return OutputBuffer;
    }
    
}