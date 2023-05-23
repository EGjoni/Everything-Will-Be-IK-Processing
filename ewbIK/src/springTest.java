

import processing.core.*;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import g4p_controls.GEvent;
import g4p_controls.GKnob;
import g4p_controls.GView;
import math.doubleV.Vec3d;
import math.floatV.Vec3f;

import java.util.ArrayList;
import java.util.Collection;

import IK.doubleIK.AbstractBone;
import IK.doubleIK.AbstractIKPin;
import data.EWBIKSaver;

public class springTest extends PApplet{
	public static void main(String[] args) {
		PApplet.main("springTest");
	}

	public void settings(){
		size(1200, 900, P2D);
		noSmooth();
	}

	dBone selectedBone;
	float zoomScalar = 7f/height;
	float orthoHeight = height;
	float orthoWidth = width;
	boolean solve = false;
	G4PUI guiView;
	

	dArmature simpleArmature;
	dBone  rootBone, initialBone, 
	secondBone, thirdBone; 
		
	UI ui;

	public dAxes worldAxes;
	public static dIKPin activePin; 
	public boolean multipassAllowed = true;
	public int frame = 0;
	int nextProgress = 0;

	public void setup() {
		//Create global axes so we can easily manipulate the whole scene. (not necessary, just convenient)  
		worldAxes = new dAxes(); 
		//Create an armature
		simpleArmature = new dArmature("example");
		
		//attach the armature to the world axes (not necessary, just convenient)
		simpleArmature.localAxes().setParent(worldAxes);
		
		//translate everything down to where the user can see it, 
		//and rotate it 180 degrees about the z-axis so it's not upside down. 
		worldAxes.translateTo(new DVector(0, 0, 0));
		//worldAxes.rotateAboutZ(Math.PI, true);

		//specify that we want the solver to run 10 iteration whenever we call it.  
		simpleArmature.setDefaultIterations(10);
		//specify the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results) 
		simpleArmature.setDefaultDampening(Math.toRadians(20d));
		//specify that the armature should avoid degenerate solutions. 
		simpleArmature.setDefaultStabilizingPassCount(1);
		//benchmark performance
		simpleArmature.setPerformanceMonitor(true);
		
		
		
		initializeBones(); 		
		setBoneConstraints();
		
		
		
		
		//Tell the Bone class that all bones should draw their kusudamas.
		dBone.setDrawKusudamas(true);
		zoomScalar = PI/2f;
		EWBIKSaver newSaver = new EWBIKSaver();
		
		
		//ui = new UI(this, multipassAllowed);		
		guiView = new G4PUI(this, zoomScalar, multipassAllowed, simpleArmature, worldAxes, null, null);
		//newSaver.saveArmature(simpleArmature, "newArmature.arm");
	}
	
	public void populateBonesList(ArrayList<AbstractBone> bonesList) {
		bonesList.clear();
		ArrayList<AbstractBone> armatureBones = (ArrayList<AbstractBone>) simpleArmature.getBoneList();
		bonesList.addAll(armatureBones);
	}
	
	public void draw() {
		clear();
		
		//worldAxes.rotateAboutY(PI/500f, true);
		//run the IK solver on the armature.
		if(guiView.solveMode == guiView.PERPETUAL) {
			simpleArmature.IKSolver(rootBone);
		} else if(guiView.solveMode == guiView.INTERACTION && guiView.doSolve) {
			simpleArmature.IKSolver(rootBone);
		}
		
		//println(((dKusudama)secondBone.getConstraint()).getTwistRatio());
		if(nextProgress == -1) {
			simpleArmature._doSinglePullbackStep();
		}
		else if(nextProgress == 1) {
			simpleArmature._doSingleTowardTargetStep();
		}
		
		nextProgress = 0;
		  
		//ui.drawScene(zoomScalar, 20f, null, simpleArmature, null, activePin, null, false);
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
		//secondBone.enablePin();	
		//determine how much precedence each of this pin's axes get 
		//in relation to other axes on other pins being considered by the solver.
		//this line state that the solver should care about this bone's X and Y headings 
		//aligning with its targets about 5 times as much as it cares about the X and Y headings of any other bones.  
		//it also tells the solver to ignore the z heading entirely. 
		//secondBone.getIKPin().setTargetPriorities(5f, 5f, 0f);
		thirdBone.enablePin();	
	}

	public void setBoneConstraints() {    		

		dKusudama firstConstraint = new dKusudama(initialBone);
		firstConstraint.addLimitConeAtIndex(0, new DVector(1f, 0f, 0f), Math.toRadians(90d));
		//firstConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 0.7f);
		firstConstraint.setAxialLimits(-1f, 2f);
		firstConstraint.enable();
		initialBone.addConstraint(firstConstraint);
		firstConstraint.optimizeLimitingAxes();

		/*dKusudama secondConstraint = new dKusudama(secondBone);
		secondConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),0.6f);
		secondConstraint.addLimitConeAtIndex(1, new DVector(-1f, 1f, 0f), 0.2f);
		secondConstraint.setAxialLimits(0.1f,0.9f);
		secondConstraint.enable();
		secondBone.addConstraint(secondConstraint);
		secondConstraint.optimizeLimitingAxes();

		dKusudama thirdConstraint = new dKusudama(thirdBone);
		thirdConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 0.8f);
		thirdConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 0.8f);
		thirdConstraint.setAxialLimits(0.1f,0.3f);
		thirdConstraint.enable();
		thirdBone.addConstraint(thirdConstraint);
		thirdConstraint.optimizeLimitingAxes();*/
	}

	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		if(event.isShiftDown()) {
			guiView.activePin.getAxes().rotateAboutZ(e/TAU, true);
		}else if (event.isControlDown()) {
			guiView.activePin.getAxes().rotateAboutX(e/TAU, true);
		}  else {
			guiView.activePin.getAxes().rotateAboutY(e/TAU, true);
		}
		guiView.activePin.solveIKForThisAndChildren();    
	}
	public void mousePressed(MouseEvent event) {
		
	}

	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == DOWN) {            
				int currentPinIndex =(guiView.pins.indexOf(guiView.activePin) + 1) % guiView.pins.size();
				guiView.activePin  = guiView.pins.get(currentPinIndex);
			} else if (keyCode == UP) {
				int idx = guiView.pins.indexOf(guiView.activePin);
				int currentPinIndex =  (guiView.pins.size()-1) -(((guiView.pins.size()-1) - (idx - 1)) % guiView.pins.size());
				guiView.activePin  = guiView.pins.get(currentPinIndex);
			} else if(keyCode == RIGHT) {
				nextProgress = 1;
			} else if(keyCode == LEFT) {
				nextProgress = -1;
			}
		} else {
			if(key == 'c') {
				solve = true;
			} else if (key == 'r') {
				guiView.reloadShaders();
			} else if (key == 's') {
				//guiView.sceneView.stencil.save("stenciltest.png");
			}
		}
	}
	
	public void mouseReleased() {
		this.guiView.mouseReleased();
	}

	public void camera(DVector cpd, DVector sod, DVector upd, PGraphics pg) {
		PVector cp = cpd.toPVec(); 
		PVector so = sod.toPVec(); 
		PVector up = upd.toPVec();
		pg.camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
	}

}
