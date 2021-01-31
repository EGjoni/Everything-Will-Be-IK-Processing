import data.EWBIKLoader;
import ewbik.processing.EWBKIO;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.Vec3d;
String delim = File.separator;

dArmature loadedArmature;
//decrease the numerator to increase the zoom. 
float zoomScalar = 1.7f/height;
UI ui; 

public void setup() {
	size(1200, 900, P3D);
	ui = new UI(false);
	String path = sketchPath()+File.separator;
	loadedArmature = EWBKIO.LoadArmature_doublePrecision(path+ui.pathUp+delim+"armatures"+delim+"Humanoid.arm");
	worldAxes = (dAxes) loadedArmature.localAxes().getParentAxes(); 
	if(worldAxes == null) { 
		worldAxes = new dAxes();
		loadedArmature.localAxes().setParent(worldAxes);
	}
	ui.updatePinList(loadedArmature); 
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

public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(dArmature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}

ArrayList<dIKPin> pins = new ArrayList<dIKPin>();
dIKPin activePin;
dAxes worldAxes;
