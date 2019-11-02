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

package ewbik.processing.doublePrecision.sceneGraph;
import IK.doubleIK.AbstractArmature;
import math.Vec;
import math.doubleV.AbstractAxes;
import math.doubleV.AbstractBasis;
import math.doubleV.CartesianAxes;
import math.doubleV.Rot;
import math.doubleV.SGVec_3d;
import math.doubleV.Vec3d;
import math.doubleV.sgRayd;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix3D;

/*
 * This class is a reference implementation showing how to extend AbstractAxes. 
 * You can use it as-is if you don't mind working with the Vector representations 
 * used by this library. But if you are dropping this library into an existing framework, 
 * it is recommended that you extend AbstractAxes to behave as a wrapper 
 * that takes your framework's Vector instances as input, and converts them to this 
 * libraries native implementation before processing, then back into your preferred implementation 
 * before returning. 
 * 
 * If you can think of a better way to handle this that increases extensibility without 
 * sacrificing speed, please let me know or contribute a better solution. 
 */

/**
 * Note, this class is a concrete implementation of the abstract class AbstractArmature. Please refer to the {@link AbstractAxes AbstractAxes docs.} 
 * @param name A label for this armature.  
 */	
public class dAxes extends CartesianAxes {
	public static int renderMode = 1; 

	public dAxes(AbstractBasis b, AbstractAxes parent) {
		super(b, parent);
	}

	/**
	 * 
	 * @param origin the center of this axes basis. The basis vector parameters will be automatically ADDED to the origin in order to create this basis vector.
	 * @param inX the direction of the X basis vector in global coordinates, given as an offset from this base's origin in global coordinates.   
	 * @param inY the direction of the Y basis vector in global coordinates, given as an offset from this base's origin in global coordinates.
	 * @param inZ the direction of the Z basis vector in global coordinates, given as an offset from this base's origin in global coordinates.
	 * @param forceOrthoNormality
	 */
	public dAxes( Vec3d<?> origin, 
			Vec3d<?> inX, 
			Vec3d<?> inY, 
			Vec3d<?> inZ,
			CartesianAxes parent) {

		super(
				origin,
				inX, 
				inY,
				inZ,
				parent
				);
	}

	public dAxes( Vec3d<?> origin, 
			Vec3d<?> inX, 
			Vec3d<?> inY, 
			Vec3d<?> inZ) {
		this(origin, inX, inY, inZ, null);
	}


	public dAxes() {
		super(
				new DVector(0,0,0),
				new DVector(1,0,0), 
				new DVector(0,1,0),
				new DVector(0,0,1),
				(CartesianAxes)null
				);
	}

	public dAxes(DVector origin, DVector inX, DVector inY, DVector inZ, AbstractAxes parent) {
		super(origin, inX, inY, inZ, parent);
	}

	/*conversion functions. Replace these with functions that convert to and from your 
	 * framework's native vector and ray representations.
	 */
	//////////////////////////////////////////////////////////////////////////////////////////////
	public static DVector toDVector(SGVec_3d sv) {
		return new DVector(sv.x, sv.y, sv.z);
	}

	public static void toDVector(SGVec_3d sv, DVector storeIn) {
		storeIn.x = sv.x;
		storeIn.y = sv.y;
		storeIn.z = sv.z;
	}	


	/**
	 * Make a GlobalCopy of these Axes. 
	 * @return
	 */
	@Override
	public dAxes getGlobalCopy() {
		this.updateGlobal();
		return new dAxes(getGlobalMBasis(), this.getParentAxes());
	}
	
	double[][] outMatLocal = new double[4][4]; 
	double[][] outMatGlobal = new double[4][4];

	private void updateMatrix(AbstractBasis b, double[][] outputMatrix) {
		Rot rotation = b.rotation;
		b.refreshPrecomputed();

		Vec3d x = b.getXHeading();
		Vec3d y = b.getYHeading();
		Vec3d z = b.getZHeading();
		
		Vec3d origin = b.getOrigin();
		
		outputMatrix[0][0] = x.x;
		outputMatrix[0][1] = x.y;
		outputMatrix[0][2] = x.z;

		outputMatrix[1][0] = y.x;
		outputMatrix[1][1] = y.y;
		outputMatrix[1][2] = y.z;

		outputMatrix[2][0] = z.x;
		outputMatrix[2][1] = z.y;
		outputMatrix[2][2] = z.z;

		outputMatrix[3][3] = 1;

		outputMatrix[3][0] = origin.x; 
		outputMatrix[3][1] = origin.y; 
		outputMatrix[3][2] = origin.z; 

	}

	////////////////////////?End of wrapper functions 

	public PMatrix getLocalPMatrix() {
		updateMatrix(getLocalMBasis(), outMatLocal);
		double[][] m = outMatLocal;
		PMatrix result = new PMatrix3D(
				(float)m[0][0], (float)m[1][0], (float)m[2][0], (float)m[3][0], 
				(float)m[0][1], (float)m[1][1], (float)m[2][1], (float)m[3][1], 
				(float)m[0][2], (float)m[1][2], (float)m[2][2], (float)m[3][2], 
				(float)m[0][3], (float)m[1][3], (float)m[2][3], (float)m[3][3]);

		return result;
	}

	public PMatrix getGlobalPMatrix() {
		this.updateGlobal();
		updateMatrix(getGlobalMBasis(), outMatGlobal);
		double[][] m = outMatGlobal;
		PMatrix result = new PMatrix3D(
				(float)m[0][0], (float)m[1][0], (float)m[2][0], (float)m[3][0], 
				(float)m[0][1], (float)m[1][1], (float)m[2][1], (float)m[3][1], 
				(float)m[0][2], (float)m[1][2], (float)m[2][2], (float)m[3][2], 
				(float)m[0][3], (float)m[1][3], (float)m[2][3], (float)m[3][3]);

		return result;
	}

	public void drawMe(PGraphics pg, float size) {
	
			pg.noStroke();
			updateGlobal();
			pg.pushMatrix();
			pg.setMatrix(getGlobalPMatrix());
			if(renderMode == 1) pg.fill(0,255,0);
			else pg.fill(0,0,0,255); 
			pg.pushMatrix();
			pg.translate(size/2f, 0, 0);
			pg.box(size, size/10f,  size/10f);
			pg.popMatrix();			
			drawRay(pg, x_().getRayScaledTo(size));
			if(renderMode == 1) pg.fill(255,0, 0);
			else pg.fill(0,0,0,255);
			pg.pushMatrix();
			pg.translate(0, size/2f, 0);
			pg.box(size/10f, size, size/10f);
			pg.popMatrix();
			//drawRay(pg, y_().getRayScaledTo(size));
			if(renderMode == 1) pg.fill(0, 0, 255);
			else pg.fill(0,0,0,255); 
			pg.pushMatrix();
			pg.translate(0, 0, size/2f);
			pg.box(size/10f, size/10f, size);
			pg.popMatrix();
			pg.popMatrix();
			//pg.applyMatrix(previous);
		
	}	


	public static void drawRay(PGraphics p, sgRayd r) {
		p.line((float)r.p1().x,(float) r.p1().y, (float)r.p1().z, (float)r.p2().x, (float)r.p2().y, (float)r.p2().z);
	}

	public static void drawPoint(PGraphics p, SGVec_3d pt) {
		p.point((float)pt.x, (float)pt.y, (float)pt.z);
	}


}
