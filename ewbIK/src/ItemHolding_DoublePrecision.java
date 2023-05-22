

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import data.EWBIKLoader;
import data.EWBIKSaver;
import ewbik.processing.EWBKIO;
import ewbik.processing.doublePrecision.dArmature;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dIKPin;
import ewbik.processing.doublePrecision.dKusudama;
import ewbik.processing.doublePrecision.sceneGraph.DVector;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import math.doubleV.SGVec_3d;
import math.floatV.SGVec_3f;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;

public class ItemHolding_DoublePrecision extends PApplet{

	public static void main(String[] args) {
		PApplet.main("ItemHolding_DoublePrecision");
	}

	public void settings(){
		size(1200, 900, P2D);
	}

	dArmature loadedArmature;
	ArrayList<dIKPin> pins = new ArrayList<>();
	G4PUI guiView;

	dIKPin activePin; 
	dAxes worldAxes, cubeAxes;
	
	float zoomScalar =  200f/height;      
	boolean cubeMode = true;
	
	public void setup() {
		//ui =new UI(this, true);
		String path = sketchPath()+File.separator;
		loadedArmature = EWBKIO.LoadArmature_doublePrecision(path+"Humanoid_Holding_Item.arm");
		worldAxes = (dAxes) loadedArmature.localAxes().getParentAxes(); 
		if(worldAxes == null) { 
			worldAxes = new dAxes();
			loadedArmature.localAxes().setParent(worldAxes);
		}
		updatePinList();   	    
		cubeAxes = new dAxes(); 		
		
		activePin = pins.get(pins.size()-1);
		
		loadedArmature.setPerformanceMonitor(true); //print performance stats
		
		//Tell the Bone class that all bones should draw their kusudamas.
		dBone.setDrawKusudamas(false);
		//Enable fancy multipass shading for translucent kusudamas. 
		//dKusudama.enableMultiPass(true);

		/**
		 * The armature we're loading is already posed such that its hands touch
		 * a box. So all we need to do is , first
		 * move our box into the appropriate postion 
		 * */
		cubeAxes.translateTo(new DVector(-13,-27,32));
		cubeAxes.setRelativeToParent(worldAxes);
		/**
		 * and then specify that the  transformations of the left hand and right hand pins 
		 * should be computed relative to the axes of the cube we're drawing, 
		 * Thereby, any time we transform the parent cube's axes, the pins will follow.
		 */
		loadedArmature.getBoneTagged("left hand").getIKPin().getAxes().setParent(cubeAxes);
		loadedArmature.getBoneTagged("right hand").getIKPin().getAxes().setParent(cubeAxes);
		loadedArmature.getBoneTagged("right collar bone").getConstraint().setPainfulness(0.8d);
		loadedArmature.getBoneTagged("left collar bone").getConstraint().setPainfulness(0.8d);
		loadedArmature.getBoneTagged("left upper arm").getConstraint().setPainfulness(0.5d);
		loadedArmature.getBoneTagged("right upper arm").getConstraint().setPainfulness(0.5d);
		loadedArmature.setDefaultDampening(Math.toRadians(10d));
		loadedArmature.setDefaultStabilizingPassCount(1);
		loadedArmature.setDefaultIterations(30);
		guiView = new G4PUI(this, zoomScalar, true, loadedArmature, worldAxes, (buffer, mode) -> drawArmature(buffer, mode), null);
		//ui.buildBoneList(loadedArmature.getBoneList());
	}


	public void draw() {
		if(guiView.solveMode == guiView.PERPETUAL) {
			loadedArmature.IKSolver(loadedArmature.getRootBone());
		} else if(guiView.solveMode == guiView.INTERACTION && guiView.doSolve) {
			loadedArmature.IKSolver(loadedArmature.getRootBone());
		}
	
		/*if(mousePressed) {
			if(cubeMode) { 
				cubeAxes.translateTo(new DVector(ui.mouse.x, ui.mouse.y,cubeAxes.origin_().z));
			} else { 
				activePin.translateTo(new DVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));
			}
			loadedArmature.IKSolver(loadedArmature.getRootBone());
		}else {			
			worldAxes.rotateAboutY(PI/500f, true);
		}    
		String additionalInstructions = "Hit the 'C' key to select or deselect the cube";  
		//decrease the numerator to increase the zoom. 
		zoomScalar = 200f/height;        
		ui.drawScene(zoomScalar, 10f, ()->drawHoldCube(), loadedArmature, additionalInstructions, activePin, cubeAxes, cubeMode);*/
	}
	
	public void drawArmature(PGraphics3D buff, int mode) {
		if(mode == guiView.STENCIL) { 
			buff.fill(0,0,0,255);
			buff.emissive(0);
			buff.noStroke();
		} else {
			buff.fill(60,60,60);
			buff.strokeWeight(1);
			buff.stroke(255);
		}
		buff.pushMatrix();
		buff.applyMatrix(cubeAxes.getGlobalPMatrix());
		buff.box(40, 20, 20);
		buff.popMatrix();	
	}
	
	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		dAxes axes = cubeMode ? cubeAxes  : (dAxes) activePin.getAxes(); 
		if(event.isShiftDown()) {
			axes.rotateAboutZ(e/TAU, true);
		}else if (event.isControlDown()) {
			axes.rotateAboutX(e/TAU, true);
		}  else {
			axes.rotateAboutY(e/TAU, true);
		}
		activePin.solveIKForThisAndChildren();    
	}

	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == DOWN) {      
				cubeMode = false;
				int currentPinIndex =(pins.indexOf(activePin) + 1) % pins.size();
				activePin  = pins.get(currentPinIndex);
			} else if (keyCode == UP) {
				cubeMode = false;
				int idx = pins.indexOf(activePin);
				int currentPinIndex =  (pins.size()-1) -(((pins.size()-1) - (idx - 1)) % pins.size());
				activePin  = pins.get(currentPinIndex);
			} 
		} else if(key == 'c') {
			cubeMode = !cubeMode;
		}
	}

	public void updatePinList() {
		pins.clear();
		recursivelyAddToPinnedList(pins, loadedArmature.getRootBone());
	}

	public void recursivelyAddToPinnedList(ArrayList<dIKPin> pins, dBone descendedFrom) {
		ArrayList<dBone> pinnedChildren = (ArrayList<dBone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
		for(dBone b : pinnedChildren) {
			dIKPin pin = (dIKPin) b.getIKPin();
			pins.add(pin);
		}
		for(dBone b : pinnedChildren) {
			ArrayList<dBone> children = b.getChildren(); 
			for(dBone b2 : children) {
				recursivelyAddToPinnedList(pins, b2);
			}
		}
	}


}


