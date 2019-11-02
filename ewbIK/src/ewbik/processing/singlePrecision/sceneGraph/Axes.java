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

package ewbik.processing.singlePrecision.sceneGraph;
import math.floatV.*;
import math.floatV.AbstractAxes;
import math.floatV.CartesianAxes;
import math.floatV.SGVec_3f;
import math.floatV.Vec3f;
import math.floatV.sgRayf;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;

/*
 * This class is a reference implementation showing how to extend AbstractAxes. 
 * You can use it as-is if you don't mind working with the Vector representations 
 * used by this library. But if you are dropping this library into an existing framework, 
 * it is recommended that you extend AbstractAxes to behave as a wrapper 
 * that takes your framework's Vector instances as input, and converts them to this 
 * library's native implementation before processing, then back into your preferred implementation 
 * before returning. 
 * 
 * If you can think of a better way to handle this that increases extensibility without 
 * sacrificing speed, please let me know or contribute a better solution. 
 *  
 */

/**
 * Note, this class is a concrete implementation of the abstract class AbstractArmature. Please refer to the {@link AbstractAxes AbstractAxes docs.} 
 * @param name A label for this armature.  
 */	
public class Axes extends CartesianAxes {
	public static int renderMode = 1; 
	
	
	public Axes(AbstractBasis b, AbstractAxes parent) {
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
	public Axes(PVector origin, 
			PVector inX, 
			PVector inY, 
			PVector inZ, 
			AbstractAxes parent) {
		
		super(
				toSGVec(origin),
				toSGVec(inX), 
				toSGVec(inY),
				toSGVec(inZ),
				parent
				);
	}
	
	public Axes(Vec3f<?> origin, 
			Vec3f<?> x, 
			Vec3f<?> y, 
			Vec3f<?> z) {
		this(origin, x, y, z, true, null);
	}
	
	
	public Axes() {
		super(
				new SGVec_3f(0,0,0),
				new SGVec_3f(1,0,0), 
				new SGVec_3f(0,1,0),
				new SGVec_3f(0,0,1),
				(AbstractAxes)null
				);
	}
	
	public Axes(Vec3f<?> origin, Vec3f<?> x, Vec3f<?> y, Vec3f<?> z, boolean forceOrthoNormality, AbstractAxes parent) {
		super(origin, x, y, z, parent);
	}

	/**conversion functions. Replace these with functions that convert to and from your 
	* framework's native vector and ray representations.
	*/
	//////////////////////////////////////////////////////////////////////////////////////////////
	public static PVector toPVector(Vec3f sv) {
		return new PVector(sv.x, sv.y, sv.z);
	}
	
	public static void toDVector(Vec3f sv, PVector storeIn) {
		storeIn.x = sv.x;
		storeIn.y = sv.y;
		storeIn.z = sv.z;
	}

		
	public static SGVec_3f toSGVec(PVector ev) {
		return new SGVec_3f(ev.x, ev.y, ev.z);
	}
	
	public static sgRayf toSgRay(Ray er) {
		return new sgRayf(
					toSGVec(er.p1), 
					toSGVec(er.p2)
				);
	}
	
	//////////////////// END OF CONVERSION FUNCTIONS
	
	public PVector origin() {
		return toPVector(this.origin_());
	}
	
	
	///WRAPPER FUNCTIONS. Basically just find + replace these with the appropriate class names and conversion functions above if you need them
	//and you should be good to go. 

	
	@Override
	public Axes getGlobalCopy() {
		this.updateGlobal();
		return new Axes(getGlobalMBasis(), this.getParentAxes());
	}

	
	public PVector getGlobalOf(PVector local_input){
		return toPVector(
				super.getGlobalOf(
						toSGVec(local_input))
			);
	}
	
	public PVector setToGlobalOf(PVector local_input){
		return toPVector(
				super.setToGlobalOf(
						toSGVec(local_input)
						)
			);
	}
	
	public void setToGlobalOf(PVector local_input, PVector global_output){
			toDVector(
				super.setToGlobalOf(
						toSGVec(local_input)
						),
				global_output
			);		
	}
	
	public void translateByGlobal(PVector translate){
		super.translateByGlobal(
				toSGVec(translate)
			);
	}
	public void translateTo(PVector translate, boolean slip){
		super.translateTo(
				toSGVec(translate),
				false
			);
		
	}
	public void translateTo(PVector translate){
		super.translateTo(
				toSGVec(translate)
			);		
	}
	
	public PVector getOrigin() {
		return toPVector(origin_());
	}
	
	public PVector getLocalOf(PVector global_input){
		return toPVector(
				super.getLocalOf(
						toSGVec(global_input)
						)
			);
	}
	public PVector setToLocalOf(PVector  global_input){	
		toDVector(
				super.setToLocalOf(
						toSGVec(global_input)
						),				
					global_input
			);
		return global_input;
	}
	public void setToLocalOf(PVector global_input, PVector local_output){
		SGVec_3f tempVec = new SGVec_3f(); 
		super.setToLocalOf(
				toSGVec(global_input), 
				tempVec
				);
		toDVector(
				tempVec,
				local_output
			);				
	}
	
	////////////////////////?End of wrapper functions 

	float[][] outMatLocal = new float[4][4]; 
	float[][] outMatGlobal = new float[4][4];
	
	private void updateMatrix(AbstractBasis b, float[][] outputMatrix) {
		Rot rotation = b.rotation;
		b.refreshPrecomputed();

		Vec3f x = b.getXHeading();
		Vec3f y = b.getYHeading();
		Vec3f z = b.getZHeading();
		
		Vec3f origin = b.getOrigin();
		
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
	
	
	public PMatrix getLocalPMatrix() {
		updateMatrix(getLocalMBasis(), outMatLocal);
		float[][] m = outMatLocal;
		PMatrix result = new PMatrix3D(
				m[0][0], m[1][0], m[2][0], m[3][0], 
				m[0][1], m[1][1], m[2][1], m[3][1], 
				m[0][2], m[1][2], m[2][2], m[3][2], 
				m[0][3], m[1][3], m[2][3], m[3][3]);
		return result;
	}
	
	public PMatrix getGlobalPMatrix() {
		updateMatrix(getGlobalMBasis(), outMatGlobal);
		float[][] m = outMatGlobal;
		PMatrix result = new PMatrix3D(
				m[0][0], m[1][0], m[2][0], m[3][0], 
				m[0][1], m[1][1], m[2][1], m[3][1], 
				m[0][2], m[1][2], m[2][2], m[3][2], 
				m[0][3], m[1][3], m[2][3], m[3][3]);
		return result;
	}
	
	
	public void drawMe(PGraphics pg, float size) {
		PMatrix previous = pg.getMatrix();
		updateGlobal();
		pg.resetMatrix();
		if(renderMode == 1) pg.stroke(pg.color(0,255,0));
		else pg.stroke(pg.color(0,0,0,0)); 
		drawRay(pg, x_().getRayScaledTo(size));
		if(renderMode == 1) pg.stroke(pg.color(255,0, 0));
		else pg.stroke(pg.color(0,0,0,0)); 
		drawRay(pg, y_().getRayScaledTo(size));
		if(renderMode == 1) pg.stroke(pg.color(0, 0, 255));
		else pg.stroke(pg.color(0,0,0,0)); 
		drawRay(pg, z_().getRayScaledTo(size));
		pg.applyMatrix(previous);
	}
	
	public static void drawRay(PGraphics p, sgRayf r) {
		p.line(r.p1().x, r.p1().y, r.p1().z, r.p2().x, r.p2().y, r.p2().z);
	}
	
	public static void drawPoint(PGraphics p, SGVec_3f pt) {
		p.point(pt.x, pt.y, pt.z);
	}





}
