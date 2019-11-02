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

package ewbik.processing.doublePrecision;


import java.util.ArrayList;

import IK.IKExceptions.NullParentForBoneException;
import IK.doubleIK.AbstractArmature;
import IK.doubleIK.AbstractBone;
import ewbik.processing.doublePrecision.*;
import ewbik.processing.doublePrecision.sceneGraph.*;
import ewbik.processing.singlePrecision.sceneGraph.Axes;
import math.doubleV.AbstractAxes;
import math.doubleV.Vec3d;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractBone. Please refer to the {@link AbstractBone AbstractBone docs.} 
 */	
public class dBone extends AbstractBone {

	public static int renderMode = 1; 
	public static boolean drawKusudamas = false;

	public dBone() {}

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
	public dBone (dBone par, //parent bone
			Vec3d<?> tipHeading, //the orienational heading of this bone (global vs relative coords specified in coordinateType)
			Vec3d<?>  rollHeading, //axial rotation heading of the bone (it's z-axis) 
			String inputTag,	 //some user specified name for the bone, if desired 
			double inputBoneHeight, //bone length 
			frameType coordinateType							
			) throws NullParentForBoneException {
		super(
				par, 
				tipHeading, 
				rollHeading,
				inputTag, 
				inputBoneHeight, 
				coordinateType);
	}


	/**
	 * 
	 * @param armature the parent armature for this bone
	 * @param tipHeading the orienational heading of this bone (global vs relative coords specified in coordinateType)
	 * @param rollHeading axial rotation heading of the bone (it's z-axis) 
	 * @param inputTag some user specified name for the bone, if desired 
	 * @param boneHeight bone length 
	 * @param coordinateType
	 * @throws NullParentForBoneException
	 */
	public dBone(
			AbstractArmature armature, 
			Vec3d<?>  tipHeading, 
			Vec3d<?>  rollHeading, 
			String inputTag, 
			double boneHeight,
			frameType coordinateType) {
		super(
				armature, 
				tipHeading, 
				rollHeading,
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

	public dBone (AbstractBone par, //parent bone
			String inputTag, //some user specified name for the bone, if desired
			double inputBoneHeight //bone length 
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
	public dBone (AbstractBone par, //parent bone
			double xAngle, //how much the bone should be pitched relative to its parent bone
			double yAngle, //how much the bone should be rolled relative to its parent bone
			double zAngle, //how much the bone should be yawed relative to its parent bone
			String inputTag, //some user specified name for the bone, if desired
			double inputBoneHeight //bone length 
			) {
		super(par, xAngle, yAngle, zAngle,inputTag, inputBoneHeight);		
	}


	@Override
	protected void generateAxes( Vec3d<?> origin,  Vec3d<?> x,  Vec3d<?> y,  Vec3d<?> z) {
		this.localAxes = new dAxes(origin, x, y, z);
	}

	public DVector getBase() {
		return (DVector) super.getBase_();
	}

	public DVector getTip() { 		
		return (DVector) super.getTip_();
	}

	@Override
	protected dIKPin createAndReturnPinOnAxes(AbstractAxes on) {
				return new dIKPin(
						(dAxes) on, 
						true, 
						this
					);		
	}


	public void enablePin(DVector pin) {
		super.enablePin_(pin);
	}


	public void setPin(DVector pin) {
		super.setPin_(pin);
	}


	/**
	 * @return In the case of this out-of-the-box class, getPin() returns a IKVector indicating
	 * the spatial target of the pin. 
	 */
	public DVector getPinLocation() {
		if(pin == null) return null;
		else return (DVector) pin.getLocation_();
	}


	public void drawMeAndChildren(PGraphics pg, int boneCol, float pinSize) {
		
		if(this.constraints != null && drawKusudamas) {
			pg.pushMatrix();
			((dKusudama)constraints).drawMe(pg, boneCol, pinSize);
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
			

		for(dBone b : getChildren()) {			
			pg.pushMatrix();
			b.drawMeAndChildren(pg, boneCol+10, pinSize);
			pg.popMatrix();
		}		



		pg.strokeWeight(4f);
		if(this.isPinned()) {
			((dAxes)this.getIKPin().getAxes()).drawMe(pg, pinSize);
		}

		if(this.isPinned()) {
			pg.strokeWeight(2f);
			localAxes().drawMe(pg, pinSize);
		}
	}

	/**
	 * Get the Axes associated with this bone. 
	 */
	public dAxes localAxes() {
		return (dAxes) this.localAxes;
	}	

	/**
	 * Get the Axes relative to which this bone's rotations are defined. (If the bone has constraints, this will be the 
	 * constraint Axes)
	 * @return
	 */	
	public dAxes getMajorRotationAxes() {
		return (dAxes)this.majorRotationAxes;
	}

	public ArrayList<dBone> getChildren() {
		return (ArrayList<dBone>)super.getChildren();
	}
	
	public dIKPin getIKPin() {
		return (dIKPin) super.getIKPin();
	}
	
	public static void setDrawKusudamas(boolean draw) {
		drawKusudamas = draw;
	}

}
