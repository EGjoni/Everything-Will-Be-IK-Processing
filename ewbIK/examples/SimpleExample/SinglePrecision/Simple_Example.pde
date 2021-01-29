import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;
import math.floatV.Vec3f;
import java.util.ArrayList;
import java.io.File;
String delim = File.separator;

Armature simpleArmature;
Bone  rootBone, initialBone, 
secondBone, thirdBone, 
fourthBone, fifthBone, 
bFourthBone, bFifthBone, 
bSixthBone;

UI ui;

ArrayList<IKPin> pins = new ArrayList<IKPin>();
Axes worldAxes;

IKPin activePin; 

public void setup() {
	size(1200, 900, P3D);
	ui =new UI(false);
	worldAxes = new Axes(); 
	simpleArmature = new Armature("example");
	//attach the armature to the world axes (not necessary, just convenient)
	simpleArmature.localAxes().setParent(worldAxes);

	//specify that we want the solver to run 10 iteration whenever we call it.  
	simpleArmature.setDefaultIterations(10);
	//specify the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results) 
	simpleArmature.setDefaultDampening(10f);		
	//benchmark performance
	simpleArmature.setPerformanceMonitor(true);

	//translate everything down to where the user can see it, 
	//and rotate it 180 degrees about the z-axis so it's not upside down. 
	worldAxes.translateTo(new PVector(0, 150, 0));
	simpleArmature.localAxes().rotateAboutZ(PI, true);

	//Add some bones to the armature
	initializeBones(); 

	//Pin some of the bones.
	rootBone.enablePin();  
	fifthBone.enablePin();		
	bSixthBone.enablePin();

	//add the pins/targets to an array so we can cycle through them easily with keyboad input. 
	updatePinList();		 

	//select which pin we'll be manipulating to start with. 
	activePin = fifthBone.getIKPin();				 
}

public void draw() {		
	if(mousePressed) {
		ui.mouse.z = (float) activePin.getAxes().origin_().z;
		activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	
		simpleArmature.IKSolver(simpleArmature.getRootBone());
	} else {
		worldAxes.rotateAboutY(PI/500f, true);
	}    
	ui.drawScene(0.5f, 30f, null, simpleArmature, null, activePin, null,  false);       
}

PVector  mouse = new PVector(0,0,0);


public void mouseWheel(MouseEvent event) {
	float e = event.getCount();
	if(event.isShiftDown()) {
		activePin.getAxes().rotateAboutZ(e/TAU, true);
	}else if (event.isControlDown()) {
		activePin.getAxes().rotateAboutX(e/TAU, true);
	}  else {
		activePin.getAxes().rotateAboutY(e/TAU, true);
	}
	activePin.solveIKForThisAndChildren();    
}

public void keyPressed() {
	if (key == CODED) {
		if (keyCode == DOWN) {	      
			int currentPinIndex =(pins.indexOf(activePin) + 1) % pins.size();
			activePin  = pins.get(currentPinIndex);				
		} else if (keyCode == UP) {
			int idx = pins.indexOf(activePin);
			int currentPinIndex =  (pins.size()-1) -(((pins.size()-1) - (idx - 1)) % pins.size());
			activePin  = pins.get(currentPinIndex);
		} 
	}
}

public void initializeBones() {		
	rootBone = simpleArmature.getRootBone();		
	rootBone.localAxes().markDirty();
	rootBone.localAxes().updateGlobal();
	initialBone = new Bone(rootBone, "initial", 74f);
	secondBone = new Bone(initialBone, "nextBone", 86f);
	thirdBone = new Bone(secondBone, "anotherBone", 98f); 
	fourthBone = new Bone(thirdBone, "oneMoreBone", 70f);
	fifthBone = new Bone(fourthBone, "fifthBone", 80f);  

	bFourthBone = new Bone(thirdBone, "branchBone", 80f);
	bFifthBone = new Bone(bFourthBone, "nextBranch", 70f);
	bSixthBone = new Bone(bFifthBone, "leaf", 80f); 

	secondBone.rotAboutFrameZ(.4f);
	thirdBone.rotAboutFrameZ(.4f);

	bFourthBone.rotAboutFrameZ(-.5f);
	bFifthBone.rotAboutFrameZ(-1f);
	bSixthBone.rotAboutFrameZ(-.2f);
	initialBone.rotAboutFrameX(.01f);	
}

public void updatePinList() {
	pins.clear();
	recursivelyAddToPinnedList(pins, simpleArmature.getRootBone());
}

public void recursivelyAddToPinnedList(ArrayList<IKPin> pins, Bone descendedFrom) {
	ArrayList<Bone> pinnedChildren = (ArrayList<Bone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
	for(Bone b : pinnedChildren) {
		pins.add(b.getIKPin());
	}
	for(Bone b : pinnedChildren) {
		ArrayList<Bone> children = b.getChildren(); 
		for(Bone b2 : children) {
			recursivelyAddToPinnedList(pins, b2);
		}
	}
}
