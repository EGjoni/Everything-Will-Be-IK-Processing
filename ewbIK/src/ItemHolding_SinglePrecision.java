

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import data.EWBIKLoader;
import data.EWBIKSaver;
import ewbik.processing.EWBKIO;
import ewbik.processing.singlePrecision.Armature;
import ewbik.processing.singlePrecision.Bone;
import ewbik.processing.singlePrecision.IKPin;
import ewbik.processing.singlePrecision.Kusudama;
import ewbik.processing.singlePrecision.sceneGraph.Axes;
import math.doubleV.SGVec_3d;
import math.floatV.SGVec_3f;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

public class ItemHolding_SinglePrecision extends PApplet{

	public static void main(String[] args) {
		PApplet.main("ItemHolding_SinglePrecision");
	}

	public void settings(){
		size(1200, 900, P3D);
	}

	Armature loadedArmature;
	ArrayList<IKPin> pins = new ArrayList<>();
	UI ui;

	IKPin activePin; 
	Axes worlAxes, cubeAxes;
	
	float zoomScalar =  200f/height;      
	boolean cubeMode = true;
	
	public void setup() {
		ui =new UI(this, true);
		String path = sketchPath()+File.separator;
		loadedArmature = EWBKIO.LoadArmature_singlePrecision(path+"Humanoid_Holding_Item.arm");
		worlAxes = (Axes) loadedArmature.localAxes().getParentAxes(); 
		if(worlAxes == null) { 
			worlAxes = new Axes();
			loadedArmature.localAxes().setParent(worlAxes);
		}
		updatePinList();   	    
		cubeAxes = new Axes(); 		
		
		activePin = pins.get(pins.size()-1);
		
		loadedArmature.setPerformanceMonitor(true); //print performance stats
		
		//Tell the Bone class that all bones should draw their kusudamas.
		Bone.setDrawKusudamas(true);
		//Enable fancy multipass shading for translucent kusudamas. 
		Kusudama.enableMultiPass(true);

		/**
		 * The armature we're loading is already posed such that its hands touch
		 * a box. So all we need to do is , first
		 * move our box into the appropriate postion 
		 * */
		cubeAxes.translateTo(new PVector(-13,-27,32));
		cubeAxes.setRelativeToParent(worlAxes);
		/**
		 * and then specify that the  transformations of the left hand and right hand pins 
		 * should be computed relative to the axes of the cube we're drawing, 
		 * Thereby, any time we transform the parent cube's axes, the pins will follow.
		 */
		loadedArmature.getBoneTagged("left hand").getIKPin().getAxes().setParent(cubeAxes);
		loadedArmature.getBoneTagged("right hand").getIKPin().getAxes().setParent(cubeAxes);
		
		
	}


	public void draw() {
		
		if(mousePressed) {
			if(cubeMode) { 
				cubeAxes.translateTo(new PVector(ui.mouse.x, ui.mouse.y,cubeAxes.origin_().z));
			} else { 
				activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));
			}
			loadedArmature.IKSolver(loadedArmature.getRootBone());
		}else {			
			worlAxes.rotateAboutY(PI/500f, true);
		}    
		String additionalInstructions = "Hit the 'C' key to select or deselect the cube";  
		//decrease the numerator to increase the zoom. 
		zoomScalar = 200f/height;        
		ui.drawScene(zoomScalar, 10f, ()->drawHoldCube(), loadedArmature, additionalInstructions, activePin, cubeAxes, cubeMode);
	}

	public void drawHoldCube() {
		PGraphics currentDisplay = ui.getCurrentDrawSurface();
		if(ui.scene == currentDisplay) { 
			currentDisplay.fill(60,60,60);
			currentDisplay.strokeWeight(1);
			currentDisplay.stroke(255);
		} else {
			currentDisplay.fill(0,0,0,255);
			currentDisplay.emissive(0);
			currentDisplay.noStroke();
		}
		currentDisplay.pushMatrix();
		currentDisplay.applyMatrix(cubeAxes.getGlobalPMatrix());
		currentDisplay.box(40, 20, 20);
		currentDisplay.popMatrix();		
	}
	
	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		Axes axes = cubeMode ? cubeAxes  : (Axes) activePin.getAxes(); 
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

	public void recursivelyAddToPinnedList(ArrayList<IKPin> pins, Bone descendedFrom) {
		ArrayList<Bone> pinnedChildren = (ArrayList<Bone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
		for(Bone b : pinnedChildren) {
			IKPin pin = (IKPin) b.getIKPin();
			pins.add(pin);
		}
		for(Bone b : pinnedChildren) {
			ArrayList<Bone> children = b.getChildren(); 
			for(Bone b2 : children) {
				recursivelyAddToPinnedList(pins, b2);
			}
		}
	}


}


