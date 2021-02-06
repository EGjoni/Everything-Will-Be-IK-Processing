import ewbik.processing.doublePrecision.sceneGraph.DVector;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.Axes;
import math.floatV.SGVec_3f;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class ConstraintExample_SinglePrecision extends PApplet{
	public static void main(String[] args) {
		PApplet.main("ConstraintExample_SinglePrecision");
	}

	public void settings(){
        size(1200, 900, P3D);
    }
	
	Armature simpleArmature;
	Bone  rootBone, initialBone, 
	secondBone, thirdBone, 
	fourthBone, fifthBone, 
	bFourthBone, bFifthBone, 
	bSixthBone;
	
	UI ui;

	ArrayList<IKPin> pins = new ArrayList<>();
	Axes worldAxes;
	
	IKPin activePin; 

	public void setup() {
		ui =new UI(this, false);
		worldAxes = new Axes(); 
		simpleArmature = new Armature("example");
		//attach the armature to the world axes (not necessary, just convenient)
		simpleArmature.localAxes().setParent(worldAxes);
		
		//specify that we want the solver to run 10 iteration whenever we call it.  
		simpleArmature.setDefaultIterations(10);
		//specify the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results) 
		simpleArmature.setDefaultDampening(0.05f);		
		
		//benchmark performance
		simpleArmature.setPerformanceMonitor(true);
		
		//translate everything down to where the user can see it, 
		//and rotate it 180 degrees about the z-axis so it's not upside down. 
		worldAxes.translateTo(new PVector(0, 150, 0));
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
		 
		//Tell the Bone class that all bones should draw their kusudamas.
		Bone.setDrawKusudamas(true);	
		
		//specify that the armature should avoid degenerate results when facing impossible 
		//situations. This comes at the 
		//cost of some performance (and temporal continuity in extreme poses 
		//[temporal discontinuity may be mitigated with a smaller dampening parameter]).
		simpleArmature.setDefaultStabilizingPassCount(1);
				
	}

	public void draw() {		
		if(mousePressed) {
	        ui.mouse.z = (float) activePin.getAxes().origin_().z;
	        activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	
	        simpleArmature.IKSolver(simpleArmature.getRootBone());
	    } else {
	        worldAxes.rotateAboutY(PI/500f, true);
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

	public void setBoneConstraints() {    

		Kusudama firstConstraint = new Kusudama(initialBone);
		firstConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 1f);
		firstConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
		firstConstraint.setAxialLimits(0.1f,0.3f);
		firstConstraint.enable();
		initialBone.addConstraint(firstConstraint);

		Kusudama secondConstraint = new Kusudama(secondBone);
		secondConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),1f);
		secondConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
		secondConstraint.setAxialLimits(0.1f,0.3f);
		secondConstraint.enable();
		secondBone.addConstraint(secondConstraint);

		Kusudama thirdConstraint = new Kusudama(thirdBone);
		thirdConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 1f);
		thirdConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
		thirdConstraint.setAxialLimits(0.1f,0.3f);
		thirdConstraint.enable();
		thirdBone.addConstraint(thirdConstraint);

		Kusudama fourthConstraint = new Kusudama(fourthBone);
		fourthConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 1f);
		fourthConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
		fourthConstraint.setAxialLimits(0.1f,0.3f);
		fourthConstraint.enable();
		fourthBone.addConstraint(fourthConstraint);

		Kusudama fifthConstraint = new Kusudama(fifthBone);
		fifthConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),0.5f);
		fifthConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
		fifthConstraint.setAxialLimits(0.1f, 0.3f);
		fifthConstraint.enable();
		fifthBone.addConstraint(fifthConstraint); 

		Kusudama bFourthConstraint = new Kusudama(bFourthBone);
		bFourthConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),0.7f);
		bFourthConstraint.setAxialLimits(0.1f,0.3f);
		bFourthConstraint.enable();
		bFourthBone.addConstraint(bFourthConstraint);        
		
		Kusudama bSixthContstaint = new Kusudama(bSixthBone);
		bSixthContstaint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),0.5f);
		bSixthContstaint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
		bSixthContstaint.setAxialLimits(0.1f, 0.3f);
		bSixthContstaint.enable();
		bSixthBone.addConstraint(bSixthContstaint); 		
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

}
