import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.Vec3d;
import java.util.ArrayList;
import java.io.File;
String delim = File.separator;

dArmature simpleArmature;
dBone  rootBone, initialBone, 
secondBone, thirdBone, 
fourthBone, fifthBone, 
bFourthBone, bFifthBone, 
bSixthBone;

UI ui;

ArrayList<dIKPin> pins = new ArrayList();
dAxes worldAxes;

dIKPin activePin; 

public void setup() {
	size(1200, 900, P3D);
	ui =new UI(false);
	worldAxes = new dAxes(); 
	simpleArmature = new dArmature("example");
	//attach the armature to the world axes (not necessary, just convenient)
	simpleArmature.localAxes().setParent(worldAxes);
	
	//specify that we want the solver to run 10 iteration whenever we call it.  
	simpleArmature.setDefaultIterations(10);
	//specify the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results) 
	simpleArmature.setDefaultDampening(0.05d);		
	
	//benchmark performance
	simpleArmature.setPerformanceMonitor(true);
	
	//translate everything down to where the user can see it, 
	//and rotate it 180 degrees about the z-axis so it's not upside down. 
	worldAxes.translateTo(new DVector(0, 150, 0));
	simpleArmature.localAxes().rotateAboutZ(PI, true);
	
	//Add some bones to the armature
	initializeBones(); 
	
	setBoneConstraints();
	
	//Pin some of the bones.
	rootBone.enablePin();  
	fifthBone.enablePin();		
	bSixthBone.enablePin();
					
	//add the pins/targets to an array so we can cycle through them easily with keyboad input. 
	 updatePinList();		 		 
	 //select which pin we'll be manipulating to start with. 
	 activePin = bSixthBone.getIKPin();		
	 
	//Tell the dBone class that all bones should draw their kusudamas.
	dBone.setDrawKusudamas(true);	
	
	//specify that the armature should avoid degenerate results when facing impossible 
	//situations. This comes at the 
	//cost of some performance (and temporal continuity in extreme poses 
	//[temporal discontinuity may be mitigated with a smaller dampening parameter]).
	simpleArmature.setDefaultStabilizingPassCount(1);
			
}

public void draw() {		
	if(mousePressed) {
        ui.mouse.z = (float) activePin.getAxes().origin_().z;
        activePin.translateTo(new DVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	
        simpleArmature.IKSolver(simpleArmature.getRootBone());
    } else {
        worldAxes.rotateAboutY(PI/500d, true);
    }    
   ui.drawScene(0.5f, 10f, null, simpleArmature, null, activePin, null,  false);       
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
	initialBone = new dBone(rootBone, "initial", 74d);
	secondBone = new dBone(initialBone, "nextBone", 86d);
	thirdBone = new dBone(secondBone, "anotherBone", 98d); 
	fourthBone = new dBone(thirdBone, "oneMoreBone", 70d);
	fifthBone = new dBone(fourthBone, "fifthBone", 80d);  

	bFourthBone = new dBone(thirdBone, "branchBone", 80d);
	bFifthBone = new dBone(bFourthBone, "nextBranch", 70d);
	bSixthBone = new dBone(bFifthBone, "leaf", 80d); 

	secondBone.rotAboutFrameZ(.4d);
	thirdBone.rotAboutFrameZ(.4d);

	bFourthBone.rotAboutFrameZ(-.5d);
	bFifthBone.rotAboutFrameZ(-1d);
	bSixthBone.rotAboutFrameZ(-.2d);
	initialBone.rotAboutFrameX(.01d);	
}

public void setBoneConstraints() {    

	dKusudama firstConstraint = new dKusudama(initialBone);
	firstConstraint.addLimitConeAtIndex(0, new DVector(.5d, 1d, 0d), 1d);
	firstConstraint.addLimitConeAtIndex(1, new DVector(-.5d, 1d, 0d), 1d);
	firstConstraint.setAxialLimits(0.1d,0.3d);
	firstConstraint.enable();
	initialBone.addConstraint(firstConstraint);

	dKusudama secondConstraint = new dKusudama(secondBone);
	secondConstraint.addLimitConeAtIndex(0, new DVector(.5d, 1d, 0d),1d);
	secondConstraint.addLimitConeAtIndex(1, new DVector(-.5d, 1d, 0d), 1d);
	secondConstraint.setAxialLimits(0.1d,0.3d);
	secondConstraint.enable();
	secondBone.addConstraint(secondConstraint);

	dKusudama thirdConstraint = new dKusudama(thirdBone);
	thirdConstraint.addLimitConeAtIndex(0, new DVector(.5d, 1d, 0d), 1d);
	thirdConstraint.addLimitConeAtIndex(1, new DVector(-.5d, 1d, 0d), 1d);
	thirdConstraint.setAxialLimits(0.1d,0.3d);
	thirdConstraint.enable();
	thirdBone.addConstraint(thirdConstraint);

	dKusudama fourthConstraint = new dKusudama(fourthBone);
	fourthConstraint.addLimitConeAtIndex(0, new DVector(.5d, 1d, 0d), 1d);
	fourthConstraint.addLimitConeAtIndex(1, new DVector(-.5d, 1d, 0d), 1d);
	fourthConstraint.setAxialLimits(0.1d,0.3d);
	fourthConstraint.enable();
	fourthBone.addConstraint(fourthConstraint);

	dKusudama fifthConstraint = new dKusudama(fifthBone);
	fifthConstraint.addLimitConeAtIndex(0, new DVector(.5d, 1d, 0d),0.5d);
	fifthConstraint.addLimitConeAtIndex(1, new DVector(-.5d, 1d, 0d), 1d);
	fifthConstraint.setAxialLimits(0.1d, 0.3d);
	fifthConstraint.enable();
	fifthBone.addConstraint(fifthConstraint); 

	dKusudama bFourthConstraint = new dKusudama(bFourthBone);
	bFourthConstraint.addLimitConeAtIndex(0, new DVector(.5d, 1d, 0d),0.7d);
	bFourthConstraint.setAxialLimits(0.1d,0.3d);
	bFourthConstraint.enable();
	bFourthBone.addConstraint(bFourthConstraint);        
	
	dKusudama bSixthContstaint = new dKusudama(bSixthBone);
	bSixthContstaint.addLimitConeAtIndex(0, new DVector(.5d, 1d, 0d),0.5d);
	bSixthContstaint.addLimitConeAtIndex(1, new DVector(-.5d, 1d, 0d), 1d);
	bSixthContstaint.setAxialLimits(0.1d, 0.3d);
	bSixthContstaint.enable();
	bSixthBone.addConstraint(bSixthContstaint); 		
}

public void updatePinList() {
	pins.clear();
	recursivelyAddToPinnedList(pins, simpleArmature.getRootBone());
}

public void recursivelyAddToPinnedList(ArrayList<dIKPin> pins, dBone descendedFrom) {
	ArrayList<dBone> pinnedChildren = (ArrayList<dBone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
	for(dBone b : pinnedChildren) {
		pins.add(b.getIKPin());
	}
	for(dBone b : pinnedChildren) {
		ArrayList<dBone> children = b.getChildren(); 
		for(dBone b2 : children) {
			recursivelyAddToPinnedList(pins, b2);
		}
	}
}
