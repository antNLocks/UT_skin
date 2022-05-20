package skin;

import java.util.List;

public class MUtils
{


	public static float Map(float value, float fromLow, float fromHigh, float toLow, float toHigh)
	{
		return (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow) + toLow;
	}
	
	public static float Clamp(float value, float min, float max)
	{
		return Math.min(Math.max(value, min), max);
	}
	
	public static  float[][] OneDToTwoD(float[] buffer, int col, int row)
	{
		float[][] result = new float[col][row];

		for (int i = 0; i < buffer.length; i++)
			result[i % col][ i / col] = buffer[i];

		return result;
	}

	public static  int[][] OneDToTwoD(int[] buffer, int col, int row)
	{
		int[][] result = new int[col][row];

		for (int i = 0; i < buffer.length; i++)
			result[i % col][ i / col] = buffer[i];

		return result;
	}

	public static float[] TwoDToOneD(float[][] buffer2d)
	{
		float[] result = new float[buffer2d.length * buffer2d[0].length];

		for (int i = 0; i < buffer2d.length; i++)
			for (int j = 0; j < buffer2d[i].length; j++)
				result[j * buffer2d.length + i] = buffer2d[i][j];

		return result;
	}

	public static int[] TwoDToOneD(int[][] buffer2d)
	{
		int[] result = new int[buffer2d.length * buffer2d[0].length];

		for (int i = 0; i < buffer2d.length; i++)
			for (int j = 0; j < buffer2d[i].length; j++)
				result[j * buffer2d.length + i] = buffer2d[i][j];

		return result;
	}
	
	public static int[] ToArray(List<Integer> buffer)
	{
		int[] result = new int[buffer.size()];
		
		
		for(int  i = 0; i < buffer.size(); i++)
			result[i] = buffer.get(i);
		
		return result;
	}
}