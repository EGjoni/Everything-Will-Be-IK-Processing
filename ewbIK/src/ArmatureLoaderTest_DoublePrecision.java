

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import data.EWBIKLoader;
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
import processing.core.PVector;
import processing.event.MouseEvent;
import java.io.File;


public class ArmatureLoaderTest_DoublePrecision extends PApplet{

	public static void main(String[] args) {
		PApplet.main("ArmatureLoaderTest_DoublePrecision");
	}

	public void settings(){
		size(1200, 900, P3D);
	}

	char delim = File.pathSeparatorChar;
	dArmature loadedArmature;
	ArrayList<dIKPin> pins = new ArrayList<>();
	UI ui;

	dIKPin activePin; 
	dAxes worldAxes;
	//decrease the numerator to increase the zoom. 
	float zoomScalar = 1.7f/height;

	public void setup() {
		ui =new UI(this, true);

		String path = sketchPath()+File.separator;
		loadedArmature = EWBKIO.LoadArmature_doublePrecision(path+"Humanoid.arm");
		worldAxes = (dAxes) loadedArmature.localAxes().getParentAxes(); 
		if(worldAxes == null) { 
			worldAxes = new dAxes();
			loadedArmature.localAxes().setParent(worldAxes);
		}
		updatePinList();   	    
		activePin = pins.get(pins.size()-1);
		loadedArmature.setPerformanceMonitor(true); //print performance stats

		dBone.setDrawKusudamas(true);
	}


	public void draw() {
		zoomScalar = 200f/height;        
		if(mousePressed) {
			ui.mouse.z = (float) activePin.getAxes().origin_().z;
			activePin.translateTo(new DVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));
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


