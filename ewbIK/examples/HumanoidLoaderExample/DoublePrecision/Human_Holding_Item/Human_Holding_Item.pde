import java.util.ArrayList;
import java.util.Collection;
import data.EWBIKLoader;
import ewbik.processing.EWBKIO;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.*;
import math.floatV.*;
import java.io.File;
String delim = File.separator;

dArmature loadedArmature;
ArrayList<dIKPin> pins = new ArrayList();
UI ui;

dIKPin activePin; 
dAxes worldAxes, cubeAxes;

float zoomScalar =  200f/height;      
boolean cubeMode = true;

public void setup() {
	size(1200, 900, P3D);
	ui =new UI(true);
	String path = sketchPath()+delim;
	loadedArmature = EWBKIO.LoadArmature_doublePrecision(path+ui.pathUp+"armatures"+delim+"Humanoid_Holding_Item.arm");
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
	dBone.setDrawKusudamas(true);
	//Enable fancy multipass shading for translucent kusudamas. 
	dKusudama.enableMultiPass(true);

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


}


public void draw() {

	if(mousePressed) {
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

  //Next two lines are for rendering things, you can ignore them.
  Runnable drawCube = new Runnable() {public void run() {drawHoldCube();}};
	ui.drawScene(zoomScalar, 10f, drawCube, loadedArmature, additionalInstructions, activePin, cubeAxes, cubeMode);
}

public void drawHoldCube() {
	PGraphics currentDisplay = ui.getCurrentDrawSurface();
	if(ui.display == currentDisplay) { 
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
