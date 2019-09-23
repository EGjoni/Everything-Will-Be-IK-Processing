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
import IK.floatIK.AbstractArmature;
import IK.floatIK.AbstractBone.frameType;
import data.SaveManager;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.*;
import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PVector;
import sceneGraph.math.floatV.SGVec_3f;

/**
 * Note, this class is a concrete implementation of the abstract class AbstractArmature. Please refer to the {@link AbstractArmature AbstractArmature docs.} 
 */	
public class Armature extends AbstractArmature{
	
	//default constructor required for file loading to work
	public Armature() {}
	
	
	public Armature(String name) {		
		super(new Axes(
				new PVector(0,0,0), new PVector(1,0,0), new PVector(0,1,0), new PVector(0,0,1), 
				true, null), name);
	}


	@Override
	protected  void initializeRootBone(
			AbstractArmature armature,
			SGVec_3f tipHeading,
			SGVec_3f rollHeading,
			String inputTag,
			float boneHeight, 
			frameType coordinateType) {
		this.rootBone = new Bone(this, 
												Axes.toPVector(tipHeading), 
												Axes.toPVector(rollHeading), 
												inputTag, 
												boneHeight, 
												coordinateType);	
	}

	
	public void drawMe(PApplet p, int color, float pinSize) {
		PMatrix localMat = localAxes().getGlobalPMatrix();
		p.applyMatrix(localMat);
		p.pushMatrix(); 
			getRootBone().drawMeAndChildren(p, color, pinSize);
		p.popMatrix();
	}
	
	@Override 
	public Bone getRootBone() {
		return (Bone)rootBone;
	}
	
	@Override
	public Bone getBoneTagged(String tag) {
		return (Bone)boneMap.get(tag);	
	}
	
	@Override 
	public Axes localAxes() {
		return (Axes) super.localAxes();
	}

}
