using Bhaptics.Tact;
using Bhaptics.Tact.Unity;
using System.Collections.Generic;
using UnityEngine;

public class Vest : MonoBehaviour, ISkinListener
{
    private IHaptic _player;
    private Motors _topMotors;
    private readonly int _nbMotorH = 4;
    private readonly int _nbMotorV = 2;

    public bool gaussianConvolution = true;
    [Range(0, 1000)] public float rayGaussian = 60;
    [Range(0, 1000)] public float rayUniformAverage = 60;

    public bool recomputeBufffer = false;


    // Start is called before the first frame update
    void Start()
    {
        _player = BhapticsManager.GetHaptic();
        SkinProcessor.Instance.Register(this);

        _topMotors = new Motors(SkinProcessor.Instance.ProcessedBufferCol, SkinProcessor.Instance.ProcessedBufferRow, 2, 4);
    }

    // Update is called once per frame
    int counter = 0;
    void Update()
    {
        if (counter++ > 20 && recomputeBufffer)
        {
            _topMotors.RayGaussian = rayGaussian;
            _topMotors.RayUniformAverage = rayUniformAverage;
            _topMotors.ComputeGaussianConvolBuffers();
            _topMotors.ComputeUniformAverageConvolBuffers();
            counter = 0;
        }

    }

    public void BufferUpdate()
    {
        _topMotors.InputBuffer = SkinProcessor.Instance.ProcessedBuffer;

        if(gaussianConvolution)
            _topMotors.CalculateGaussianOutput();
        else
            _topMotors.CalculateUniformAverageOutput();

        float[,] motorIntensityBuffer = MUtils.OneDToTwoD(_topMotors.OutputBuffer, _nbMotorV, _nbMotorH);

        List<DotPoint> points = new List<DotPoint>();

        for (int i = 0; i < _nbMotorV; i++)
            for (int j = 0; j < _nbMotorH; j++)
                points.Add(new DotPoint((_nbMotorV - 1 - i) * _nbMotorH + j, (int)(motorIntensityBuffer[i, j]/256f*100f)));
        

        _player.Submit("_", PositionType.VestBack, points, 100);

    }

  
}
