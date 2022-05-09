using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SkinVisualisation : MonoBehaviour, ISkinListener
{
    float[,] _buffer = new float[12,21];

    // Start is called before the first frame update
    void Start()
    {
        SkinProcessor.Instance.Register(this);

        
    }

    // Update is called once per frame
    void Update()
    {
        Texture2D texture = new Texture2D(12, 21);
        GetComponent<Renderer>().material.mainTexture = texture;

        for (int y = 0; y < texture.height; y++)
        {
            for (int x = 0; x < texture.width; x++)
            {
                Color color = new Color(_buffer[x, y], _buffer[x, y], _buffer[x, y]);
                texture.SetPixel(x, y, color);
            }
        }
        texture.Apply();
    }

    public void BufferUpdate(float[,] buffer)
    {
        _buffer = buffer;
    }
}
