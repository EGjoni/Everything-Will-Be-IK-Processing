

import processing.core.*;
import processing.event.MouseEvent;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import ewbik.processing.singlePrecision.Bone;

import java.util.ArrayList;

public class IKTest_DoublePrecision extends PApplet{
    public static void main(String[] args) {
        PApplet.main("iktest.IKTest_DoublePrecision");
    }

    public void settings(){
        size(1200, 900, P3D);
    }

    dArmature simpleArmature;
    dBone  rootBone, initialBone, 
          secondBone, thirdBone, 
          fourthBone, fifthBone, 
          bFourthBone, bFifthBone, 
          bSixthBone;

    dBone activeBone;
    
    public void setup() {
		simpleArmature = new dArmature("example");
		simpleArmature.localAxes().translateTo(new DVector(0, 200, 0));
		simpleArmature.localAxes().rotateAboutZ(PI, true);
		initializeBones(); 
		setBoneConstraints();
		 activeBone = fifthBone;		
	}

      public void draw() {
    	  background(160, 100, 100);
  			directionalLight(248, 248, 248, 0, 10, -10);
  			mouse.x =  mouseX - (width/2); mouse.y = mouseY - (height/2);
  			camera(cameraPosition, lookAt, up);
  			ortho(-width/2, width/2, -height/2, height/2, -10000, 10000);
  			drawInstructions(); 
  			drawBones();  

  			if(mousePressed) {
  				activeBone.setPin(mouse);             
  				simpleArmature.IKSolver(
  					rootBone, //the bone the solver should start from 
  					0.1f, // dampening parameter (in radians), determines the maximum amount a bone can rotate per iteration step. 
  					// keeping this value low will result in higher quality poses, but those poses will take more iterations to converge. 
  					10, //inverse weighting helps prevent bones near the target from doing all of the work 
  					//so  you don't need to rely on small dampening factors mixed with high iteration counts 
  					//to get natural looking poses, but it makes armatures with multiple end effectors more unstable. 
  					// since this armature has multiple end effectors, we leave this off.
  					1 //just leave this set to 1, honestly.
  					);
  				}
  			// simpleArmature.ambitiousIKSolver(rootBone, 0.1, 20);
        	}
      


    public void initializeBones() {
    	rootBone = simpleArmature.getRootBone();
		initialBone = new dBone(rootBone, "initial", 74f);
		secondBone = new dBone(initialBone, "nextBone", 86f);
		thirdBone = new dBone(secondBone, "anotherBone", 98f); 
		fourthBone = new dBone(thirdBone, "oneMoreBone", 70f);
		fifthBone = new dBone(fourthBone, "fifthBone", 80f);  

		bFourthBone = new dBone(thirdBone, "branchBone", 80f);
		bFifthBone = new dBone(bFourthBone, "nextBranch", 70f);
		bSixthBone = new dBone(bFifthBone, "leaf", 80f); 

		secondBone.rotAboutFrameZ(.4f);
		thirdBone.rotAboutFrameZ(.4f);

		bFourthBone.rotAboutFrameZ(-.5f);
		bFifthBone.rotAboutFrameZ(-1f);
		bSixthBone.rotAboutFrameZ(-.5f);
		initialBone.rotAboutFrameX(.01f);
        
      rootBone.enablePin();  
      fifthBone.enablePin();
      bSixthBone.enablePin();
    }

    public void setBoneConstraints() {    
    	
    	dKusudama firstConstraint = new dKusudama(initialBone);
		firstConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 1f);
		firstConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
		firstConstraint.setAxialLimits(0.1f,0.3f);
		firstConstraint.enable();
		initialBone.addConstraint(firstConstraint);

		dKusudama secondConstraint = new dKusudama(secondBone);
		secondConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),1f);
		secondConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
		secondConstraint.setAxialLimits(0.1f,0.3f);
		secondConstraint.enable();
		secondBone.addConstraint(secondConstraint);

		dKusudama thirdConstraint = new dKusudama(thirdBone);
		thirdConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 1f);
		thirdConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
		thirdConstraint.setAxialLimits(0.1f,0.3f);
		thirdConstraint.enable();
		thirdBone.addConstraint(thirdConstraint);

		dKusudama fourthConstraint = new dKusudama(fourthBone);
		fourthConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 1f);
		fourthConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
		fourthConstraint.setAxialLimits(0.1f,0.3f);
		fourthConstraint.enable();
		fourthBone.addConstraint(fourthConstraint);

		dKusudama fifthConstraint = new dKusudama(fifthBone);
		fifthConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),1f);
		fifthConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
		fifthConstraint.setAxialLimits(0.1f, 0.3f);
		fifthConstraint.enable();
		fifthBone.addConstraint(fifthConstraint); 

		dKusudama bFourthConstraint = new dKusudama(bFourthBone);
		bFourthConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),1f);
		bFourthConstraint.setAxialLimits(0.1f,0.3f);
		bFourthConstraint.enable();
		bFourthBone.addConstraint(bFourthConstraint);        
		
		rootBone.enablePin();  
		fifthBone.enablePin();
		fifthBone.setPin(new DVector(-200, 0, 0));
		bSixthBone.enablePin();
		bSixthBone.setPin(new DVector(100, 50, 0));
        
      }
      
    
	public void mouseWheel(MouseEvent event) {
		  float e = event.getCount();
		  activeBone.getIKPin().getAxes().rotateAboutY(e/TAU, true);
		  activeBone.getIKPin().solveIKForThisAndChildren();
		  println(e);
	}
      
      DVector mouse = new DVector(0,0,0);
      public void drawBones() {
    	  ArrayList<?> boneList = simpleArmature.getBoneList();
  			for(Object b : boneList) {
  				drawdBoneInfo(((dBone)b));
  			}   		
  		simpleArmature.drawMe(this, 100, 30f);
      }
      
      public void drawdBoneInfo(dBone bone) {
        if(bone.isPinned()) {
          strokeWeight(10); 
          stroke(255,0,0); 
          point(bone.getPinLocation());
        }
        
        String boneAngles = "";
        try {
        double[] angleArr = bone.getXYZAngle();
         boneAngles += " D ( " + degrees((float)angleArr[0]) + ",   " + degrees((float)angleArr[1]) + ",   " + degrees((float)angleArr[2]) + "  )";
        fill(0);
        text(boneAngles,(float)bone.getBase().x, (float)bone.getBase().y); 
        } catch (Exception e) {
        }        
      }
      
      public void drawInstructions() {
  		fill(0);
  		String instructionText =
  				"-Click and drag to move the selected pin.\n"
  				 +"-To select a different pin, use the Up and Down arrows.\n"
  				+ "-Use the mouse wheel to rotate the pin about its (red) Y axis.\n" 
  				+ "-Hold shift while using the mouse wheel to rotate the pin about its (blue) Z axis";
  		text(instructionText, (-width/2) +10, (-height/2) + 50); 
  		
  	}

    public void printXY(DVector pd) {
    	PVector p = pd.toPVec();
      println(screenX(p.x, p.y, p.z)
        +", " + screenY(p.x, p.y, p.z));
    }
    public void line(DVector p1, DVector p2) {
    	PVector p1f = p1.toPVec();
    	PVector p2f = p2.toPVec(); 
      line(p1f.x, p1f.y, p1f.z, p2f.x, p2f.y, p2f.z);
    }

    public void point(DVector pd) {
    	PVector p = pd.toPVec();
      point(p.x, p.y, p.z);
    }

    DVector cameraPosition = new DVector(0, 0, 70); 
    DVector lookAt = new DVector(0, 0, 0);
    DVector up = new DVector(0, 1, 0);

    public void camera(DVector cpd, DVector sod, DVector upd) {
    	PVector cp = cpd.toPVec(); 
    	PVector so = sod.toPVec(); 
    	PVector up = upd.toPVec();
      camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
    }

}
