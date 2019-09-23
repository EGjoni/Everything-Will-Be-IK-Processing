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
import IK.floatIK.AbstractBone;
import data.SaveManager;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix;
import processing.core.PVector;
import sceneGraph.math.floatV.AbstractAxes;
import sceneGraph.math.floatV.Matrix4f;
import sceneGraph.math.floatV.Rot;
import sceneGraph.math.floatV.SGVec_3f;
import sceneGraph.math.floatV.Vec3f;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractBone. Please refer to the {@link AbstractBone AbstractBone docs.} 
 */	
public class Bone extends AbstractBone {
	
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
			Armature armature, 
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
	protected void generateAxes(SGVec_3f origin, SGVec_3f x, SGVec_3f y, SGVec_3f z) {
		this.localAxes = new Axes(origin, x, y, z);
	}
	
	public PVector getBase() {
		return Axes.toPVector(super.getBase_());
	}

	public PVector getTip() { 		
		return Axes.toPVector(super.getTip_());
	}

	@Override
	protected IKPin createAndReturnPinAtOrigin(SGVec_3f origin) {
		// TODO Auto-generated method stub
		Axes thisBoneAxes = localAxes().getGlobalCopy(); 
		thisBoneAxes.setOrthoNormalityConstraint(true);
		thisBoneAxes.translateTo(origin);
		return new IKPin(
						thisBoneAxes, 
						true, 
						this
				);
	}
	
	public SGVec_3f getBase_() {
		return localAxes.origin_().copy();
	}

	public SGVec_3f getTip_() { 		
		return localAxes.y_().getScaledTo(boneHeight);
	}

	
	
	
	public void enablePin(PVector pin) {
		super.enablePin_(Axes.toSGVec(pin));
	}
	
	
	public void setPin(PVector pin) {
		super.setPin_(Axes.toSGVec(pin));
	}
	

	/**
	 * @return the location of the pin. 
	 */
	public PVector getPinLocation() {
		if(pin == null) return null;
		else return Axes.toPVector(pin.getLocation_());
	}

	
	public void drawMeAndChildren(PApplet p, int boneCol, float pinSize) {
		
		PMatrix localMat = localAxes().getLocalPMatrix();
		p.applyMatrix(localMat);
		p.beginShape(PConstants.TRIANGLE_FAN);
		p.fill(p.color(0, 255-boneCol, boneCol));
		//p.stroke(lineColor);
		float circumference = boneHeight/8f; 
		p.noStroke();
			p.vertex(0, boneHeight, 0);
			p.vertex(circumference,	circumference,	0);
			p.vertex(0, circumference,	circumference);
			p.vertex(-circumference,	circumference,	0);
			p.vertex(0, circumference,	-circumference);
			p.vertex(circumference,	circumference,	0);
		p.endShape();
		p.beginShape();
			p.vertex(0, 0, 0);
			p.vertex(0, circumference,	-circumference);
			p.vertex(circumference,	circumference,	0);
			p.vertex(0, circumference,	circumference);
			p.vertex(-circumference,	circumference,	0);		
			p.vertex(0, circumference,	-circumference);			
		p.endShape();
		p.emissive(0,0,0);
			
		
		for(Bone b : getChildren()) {			
			p.pushMatrix();
			b.drawMeAndChildren(p, boneCol+10, pinSize);
			p.popMatrix();
		}		
		
		p.strokeWeight(4f);
		if(this.isPinned()) {
			((Axes)this.getIKPin().getAxes()).drawMe(p, pinSize);
		}
		
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

	public IKPin getIKPin() {
		return (IKPin) super.getIKPin();
	}
	
	public ArrayList<Bone> getChildren() {
		return (ArrayList<Bone>)super.getChildren();
	}
	

	
}
