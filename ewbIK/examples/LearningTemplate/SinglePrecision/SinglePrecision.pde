import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import java.util.ArrayList;

Axes worldAxes;
Armature simpleArmature;
UI ui;

public void setup() {
	size(1200, 900, P3D);  
	simpleArmature = new Armature("example");
  setupVisualizationParams(simpleArmature);
  //USER CODE GOES BELOW THIS LINE: 

  Bone rootBone = simpleArmature.getRootBone();
	Bone initialBone = new Bone(rootBone, "initial", 74f);
  Bone secondBone = new Bone(initialBone, "nextBone", 76f);
  Bone thirdBone = new Bone(secondBone, "anotherBone", 65f); 
  Bone fourthBone = new Bone(thirdBone, "oneMoreBone", 70f);
  Bone fifthBone = new Bone(fourthBone, "fifthBone", 60f);	
  
  rootBone.enablePin();
  fifthBone.enablePin();

  ui.updatePinList(simpleArmature);
  simpleArmature.setPerformanceMonitor(true);
  IKPin fithBonePin = fifthBone.getIKPin();
  fithBonePin.translateTo(new PVector(200, 100, 0));
  fithBonePin.rotateAboutZ(PI/2f);
  simpleArmature.IKSolver(rootBone);
  
}

public void draw() {		
	if(mousePressed) {		
		activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	

    //THIS FUNCTION TELLS THE ARMATURE RUN THE IK SOLVER
		simpleArmature.IKSolver(simpleArmature.getRootBone());
	} else {
		worldAxes.rotateAboutY(PI/500f, true);
	}    
	ui.drawScene(0.5f, 30f, null, simpleArmature, null, activePin, null,  false);       
}

public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(Armature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}

ArrayList<IKPin> pins = new ArrayList<IKPin>();
IKPin activePin;
