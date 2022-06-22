#define RX0 0
#define RX1 6
#define RX2 1
#define RX3 7
#define RX4 2
#define RX5 8
#define RX6 3
#define RX7 4
#define RX8 9
#define RX9 10

#define TX0 19
#define TX1 9
#define TX2 18
#define TX3 8
#define TX4 17
#define TX5 7
#define TX6 16
#define TX7 6
#define TX8 15
#define TX9 5
#define TX10 14
#define TX11 4
#define TX12 13
#define TX13 3
#define TX14 12
#define TX15 2
#define TX16 11
#define TX17 1
#define TX18 10
#define TX19 0





void forEachRx(int capture, void (*func)(int, int)) {
#ifdef RX0
    func(RX0, capture);
#endif
#ifdef RX1
    func(RX1, capture);
#endif
#ifdef RX2
    func(RX2, capture);
#endif
#ifdef RX3
    func(RX3, capture);
#endif
#ifdef RX4
    func(RX4, capture);
#endif
#ifdef RX5
    func(RX5, capture);
#endif
#ifdef RX6
    func(RX6, capture);
#endif
#ifdef RX7
    func(RX7, capture);
#endif
#ifdef RX8
    func(RX8, capture);
#endif
#ifdef RX9
    func(RX9, capture);
#endif
#ifdef RX10
    func(RX10, capture);
#endif
#ifdef RX11
    func(RX11, capture);
#endif
}

void forEachTx(int capture, void (*func)(int, int)) {
#ifdef TX0
    func(TX0, capture);
#endif
#ifdef TX1
    func(TX1, capture);
#endif
#ifdef TX2
    func(TX2, capture);
#endif
#ifdef TX3
    func(TX3, capture);
#endif
#ifdef TX4
    func(TX4, capture);
#endif
#ifdef TX5
    func(TX5, capture);
#endif
#ifdef TX6
    func(TX6, capture);
#endif
#ifdef TX7
    func(TX7, capture);
#endif
#ifdef TX8
    func(TX8, capture);
#endif
#ifdef TX9
    func(TX9, capture);
#endif
#ifdef TX10
    func(TX10, capture);
#endif
#ifdef TX11
    func(TX11, capture);
#endif
#ifdef TX12
    func(TX12, capture);
#endif
#ifdef TX13
    func(TX13, capture);
#endif
#ifdef TX14
    func(TX14, capture);
#endif
#ifdef TX15
    func(TX15, capture);
#endif
#ifdef TX16
    func(TX16, capture);
#endif
#ifdef TX17
    func(TX17, capture);
#endif
#ifdef TX18
    func(TX18, capture);
#endif
#ifdef TX19
    func(TX19, capture);
#endif
#ifdef TX20
    func(TX20, capture);
#endif
}

/*const byte Rx_index[] = {0, 6, 1, 7, 2, 8, 3, 4, 9, 10};
const byte Tx_index[] = {19, 9, 18, 8, 17, 7, 16, 6, 15, 5, 14, 4, 13, 3, 12, 2, 11, 1, 10, 0};

void setup() {
  Serial.begin(250000);

  for(int i = 0; i < 20; i++){
    char buffer[100];
    sprintf(buffer, "#define TX%d %d", i, Tx_index[i]);
    Serial.println(buffer);
  }

  for(int i = 0; i < 11; i++){
     char buffer[100];
    sprintf(buffer, "#ifdef RX%d\n\tfunc(RX%d, capture);\n#endif", i, i);
    Serial.println(buffer);
  }
}

void loop(){}*/

