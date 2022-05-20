package skin;

public class NaiveInterpolation {

	public static float[] ResizeBufferBilinear(float[] rawBuffer, int resizeFactor, int col, int row)
	{
		float[] result = new float[col * resizeFactor * row * resizeFactor];

		float[][] result2d = new float[row * resizeFactor][col* resizeFactor];

		float[][] colInter = new float [col][row * resizeFactor];

		float[][] rawBuffer2d = new float[col][row];

		for (int i = 0; i < col * row; i++)
			rawBuffer2d[i % col][i / col] = rawBuffer[i];

		for (int i = 0; i < col; i++)
			colInter[i] = InterLinear(rawBuffer2d[i], resizeFactor);

		float[][] colInterT = new float[row * resizeFactor][col];
		for (int i = 0; i < colInterT.length; i++)
			for (int j = 0; j < colInterT[i].length; j++)
				colInterT[i][j] = colInter[j][i];

		for (int i = 0; i < row * resizeFactor; i++)
			result2d[i] = InterLinear(colInterT[i], resizeFactor);

		for (int i = 0; i < result2d.length; i++)
			for (int j = 0; j < result2d[i].length; j++)
				result[i * result2d[i].length + j] = result2d[i][j];

		return result;
	}

	private static float[] InterLinear(float[] column, int resizeFactor)
	{
		float[] result = new float[column.length * resizeFactor];

		for (int i = 0; i < column.length - 1; i++)
			for (int j = 0; j < resizeFactor; j++)
			{
				float t = j / (float)resizeFactor;
				result[i * resizeFactor + resizeFactor / 2 + j] = (1 - t) * column[i] + t * column[i + 1];
			}

		for(int i = 0; i < resizeFactor; i++)
		{
			if(i < resizeFactor / 2)
				result[i] = column[0];
			else
				result[(column.length - 1) * resizeFactor + i] = column[column.length - 1];
		}
		
		return result;
	}

	public static int[] ResizeBufferNearest(int[] rawBuffer, int resizeFactorCol, int resizeFactorRow, int col, int row)
	{
		int[] result = new int[col * resizeFactorCol * row * resizeFactorRow];

		int[][] result2d = new int[row * resizeFactorRow][col* resizeFactorCol];

		int[][] colInter = new int [col][row * resizeFactorRow];

		int[][] rawBuffer2d = new int[col][row];

		for (int i = 0; i < col * row; i++)
			rawBuffer2d[i % col][i / col] = rawBuffer[i];

		for (int i = 0; i < col; i++)
			colInter[i] = InterNearest(rawBuffer2d[i], resizeFactorRow);

		int[][] colInterT = new int[row * resizeFactorRow][col];
		for (int i = 0; i < colInterT.length; i++)
			for (int j = 0; j < colInterT[i].length; j++)
				colInterT[i][j] = colInter[j][i];

		for (int i = 0; i < row * resizeFactorRow; i++)
			result2d[i] = InterNearest(colInterT[i], resizeFactorCol);

		for (int i = 0; i < result2d.length; i++)
			for (int j = 0; j < result2d[i].length; j++)
				result[i * result2d[i].length + j] = result2d[i][j];

		return result;
	}

	private static int[] InterNearest(int[] column, int resizeFactor)
	{
		int[] result = new int[column.length * resizeFactor];

		for (int i = 0; i < column.length - 1; i++)
			for (int j = 0; j < resizeFactor; j++)
			{
				float t = j / (float)resizeFactor;
				result[i * resizeFactor + resizeFactor / 2 + j] = t < 0.5 ? column[i] : column[i + 1];
			}

		for(int i = 0; i < resizeFactor; i++)
		{
			if(i < resizeFactor / 2)
				result[i] = column[0];
			else
				result[(column.length - 1) * resizeFactor + i] = column[column.length - 1];

		}
		return result;
	}
}
