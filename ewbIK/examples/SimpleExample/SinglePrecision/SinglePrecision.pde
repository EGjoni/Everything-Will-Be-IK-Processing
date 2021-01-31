import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;

Armature simpleArmature;
UI ui;

public void setup() {
	size(1200, 900, P3D);
	ui =new UI(false);
	simpleArmature = new Armature("example");
  setupVisualizationParams(simpleArmature);
  
	//Add some bones to the armature
	Bone rootBone = simpleArmature.getRootBone();    
  Bone initialBone = new Bone(rootBone, "initial", 74f);
  Bone secondBone = new Bone(initialBone, "nextBone", 86f);
  Bone thirdBone = new Bone(secondBone, "anotherBone", 98f); 
  Bone fourthBone = new Bone(thirdBone, "oneMoreBone", 70f);
  Bone fifthBone = new Bone(fourthBone, "fifthBone", 80f); 
  Bone bFourthBone = new Bone(thirdBone, "branchBone", 80f);
  Bone bFifthBone = new Bone(bFourthBone, "nextBranch", 70f);
  Bone bSixthBone = new Bone(bFifthBone, "leaf", 80f); 

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
		activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	
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
Axes worldAxes;
