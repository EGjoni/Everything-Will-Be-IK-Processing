import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import java.util.ArrayList;

Axes worldAxes;
Armature simpleArmature;
UI ui;

public void setup() {
	size(1200, 900, P3D);  
	simpleArmature = new Armature("example");
  setupVisualizationParams(simpleArmature);
  //USER CODE GOES BELOW THIS LINE: 

  
}

public void draw() {		
	if(mousePressed) {		
		activePin.translateTo(new PVector(ui.mouse.x, ui.mouse.y, activePin.getLocation_().z));	

    //THIS FUNCTION TELLS THE ARMATURE RUN THE IK SOLVER
		simpleArmature.IKSolver(simpleArmature.getRootBone());
	} else {
		worldAxes.rotateAboutY(PI/500f, true);
	}    
	ui.drawScene(0.5f, 30f, null, simpleArmature, null, activePin, null,  false);       
}

public void mouseWheel(MouseEvent event) {ui.mouseWheelFunctions(event);}
public void keyPressed() {ui.keyboardFunctions();}
public void setupVisualizationParams(Armature toVisualize) {
  ui = new UI(true); ui.initWorldFor(toVisualize);
}

ArrayList<IKPin> pins = new ArrayList<IKPin>();
IKPin activePin;
