

import processing.core.*;
import processing.event.MouseEvent;
import processing.opengl.PShader;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.Vec3d;
import math.floatV.MathUtils;
import math.floatV.Vec3f;

import java.util.ArrayList;

import data.EWBIKSaver;

public class Humanoid_Manual extends PApplet{
	public static void main(String[] args) {
		PApplet.main("Humanoid_Manual");
	}
	PShader blurshader;
	PGraphics stencil, display;

	public void settings(){
		size(1200, 900, P3D);
		noSmooth();
	}


	float zoomScalar = 7f/height;
	float orthoHeight = height;
	float orthoWidth = width;
	
	
	UI ui; 

	dArmature humanArmature;
	dBone  rootBone, 
	c1, c2, c3, c4, c5,
	l_collar_bone,	r_collar_bone,
	l_upper_arm,		r_upper_arm,
	l_lower_arm,		r_lower_arm,
	l_hand,					r_hand,
	neck_1,
	neck_2,
	head;


	dAxes worldAxes;
	ArrayList<dIKPin> pins = new ArrayList<>();

	dAxes cubeAxes;
	public static dIKPin activePin; 


	public void setup() {
		
		ui = new UI(this, true); //ignore this line, it's just for user Interace stuff. 
		
		worldAxes = new dAxes(); 
		cubeAxes = new dAxes(); 
		cubeAxes.setParent(worldAxes);
		humanArmature = new dArmature( "example");
		humanArmature.localAxes().setParent(worldAxes);
		worldAxes.translateTo(new DVector(0, 50, 0));
		humanArmature.localAxes().rotateAboutZ(Math.PI, true);

		humanArmature.setDefaultIterations(15);
		humanArmature.setDefaultDampening(0.05d);

		humanArmature.setDefaultStabilizingPassCount(0);
		humanArmature.setPerformanceMonitor(true);
		orthoHeight = height;
		orthoWidth = width;
		initializeBones(); 		
		setBoneConstraints();
		updatePinList();
		humanArmature.regenerateShadowSkeleton();
		humanArmature.IKSolver(rootBone, 20, 1);

		//Tell the Bone class that all bones should draw their kusudamas.
		dBone.setDrawKusudamas(true);
		//Enable fancy multipass shading for translucent kusudamas. 
		dKusudama.enableMultiPass(true);

	}

	public void draw() {		
		if(mousePressed) {
			activePin.translateTo(new DVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	
			humanArmature.IKSolver(rootBone);
		}else {
			worldAxes.rotateAboutY(PI/500f, true);
		}    
		zoomScalar = 200f/height;
		String additionalInstructions =  "\n HIT THE S KEY TO SAVE THE CURRENT ARMATURE CONFIGURATION.";
		ui.drawScene(zoomScalar, 10f, null, humanArmature, additionalInstructions, activePin, cubeAxes, false);
	}


	public void initializeBones() {
		rootBone = humanArmature.getRootBone();
		rootBone.setBoneHeight(1f);
		rootBone.localAxes().markDirty();
		rootBone.localAxes().updateGlobal();
		c1 = new dBone(rootBone, "c1", 10f);
		c2 = new dBone(c1, "c2", 10f);
		c3 = new dBone(c2, "c3", 10f);
		c4 = new dBone(c3, "c4", 10f);
		c5 = new dBone(c4, "c5", 10f);
		neck_1 = new dBone(c5, "neck 1", 12f);
		neck_2 = new dBone(neck_1, "neck 2", 12f); 
		head = new dBone(neck_2, "head", 15f);

		r_collar_bone = new dBone(c5, "right collar bone", 15f); 
		r_collar_bone.rotAboutFrameZ(MathUtils.toRadians(-50));		

		r_upper_arm = new dBone(r_collar_bone, "right upper arm", 40f);
		r_upper_arm.rotAboutFrameZ(Math.toRadians(-130d));		
		r_lower_arm = new dBone(r_upper_arm, "right lower arm", 40f);
		r_hand = new dBone(r_lower_arm, "right hand", 10f);

		l_collar_bone = new dBone(c5, "left collar bone", 15f); 
		l_collar_bone.rotAboutFrameZ(MathUtils.toRadians(50));		

		l_upper_arm = new dBone(l_collar_bone, "left upper arm", 40f);
		l_upper_arm.rotAboutFrameZ(Math.toRadians(130d));		
		l_lower_arm = new dBone(l_upper_arm, "right lower arm", 40f);
		l_hand = new dBone(l_lower_arm, "left hand", 10f);

		l_hand.enablePin();
		l_hand.getIKPin().getAxes().rotateAboutX(Math.toRadians(90d), true);
		l_hand.getIKPin().setTargetPriorities(.5, 0, .5);
		l_hand.getIKPin().getAxes().translateByLocal(new DVector(20, -20, 20));
		r_hand.enablePin();
		r_hand.getIKPin().getAxes().rotateAboutX(Math.toRadians(90d), true);
		r_hand.getIKPin().setTargetPriorities(.5, 0, .5);
		r_hand.getIKPin().getAxes().translateByLocal(new DVector(-20, -20, 20));

		head.enablePin();
		head.getIKPin().setPinWeight(5d);
		head.getIKPin().setTargetPriorities(5d, 5d, 5d);
		rootBone.enablePin();

		worldAxes.rotateAboutX(Math.toRadians(-10d), true);
	}

	public void setBoneConstraints() {    
		dKusudama r_collar_joint = new dKusudama(r_collar_bone);
		r_collar_joint.addLimitConeAtIndex(0, new DVector(1.0, 0.4, 0), 0.7);
		r_collar_joint.setAxialLimits(-0.3d, 1d);
		r_collar_joint.optimizeLimitingAxes();
		r_collar_joint.setPainfulness(0.8d);

		dKusudama r_shoulder = new dKusudama(r_upper_arm); 
		r_shoulder.addLimitConeAtIndex(0, new DVector(1, .9, 0.5), 1f);
		r_shoulder.addLimitConeAtIndex(1, new DVector(1, 1, 0.5), 1f);
		r_shoulder.setAxialLimits(-1.7d, 1.7d);
		r_shoulder.optimizeLimitingAxes();
		r_shoulder.setPainfulness(0.05d);

		dKusudama r_elbow = new dKusudama(r_lower_arm);
		r_elbow.addLimitConeAtIndex(0, new DVector(0, -1, 0.1), 0.025);
		r_elbow.addLimitConeAtIndex(1, new DVector(0, 1, 0.1), 0.025);
		r_elbow.setAxialLimits(-2.7d, 2.7d);
		r_elbow.optimizeLimitingAxes();

		dKusudama r_wrist = new dKusudama(r_hand);
		r_wrist.addLimitConeAtIndex(0, new DVector(0, 0.7, -0.7), Math.toRadians(45d));
		r_wrist.addLimitConeAtIndex(1, new DVector(0, 0.7, 0.7), Math.toRadians(45d));
		r_wrist.setAxialLimits(-0.01d, 0.02d);
		r_wrist.optimizeLimitingAxes();

		dKusudama l_collar_joint = new dKusudama(l_collar_bone);
		l_collar_joint.addLimitConeAtIndex(0, new DVector(-1.0, 0.4, 0), 0.7);
		l_collar_joint.setAxialLimits(-0.7d, 1d);
		l_collar_joint.optimizeLimitingAxes();
		l_collar_joint.setPainfulness(0.8d);

		dKusudama l_shoulder = new dKusudama(l_upper_arm); 
		l_shoulder.addLimitConeAtIndex(0, new DVector(-1, .9, 0.5), 1f);
		l_shoulder.addLimitConeAtIndex(1, new DVector(-1, 1, 0.5), 1f);
		l_shoulder.setAxialLimits(-.3d, 1.7d);
		l_shoulder.optimizeLimitingAxes();
		l_shoulder.setPainfulness(0.05d);

		dKusudama l_elbow = new dKusudama(l_lower_arm);
		l_elbow.addLimitConeAtIndex(0, new DVector(0, -1, 0.1), 0.025);
		l_elbow.addLimitConeAtIndex(1, new DVector(0, 1, 0.1), 0.025);
		l_elbow.setAxialLimits(-0.0d, 2.7d);
		l_elbow.optimizeLimitingAxes();

		dKusudama l_wrist = new dKusudama(l_hand);
		l_wrist.addLimitConeAtIndex(0, new DVector(0, 0.7, -0.7), Math.toRadians(45d));
		l_wrist.addLimitConeAtIndex(1, new DVector(0, 0.7, 0.7), Math.toRadians(45d));
		l_wrist.setAxialLimits(0.01d, 0.02d);
		l_wrist.optimizeLimitingAxes();

		dKusudama neck1j = new dKusudama(neck_1);
		neck1j.addLimitConeAtIndex(0, new DVector(0,1,0), 0.01d);
		neck1j.setAxialLimits(0.001d, 0.002d);
		neck1j.optimizeLimitingAxes();		

		dKusudama c1j = new dKusudama(c1);
		c1j.addLimitConeAtIndex(0, new DVector(0,1,0), Math.toRadians(10d));
		c1j.setAxialLimits(-Math.toRadians(10d), Math.toRadians(20d));
		c1j.optimizeLimitingAxes();

		dKusudama c2j = new dKusudama(c2);
		c2j.addLimitConeAtIndex(0, new DVector(0,1,0), Math.toRadians(10d));
		c2j.setAxialLimits(-Math.toRadians(10d), Math.toRadians(20d));
		c2j.optimizeLimitingAxes();

		dKusudama c3j = new dKusudama(c3);
		c3j.addLimitConeAtIndex(0, new DVector(0,1,0), Math.toRadians(10d));
		c3j.setAxialLimits(-Math.toRadians(10d), Math.toRadians(20d));
		c3j.optimizeLimitingAxes();

		dKusudama c4j = new dKusudama(c4);
		c4j.addLimitConeAtIndex(0, new DVector(0,1,0), Math.toRadians(10d));
		c4j.setAxialLimits(-Math.toRadians(10d), Math.toRadians(20d));
		c4j.optimizeLimitingAxes();

		dKusudama c5j = new dKusudama(c5);
		c5j.addLimitConeAtIndex(0, new DVector(0,1,0), Math.toRadians(10d));
		c5j.setAxialLimits(-Math.toRadians(10d), Math.toRadians(20d));
		c5j.optimizeLimitingAxes();

		dKusudama neck2j = new dKusudama(neck_2);
		neck2j.addLimitConeAtIndex(0, new DVector(0,1,0), Math.toRadians(10d));
		neck2j.setAxialLimits(-Math.toRadians(10d), Math.toRadians(20d));
		neck2j.optimizeLimitingAxes();

		dKusudama headj = new dKusudama(head);
		headj.addLimitConeAtIndex(0, new DVector(0,1,0), Math.toRadians(40d));
		headj.setAxialLimits(-Math.toRadians(10d), Math.toRadians(20d));
		headj.optimizeLimitingAxes();
	}


	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		dAxes axes = (dAxes) activePin.getAxes(); 
		if(event.isShiftDown()) {
			axes.rotateAboutZ(e/TAU, true);
		}else if (event.isControlDown()) {
			axes.rotateAboutX(e/TAU, true);
		}  else {
			axes.rotateAboutY(e/TAU, true);
		}
		humanArmature.IKSolver(rootBone);  
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
		} else if(key == 's') {
			println("Saving");
			EWBIKSaver newSaver = new EWBIKSaver();
			newSaver.saveArmature(humanArmature, "Humanoid.arm");			
		}
	}

	public void updatePinList() {
		pins.clear();
		recursivelyAddToPinnedList(pins, humanArmature.getRootBone());
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
