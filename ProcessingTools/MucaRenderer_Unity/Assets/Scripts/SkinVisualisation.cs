using UnityEngine;

/*
* This script is a subscriber to SkinProcessor. 
* When it receives processed data, it updates its texture to obtain a visualization of the data.
*/
public class SkinVisualisation : MonoBehaviour, ISkinListener
{
    float[,] _buffer;
    Texture2D texture;


    // Start is called before the first frame update
    void Start()
    {
        SkinProcessor.Instance.Register(this);
        _buffer = new float[SkinProcessor.Instance.ProcessedBufferCol, SkinProcessor.Instance.ProcessedBufferRow];
        texture = new Texture2D(SkinProcessor.Instance.ProcessedBufferCol, SkinProcessor.Instance.ProcessedBufferRow);
        GetComponent<Renderer>().material.mainTexture = texture;
    }

    // Update is called once per frame
    void Update()
    {
        for (int y = 0; y < texture.height; y++)
        {
            for (int x = 0; x < texture.width; x++)
            {
                float v = _buffer[x, y] / 256f;
                Color color = new Color(v, v, v);
                texture.SetPixel(texture.width -  x, y, color);
            }
        }

        texture.Apply();
    }

    // Called by SkinProcessor.Instance
    public void BufferUpdate()
    {
        _buffer = SkinProcessor.Instance.ProcessedBuffer2d;
    }
}
