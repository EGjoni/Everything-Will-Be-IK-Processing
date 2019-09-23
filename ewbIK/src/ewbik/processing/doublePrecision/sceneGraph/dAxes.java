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
import IK.floatIK.AbstractArmature;
import data.JSONObject;
import data.LoadManager;
import data.SaveManager;
import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import sceneGraph.math.doubleV.AbstractAxes;
import sceneGraph.math.doubleV.Matrix4d;
import sceneGraph.math.doubleV.SGVec_3d;
import sceneGraph.math.doubleV.Vec3d;
import sceneGraph.math.doubleV.sgRayd;
import sceneGraph.math.floatV.SGVec_3f;
import sceneGraph.math.floatV.sgRayf;

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
public class dAxes extends AbstractAxes {

	
	/**
	 * 
	 * @param origin the center of this axes basis. The basis vector parameters will be automatically ADDED to the origin in order to create this basis vector.
	 * @param inX the direction of the X basis vector in global coordinates, given as an offset from this base's origin in global coordinates.   
	 * @param inY the direction of the Y basis vector in global coordinates, given as an offset from this base's origin in global coordinates.
	 * @param inZ the direction of the Z basis vector in global coordinates, given as an offset from this base's origin in global coordinates.
	 * @param forceOrthoNormality
	 */
	public dAxes(DVector origin, 
			DVector inX, 
			DVector inY, 
			DVector inZ, 
			boolean forceOrthoNormality,
			AbstractAxes parent) {
		
		super(
				toSGVec(origin),
				toSGVec(inX), 
				toSGVec(inY),
				toSGVec(inZ),
				forceOrthoNormality,
				parent
				);
	}
	
	public dAxes(SGVec_3d origin, 
			SGVec_3d inX, 
			SGVec_3d inY, 
			SGVec_3d inZ) {
		this(origin, inX, inY, inZ, true, null);
	}
	
	
	public dAxes() {
		super(
				new SGVec_3d(0,0,0),
				new SGVec_3d(1,0,0), 
				new SGVec_3d(0,1,0),
				new SGVec_3d(0,0,1),
				true,
				(AbstractAxes)null
				);
	}
	
	public dAxes(SGVec_3d origin, SGVec_3d inX, SGVec_3d inY, SGVec_3d inZ, boolean forceOrthoNormality, AbstractAxes parent) {
		super(origin, inX, inY, inZ, forceOrthoNormality, parent);
	}
	@Override
	protected AbstractAxes instantiate(
			SGVec_3d origin, 
			SGVec_3d gXHeading,
			SGVec_3d gYHeading, 
			SGVec_3d gZHeading, 
			boolean forceOrthoNormality,
			AbstractAxes parent) {
		return new dAxes(origin, gXHeading, gYHeading, gZHeading, forceOrthoNormality, parent);
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

	public static dRay toRay(sgRayd sr) {
		return new dRay(
					toDVector(sr.p1()),
					toDVector(sr.p2())
				);
	}
	
	public static void toRay(sgRayd sr, dRay storeIn) {
		if(storeIn.p1 == null) storeIn.p1 = new DVector();
		if(storeIn.p2 == null) storeIn.p2 = new DVector();
		
		storeIn.p1.set( sr.p1().y, sr.p1().y, sr.p1().z);
		storeIn.p2.set( sr.p2().y, sr.p2().y, sr.p2().z);
	}
	
	public static SGVec_3d toSGVec(DVector ev) {
		return new SGVec_3d(ev.x, ev.y, ev.z);
	}
	
	public static sgRayd toSgRay(dRay er) {
		return new sgRayd(
					toSGVec(er.p1), 
					toSGVec(er.p2)
				);
	}
	
	//////////////////// END OF CONVERSION FUNCTIONS
	
	
	
	///WRAPPER FUNCTIONS. Basically just find + replace these with the appropriate class names and conversion functions above and you should be good to go. 

	/**
	 * Make a GlobalCopy of these Axes. 
	 * @return
	 */
	public dAxes getGlobalCopy() {
		return (dAxes) super.getGlobalCopy();
	}

	public dRay x(){
		return toRay(
				super.x_()
			);
	}
	public dRay y(){ 
		return toRay(
				super.y_()
			);
	}
	public dRay z(){ 
		return toRay(
				super.z_()
				);
	}
	public dRay x_norm(){ 
		return toRay(
				super.x_norm_()
				);
	}
	
	public dRay y_norm(){
		return toRay(
				super.y_norm_()
				);
	}
	public dRay z_norm(){ 
		return toRay(
				super.z_norm_()
				);
	}
	
	public dRay x_raw(){ 
		return toRay(
				super.x_raw_()
			);
	}
	public dRay y_raw(){ 
		return toRay(
				super.y_raw_()
				);
	}
	public dRay z_raw(){ 
		return toRay(
				super.z_raw_()
				);
	}		
	
	public DVector orthonormal_Z(){
		return toDVector(
				super.orthonormal_Z_()
			);
	}
	
	public DVector getGlobalOf(DVector local_input){
		return toDVector(
				super.getGlobalOf(
						toSGVec(local_input))
			);
	}
	public DVector getOrthoNormalizedGlobalOf(DVector local_input){
		return toDVector(
				super.getOrthoNormalizedGlobalOf(
						toSGVec(local_input)
						)
			);
	}
	
	public DVector setToGlobalOf(DVector local_input){
		return toDVector(
				super.setToGlobalOf(
						toSGVec(local_input)
						)
			);
	}
	
	public void setToGlobalOf(DVector local_input, DVector global_output){
			toDVector(
				super.setToGlobalOf(
						toSGVec(local_input)
						),
				global_output
			);		
	}
	
	public void setToRawGlobalOf(DVector local_input, DVector global_output) {
		SGVec_3d tempVec = new SGVec_3d(); 
		super.setToRawGlobalOf(
				toSGVec(local_input), 
				tempVec
				);
		toDVector(
				tempVec,
				global_output
			);
	}
	public void setToOrthoNormalizedGlobalOf(DVector local_input, DVector global_output){
		SGVec_3d tempVec = new SGVec_3d(); 
		super.setToOrthoNormalizedGlobalOf(
				toSGVec(local_input), 
				tempVec
				);
		toDVector(
				tempVec,
				global_output
			);		
	}
	public void setToOrthoNormalizedGlobalOf(dRay local_input, dRay global_output){
		sgRayd tempRay = new sgRayd(); 
		super.setToOrthoNormalizedGlobalOf(
				toSgRay(local_input), 
				tempRay
				);
		toRay(
				tempRay,
				global_output
			);		
	}
	public void setToRawGlobalOf(dRay local_input, dRay global_output){
		sgRayd tempRay = new sgRayd(); 
		super.setToRawGlobalOf(
				toSgRay(local_input), 
				tempRay
				);
		toRay(
				tempRay,
				global_output
			);				
	}
	public void setToGlobalOf(dRay local_input, dRay global_output){
		sgRayd tempRay = new sgRayd(); 
		super.setToGlobalOf(
				toSgRay(local_input), 
				tempRay
				);
		toRay(
				tempRay,
				global_output
			);	
		
	}
	public dRay getGlobalOf(dRay local_input){ 
		return toRay(
				super.getGlobalOf(
						toSgRay(local_input)
					)
			);
	}
	
	public dRay getLocalOf(dRay global_input){ 
		return toRay(
				super.getLocalOf(
						toSgRay(global_input)
					)
			);
	}	
	
	public dRay getRawGlobalOf(dRay local_input){ 
		return toRay(
				super.getRawGlobalOf(
						toSgRay(local_input)
					)
			);
	}
	public dRay getRawLocalOf(dRay global_input){ 
		return toRay(
				super.getRawLocalOf(
						toSgRay(global_input)
					)
			);
	}
	public dRay getOrthoNormalizedLocalOf(dRay global_input){ 
		return toRay(
				super.getOrthoNormalizedLocalOf(
						toSgRay(global_input)
					)
			);
		}
	public DVector getRawLocalOf(DVector global_input){
		return toDVector(getRawLocalOf(
				super.getOrthoNormalizedGlobalOf(
						toSGVec(global_input)
						)
				)
			);
		
	}
	
	public DVector getRawGlobalOf(DVector local_input){
		return toDVector(getRawLocalOf(
				super.getRawGlobalOf(
						toSGVec(local_input)
						)
				)
			);		
	}
	public void translateByGlobal(DVector translate){
		super.translateByGlobal(
				toSGVec(translate)
			);
	}
	public void translateTo(DVector translate, boolean slip){
		super.translateTo(
				toSGVec(translate),
				false
			);
		
	}
	public void translateTo(DVector translate){
		super.translateTo(
				toSGVec(translate)
			);		
	}
	public DVector getLocalOf(DVector global_input){
		return toDVector(
				super.getLocalOf(
						toSGVec(global_input)
						)
			);
	}
	public DVector setToLocalOf(DVector  global_input){	
		toDVector(
				super.setToLocalOf(
						toSGVec(global_input)
						),				
					global_input
			);
		return global_input;
	}
	public void setToLocalOf(DVector global_input, DVector local_output){
		SGVec_3d tempVec = new SGVec_3d(); 
		super.setToLocalOf(
				toSGVec(global_input), 
				tempVec
				);
		toDVector(
				tempVec,
				local_output
			);				
	}
	public void setToLocalOf(dRay global_input, dRay local_output){
		sgRayd tempRay = new sgRayd(); 
		super.setToLocalOf(
				toSgRay(global_input), 
				tempRay
				);
			toRay(
				tempRay,
				local_output
			);		
	}
	
	public void setToRawLocalOf(dRay global_input, dRay local_output){
		sgRayd tempRay = new sgRayd(); 
		super.setToRawLocalOf(
				toSgRay(global_input), 
				tempRay
				);
		toRay(
				tempRay,
				local_output
			);	
	}
	
	public void setToOrthonormalLocalOf(dRay global_input, dRay local_output){
		sgRayd tempRay = new sgRayd(); 
		super.setToOrthonormalLocalOf(
				toSgRay(global_input), 
				tempRay
				);
			toRay(
				tempRay,
				local_output
			);	
	}	
	
	
	public DVector   getOrthoNormalizedLocalOf(DVector global_input){
		return toDVector(
				super.getOrthoNormalizedLocalOf(
						toSGVec(global_input)
						)
			);
	}
	
	
	public DVector  getOrientationalLocalOf(DVector input_global){
		return toDVector(
				super.getOrientationalLocalOf(
						toSGVec(input_global)
						)
			);
		
	}
	public DVector getOrientationalGlobalOf(DVector input_local){
		return toDVector(
				super.getOrientationalGlobalOf(
						toSGVec(input_local)
						)
			);
		
	}
	
	public void setToRawLocalOf(DVector in, DVector out){
		SGVec_3d tempVec = new SGVec_3d(); 
		super.setToRawLocalOf(
				toSGVec(in), 
				tempVec
				);
		toDVector(
				tempVec,
				out
			);
	}
	
		
	public void setToOrthoNormalLocalOf(DVector input_global, DVector output_local_normalized){
		SGVec_3d tempVec = new SGVec_3d(); 
		super.setToOrthoNormalLocalOf(
				toSGVec(input_global), 
				tempVec
				);
		toDVector(
				tempVec,
				output_local_normalized
			);
		
	}
	public void setToOrientationalLocalOf(DVector input_global, DVector output_local_orthonormal_chiral){
		SGVec_3d tempVec = new SGVec_3d(); 
		super.setToOrientationalLocalOf(
				toSGVec(input_global), 
				tempVec
				);
		toDVector(
				tempVec,
				output_local_orthonormal_chiral
			);
		
	}
	public void setToOrientationalGlobalOf(DVector input_local, DVector output_global_orthonormal_chiral){
		SGVec_3d tempVec = new SGVec_3d(); 
		super.setToOrientationalGlobalOf(
				toSGVec(input_local), 
				tempVec
				);
		toDVector(
				tempVec,
				output_global_orthonormal_chiral
			);		
	}



	////////////////////////?End of wrapper functions 
	
	public PMatrix getLocalPMatrix() {
		double[] m = localMBasis.getComposedMatrix().val;
		SGVec_3f trans = localMBasis.translate.toSGVec3f();
		PMatrix result = new PMatrix3D(
				(float)m[Matrix4d.M00], (float)m[Matrix4d.M01], (float)m[Matrix4d.M02], trans.x, 
				(float)m[Matrix4d.M10], (float)m[Matrix4d.M11], (float)m[Matrix4d.M12], trans.y, 
				(float)m[Matrix4d.M20],(float) m[Matrix4d.M21], (float)m[Matrix4d.M22], trans.z, 
				(float)m[Matrix4d.M30],(float) m[Matrix4d.M31], (float)m[Matrix4d.M32], (float)m[Matrix4d.M33]);
		
		return result;
	}
	
	public PMatrix getGlobalPMatrix() {
		this.updateGlobal();
		double[] m = globalMBasis.getComposedMatrix().val;
		SGVec_3f trans = globalMBasis.translate.toSGVec3f();
		PMatrix result = new PMatrix3D(
				(float)m[Matrix4d.M00], (float)m[Matrix4d.M01], (float)m[Matrix4d.M02], trans.x, 
				(float)m[Matrix4d.M10], (float)m[Matrix4d.M11], (float)m[Matrix4d.M12], trans.y, 
				(float)m[Matrix4d.M20],(float) m[Matrix4d.M21], (float)m[Matrix4d.M22], trans.z, 
				(float)m[Matrix4d.M30],(float) m[Matrix4d.M31], (float)m[Matrix4d.M32], (float)m[Matrix4d.M33]);
		
		return result;
	}

	public void drawMe(PApplet p, float size) {
		PMatrix previous = p.getMatrix();
		updateGlobal();
		p.resetMatrix();
		p.stroke(p.color(0,255,0));
		drawRay(p, x_().getRayScaledTo(size));
		p.stroke(p.color(255,0, 0));
		drawRay(p, y_().getRayScaledTo(size));
		p.stroke(p.color(0, 0, 255));
		drawRay(p, z_().getRayScaledTo(size));
		p.applyMatrix(previous);		
	}	
	
	
	public static void drawRay(PApplet p, sgRayd r) {
		p.line((float)r.p1().x,(float) r.p1().y, (float)r.p1().z, (float)r.p2().x, (float)r.p2().y, (float)r.p2().z);
	}
	
	public static void drawPoint(PApplet p, SGVec_3d pt) {
		p.point((float)pt.x, (float)pt.y, (float)pt.z);
	}

	
}
