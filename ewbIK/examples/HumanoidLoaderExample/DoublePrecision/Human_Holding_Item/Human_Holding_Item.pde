import ewbik.processing.EWBKIO;
import data.EWBIKSaver;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.Vec3d;

String delim = File.separator;
dArmature loadedArmature;
UI ui;
float zoomScalar =  200f/height;      
boolean cubeMode = true;
dAxes cubeAxes;

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
	ui.updatePinList(loadedArmature);   	    
	cubeAxes = new dAxes();	
	/**
	 * The armature we're loading is already posed such that its hands touch
	 * a box. So all we need to do is , first
	 * move our box into the appropriate postion **/
	cubeAxes.translateTo(new DVector(-13,-27,32));
	cubeAxes.setRelativeToParent(worldAxes);
	/**
	 * and then specify that the  transformations of the left hand and right hand pins 
	 * should be computed relative to the axes of the cube we're drawing, 
	 * Thereby, any time we transform the parent cube's axes, the pins will follow.**/
	loadedArmature.getBoneTagged("left hand").getIKPin().getAxes().setParent(cubeAxes);
	loadedArmature.getBoneTagged("right hand").getIKPin().getAxes().setParent(cubeAxes);
  loadedArmature.setPerformanceMonitor(true); //print performance stats
  //Tell the Bone class that all bones should draw their kusudamas.
  dBone.setDrawKusudamas(true);
  //Enable fancy multipass shading for translucent kusudamas. 
  dKusudama.enableMultiPass(true);

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

public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(dArmature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}
ArrayList<dIKPin> pins = new ArrayList<dIKPin>();
dIKPin activePin;
dAxes worldAxes;
