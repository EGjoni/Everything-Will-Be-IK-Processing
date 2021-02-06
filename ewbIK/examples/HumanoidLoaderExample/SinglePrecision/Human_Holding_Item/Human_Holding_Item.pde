import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import data.EWBIKLoader;
import ewbik.processing.EWBKIO;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import math.floatV.SGVec_3f;
import java.io.File;
String delim = File.separator;

Armature loadedArmature;
UI ui; 
Axes cubeAxes;

float zoomScalar =  200f/height;      
boolean cubeMode = true;

public void setup() {
	size(1200, 900, P3D);
	ui =new UI(true);
	String path = sketchPath()+File.separator;
	loadedArmature = EWBKIO.LoadArmature_singlePrecision(path+ui.pathUp+delim+"armatures"+delim+"Humanoid_Holding_Item.arm");
	worldAxes = (Axes) loadedArmature.localAxes().getParentAxes(); 
	if(worldAxes == null) { 
		worldAxes = new Axes();
		loadedArmature.localAxes().setParent(worldAxes);
	}
	ui.updatePinList(loadedArmature);   	    
	cubeAxes = new Axes();
	/**
	 * The armature we're loading is already posed such that its hands touch
	 * a box. So all we need to do is , first
	 * move our box into the appropriate postion 
	 * */
	cubeAxes.translateTo(new PVector(-13,-27,32));
	cubeAxes.setRelativeToParent(worldAxes);
	/**
	 * and then specify that the  transformations of the left hand and right hand pins 
	 * should be computed relative to the axes of the cube we're drawing, 
	 * Thereby, any time we transform the parent cube's axes, the pins will follow.
	 */
	loadedArmature.getBoneTagged("left hand").getIKPin().getAxes().setParent(cubeAxes);
	loadedArmature.getBoneTagged("right hand").getIKPin().getAxes().setParent(cubeAxes);

  loadedArmature.setPerformanceMonitor(true); //print performance stats
  //Tell the Bone class that all bones should draw their kusudamas.
  Bone.setDrawKusudamas(true);
  //Enable fancy multipass shading for translucent kusudamas. 
  Kusudama.enableMultiPass(true);
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
public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(Armature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}

ArrayList<IKPin> pins = new ArrayList<IKPin>();
IKPin activePin;
Axes worldAxes;
