public class Motors {

  private float[] outputBuffer;
  private int outputCol, outputRow;

  private float[] inputBuffer;
  private int inputCol, inputRow;

  private float[][] gaussianConvolBuffers;
  private float[][] averageConvolBuffers;



  public Motors(int inputCol, int inputRow, int outputCol, int outputRow) {
    this.inputCol = inputCol;
    this.inputRow = inputRow;
    this.outputCol = outputCol;
    this.outputRow = outputRow;

    outputBuffer = new float[outputCol*outputRow];
    computeGaussianConvolBuffers();
    computeAverageConvolBuffers();
  }

  private void computeGaussianConvolBuffers() {
    gaussianConvolBuffers = new float[outputCol*outputRow][inputCol*inputRow];

    for (int i = 0; i < gaussianConvolBuffers.length; i++) {
      float centerX = (i % outputCol) / (float) outputCol * inputCol + inputCol / (float) outputCol / 2;
      float centerY = (i / outputCol) / (float) outputRow * inputRow + inputRow / (float) outputRow / 2;
      gaussianConvolBuffers[i] = gaussBuffer(centerX, centerY, 60);
    }
  }

  private float[] gaussBuffer(float centerX, float centerY, float sigma) {
    float[][] result = new float[inputCol][inputRow];

    for (int i = 0; i < result.length; i++)
      for (int j = 0; j < result[i].length; j++)
        result[i][j] = 1 / (2*PI*sigma*sigma) * exp(-((i - centerX)*(i - centerX) + (j - centerY)*(j - centerY)) / (2 * sigma * sigma));

    return twoDToOneD(result);
  }

  private void computeAverageConvolBuffers() {
    averageConvolBuffers = new float[outputCol*outputRow][inputCol*inputRow];

    for (int i = 0; i < averageConvolBuffers.length; i++) {
      float centerX = (i % outputCol) / (float) outputCol * inputCol + inputCol / (float) outputCol / 2;
      float centerY = (i / outputCol) / (float) outputRow * inputRow + inputRow / (float) outputRow / 2;
      averageConvolBuffers[i] = averageBuffer(centerX, centerY, 60, 60);
    }
  }

  private float[] averageBuffer(float centerX, float centerY, float rayonX, float rayonY) {
    float[][] result = new float[inputCol][inputRow];

    for (int i = 0; i < result.length; i++)
      for (int j = 0; j < result[i].length; j++)
        if (abs(i - centerX) < rayonX && abs(j - centerY) < rayonY)
          result[i][j] = 1 / (4f * rayonX * rayonY) ;
        else
          result[i][j] = 0;

    return twoDToOneD(result);
  }

  public float[][] getAverageConvolBuffers() {
    return averageConvolBuffers;
  }

  public float[][] getGaussianConvolBuffers() {
    return gaussianConvolBuffers;
  }

  public void setInputBuffer(float[] buffer) {
    this.inputBuffer = buffer;
  }



  public void calculateGaussianOutput() {
    for (int i = 0; i < gaussianConvolBuffers.length; i++) {
      float sum = 0;
      for (int j = 0; j < inputCol*inputRow; j++)
        sum += gaussianConvolBuffers[i][j] * inputBuffer[j];

      outputBuffer[i] = sum;
    }
  }
  
   public void calculateAverageOutput() {
    for (int i = 0; i < gaussianConvolBuffers.length; i++) {
      float sum = 0;
      for (int j = 0; j < inputCol*inputRow; j++)
        sum += averageConvolBuffers[i][j] * inputBuffer[j];

      outputBuffer[i] = sum;
    }
  }

  public float[] getOutputBuffer() {
    return outputBuffer;
  }

  public PImage getOutputVisual(Size s) {
    PImage result = createImage((int) s.width, (int) s.height, RGB);

    return result;
  }

  private float[][] oneDToTwoD(float[] buffer, int col, int row) {
    float[][] result = new float[col][row];

    for (int i = 0; i < buffer.length; i++)
      result[i % col][i / col] = buffer[i];

    return result;
  }



  private float[] twoDToOneD(float[][] buffer2d) {
    float[] result = new float[buffer2d.length * buffer2d[0].length];

    for (int i = 0; i < buffer2d.length; i++)
      for (int j = 0; j < buffer2d[i].length; j++)
        result[j*buffer2d.length + i] = buffer2d[i][j];

    return result;
  }
}
