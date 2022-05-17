public static class MUtils
{

    public static T[][] CreateArray<T>(int col, int row)
    {
        T[][] result = new T[col][];

        for (int i = 0; i < col; i++)
            result[i] = new T[row];

        return result;
    }

    public static float Map(float value, float fromLow, float fromHigh, float toLow, float toHigh)
    {
        return (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow) + toLow;
    }

    public static T[,] OneDToTwoD<T>(T[] buffer, int col, int row)
    {
        T[,] result = new T[col, row];

        for (int i = 0; i < buffer.Length; i++)
            result[i % col, i / col] = buffer[i];

        return result;
    }

    public static T[] TwoDToOneD<T>(T[][] buffer2d)
    {
        T[] result = new T[buffer2d.Length * buffer2d[0].Length];

        for (int i = 0; i < buffer2d.Length; i++)
            for (int j = 0; j < buffer2d[i].Length; j++)
                result[j * buffer2d.Length + i] = buffer2d[i][j];

        return result;
    }
}

public interface ISkinListener
{
    public void BufferUpdate();
}
