import ewbik.processing.singlePrecision.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;
import sceneGraph.math.floatV.SGVec_3f;

import java.util.ArrayList;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

Armature simpleArmature;
Bone  rootBone, initialBone, 
secondBone, thirdBone, 
fourthBone, fifthBone, 
bFourthBone, bFifthBone, 
bSixthBone;

ArrayList<IKPin> pinnedBones = new ArrayList<IKPin>();
Bone activeBone;
IKPin activePin; 

public void setup() {
    size(1200, 900, P3D);
    simpleArmature = new Armature("example");
    
    //rotate and translate the armature into the view window.
        simpleArmature.localAxes().translateTo(new SGVec_3f(0, 200, 0));  
        simpleArmature.localAxes().rotateAboutZ(PI, true);
    
    initializeBones(); //create the bones
    setBoneConstraints(); //set some constratints on them
    updatePinList(); //add end effectors (and create a list of them the user can cycle through)
    activeBone = fifthBone;
    
}

public void draw() {
    setSceneAndCamera();   

    if(mousePressed) {
        activeBone.setPin(mouse);             
        simpleArmature.IKSolver(
                rootBone, //the bone the solver should start from 
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


public void initializeBones() {
    rootBone = simpleArmature.getRootBone();
    initialBone = new Bone(rootBone, "initial", 74f);
    secondBone = new Bone(initialBone, "nextBone", 86f);
    thirdBone = new Bone(secondBone, "anotherBone", 98f); 
    fourthBone = new Bone(thirdBone, "oneMoreBone", 70f);
    fifthBone = new Bone(fourthBone, "fifthBone", 80f);  

    bFourthBone = new Bone(thirdBone, "branchBone", 80f);
    bFifthBone = new Bone(bFourthBone, "nextBranch", 70f);
    bSixthBone = new Bone(bFifthBone, "leaf", 80f); 

    secondBone.rotAboutFrameZ(.4f);
    thirdBone.rotAboutFrameZ(.4f);

    bFourthBone.rotAboutFrameZ(-.5f);
    bFifthBone.rotAboutFrameZ(-1f);
    bSixthBone.rotAboutFrameZ(-.5f);
    initialBone.rotAboutFrameX(.01f);
}

public void setBoneConstraints() {    

    Kusudama firstConstraint = new Kusudama(initialBone);
    firstConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 1f);
    firstConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
    firstConstraint.setAxialLimits(0.1f,0.3f);
    firstConstraint.enable();
    initialBone.addConstraint(firstConstraint);

    Kusudama secondConstraint = new Kusudama(secondBone);
    secondConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),1f);
    secondConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
    secondConstraint.setAxialLimits(0.1f,0.3f);
    secondConstraint.enable();
    secondBone.addConstraint(secondConstraint);

    Kusudama thirdConstraint = new Kusudama(thirdBone);
    thirdConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 1f);
    thirdConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
    thirdConstraint.setAxialLimits(0.1f,0.3f);
    thirdConstraint.enable();
    thirdBone.addConstraint(thirdConstraint);

    Kusudama fourthConstraint = new Kusudama(fourthBone);
    fourthConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f), 1f);
    fourthConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
    fourthConstraint.setAxialLimits(0.1f,0.3f);
    fourthConstraint.enable();
    fourthBone.addConstraint(fourthConstraint);

    Kusudama fifthConstraint = new Kusudama(fifthBone);
    fifthConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),0.5f);
    fifthConstraint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
    fifthConstraint.setAxialLimits(0.1f, 0.3f);
    fifthConstraint.enable();
    fifthBone.addConstraint(fifthConstraint); 

    Kusudama bFourthConstraint = new Kusudama(bFourthBone);
    bFourthConstraint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),0.7f);
    bFourthConstraint.setAxialLimits(0.1f,0.3f);
    bFourthConstraint.enable();
    bFourthBone.addConstraint(bFourthConstraint);        
    
    Kusudama bSixthContstaint = new Kusudama(bSixthBone);
    bSixthContstaint.addLimitConeAtIndex(0, new PVector(.5f, 1f, 0f),0.5f);
    bSixthContstaint.addLimitConeAtIndex(1, new PVector(-.5f, 1f, 0f), 1f);
    bSixthContstaint.setAxialLimits(0.1f, 0.3f);
    bSixthContstaint.enable();
    bSixthBone.addConstraint(bSixthContstaint); 
    
    rootBone.enablePin();  
    fifthBone.enablePin();
    fifthBone.setPin(new PVector(-200, 0, 0));
    bSixthBone.enablePin();
    bSixthBone.setPin(new PVector(100, 50, 0));
}


PVector  mouse = new PVector(0,0,0);
public void drawBones() {
  simpleArmature.drawMe(this, 100, 30f);
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
   ArrayList<?> BoneList = simpleArmature.getBoneList();
   for(Object ob : BoneList) {
     Bone b = (Bone)ob;
     String boneAngles = "";
     float[] angleArr = b.getXYZAngle();
     boneAngles += "  ( " + degrees(angleArr[0]) + ",   " + degrees(angleArr[1]) + ",   " + degrees(angleArr[2]) + "  )";
     fill(0);
     text(boneAngles,b.getBase().x, b.getBase().y); 
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

public void updatePinList() {
    pinnedBones.clear();
    recursivelyAddToPinnedList(pinnedBones, simpleArmature.getRootBone());
}

public void recursivelyAddToPinnedList(ArrayList<IKPin> pins, Bone descendedFrom) {
    ArrayList<Bone> pinnedChildren = (ArrayList<Bone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
    for(Bone b : pinnedChildren) {
        pins.add(b.getIKPin());
    }
    for(Bone b : pinnedChildren) {
        ArrayList<Bone> children = b.getChildren(); 
        for(Bone b2 : children) {
            recursivelyAddToPinnedList(pins, b2);
        }
    }
}