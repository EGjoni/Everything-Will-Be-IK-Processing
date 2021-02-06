import java.io.File;
import data.EWBIKLoader;
import ewbik.processing.EWBKIO;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
String delim = File.separator;

Armature loadedArmature;
UI ui;
float zoomScalar = 300f/height;
Axes worldAxes;

public void setup() {
	size(1200, 900, P3D);
  String path = sketchPath()+delim;
	ui =new UI(false);	
	loadedArmature = EWBKIO.LoadArmature_singlePrecision(path+ui.pathUp+delim+"armatures"+delim+"Humanoid.arm");
	worldAxes = (Axes) loadedArmature.localAxes().getParentAxes(); 
	if(worldAxes == null) { 
		worldAxes = new Axes();
		loadedArmature.localAxes().setParent(worldAxes);
	}

	ui.updatePinList(loadedArmature);   	    
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


public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(Armature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}

ArrayList<IKPin> pins = new ArrayList<IKPin>();
IKPin activePin;
