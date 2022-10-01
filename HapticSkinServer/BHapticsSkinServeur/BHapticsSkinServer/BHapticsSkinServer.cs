using System;
using System.Net;
using System.Net.Sockets;
using Bhaptics.Tact;
using System.Collections.Generic;

namespace BHapticsSkinServer
{
    class BHapticsSkinServer
    {
        private static IHapticPlayer _player;
        private static byte[] _motorsMapping = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
        private static byte _durationFrame = 30;
        private static bool _isReceivingMotorMapping = false;


        [Obsolete]
        public static int Main(String[] args)
        {
            _player = new HapticPlayer("BHapticsSkinServerID", "BHapticsSkinServer");

            StartListening();

            return 0;
        }


        public static void StartListening()
        {
            byte[] bytes = new Byte[1024]; // Data buffer for incoming data.  

            IPEndPoint localEndPoint = new IPEndPoint(IPAddress.Any, 51470);
            Socket listener = new Socket(IPAddress.Any.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            try
            {
                listener.Bind(localEndPoint);
                listener.Listen(10); // Listen for incoming connections

                Console.WriteLine("Waiting for a connection on port 51470 ...");

                while (true)
                {
                    Socket handler = listener.Accept(); // Waiting for an incoming connection
                    Console.WriteLine();
                    Console.WriteLine("Client application connected");

                    List<byte> buffer = new List<byte>();
                    try
                    {
                        while (!(handler.Poll(1, SelectMode.SelectRead) && handler.Available == 0))
                        {
                            int bytesRec = handler.Receive(bytes);
                            for (int i = 0; i < bytesRec; i++)
                            {
                                if (bytes[i] == 0xFF)
                                {
                                    if (!_isReceivingMotorMapping)
                                    {
                                        if (buffer.Count == _motorsMapping.Length)
                                            BufferUpdate(buffer);

                                        if (buffer.Count == 0)
                                            _isReceivingMotorMapping = true;
                                    }
                                    else if (buffer.Count == 0)
                                        _isReceivingMotorMapping = true;
                                    else
                                        ParseMotorsMapping(buffer);

                                    buffer = new List<byte>();
                                }
                                else
                                    buffer.Add(bytes[i]);
                            }
                        }
                        Console.WriteLine("Client application disconnected");
                    } catch(Exception e) { Console.WriteLine(e.ToString()); }
                }
            }
            catch (Exception e) { Console.WriteLine(e.ToString()); }
        }

        public static void BufferUpdate(List<byte> buffer)
        {

            List<DotPoint> points = new List<DotPoint>();

            for(int i = 0; i < buffer.Count; i++)
                points.Add(new DotPoint(_motorsMapping[i], (int)(buffer[i] / 2.54f)));

            _player.Submit("_", PositionType.VestBack, points, _durationFrame);
        }

        public static void ParseMotorsMapping(List<byte> buffer)
        {
            _durationFrame = buffer[0];
            buffer.RemoveAt(0);
            _motorsMapping = buffer.ToArray();
            Console.Write("Duration of a frame : {0} ms\nNew motors mapping : ", _durationFrame);
            foreach(var b in _motorsMapping)
            {
                Console.Write("{0}, ", b);
            }
            Console.WriteLine();
            _isReceivingMotorMapping = false;
        }
    }
}