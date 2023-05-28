import processing.core.*;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;
import ewbik.processing.EWBKIO;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import g4p_controls.GEvent;
import g4p_controls.GKnob;
import g4p_controls.GView;
import math.doubleV.Vec3d;
import math.floatV.Vec3f;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import IK.doubleIK.AbstractBone;
import IK.doubleIK.AbstractIKPin;
import data.EWBIKSaver;

public class SymmetryTest extends PApplet {
	
	public static void main(String[] args) {
		PApplet.main("SymmetryTest");
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
	G4PUI ui;
	
	dArmature simpleArmature;
	dBone  rootBone, initialBone, 
	secondBone, thirdBone; 
		
	public dAxes worldAxes;
	public static dIKPin activePin; 
	public boolean multipassAllowed = true;
	public int frame = 0;
	int nextProgress = 0;

	public void setup() {
		worldAxes = new dAxes(); 
		//load an armature
		String path = sketchPath()+File.separator;
		simpleArmature = EWBKIO.LoadArmature_doublePrecision(path+"weightless.arm");
		//simpleArmature.localAxes().scaleXBy(-1d);
		simpleArmature.localAxes().setRelativeToParent(this.worldAxes); // set it relative to our custom world axes for ui convenience
		//surgery(); //amputate the left arm, make a clone in the mirrorverse, steal its right arm then graft it back on to our dude.
		dBone.setDrawKusudamas(true); 	//Tell the Bone class that all bones should draw their kusudama constraints.
		ui = new G4PUI(this, multipassAllowed, simpleArmature, worldAxes, null, null); //initialize UI stuff
	}
	
	public void populateBonesList(ArrayList<AbstractBone> bonesList) {
		bonesList.clear();
		ArrayList<AbstractBone> armatureBones = (ArrayList<AbstractBone>) simpleArmature.getBoneList();
		bonesList.addAll(armatureBones);
	}
	
	public void draw() {
		clear();		
		if(ui.doSolve()) //run the IK solver on the armature if the ui says that the user wants us to
			ui.armature.IKSolver(ui.activePin.forBone());
	}
	
	public void surgery() {
		dBone rCollar = simpleArmature.getBoneTagged("right collar bone");
		dBone lCollar = simpleArmature.getBoneTagged("left collar bone");
		lCollar.deleteBone();
		String path = sketchPath()+File.separator;
		dArmature cloneDude = EWBKIO.LoadArmature_doublePrecision(path+"Humanoid2.arm");
		dBone clone_rCollar = cloneDude.getBoneTagged("right collar bone");
		//cloneDude.localAxes().scaleXBy(-1d);
		
	}

	public void setBoneConstraints() {    		

		dKusudama firstConstraint = new dKusudama(initialBone);
		firstConstraint.addLimitConeAtIndex(0, new DVector(-.5f, 1f, 0f), 0.7f);
		firstConstraint.addLimitConeAtIndex(1, new DVector(-1f, 1f, 0f), 0.2f);
		firstConstraint.setAxialLimits(-1f, 2f);
		firstConstraint.enable();
		initialBone.addConstraint(firstConstraint);
		firstConstraint.optimizeLimitingAxes();

		dKusudama secondConstraint = new dKusudama(secondBone);
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
		thirdConstraint.optimizeLimitingAxes();
	}

	public void mouseWheel(MouseEvent event) {
		ui.mouseWheel(event);
	}
	public void mousePressed(MouseEvent event) {
		ui.mousePressed(event);
	}
	public void keyPressed() {
		ui.keyPressed();
	}
	public void mouseReleased() {
		this.ui.mouseReleased();
	}
}
