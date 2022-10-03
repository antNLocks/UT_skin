
import controlP5.*;

ControlP5 cp5;
Accordion accordion;

RadioButton rCon, rAve, rBin;

void InterfaceSetup() {
  cp5 = new ControlP5(this);
  
  Group g0 = cp5.addGroup("Serial Communication")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(70)
    ;
  
   rCon = cp5.addRadioButton("Serial Port") // INTER_NEAREST // 1 INTER_LINEAR  // 2 INTER_CUBIC  3 // INTER_AREA  4 // INTER_LANCZOS4
    .setPosition(10, 15)
    .setLabel("lol")
    .setSize(10, 10)
    .setColorForeground(color(120))
    .setColorLabel(color(255))
    .setItemsPerRow(1)
    .setSpacingColumn(50)
    .moveTo(g0)
    ;
    
    for(int i = 0; i < Serial.list().length; i++){
      if(!Serial.list()[i].startsWith("/dev/ttyS"))
        rCon.addItem(Serial.list()[i], i);
    }
    
    rCon.activate(SERIAL_PORT);

  for (Toggle t : rCon.getItems()) {
    t.getCaptionLabel().setColorBackground(color(255, 0));
    t.getCaptionLabel().getStyle().moveMargin(-7, 0, 0, -3);
    t.getCaptionLabel().getStyle().movePadding(7, 0, 0, 3);
    t.getCaptionLabel().getStyle().backgroundWidth = 40;
    t.getCaptionLabel().getStyle().backgroundHeight = 13;
  }
  
  
 cp5.addButton("connect")
    .setPosition(100, 15)
    .setSize(50, 20)
    .moveTo(g0)
    ;

  ////////////////////////////////
  //      DEFAULT SETTINGS
  ///////////////////////////////
  Group g1 = cp5.addGroup("Value Range")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(80)
    ;

  cp5.addButton("launch_calibration")
    .setPosition(10, 10)
    .setSize(100, 20)
    .moveTo(g1)
    ;


  cp5.addSlider("min_threshold")
    .setPosition(10, 40)
    .setRange(0, 254)
    .moveTo(g1)
    ;

  cp5.addSlider("max_threshold")
    .setPosition(10, 55)
    .setRange(0, 254)
    .moveTo(g1)
    ;


  Group g2 = cp5.addGroup("Noise reduction")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(90)
    ;

  cp5.addTextlabel("Average algorithme :")
    .setText("Average algorithme :")        
    .setPosition(10, 10)
    .moveTo(g2);
  ;

  rAve = cp5.addRadioButton("AverageAlgo") 
    .setPosition(10, 25)
    .setLabel("lol2")
    .setSize(10, 10)
    .setColorForeground(color(120))
    .setColorLabel(color(255))
    .setItemsPerRow(2)
    .setSpacingColumn(100)
    .addItem("rolling_average", 0)
    .addItem("interpolation_previous_frames", 1)
    .activate(averageAlgo)
    .moveTo(g2)
    ;

  cp5.addSlider("frames_for_average")
    .setPosition(10, 55)
    .setRange(1, 10)
    .moveTo(g2)
    ;

  cp5.addSlider("interpolation_factor")
    .setPosition(10, 70)
    .setRange(0, 1)
    .moveTo(g2)
    ;


  Group g3 = cp5.addGroup("Binary")
    .setBackgroundColor(color(64, 64, 64))
    .setBackgroundHeight(60);
  ;

  rBin = cp5.addRadioButton("BinaryRadioButton")
    .setPosition(10, 10)
    .setSize(10, 10)
    .addItem("enable_binary", 1)
    .moveTo(g3)
    ;

  cp5.addSlider("binary_threshold")
    .setPosition(10, 30)
    .setRange(0, 254)
    .moveTo(g3)
    ;


  ////////////////////////////////
  //      ACCORDION
  ///////////////////////////////

  accordion = cp5.addAccordion("acc")
    .setPosition((int) (2.0*finalSize.width + 90), 30)
    .setWidth(280)
    .addItem(g0)
    .addItem(g1)
    .addItem(g2)
    .addItem(g3)
    ;

  accordion.open(0, 1, 2, 3);
  accordion.setCollapseMode(Accordion.MULTI);
}

void connect(){
  skinPort = new Serial( this, Serial.list( )[ SERIAL_PORT ], SERIAL_RATE );
}

void launch_calibration() {
  String t= "c\n";
  println("Sending: " + t);
  skinPort.write(t);
}


void controlEvent(ControlEvent theEvent) {
  if (theEvent.isFrom(rCon)) 
    if (int(theEvent.getGroup().getValue()) != -1)
      SERIAL_PORT = int(theEvent.getGroup().getValue());
  
  if (theEvent.isFrom(rAve)) 
    if (int(theEvent.getGroup().getValue()) != -1)
      averageAlgo = int(theEvent.getGroup().getValue());

  if (theEvent.isFrom(rBin)) 
    binary = int(theEvent.getGroup().getValue()) != -1;
}
