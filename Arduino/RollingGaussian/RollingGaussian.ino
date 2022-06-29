byte gaussianBuffer[1008];

void setup() {
  Serial.begin(250000);

  computeGaussian(3);
}


void loop() {

  serialGaussian(6, 10);
  delay(20);

  for (int i = 0; i < 21; i++) {
    for (int j = 0; j < 12; j++) {
      Serial.write(0x01);
    }
  }
  Serial.write(0x00);
  Serial.flush();
  delay(2000);

}
void computeGaussian(float sigma) {
  for (int i = 0; i < 42; i++) {
    for (int j = 0; j < 24; j++) {
      int d = 255 * exp(-((12 - j) * (12 - j) + (i - 21) * (i - 21)) / (2 * sigma * sigma));
      byte b = constrain(d, 1, 255);
      gaussianBuffer[42 * j + i] = b;
    }
  }
}

void serialGaussian(int x, int y) {
  for (int i = 0; i < 21; i++) {
    for (int j = 0; j < 12; j++) {
      Serial.write(gaussianBuffer[42 * (j + x) + i + y]);
    }
  }
  Serial.write(0x00);
  Serial.flush();
}

