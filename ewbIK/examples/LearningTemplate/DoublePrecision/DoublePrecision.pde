import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.Vec3d;

dArmature simpleArmature;
UI ui;

public void setup() {
	size(1200, 900, P3D);
  ui =new UI(false);
  simpleArmature = new dArmature("example");
  setupVisualizationParams(simpleArmature);
  //USER CODE GOES BELOW THIS LINE: 
	 
}

public void draw() {    
  if(mousePressed) {
    activePin.translateTo(new DVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));  
    simpleArmature.IKSolver(simpleArmature.getRootBone());
  } else {
    worldAxes.rotateAboutY(PI/500f, true);
  }    
  ui.drawScene(0.5f, 30f, null, simpleArmature, null, activePin, null,  false);       
}

public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(dArmature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}

ArrayList<dIKPin> pins = new ArrayList<dIKPin>();
dIKPin activePin;
dAxes worldAxes;
