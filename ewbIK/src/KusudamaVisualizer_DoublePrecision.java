

import processing.core.*;
import processing.event.MouseEvent;
import processing.opengl.PShader;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.Vec3d;
import math.floatV.Vec3f;

import java.util.ArrayList;

import data.EWBIKSaver;

public class KusudamaVisualizer_DoublePrecision extends PApplet{
	public static void main(String[] args) {
		PApplet.main("KusudamaVisualizer_DoublePrecision");
	}

	public void settings(){
		size(1200, 900, P3D);
		noSmooth();
	}

	float zoomScalar = 7f/height;
	float orthoHeight = height;
	float orthoWidth = width;

	dArmature simpleArmature;
	dBone  rootBone, initialBone, 
	secondBone, thirdBone; 
		
	UI ui;

	dAxes worldAxes;
	ArrayList<dIKPin> pins = new ArrayList<>();
	public static dIKPin activePin; 
	public boolean multipassAllowed = true;

	public void setup() {
		ui = new UI(this, multipassAllowed);		
		
		//Create global axes so we can easily manipulate the whole scene. (not necessary, just convenient)  
		worldAxes = new dAxes(); 
		
		//Create an armature
		simpleArmature = new dArmature( "example");
		
		//attach the armature to the world axes (not necessary, just convenient)
		simpleArmature.localAxes().setParent(worldAxes);
		
		//translate everything down to where the user can see it, 
		//and rotate it 180 degrees about the z-axis so it's not upside down. 
		worldAxes.translateTo(new DVector(0, 150, 0));
		simpleArmature.localAxes().rotateAboutZ(Math.PI, true);

		//specify that we want the solver to run 10 iteration whenever we call it.  
		simpleArmature.setDefaultIterations(10);
		//specify the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results) 
		simpleArmature.setDefaultDampening(0.03d);
		//specify that the armature should avoid degenerate solutions. 
		simpleArmature.setDefaultStabilizingPassCount(1);
		//benchmark performance
		simpleArmature.setPerformanceMonitor(true);
		
		initializeBones(); 		
		setBoneConstraints();
		
		
		updatePinList();
		
		//Tell the Bone class that all bones should draw their kusudamas.
		dBone.setDrawKusudamas(true);
		       
		EWBIKSaver newSaver = new EWBIKSaver();
		newSaver.saveArmature(simpleArmature, "newArmature.arm");
	}
	
	public void initializeBones() {
		rootBone = simpleArmature.getRootBone();
		rootBone.setBoneHeight(20f);
		initialBone = new dBone(rootBone, "initial", 74f);
		secondBone = new dBone(initialBone, "secondBone", 86f);
		thirdBone = new dBone(secondBone, "thirdBone", 98f); 
		
		initialBone.rotAboutFrameX(.01f);		
		//pin the root
		rootBone.enablePin();				
		//intermediary pin a few bones up the chain. 
		secondBone.enablePin();		
		//determine how much precedence each of this pin's axes get 
		//in relation to other axes on other pins being considered by the solver.
		//this line state that the solver should care about this bone's X and Y headings 
		//aligning with its targets about 5 times as much as it cares about the X and Y headings of any other bones.  
		//it also tells the solver to ignore the z heading entirely. 
		secondBone.getIKPin().setTargetPriorities(5f, 5f,0f);
	}

	public void setBoneConstraints() {    		

		dKusudama firstConstraint = new dKusudama(initialBone);
		firstConstraint.addLimitConeAtIndex(0, new DVector(.5, 1f, 0f), 0.5f);
		firstConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 0.7f);
		firstConstraint.setAxialLimits(0.01f,0.03f);
		firstConstraint.enable();
		initialBone.addConstraint(firstConstraint);
		firstConstraint.optimizeLimitingAxes();

		dKusudama secondConstraint = new dKusudama(secondBone);
		secondConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),0.6f);
		secondConstraint.addLimitConeAtIndex(1, new DVector(-1f, 1f, 0f), 0.2f);
		secondConstraint.setAxialLimits(0.1f,0.9f);
		secondConstraint.enable();
		secondBone.addConstraint(secondConstraint);
		//secondConstraint.optimizeLimitingAxes();

		dKusudama thirdConstraint = new dKusudama(thirdBone);
		thirdConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 0.8f);
		thirdConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 0.8f);
		thirdConstraint.setAxialLimits(0.1f,0.3f);
		thirdConstraint.enable();
		thirdBone.addConstraint(thirdConstraint);
		thirdConstraint.optimizeLimitingAxes();
	}

	public void draw() {		
		if(mousePressed) {
			//Set the selected pin to the position of the mouse if the user is dragging it.
			activePin.translateTo(
					new DVector(
							ui.mouse.x, 
							ui.mouse.y, 
							activePin.getLocation_().z));        
			
			//run the IK solver on the armature.
			simpleArmature.IKSolver(rootBone);
			//println(((dKusudama)secondBone.getConstraint()).getTwistRatio());
		}else {
			//rotate the world so the user can inspect the pose
			worldAxes.rotateAboutY(PI/500f, true);
		}    
		
		zoomScalar = 350f/height;
		ui.drawScene(zoomScalar, 20f, null, simpleArmature, null, activePin, null, false);
	}

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

	public void camera(DVector cpd, DVector sod, DVector upd, PGraphics pg) {
		PVector cp = cpd.toPVec(); 
		PVector so = sod.toPVec(); 
		PVector up = upd.toPVec();
		pg.camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
	}

	public void updatePinList() {
		pins.clear();
		recursivelyAddToPinnedList(pins, simpleArmature.getRootBone());
		if(pins .size() > 0) {
			activePin = pins.get(pins.size()-1);
		}
	}

	public void recursivelyAddToPinnedList(ArrayList<dIKPin> pins, dBone descendedFrom) {
		ArrayList<dBone> pinnedChildren = (ArrayList<dBone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
		for(dBone b : pinnedChildren) {
			pins.add((dIKPin)b.getIKPin());
			b.getIKPin().getAxes().setParent(worldAxes);
		}
		for(dBone b : pinnedChildren) {
			ArrayList<dBone> children = b.getChildren(); 
			for(dBone b2 : children) {
				recursivelyAddToPinnedList(pins, b2);
			}
		}
	}

	

}
