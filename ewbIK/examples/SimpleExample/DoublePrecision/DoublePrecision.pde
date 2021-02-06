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
  
  //Add some bones to the armature
  dBone rootBone = simpleArmature.getRootBone();    
  dBone initialBone = new dBone(rootBone, "initial", 74f);
  dBone secondBone = new dBone(initialBone, "nextBone", 86f);
  dBone thirdBone = new dBone(secondBone, "anotherBone", 98f); 
  dBone fourthBone = new dBone(thirdBone, "oneMoreBone", 70f);
  dBone fifthBone = new dBone(fourthBone, "fifthBone", 80f); 
  dBone bFourthBone = new dBone(thirdBone, "branchBone", 80f);
  dBone bFifthBone = new dBone(bFourthBone, "nextBranch", 70f);
  dBone bSixthBone = new dBone(bFifthBone, "leaf", 80f); 

  //Pre-rotate the bones into some interesting positions
  secondBone.rotAboutFrameZ(.4f);
  thirdBone.rotAboutFrameZ(.4f);
  bFourthBone.rotAboutFrameZ(-.5f);
  bFifthBone.rotAboutFrameZ(-1f);
  bSixthBone.rotAboutFrameZ(-.2f);
  initialBone.rotAboutFrameX(.01f);  

  //Pin some of the bones.
  rootBone.enablePin();  
  fifthBone.enablePin();    
  bSixthBone.enablePin();

  //add the pins/targets to an array so we can cycle through them easily with keyboad input. 
  ui.updatePinList(simpleArmature);  
  
  simpleArmature.setDefaultIterations(10); //tell the solver to run 10 iteration whenever we call it.   
  simpleArmature.setDefaultDampening(10f); //set the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results)    
  simpleArmature.setPerformanceMonitor(true); //benchmark performance			 
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
