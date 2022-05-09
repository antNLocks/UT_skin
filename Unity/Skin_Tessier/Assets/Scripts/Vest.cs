using Bhaptics.Tact;
using Bhaptics.Tact.Unity;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Vest : MonoBehaviour, ISkinListener
{
    private IHaptic _player;

    // Start is called before the first frame update
    void Start()
    {
        _player = BhapticsManager.GetHaptic();
        SkinProcessor.Instance.Register(this);   }

    // Update is called once per frame
    void Update()
    {

    }

    public void BufferUpdate(float[,] buffer)
    {
        List<DotPoint> points = new List<DotPoint>();

        int nbMotorH = 4;
        int nbMotorV = 4;

        float[,] averageBuffer = SpatialAverage(buffer, nbMotorV, nbMotorH);

        for (int i = 0; i < nbMotorV; i++)
        {
            for (int j = 0; j < nbMotorH; j++)
                points.Add(new DotPoint(i * nbMotorH + j, (int)averageBuffer[i, j]));
        }

        _player.Submit("_", PositionType.VestBack, points, 100);

    }

    private float[,] SpatialAverage(float[,] buffer, int nbMotorV, int nbMotorH)
    {
        float[,] averageBufferH = new float[buffer.GetLength(0), nbMotorH];

        for (int i = 0; i < buffer.GetLength(0); i++) //Horizontal
        {
            for (int j = 0; j < nbMotorH; j++)
            {
                float sum = 0;
                int dotByMotorH = buffer.GetLength(1) / nbMotorH;
                for (int k = 0; k < dotByMotorH; k++)
                    sum += buffer[i, dotByMotorH * j + k];

                averageBufferH[i, j] = sum / dotByMotorH;
            }
        }

        float[,] averageBuffer = new float[nbMotorV, nbMotorH];

        for (int j = 0; j < nbMotorH; j++) //Vertical
        {
            for (int i = 0; i < nbMotorV; i++)
            {
                float sum = 0;
                int dotByMotorV = buffer.GetLength(0) / nbMotorV;
                for (int k = 0; k < dotByMotorV; k++)
                    sum += averageBufferH[i * dotByMotorV + k, j];

                averageBuffer[i, j] = sum / dotByMotorV;
            }
        }

        return averageBuffer;
    }
}
