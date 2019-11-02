/*

Copyright (c) 2015 Eron Gjoni

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated documentation files (the "Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 

 */

package ewbik.processing.singlePrecision;


import java.util.ArrayList;

import IK.IKExceptions.NullParentForBoneException;
import IK.floatIK.AbstractArmature;
import IK.floatIK.AbstractBone;
import IK.floatIK.AbstractIKPin;
import ewbik.processing.doublePrecision.sceneGraph.DVector;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PVector;
import math.floatV.AbstractAxes;
import math.floatV.Rot;
import math.floatV.SGVec_3f;
import math.floatV.Vec3f;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractBone. Please refer to the {@link AbstractBone AbstractBone docs.} 
 */	
public class Bone extends AbstractBone {
	public static int renderMode = 1; 
	public static boolean drawKusudamas = false;
	//default constructor required for file loader to work. 
	public Bone() {};	
	/**
	 * 
	 * @param par the parent bone for this bone
	 * @param tipHeading the orienational heading of this bone (global vs relative coords specified in coordinateType)
	 * @param rollHeading axial rotation heading of the bone (it's z-axis) 
	 * @param inputTag some user specified name for the bone, if desired 
	 * @param inputBoneHeight bone length 
	 * @param coordinateType
	 * @throws NullParentForBoneException
	 */
	public Bone (Bone par, //parent bone
			PVector tipHeading, //the orienational heading of this bone (global vs relative coords specified in coordinateType)
			PVector  rollHeading, //axial rotation heading of the bone (it's z-axis) 
			String inputTag,	 //some user specified name for the bone, if desired 
			float inputBoneHeight, //bone length 
			frameType coordinateType							
			) throws NullParentForBoneException {
		super(
				par, 
				Axes.toSGVec(tipHeading), 
				Axes.toSGVec(rollHeading),
				inputTag, 
				inputBoneHeight, 
				coordinateType);
	}	

	/**
	 * 
	 * @param armature  the parent armature for this bone
	 * @param tipHeading the orienational heading of this bone (global vs relative coords specified in coordinateType)
	 * @param rollHeading axial rotation heading of the bone (it's z-axis) 
	 * @param inputTag some user specified name for the bone, if desired 
	 * @param boneHeight bone length 
	 * @param coordinateType
	 * @throws NullParentForBoneException
	 */
	public Bone(
			AbstractArmature armature, 
			PVector  tipHeading, 
			PVector  rollHeading, 
			String inputTag, 
			float boneHeight,
			frameType coordinateType) {
		super(
				armature, 
				Axes.toSGVec(tipHeading), 
				Axes.toSGVec(rollHeading),
				inputTag, 
				boneHeight, 
				coordinateType);
	}


	/** 
	 * Creates a new bone of specified length emerging from the parentBone. 
	 * The new bone extends in the same exact direction as the parentBone. 
	 * You can then manipulate its orientation using something like 
	 * rotAboutFrameX(), rotAboutFrameY(), or rotAboutFrameZ().
	 * You can also change its frame of rotation using setFrameOfRotation(Axes rotationFrame);
	 * 
	 * @param par the parent bone to which this bone is attached. 
	 * @param inputTag some user specified name for the bone, if desired
	 * @param inputBoneHeight bone length 
	 */ 

	public Bone (AbstractBone par, //parent bone
			String inputTag, //some user specified name for the bone, if desired
			float inputBoneHeight //bone length 
			) {
		super(par, 0,0,0, inputTag, inputBoneHeight);		
	}


	/**
	 * 
	 * @param par the parent bone to which this bone is attached. 
	 * @param xAngle how much the bone should be pitched relative to its parent bone
	 * @param yAngle how much the bone should be rolled relative to its parent bone
	 * @param zAngle how much the bone should be yawed relative to its parent bone
	 * @param inputTag some user specified name for the bone, if desired
	 * @param inputBoneHeight bone length 
	 */ 	
	public Bone (AbstractBone par, //parent bone
			float xAngle, //how much the bone should be pitched relative to its parent bone
			float yAngle, //how much the bone should be rolled relative to its parent bone
			float zAngle, //how much the bone should be yawed relative to its parent bone
			String inputTag, //some user specified name for the bone, if desired
			float inputBoneHeight //bone length 
			) {
		super(par, xAngle, yAngle, zAngle,inputTag, inputBoneHeight);		
	}



	@Override
	protected void generateAxes( Vec3f<?> origin,  Vec3f<?> x,  Vec3f<?> y,  Vec3f<?> z) {
		this.localAxes = new Axes(origin, x, y, z);
	}


	@Override
	protected IKPin createAndReturnPinOnAxes(AbstractAxes on) {
		return new IKPin(
				(Axes) on, 
				true, 
				this
				);		
	}


	public void enablePin(PVector pin) {
		super.enablePin_(new SGVec_3f(pin.x, pin.y, pin.z));
	}


	public void setPin(PVector pin) {
		super.setPin_(new SGVec_3f(pin.x, pin.y, pin.z));
	}


	/**
	 * @return In the case of this out-of-the-box class, getPin() returns a IKVector indicating
	 * the spatial target of the pin. 
	 */
	public PVector getPinLocation() {
		if(pin == null) return null;
		else { 
			SGVec_3f loc = (SGVec_3f) pin.getLocation_();
			return new PVector(loc.x, loc.y, loc.z);
		}
	}


	public void drawMeAndChildren(PGraphics pg, int boneCol, float pinSize) {

		if(this.constraints != null && drawKusudamas) {
			pg.pushMatrix();
			((Kusudama)constraints).drawMe(pg, boneCol, pinSize);
			pg.popMatrix();
		}		

		PMatrix localMat = localAxes().getLocalPMatrix();
		pg.applyMatrix(localMat);


		pg.beginShape(PConstants.TRIANGLE_FAN);
		if(renderMode == 1) {
			pg.fill(pg.color(0, 255-boneCol, boneCol));
		} else {
			pg.fill(pg.color(0, 0, 0));
		}
		//pg.stroke(lineColor);
		float circumference = (float) (boneHeight/8f); 
		pg.noStroke();
		pg.vertex(0, (float)boneHeight, 0);
		pg.vertex(circumference,		circumference,	0);
		pg.vertex(0, circumference,	circumference);
		pg.vertex(-circumference,		circumference,	0);
		pg.vertex(0, circumference,	-circumference);
		pg.vertex(circumference,		circumference,	0);
		pg.endShape();
		pg.beginShape(PConstants.TRIANGLE_FAN);
		pg.vertex(0, 0, 0);
		pg.vertex(0,								circumference,	-circumference);
		pg.vertex(circumference,	circumference,	0);
		pg.vertex(0, 							circumference,	circumference);
		pg.vertex(-circumference,	circumference,	0);		
		pg.vertex(0, 							circumference,	-circumference);			
		pg.endShape();
		pg.emissive(0,0,0);

		for(Bone b : getChildren()) {			
			pg.pushMatrix();
			b.drawMeAndChildren(pg, boneCol+10, pinSize);
			pg.popMatrix();
		}		

		pg.strokeWeight(4f);
		if(this.isPinned()) {
			((Axes)this.getIKPin().getAxes()).drawMe(pg, pinSize);
		}

		if(this.isPinned()) {
			pg.strokeWeight(2f);
			localAxes().drawMe(pg, pinSize);
		}
	}
	
	
	public IKPin getIKPin() {
		return (IKPin)this.pin;
	}

	/**
	 * Get the Axes associated with this bone. 
	 */
	public Axes localAxes() {
		return (Axes) this.localAxes;
	}	

	/**
	 * Get the Axes relative to which this bone's rotations are defined. (If the bone has constraints, this will be the 
	 * constraint Axes)
	 * @return
	 */	
	public Axes getMajorRotationAxes() {
		return (Axes)this.majorRotationAxes;
	}

	public ArrayList<Bone> getChildren() {
		return (ArrayList<Bone>)super.getChildren();
	}

	public static void setDrawKusudamas(boolean draw) {
		drawKusudamas = draw;
	}

}
