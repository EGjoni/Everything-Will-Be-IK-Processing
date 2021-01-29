import data.EWBIKSaver;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import math.floatV.MathUtils;

import java.util.ArrayList;
import java.io.File;
String delim = File.separator;

float zoomScalar = 7f/height;	

UI ui; 

Armature humanArmature;
Bone  rootBone, 
c1, c3, c5,
l_collar_bone,	r_collar_bone,
l_upper_arm,		r_upper_arm,
l_lower_arm,		r_lower_arm,
l_hand,					r_hand,
neck_1,
neck_2,
head;


Axes worldAxes;
ArrayList<IKPin> pins = new ArrayList<IKPin>();

Axes cubeAxes;
public static IKPin activePin; 

public void setup() {
	  size(1200, 900, P3D);
	  ui = new UI(true); //ignore this line, it's just for user Interace stuff. 
		
		worldAxes = new Axes(); 
		cubeAxes = new Axes(); 
		cubeAxes.setParent(worldAxes);
		humanArmature = new Armature( "example");
		humanArmature.localAxes().setParent(worldAxes);
		worldAxes.translateTo(new PVector(0f, 50f, 0));
		humanArmature.localAxes().rotateAboutZ(PI, true);

		//specify that we want the solver to run 15 iteration whenever we call it.
		humanArmature.setDefaultIterations(15);
		//specify the maximum amount any bone is allowed to rotate per iteration (slower convergence, nicer results) 
		humanArmature.setDefaultDampening(0.25f);
		//1 = specify that the armature should avoid wobbly solutions (more reliable) 
		//0 = allow wobbly solutions  (faster, and usually sufficient). 
		humanArmature.setDefaultStabilizingPassCount(1);
		//benchmark performance
		humanArmature.setPerformanceMonitor(true);
		
		//Add some bones to the armature
		initializeBones(); 		
		setBoneConstraints();
		//add the pins/targets to an array so we can cycle through them easily with keyboad input. 
		 updatePinList();		 		 
		 
		humanArmature.updateArmatureSegments();
		humanArmature.IKSolver(rootBone, 0.5f, 20, 1);

		//Tell the Bone class that all bones should draw their kusudamas.
		Bone.setDrawKusudamas(true);
		//Enable fancy multipass shading for translucent kusudamas. 
		Kusudama.enableMultiPass(true);

	}

	public void draw() {		
		if(mousePressed) {
			activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	
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
		c1 = new Bone(rootBone, "c1", 15f);		
		c3 = new Bone(c1, "c3", 15f);		
		c5 = new Bone(c3, "c5", 15f);
		neck_1 = new Bone(c5, "neck 1", 12f);
		neck_2 = new Bone(neck_1, "neck 2", 12f); 
		head = new Bone(neck_2, "head", 15f);

		r_collar_bone = new Bone(c5, "right collar bone", 15f); 
		r_collar_bone.rotAboutFrameZ(MathUtils.toRadians(-50f));		

		r_upper_arm = new Bone(r_collar_bone, "right upper arm", 40f);
		r_upper_arm.rotAboutFrameZ(MathUtils.toRadians(-130f));		
		r_lower_arm = new Bone(r_upper_arm, "right lower arm", 40f);
		r_hand = new Bone(r_lower_arm, "right hand", 10f);

		l_collar_bone = new Bone(c5, "left collar bone", 15f); 
		l_collar_bone.rotAboutFrameZ(MathUtils.toRadians(50f));		

		l_upper_arm = new Bone(l_collar_bone, "left upper arm", 40f);
		l_upper_arm.rotAboutFrameZ(MathUtils.toRadians(130f));		
		l_lower_arm = new Bone(l_upper_arm, "right lower arm", 40f);
		l_hand = new Bone(l_lower_arm, "left hand", 10f);

		l_hand.enablePin();
		l_hand.getIKPin().getAxes().rotateAboutX(MathUtils.toRadians(90f), true);
		l_hand.getIKPin().setTargetPriorities(.5f, 0f, .5f);
		l_hand.getIKPin().getAxes().translateByLocal(new PVector(20f, -20f, 20f));
		r_hand.enablePin();
		r_hand.getIKPin().getAxes().rotateAboutX(MathUtils.toRadians(90f), true);
		r_hand.getIKPin().setTargetPriorities(.5f, 0f, .5f);
		r_hand.getIKPin().getAxes().translateByLocal(new PVector(-20f, -20f, 20f));

		head.enablePin();
		head.getIKPin().setPinWeight(5f);
		head.getIKPin().setTargetPriorities(5f, 5f, 5f);
		rootBone.enablePin();

		worldAxes.rotateAboutX(MathUtils.toRadians(-10f), true);
	}

	public void setBoneConstraints() {    
		Kusudama r_collar_joint = new Kusudama(r_collar_bone);
		r_collar_joint.addLimitConeAtIndex(0, new PVector(1.0f, 0.4f, 0f), 0.7f);
		r_collar_joint.setAxialLimits(-0.3f, 1f);
		r_collar_joint.optimizeLimitingAxes();
		r_collar_joint.setPainfullness(0.1f);

		Kusudama r_shoulder = new Kusudama(r_upper_arm); 
		r_shoulder.addLimitConeAtIndex(0, new PVector(1f, .9f, 0.5f), 1);
		r_shoulder.addLimitConeAtIndex(1, new PVector(1f, 1f, 0.5f), 1);
		r_shoulder.setAxialLimits(-1.7f, 1.7f);
		r_shoulder.optimizeLimitingAxes();
		r_shoulder.setPainfullness(0.05f);

		Kusudama r_elbow = new Kusudama(r_lower_arm);
		r_elbow.addLimitConeAtIndex(0, new PVector(0f, -1f, 0.1f), 0.025f);
		r_elbow.addLimitConeAtIndex(1, new PVector(0f, 1f, 0.1f), 0.025f);
		r_elbow.setAxialLimits(-2.7f, 2.7f);
		r_elbow.optimizeLimitingAxes();

		Kusudama r_wrist = new Kusudama(r_hand);
		r_wrist.addLimitConeAtIndex(0, new PVector(0f, 0.7f, -0.7f), MathUtils.toRadians(45f));
		r_wrist.addLimitConeAtIndex(1, new PVector(0f, 0.7f, 0.7f), MathUtils.toRadians(45f));
		r_wrist.setAxialLimits(-0.01f, 0.02f);
		r_wrist.optimizeLimitingAxes();

		Kusudama l_collar_joint = new Kusudama(l_collar_bone);
		l_collar_joint.addLimitConeAtIndex(0, new PVector(-1.0f, 0.4f, 0f), 0.7f);
		l_collar_joint.setAxialLimits(-0.7f, 1f);
		l_collar_joint.optimizeLimitingAxes();
		l_collar_joint.setPainfullness(0.1f);

		Kusudama l_shoulder = new Kusudama(l_upper_arm); 
		l_shoulder.addLimitConeAtIndex(0, new PVector(-1f, .9f, 0.5f), 1f);
		l_shoulder.addLimitConeAtIndex(1, new PVector(-1f, 1f, 0.5f), 1f);
		l_shoulder.setAxialLimits(-.3f, 1.7f);
		l_shoulder.optimizeLimitingAxes();
		l_shoulder.setPainfullness(0.05f);

		Kusudama l_elbow = new Kusudama(l_lower_arm);
		l_elbow.addLimitConeAtIndex(0, new PVector(0f, -1f, 0.1f), 0.025f);
		l_elbow.addLimitConeAtIndex(1, new PVector(0f, 1f, 0.1f), 0.025f);
		l_elbow.setAxialLimits(-0.0f, 2.7f);
		l_elbow.optimizeLimitingAxes();

		Kusudama l_wrist = new Kusudama(l_hand);
		l_wrist.addLimitConeAtIndex(0, new PVector(0f, 0.7f, -0.7f), MathUtils.toRadians(45f));
		l_wrist.addLimitConeAtIndex(1, new PVector(0f, 0.7f, 0.7f), MathUtils.toRadians(45f));
		l_wrist.setAxialLimits(0.01f, 0.02f);
		l_wrist.optimizeLimitingAxes();

		Kusudama neck1j = new Kusudama(neck_1);
		neck1j.addLimitConeAtIndex(0, new PVector(0f,1f,0f), 0.01f);
		neck1j.setAxialLimits(0.001f, 0.002f);
		neck1j.optimizeLimitingAxes();		

		Kusudama c1j = new Kusudama(c1);
		c1j.addLimitConeAtIndex(0, new PVector(0f,1f,0f), MathUtils.toRadians(10f));
		c1j.setAxialLimits(-MathUtils.toRadians(20f), MathUtils.toRadians(20f));
		c1j.optimizeLimitingAxes();		

		Kusudama c3j = new Kusudama(c3);
		c3j.addLimitConeAtIndex(0, new PVector(0f,1f,0f), MathUtils.toRadians(10f));
		c3j.setAxialLimits(-MathUtils.toRadians(20f), MathUtils.toRadians(20f));
		c3j.optimizeLimitingAxes();		

		Kusudama c5j = new Kusudama(c5);
		c5j.addLimitConeAtIndex(0, new PVector(0f,1f,0f), MathUtils.toRadians(10f));
		c5j.setAxialLimits(-MathUtils.toRadians(20f), MathUtils.toRadians(20f));
		c5j.optimizeLimitingAxes();

		Kusudama neck2j = new Kusudama(neck_2);
		neck2j.addLimitConeAtIndex(0, new PVector(0f,1f,0f), MathUtils.toRadians(10f));
		neck2j.setAxialLimits(-MathUtils.toRadians(20f), MathUtils.toRadians(20f));
		neck2j.optimizeLimitingAxes();

		Kusudama headj = new Kusudama(head);
		headj.addLimitConeAtIndex(0, new PVector(0f,1f,0f), MathUtils.toRadians(40f));
		headj.setAxialLimits(-MathUtils.toRadians(20f), MathUtils.toRadians(20f));
		headj.optimizeLimitingAxes();
	}


	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		Axes axes = (Axes) activePin.getAxes(); 
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

	public void recursivelyAddToPinnedList(ArrayList<IKPin> pins, Bone descendedFrom) {
		ArrayList<Bone> pinnedChildren = (ArrayList<Bone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
		for(Bone b : pinnedChildren) {
			pins.add((IKPin)b.getIKPin());
			b.getIKPin().getAxes().setParent(worldAxes);
		}
		for(Bone b : pinnedChildren) {
			ArrayList<Bone> children = b.getChildren(); 
			for(Bone b2 : children) {
				recursivelyAddToPinnedList(pins, b2);
			}
		}
	}
