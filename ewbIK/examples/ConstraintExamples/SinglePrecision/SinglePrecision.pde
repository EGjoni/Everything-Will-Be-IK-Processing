import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;

Armature simpleArmature;
UI ui;
//decrease the numerator to increase the zoom. 
float zoomScalar = 300f/height;

public void setup() {
	size(1200, 900, P3D);
  ui =new UI(true);
  simpleArmature = new Armature("example");
  setupVisualizationParams(simpleArmature);		
	
  Bone rootBone = simpleArmature.getRootBone(); rootBone.setBoneHeight(20f);  
  Bone initialBone = new Bone(rootBone, "initial", 74f); initialBone.rotAboutFrameX(.01f);
  Bone secondBone = new Bone(initialBone, "secondBone", 86f);
  Bone thirdBone = new Bone(secondBone, "thirdBone", 98f);  
  
  //ADD CONSTRAINTS TO OUR BONES  
  Kusudama firstConstraint = new Kusudama(initialBone); //constrain "initialBone"
  //add a reach cone for our constraint pointing straight up and a bit toward the X direction
  //with a spread of 0.5 radians
  firstConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 0.5f);
  //connect it to another reach cone pointing straight up and a bit toward the negative X direction
  //with a spread of 0.7 radians
  firstConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 0.7f);
  //allow our bone to twist only between 0.01 radians and 0.03 radians
  firstConstraint.setAxialLimits(0.01f,0.03f);

  Kusudama secondConstraint = new Kusudama(secondBone);
  secondConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),0.6f);
  secondConstraint.addLimitConeAtIndex(1, new PVector(-1f, 1f, 0f), 0.2f);
  secondConstraint.setAxialLimits(0.1f,0.9f);

  Kusudama thirdConstraint = new Kusudama(thirdBone);
  thirdConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 0.8f);
  thirdConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 0.8f);
  thirdConstraint.setAxialLimits(0.1f,0.3f);
  
  //pin some bones
  rootBone.enablePin();       
  thirdBone.enablePin();
  
  //determine how much precedence each of this pin's axes get 
  //in relation to other axes on other pins being considered by the solver.
  //this line state that the solver should care about this bone's X and Y headings 
  //aligning with its targets about 5 times as much as it cares about the X and Y headings of any other bones.  
  //it also tells the solver to ignore the z heading entirely (usually, only two of three headings need be specified). 
  thirdBone.getIKPin().setTargetPriorities(5f, 5f,0f);	
	
	ui.updatePinList(simpleArmature);
	
	//Tell the Bone class that all bones should draw their kusudamas.
	Bone.setDrawKusudamas(true);
  //specify that we want the solver to run 20 iteration whenever we call it.  
  simpleArmature.setDefaultIterations(20);
  //specify the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results) 
  simpleArmature.setDefaultDampening(0.03f);
  //specify that the armature should avoid wobbly solutions. 
  simpleArmature.setDefaultStabilizingPassCount(1);
  //benchmark performance
  simpleArmature.setPerformanceMonitor(true);
}


public void draw() {		
	if(mousePressed) {
		//Set the selected pin to the position of the mouse if the user is dragging it.
		activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y,	activePin.getLocation_().z));        
		//run the IK solver on the armature.
		simpleArmature.IKSolver(simpleArmature.getRootBone());		
	}else {
		//rotate the world so the user can inspect the pose
		worldAxes.rotateAboutY(PI/500f, true);
	} 
	zoomScalar = 350f/height;
	ui.drawScene(zoomScalar, 20f, null, simpleArmature, null, activePin, null, false);
}

public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(Armature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}

ArrayList<IKPin> pins = new ArrayList<IKPin>();
IKPin activePin;
Axes worldAxes;
