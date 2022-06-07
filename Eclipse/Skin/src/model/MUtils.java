package model;

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

	
	public static float[] TwoDToOneD(float[][] buffer2d)  
	{
		float[] result = new float[buffer2d.length * buffer2d[0].length];

		for (int i = 0; i < buffer2d.length; i++)
			for (int j = 0; j < buffer2d[i].length; j++)
				result[j * buffer2d.length + i] = buffer2d[i][j];

		return result;
	}

	
	public static float[] ToArray(List<Float> buffer)
	{
		float[] result = new float[buffer.size()];
		
		
		for(int  i = 0; i < buffer.size(); i++)
			result[i] = buffer.get(i);
		
		return result;
	}
}