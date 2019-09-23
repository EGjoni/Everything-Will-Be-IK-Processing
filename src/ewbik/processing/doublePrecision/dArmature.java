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
import IK.doubleIK.AbstractArmature;
import IK.doubleIK.AbstractBone.frameType;
import sceneGraph.*;
import processing.core.PApplet;
import processing.core.PMatrix;
import ewbik.processing.doublePrecision.sceneGraph.DVector;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import sceneGraph.math.doubleV.SGVec_3d;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractArmature. Please refer to the {@link AbstractArmature AbstractArmature docs.} 
 */	
public class dArmature extends AbstractArmature{

	public dArmature() {}
	
	/**
	 * Note, this class is a concrete implementation of the abstract class AbstractArmature. Please refer to the {@link AbstractArmature AbstractArmature docs.} 
	 * @param name A label for this armature.  
	 */	
	public dArmature(String name) {		
		super(new dAxes(new DVector(0,0,0), new DVector(1,0,0), new DVector(0,1,0), new DVector(0,0,1), true, null), name);
	}


	@Override
	protected  void initializeRootBone(AbstractArmature armature,SGVec_3d tipHeading,SGVec_3d rollHeading, String inputTag,
			double boneHeight, frameType coordinateType) {
		this.rootBone = new dBone(this, 
												dAxes.toDVector(tipHeading), 
												dAxes.toDVector(rollHeading), 
												inputTag, 
												boneHeight, 
												coordinateType);	
	}
	
	@Override 
	public dBone getRootBone() {
		return (dBone)rootBone;
	}
	
	@Override
	public dBone getBoneTagged(String tag) {
		return (dBone)boneMap.get(tag);	
	}
	
	public void drawMe(PApplet p, int color,  float pinSize) {
		PMatrix localMat = localAxes().getGlobalPMatrix();
		p.applyMatrix(localMat);
		p.pushMatrix(); 
			getRootBone().drawMeAndChildren(p, color,  pinSize);
		p.popMatrix();
	}
		
	@Override 
	public dAxes localAxes() {
		return (dAxes) super.localAxes();
	}
	
	public void setPerformanceMonitor(boolean state) {
		super.setPerformanceMonitor(state);
	}

	
}
