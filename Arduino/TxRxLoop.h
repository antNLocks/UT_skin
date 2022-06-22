#ifndef TXRXLOOP
#define TXRXLOOP

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

void forEachTx(void (*funcCapture)(int, int), int capture, void (*func)(int, int, void (*)(int, int)));
void forEachRx(void (*funcCapture)(int, int), int capture, void (*func)(int, int, void (*)(int, int)));
void forEachTxRx(void (*func)(int Tx_index, int Rx_index));

void forEachTx(void (*funcCapture)(int, int), int capture, void (*func)(int, int, void (*)(int, int))) {
#ifdef TX0
    func(TX0, capture, funcCapture);
#endif
#ifdef TX1
    func(TX1, capture, funcCapture);
#endif
#ifdef TX2
    func(TX2, capture, funcCapture);
#endif
#ifdef TX3
    func(TX3, capture, funcCapture);
#endif
#ifdef TX4
    func(TX4, capture, funcCapture);
#endif
#ifdef TX5
    func(TX5, capture, funcCapture);
#endif
#ifdef TX6
    func(TX6, capture, funcCapture);
#endif
#ifdef TX7
    func(TX7, capture, funcCapture);
#endif
#ifdef TX8
    func(TX8, capture, funcCapture);
#endif
#ifdef TX9
    func(TX9, capture, funcCapture);
#endif
#ifdef TX10
    func(TX10, capture, funcCapture);
#endif
#ifdef TX11
    func(TX11, capture, funcCapture);
#endif
#ifdef TX12
    func(TX12, capture, funcCapture);
#endif
#ifdef TX13
    func(TX13, capture, funcCapture);
#endif
#ifdef TX14
    func(TX14, capture, funcCapture);
#endif
#ifdef TX15
    func(TX15, capture, funcCapture);
#endif
#ifdef TX16
    func(TX16, capture, funcCapture);
#endif
#ifdef TX17
    func(TX17, capture, funcCapture);
#endif
#ifdef TX18
    func(TX18, capture, funcCapture);
#endif
#ifdef TX19
    func(TX19, capture, funcCapture);
#endif
#ifdef TX20
    func(TX20, capture, funcCapture);
#endif
}

void forEachRx(void (*funcCapture)(int, int), int capture, void (*func)(int, int, void (*)(int, int))) {
#ifdef RX0
    func(RX0, capture, funcCapture);
#endif
#ifdef RX1
    func(RX1, capture, funcCapture);
#endif
#ifdef RX2
    func(RX2, capture, funcCapture);
#endif
#ifdef RX3
    func(RX3, capture, funcCapture);
#endif
#ifdef RX4
    func(RX4, capture, funcCapture);
#endif
#ifdef RX5
    func(RX5, capture, funcCapture);
#endif
#ifdef RX6
    func(RX6, capture, funcCapture);
#endif
#ifdef RX7
    func(RX7, capture, funcCapture);
#endif
#ifdef RX8
    func(RX8, capture, funcCapture);
#endif
#ifdef RX9
    func(RX9, capture, funcCapture);
#endif
#ifdef RX10
    func(RX10, capture, funcCapture);
#endif
#ifdef RX11
    func(RX11, capture, funcCapture);
#endif
}

void forEachTxRx(void (*func)(int Tx_index, int Rx_index)) {
    forEachTx(func, 0, [](int _tx_index, int _capturedUnused, void (*_funcCaptured)(int, int)) {
        forEachRx(_funcCaptured, _tx_index, [](int __rx_index, int __tx_index, void (*__funcCaptured)(int, int)) {
            __funcCaptured(__tx_index, __rx_index);
            });
        });
}

/* Way better than :


const byte Rx_index[] = { 0, 6, 1, 7, 2, 8, 3, 4, 9, 10 };
const byte Tx_index[] = { 19, 9, 18, 8, 17, 7, 16, 6, 15, 5, 14, 4, 13, 3, 12, 2, 11, 1, 10, 0 };

void forEachTxRx(void (*func)(int Tx_index, int Rx_index)) {
    for (int i = 0; i < Tx; i++) {
        for (int j = 0; j < Rx; j++) {
            func(i, j);
        }
    }
}
*/

#endif