import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;
import sceneGraph.math.floatV.SGVec_3f;

import java.util.ArrayList;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

dArmature simpledArmature;
dBone  rootdBone, initialdBone, 
seconddBone, thirddBone, 
fourthdBone, fifthdBone, 
bFourthdBone, bFifthdBone, 
bSixthdBone;

ArrayList<dIKPin> pinneddBones = new ArrayList<dIKPin>();
dBone activeBone;
dIKPin activePin; 

public void setup() {
    size(1200, 900, P3D);
    simpledArmature = new dArmature("example");
    
    //rotate and translate the armature into the view window.
        simpledArmature.localAxes().translateTo(new DVector(0, 200, 0));  
        simpledArmature.localAxes().rotateAboutZ(PI, true);
    
    initializedBones(); //create the bones
    setdBoneConstraints(); //set some constratints on them
    updatePinList(); //add end effectors (and create a list of them the user can cycle through)
    activeBone = fifthdBone;
    
}

public void draw() {
    setSceneAndCamera();   

    if(mousePressed) {
        activeBone.setPin(mouse);             
        simpledArmature.IKSolver(
                rootdBone, //the bone the solver should start from 
                0.05f, // dampening parameter (in radians), determines the maximum amount a bone can rotate per iteration step. 
                // keeping this value low will result in higher quality poses, but those poses will take more iterations to converge. 
                10, //inverse weighting helps prevent bones near the target from doing all of the work 
                //so  you don't need to rely on small dampening factors mixed with high iteration counts 
                //to get natural looking poses, but it makes armatures with multiple end effectors more unstable. 
                // since this armature has multiple end effectors, we leave this off.
                1 //just leave this set to 1, honestly.
                );
    }
    
    drawInstructions(); 
    drawBones();  
}


public void initializedBones() {
    rootdBone = simpledArmature.getRootBone();
    initialdBone = new dBone(rootdBone, "initial", 74f);
    seconddBone = new dBone(initialdBone, "nextdBone", 86f);
    thirddBone = new dBone(seconddBone, "anotherdBone", 98f); 
    fourthdBone = new dBone(thirddBone, "oneMoredBone", 70f);
    fifthdBone = new dBone(fourthdBone, "fifthdBone", 80f);  

    bFourthdBone = new dBone(thirddBone, "branchdBone", 80f);
    bFifthdBone = new dBone(bFourthdBone, "nextBranch", 70f);
    bSixthdBone = new dBone(bFifthdBone, "leaf", 80f); 

    seconddBone.rotAboutFrameZ(.4f);
    thirddBone.rotAboutFrameZ(.4f);

    bFourthdBone.rotAboutFrameZ(-.5f);
    bFifthdBone.rotAboutFrameZ(-1f);
    bSixthdBone.rotAboutFrameZ(-.5f);
    initialdBone.rotAboutFrameX(.01f);
}

public void setdBoneConstraints() {    

    dKusudama firstConstraint = new dKusudama(initialdBone);
    firstConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 1f);
    firstConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
    firstConstraint.setAxialLimits(0.1f,0.3f);
    firstConstraint.enable();
    initialdBone.addConstraint(firstConstraint);

    dKusudama secondConstraint = new dKusudama(seconddBone);
    secondConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),1f);
    secondConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
    secondConstraint.setAxialLimits(0.1f,0.3f);
    secondConstraint.enable();
    seconddBone.addConstraint(secondConstraint);

    dKusudama thirdConstraint = new dKusudama(thirddBone);
    thirdConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 1f);
    thirdConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
    thirdConstraint.setAxialLimits(0.1f,0.3f);
    thirdConstraint.enable();
    thirddBone.addConstraint(thirdConstraint);

    dKusudama fourthConstraint = new dKusudama(fourthdBone);
    fourthConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f), 1f);
    fourthConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
    fourthConstraint.setAxialLimits(0.1f,0.3f);
    fourthConstraint.enable();
    fourthdBone.addConstraint(fourthConstraint);

    dKusudama fifthConstraint = new dKusudama(fifthdBone);
    fifthConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),0.5f);
    fifthConstraint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
    fifthConstraint.setAxialLimits(0.1f, 0.3f);
    fifthConstraint.enable();
    fifthdBone.addConstraint(fifthConstraint); 

    dKusudama bFourthConstraint = new dKusudama(bFourthdBone);
    bFourthConstraint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),0.7f);
    bFourthConstraint.setAxialLimits(0.1f,0.3f);
    bFourthConstraint.enable();
    bFourthdBone.addConstraint(bFourthConstraint);        
    
    dKusudama bSixthContstaint = new dKusudama(bSixthdBone);
    bSixthContstaint.addLimitConeAtIndex(0, new DVector(.5f, 1f, 0f),0.5f);
    bSixthContstaint.addLimitConeAtIndex(1, new DVector(-.5f, 1f, 0f), 1f);
    bSixthContstaint.setAxialLimits(0.1f, 0.3f);
    bSixthContstaint.enable();
    bSixthdBone.addConstraint(bSixthContstaint); 
    
    rootdBone.enablePin();  
    fifthdBone.enablePin();
    fifthdBone.setPin(new DVector(-200, 0, 0));
    bSixthdBone.enablePin();
    bSixthdBone.setPin(new DVector(100, 50, 0));
}


DVector  mouse = new DVector(0,0,0);
public void drawBones() {
  simpledArmature.drawMe(this, 100, 30f);
}


public void setSceneAndCamera() {
  background(160, 100, 100);
  directionalLight(248, 248, 248, 0, 10, -10);
  mouse.x =  mouseX - (width/2); mouse.y = mouseY - (height/2);
  camera(cameraPosition, lookAt, up);
  ortho(-width/2, width/2, -height/2, height/2, -10000, 10000);     
}

public void drawInstructions() {
  fill(0);
  String instructionText =
      "-Click and drag to move the selected pin.\n"
       +"-To select a different pin, use the Up and Down arrows.\n"
      + "-Use the mouse wheel to rotate the pin about its (red) Y axis.\n" 
      + "-Hold shift while using the mouse wheel to rotate the pin about its (blue) Z axis. \n"
      + "-Hold ctrl while using the mouse wheel to rotate the pin about its (green) X axis";
  
  text(instructionText, (-width/2f) + 20f, (-height/2f) + 20f); 
  textSize(14f);
  
}

public void drawBoneInfo() {
   ArrayList<?> dBoneList = simpledArmature.getBoneList();
   for(Object ob : dBoneList) {
     dBone b = (dBone)ob;
     String boneAngles = "";
     double[] angleArr = b.getXYZAngle();
     boneAngles += "  ( " + degrees((float)angleArr[0]) + ",   " + degrees((float)angleArr[1]) + ",   " + degrees((float)angleArr[2]) + "  )";
     fill(0);
     text(boneAngles, (float)b.getBase().x, (float)b.getBase().y); 
  }  
}


public void mouseWheel(MouseEvent event) {
        float e = event.getCount();
        if(event.isShiftDown()) {
            activeBone.getIKPin().getAxes().rotateAboutZ(e/TAU, true);
        } else {
            activeBone.getIKPin().getAxes().rotateAboutY(e/TAU, true);
        }
        activeBone.getIKPin().solveIKForThisAndChildren();        
}

public void keyPressed() {
    if (key == CODED) {
        if (keyCode == DOWN) {            
            int currentPinIndex =(pinneddBones.indexOf(activePin) + 1) % pinneddBones.size();
            activePin  = pinneddBones.get(currentPinIndex);
            activeBone = (dBone)activePin.forBone();
        } else if (keyCode == UP) {
            int idx = pinneddBones.indexOf(activePin);
            int currentPinIndex =  (pinneddBones.size()-1) -(((pinneddBones.size()-1) - (idx - 1)) % pinneddBones.size());
            activePin  = pinneddBones.get(currentPinIndex);
            activeBone = (dBone)activePin.forBone();
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

public void updatePinList() {
    pinneddBones.clear();
    recursivelyAddToPinnedList(pinneddBones, simpledArmature.getRootBone());
}

public void recursivelyAddToPinnedList(ArrayList<dIKPin> pins, dBone descendedFrom) {
    ArrayList<dBone> pinnedChildren = (ArrayList<dBone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
    for(dBone b : pinnedChildren) {
        pins.add((dIKPin)b.getIKPin());
    }
    for(dBone b : pinnedChildren) {
        ArrayList<dBone> children = b.getChildren(); 
        for(dBone b2 : children) {
            recursivelyAddToPinnedList(pins, b2);
        }
    }
}