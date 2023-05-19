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
import IK.doubleIK.AbstractBone;
import IK.doubleIK.AbstractBone.frameType;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.opengl.PGraphics3D;
import ewbik.processing.doublePrecision.sceneGraph.DVector;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import math.doubleV.AbstractAxes;
import math.doubleV.Vec3d;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractArmature. Please refer to the {@link AbstractArmature AbstractArmature docs.} 
 */	
public class dArmature extends AbstractArmature {

	public dArmature() {}
	
	/**
	 * Note, this class is a concrete implementation of the abstract class AbstractArmature. Please refer to the {@link AbstractArmature AbstractArmature docs.} 
	 * @param name A label for this armature.  
	 */	
	public dArmature(String name) {		
		super(new dAxes(), name);
	}
	
	public dArmature(AbstractAxes inputOrigin, String name) {
		super(inputOrigin, name);
	}


	@Override
	protected  void initializeRootBone(AbstractArmature armature,  Vec3d<?> tipHeading,  Vec3d<?> rollHeading, String inputTag,
			double boneHeight, frameType coordinateType) {
		this.rootBone = new dBone(armature, 
												tipHeading, 
												rollHeading, 
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
		return (dBone)tagBoneMap.get(tag);	
	}
	
	public void drawMe(PApplet p, int color, float pinSize) {
		this.drawMe(p.g, color, pinSize);
	}
	
	public void drawMe(PGraphics pg, int color,  float pinSize) {
		PMatrix localMat = ((dAxes)localAxes().getParentAxes()).getGlobalPMatrix();
		pg.pushMatrix(); 
			pg.applyMatrix(localMat);
			getRootBone().drawMeAndChildren(pg, color,  pinSize);
			pg.strokeWeight(4f);
			/*for(AbstractBone ab: bones) { 
				dBone b = (dBone) ab;
				if(b.isPinned()) {
					((dAxes)b.getIKPin().getAxes()).drawMe(pg, pinSize);
				}
		
				if(b.isPinned()) {
					pg.strokeWeight(2f);
					localAxes().drawMe(pg, pinSize);
				}
			}*/
		pg.popMatrix();
		
	}
	
	public void drawWidgets(PGraphics3D pg, float pinSize) {
		//PMatrix localMat = ((dAxes)localAxes().getParentAxes()).getGlobalPMatrix();
		//pg.pushMatrix(); 
		//pg.applyMatrix(localMat);
			pg.strokeWeight(4f);
			for(AbstractBone ab: bones) { 
				dBone b = (dBone) ab;
				if(b.isPinned()) {
					((dAxes)b.getIKPin().getAxes()).drawMe(pg, pinSize);
				}
		
				if(b.isPinned()) {
					pg.strokeWeight(2f);
					localAxes().drawMe(pg, pinSize);
				}
			}
		//pg.popMatrix();
	}
	
	@Override 
	public dAxes localAxes() {
		return (dAxes) super.localAxes();
	}
	
	public void setPerformanceMonitor(boolean state) {
		super.setPerformanceMonitor(state);
	}
	
}
