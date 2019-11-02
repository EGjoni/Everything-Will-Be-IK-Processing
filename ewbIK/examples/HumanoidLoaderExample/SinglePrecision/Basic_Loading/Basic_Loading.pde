import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import data.EWBIKLoader;
import ewbik.processing.EWBKIO;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import math.floatV.SGVec_3f;
String delim = File.separator;

Armature loadedArmature;
ArrayList<IKPin> pins = new ArrayList();
UI ui;

IKPin activePin; 
Axes worldAxes;
//decrease the numerator to increase the zoom. 
float zoomScalar = 300f/height;

public void setup() {
	size(1200, 900, P3D);
String path = sketchPath()+delim;
println(path);
	ui =new UI(false);

	
	loadedArmature = EWBKIO.LoadArmature_singlePrecision(path+ui.pathUp+delim+"armatures"+delim+"Humanoid.arm");
	worldAxes = (Axes) loadedArmature.localAxes().getParentAxes(); 
	if(worldAxes == null) { 
		worldAxes = new Axes();
		loadedArmature.localAxes().setParent(worldAxes);
	}

	updatePinList();   	    
	activePin = pins.get(pins.size()-1);

	loadedArmature.setPerformanceMonitor(true); //print performance stats

	Bone.setDrawKusudamas(true);
}

public void draw() {
	zoomScalar = 200f/height;        
	if(mousePressed) {
		ui.mouse.z = (float) activePin.getAxes().origin_().z;
		activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	
		loadedArmature.IKSolver(loadedArmature.getRootBone());
	} else {
		worldAxes.rotateAboutY(PI/500f, true);
	}    
	ui.drawScene(zoomScalar, 10f, null, loadedArmature, null, activePin, null,  false);       
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
