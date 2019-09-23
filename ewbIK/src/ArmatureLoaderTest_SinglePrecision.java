

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import data.EWBIKLoader;
import ewbik.processing.EWBKIO;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;
import sceneGraph.math.floatV.SGVec_3f;

public class ArmatureLoaderTest_SinglePrecision extends PApplet{
	
	public static void main(String[] args) {
		PApplet.main("ArmatureLoaderTest_SinglePrecision");
	}

	public void settings(){
        size(1200, 900, P3D);
    }
	
	Armature loadedArmature;
	ArrayList<IKPin> pinnedBones = new ArrayList<>();
	
	Bone activeBone;
	IKPin activePin; 
	Axes worldAxes;

	//decrease the numerator to increase the zoom. 
	float zoomScalar = 2f/height;
	
	public void setup() {
		size(1200, 900, P3D);
		  String path = sketchPath()+File.separator;
		  loadedArmature = EWBKIO.LoadArmature_singlePrecision(path+"testArm.arm");          
		  updatePinList();   
		  
		  worldAxes = new Axes();     
		  updatePinList();       
		  setUpright(); //fix orientation (for viewing purposes)
		  
		  activePin = pinnedBones.get(pinnedBones.size()-1);
		  activeBone = (Bone) activePin.forBone();
		  
		  loadedArmature.setPerformanceMonitor(true); //print performance stats
		  
		  loadedArmature.setDefaultDampening(0.3f); //decrease this value for higher pose quality, increase it for performance.
		  loadedArmature.setDefaultIterations(10); //increase this value for faster convergence at the cost of computational resources. 
		}

		float orthoHeight = height;
		float orthoWidth = width;

		public void draw() {
		   setSceneAndCamera();   
		    if(mousePressed) {
		        mouse.z = activePin.getAxes().origin_().z;
		        activeBone.setPin(mouse); 
		        activePin.solveIKForThisAndChildren();
		    } else {
		        worldAxes.rotateAboutY(PI/500f, true);
		    }
		    
		    drawInstructions();  
		    //drawBoneInfo();
		    drawBones();
		}

		PVector  mouse = new PVector(0,0,0);
		public void drawBones() {
		  loadedArmature.drawMe(this, 100, ((orthoHeight / height)*10f));
		}

		public void drawInstructions() {
		  fill(0);
		  String instructionText =
		      "-Click and drag to move the selected pin.\n"
		       +"-To select a different pin, use the Up and Down arrows.\n"
		      + "-Use the mouse wheel to rotate the pin about its (red) Y axis.\n" 
		      + "-Hold shift while using the mouse wheel to rotate the pin about its (blue) Z axis. \n"
		      + "-Hold ctrl while using the mouse wheel to rotate the pin about its (green) X axis";
		  
		  text(instructionText, (-orthoWidth/2f) + (orthoWidth/50f), (-orthoHeight/2f) + (orthoHeight/40f)); 
		  textSize(zoomScalar*14f);
		  
		}

		public void drawBoneInfo() {
		   ArrayList<?> BoneList = loadedArmature.getBoneList();
		   for(Object ob : BoneList) {
		     Bone b = (Bone)ob;
		     String boneAngles = "";
		     float[] angleArr = b.getXYZAngle();
		     boneAngles += "  ( " + degrees(angleArr[0]) + ",   " + degrees(angleArr[1]) + ",   " + degrees(angleArr[2]) + "  )";
		     fill(0);
		     text(boneAngles,b.getBase().x, b.getBase().y); 
		  }  
		}

		public void setSceneAndCamera() {
		  background(160, 100, 100);
		  directionalLight(248, 248, 248, 0, 10, -10);
		  orthoHeight = height*zoomScalar;
		  orthoWidth = ((float)width/(float)height) * orthoHeight; 
		  mouse.x =  (mouseX - (width/2f)) * (orthoWidth/width); mouse.y = (mouseY - (height/2f)) *  (orthoHeight/height);
		  camera(cameraPosition, lookAt, up);
		  ortho(-orthoWidth/2f, orthoWidth/2f, -orthoHeight/2f, orthoHeight/2f, -10000, 10000); 
		}

		public void mouseWheel(MouseEvent event) {
		    float e = event.getCount();
		    if(event.isShiftDown()) {
		      activeBone.getIKPin().getAxes().rotateAboutZ(e/TAU, true);
		    } else if (event.isControlDown()) {
		      activeBone.getIKPin().getAxes().rotateAboutX(e/TAU, true);
		    }  else {
		     activeBone.getIKPin().getAxes().rotateAboutY(e/TAU, true);
		    }
		    activeBone.getIKPin().solveIKForThisAndChildren();
		   
		}

		public void keyPressed() {
		    if (key == CODED) {
		      if (keyCode == DOWN) {
		        
		        int currentPinIndex =(pinnedBones.indexOf(activePin) + 1) % pinnedBones.size();
		        activePin  = pinnedBones.get(currentPinIndex);
		        activeBone = (Bone)activePin.forBone();
		      } else if (keyCode == UP) {
		      int idx = pinnedBones.indexOf(activePin);
		       int currentPinIndex =  (pinnedBones.size()-1) -(((pinnedBones.size()-1) - (idx - 1)) % pinnedBones.size());
		       activePin  = pinnedBones.get(currentPinIndex);
		        activeBone = (Bone)activePin.forBone();
		      } 
		    }
		  }

		public void printXY(PVector p) {
		  println(screenX(p.x, p.y, p.z)
		      +", " + screenY(p.x, p.y, p.z));
		}
		public void line(PVector p1, PVector p2) {
		  line(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
		}

		public void point(PVector p) {
		  point(p.x, p.y, p.z);
		}
		public void point(SGVec_3f p) {
		  point(p.x, p.y, p.z);
		}


		PVector cameraPosition = new PVector(0, 0, 70); 
		PVector lookAt = new PVector(0, 0, 0);
		PVector up = new PVector(0, 1, 0);

		public void camera(PVector cp, PVector so, PVector up) {
		  camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
		}

		public void recursivelyAddToPinnedList(ArrayList<IKPin> pins, Bone descendedFrom) {
		  ArrayList<Bone> pinnedChildren = (ArrayList<Bone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
		  for(Bone b : pinnedChildren) {
		    IKPin pin = b.getIKPin();
		    pins.add(pin);
		  }
		  for(Bone b : pinnedChildren) {
		    ArrayList<Bone> children = b.getChildren(); 
		    for(Bone b2 : children) {
		      recursivelyAddToPinnedList(pins, b2);
		    }
		  }    
		}

		public void updatePinList() {
		  pinnedBones.clear();
		  recursivelyAddToPinnedList(pinnedBones, loadedArmature.getRootBone());
		}


		//processing has a different rendering convention than the framework I've switched to
		// this function just turns the armature right side up to account for the difference
		public void setUpright() {
		  loadedArmature.localAxes().setParent(worldAxes);    
		  for(IKPin pin : pinnedBones) {
		    pin.getAxes().setParent(worldAxes);
		  }
		  worldAxes.rotateAboutZ(PI, true);
		  worldAxes.translateByGlobal(new SGVec_3f(0f, 5f, 0f));
		}
	
}
	
	


