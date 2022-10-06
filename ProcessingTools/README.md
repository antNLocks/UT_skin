# Processing tools

This folder contains the source code for several implementations of applications that communicate with *MucaReader* via UART.

They are presented from the most rudimentary to the most complex.

## MucaRenderer_Processing

This is a Processing3 sketch which receives the data transmitted by *MucaReader* and displays it as it is. In addition, this sketch offers a second visualisation. It corresponds to the raw image resized and processed by filtering algorithms (thresholding and averaging).
To work, `MucaRenderer_Processing` needs the **ControlP5** and **OpenCV for Processing** libraries.

### Issues

This program doesn't work on Ubuntu even if it works well on Windows.

## MucaRenderer_Unity

It is a unity project that receives the data transmitted by *MucaReader* and processes it. It integrates the **Unity SDK of BHaptics** and can therefore control the motors of the vest. The intensity of the motors of the BHaptics vest is calculated by a specific script.
This project is mainly about its 6 scripts.
For more details on how the scripts work, read the comments associated with the scripts.
This project uses **Unity version 2020.3.7f1**.

## SkinAnalyser

It is a Java project which proposes an encapsulation of all the parameters of the algorithms. *SkinAnalyser* is primarily composed of a graphical system for managing and modifying algorithms parameters. It is then possible to visualise the data read from a *MucaReader* filtered with the specific parameters and also to send motors integrates to a *HapticSkinServer*.

This project uses *Oracle jdk 8.202* and not openjdk-8.

For more details, please read **`../Report.pdf`**.

### Build, deploy and run

Apache ANT 1.7 or higher is needed to build with the `build.xml` script.

Build and deploy (with ant) : 

```console
ant
```

Run (with Oracle jdk 8.202):

```console
java -jar SkinAnalyser.jar
```
