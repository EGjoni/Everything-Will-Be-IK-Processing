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
import data.JSONObject;
import data.LoadManager;
import data.SaveManager;
import processing.core.PApplet;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;
import sceneGraph.math.floatV.AbstractAxes;
import sceneGraph.math.floatV.Matrix4f;
import sceneGraph.math.floatV.SGVec_3f;
import sceneGraph.math.floatV.Vec3f;
import sceneGraph.math.floatV.sgRayf;

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
public class Axes extends AbstractAxes {

	
	
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
	
	public Axes(SGVec_3f origin, 
			SGVec_3f inX, 
			SGVec_3f inY, 
			SGVec_3f inZ) {
		this(origin, inX, inY, inZ, true, null);
	}
	
	
	public Axes() {
		super(
				new SGVec_3f(0,0,0),
				new SGVec_3f(1,0,0), 
				new SGVec_3f(0,1,0),
				new SGVec_3f(0,0,1),
				true,
				(AbstractAxes)null
				);
	}
	
	public Axes(SGVec_3f origin, SGVec_3f inX, SGVec_3f inY, SGVec_3f inZ, boolean forceOrthoNormality, AbstractAxes parent) {
		super(origin, inX, inY, inZ, forceOrthoNormality, parent);
	}
	@Override
	protected AbstractAxes instantiate(
			SGVec_3f origin, 
			SGVec_3f gXHeading,
			SGVec_3f gYHeading, 
			SGVec_3f gZHeading, 
			boolean forceOrthoNormality,
			AbstractAxes parent) {
		return new Axes(origin, gXHeading, gYHeading, gZHeading, forceOrthoNormality, parent);
	}

	/**conversion functions. Replace these with functions that convert to and from your 
	* framework's native vector and ray representations.
	*/
	//////////////////////////////////////////////////////////////////////////////////////////////
	public static PVector toPVector(SGVec_3f sv) {
		return new PVector(sv.x, sv.y, sv.z);
	}
	
	public static void toDVector(SGVec_3f sv, PVector storeIn) {
		storeIn.x = sv.x;
		storeIn.y = sv.y;
		storeIn.z = sv.z;
	}

	public static Ray toRay(sgRayf sr) {
		return new Ray(
					toPVector(sr.p1()),
					toPVector(sr.p2())
				);
	}
	
	public static void toRay(sgRayf sr, Ray storeIn) {
		if(storeIn.p1 == null) storeIn.p1 = new PVector();
		if(storeIn.p2 == null) storeIn.p2 = new PVector();
		
		storeIn.p1.set( sr.p1().y, sr.p1().y, sr.p1().z);
		storeIn.p2.set( sr.p2().y, sr.p2().y, sr.p2().z);
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
	
	
	
	///WRAPPER FUNCTIONS. Basically just find + replace these with the appropriate class names and conversion functions above and you should be good to go. 

	
	/**
	 * Make a GlobalCopy of these Axes. 
	 * @return
	 */
	public Axes getGlobalCopy() {
		return (Axes) super.getGlobalCopy();
	}

	public Ray x(){
		return toRay(
				super.x_()
			);
	}
	public Ray y(){ 
		return toRay(
				super.y_()
			);
	}
	public Ray z(){ 
		return toRay(
				super.z_()
				);
	}
	public Ray x_norm(){ 
		return toRay(
				super.x_norm_()
				);
	}
	
	public Ray y_norm(){
		return toRay(
				super.y_norm_()
				);
	}
	public Ray z_norm(){ 
		return toRay(
				super.z_norm_()
				);
	}
	
	public Ray x_raw(){ 
		return toRay(
				super.x_raw_()
			);
	}
	public Ray y_raw(){ 
		return toRay(
				super.y_raw_()
				);
	}
	public Ray z_raw(){ 
		return toRay(
				super.z_raw_()
				);
	}		
	
	public PVector orthonormal_Z(){
		return toPVector(
				super.orthonormal_Z_()
			);
	}
	
	public PVector getGlobalOf(PVector local_input){
		return toPVector(
				super.getGlobalOf(
						toSGVec(local_input))
			);
	}
	public PVector getOrthoNormalizedGlobalOf(PVector local_input){
		return toPVector(
				super.getOrthoNormalizedGlobalOf(
						toSGVec(local_input)
						)
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
	
	public void setToRawGlobalOf(PVector local_input, PVector global_output) {
		SGVec_3f tempVec = new SGVec_3f(); 
		super.setToRawGlobalOf(
				toSGVec(local_input), 
				tempVec
				);
		toDVector(
				tempVec,
				global_output
			);
	}
	public void setToOrthoNormalizedGlobalOf(PVector local_input, PVector global_output){
		SGVec_3f tempVec = new SGVec_3f(); 
		super.setToOrthoNormalizedGlobalOf(
				toSGVec(local_input), 
				tempVec
				);
		toDVector(
				tempVec,
				global_output
			);		
	}
	public void setToOrthoNormalizedGlobalOf(Ray local_input, Ray global_output){
		sgRayf tempRay = new sgRayf(); 
		super.setToOrthoNormalizedGlobalOf(
				toSgRay(local_input), 
				tempRay
				);
		toRay(
				tempRay,
				global_output
			);		
	}
	public void setToRawGlobalOf(Ray local_input, Ray global_output){
		sgRayf tempRay = new sgRayf(); 
		super.setToRawGlobalOf(
				toSgRay(local_input), 
				tempRay
				);
		toRay(
				tempRay,
				global_output
			);				
	}
	public void setToGlobalOf(Ray local_input, Ray global_output){
		sgRayf tempRay = new sgRayf(); 
		super.setToGlobalOf(
				toSgRay(local_input), 
				tempRay
				);
		toRay(
				tempRay,
				global_output
			);			
	}	
	
	public Ray getGlobalOf(Ray local_input){ 
		return toRay(
				super.getGlobalOf(
						toSgRay(local_input)
					)
			);
	}
	
	public Ray getLocalOf(Ray global_input){ 
		return toRay(
				super.getLocalOf(
						toSgRay(global_input)
					)
			);
	}	
	
	public Ray getRawGlobalOf(Ray local_input){ 
		return toRay(
				super.getRawGlobalOf(
						toSgRay(local_input)
					)
			);
	}
	public Ray getRawLocalOf(Ray global_input){ 
		return toRay(
				super.getRawLocalOf(
						toSgRay(global_input)
					)
			);
	}
	public Ray getOrthoNormalizedLocalOf(Ray global_input){ 
		return toRay(
				super.getOrthoNormalizedLocalOf(
						toSgRay(global_input)
					)
			);
		}
	public PVector getRawLocalOf(PVector global_input){
		return toPVector(getRawLocalOf(
				super.getOrthoNormalizedGlobalOf(
						toSGVec(global_input)
						)
				)
			);
		
	}
	
	public PVector getRawGlobalOf(PVector local_input){
		return toPVector(getRawLocalOf(
				super.getRawGlobalOf(
						toSGVec(local_input)
						)
				)
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
	public void setToLocalOf(Ray global_input, Ray local_output){
		sgRayf tempRay = new sgRayf(); 
		super.setToLocalOf(
				toSgRay(global_input), 
				tempRay
				);
			toRay(
				tempRay,
				local_output
			);		
	}
	
	public void setToRawLocalOf(Ray global_input, Ray local_output){
		sgRayf tempRay = new sgRayf(); 
		super.setToRawLocalOf(
				toSgRay(global_input), 
				tempRay
				);
		toRay(
				tempRay,
				local_output
			);	
	}
	
	public void setToOrthonormalLocalOf(Ray global_input, Ray local_output){
		sgRayf tempRay = new sgRayf(); 
		super.setToOrthonormalLocalOf(
				toSgRay(global_input), 
				tempRay
				);
			toRay(
				tempRay,
				local_output
			);	
	}	
	
	
	public PVector  getOrthoNormalizedLocalOf(PVector global_input){
		return toPVector(
				super.getOrthoNormalizedLocalOf(
						toSGVec(global_input)
						)
			);
	}
	
	
	public PVector  getOrientationalLocalOf(PVector input_global){
		return toPVector(
				super.getOrientationalLocalOf(
						toSGVec(input_global)
						)
			);
		
	}
	public PVector getOrientationalGlobalOf(PVector input_local){
		return toPVector(
				super.getOrientationalGlobalOf(
						toSGVec(input_local)
						)
			);
		
	}
	
	public void setToRawLocalOf(PVector in, PVector out){
		SGVec_3f tempVec = new SGVec_3f(); 
		super.setToRawLocalOf(
				toSGVec(in), 
				tempVec
				);
		toDVector(
				tempVec,
				out
			);
	}
	
		
	public void setToOrthoNormalLocalOf(PVector input_global, PVector output_local_normalized){
		SGVec_3f tempVec = new SGVec_3f(); 
		super.setToOrthoNormalLocalOf(
				toSGVec(input_global), 
				tempVec
				);
		toDVector(
				tempVec,
				output_local_normalized
			);
		
	}
	public void setToOrientationalLocalOf(PVector input_global, PVector output_local_orthonormal_chiral){
		SGVec_3f tempVec = new SGVec_3f(); 
		super.setToOrientationalLocalOf(
				toSGVec(input_global), 
				tempVec
				);
		toDVector(
				tempVec,
				output_local_orthonormal_chiral
			);
		
	}
	public void setToOrientationalGlobalOf(PVector input_local, PVector output_global_orthonormal_chiral){
		SGVec_3f tempVec = new SGVec_3f(); 
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
		float[] m = localMBasis.getComposedMatrix().val;
		SGVec_3f trans = localMBasis.translate;
		PMatrix result = new PMatrix3D(
				m[Matrix4f.M00], m[Matrix4f.M01], m[Matrix4f.M02], trans.x, 
				m[Matrix4f.M10], m[Matrix4f.M11], m[Matrix4f.M12], trans.y, 
				m[Matrix4f.M20], m[Matrix4f.M21], m[Matrix4f.M22], trans.z, 
				m[Matrix4f.M30], m[Matrix4f.M31], m[Matrix4f.M32], m[Matrix4f.M33]);
		return result;
	}
	
	public PMatrix getGlobalPMatrix() {
		this.updateGlobal();
		float[] m = globalMBasis.getComposedMatrix().val;
		SGVec_3f trans = globalMBasis.translate;
		PMatrix result = new PMatrix3D(
				m[Matrix4f.M00], m[Matrix4f.M01], m[Matrix4f.M02], trans.x, 
				m[Matrix4f.M10], m[Matrix4f.M11], m[Matrix4f.M12], trans.y, 
				m[Matrix4f.M20], m[Matrix4f.M21], m[Matrix4f.M22], trans.z, 
				m[Matrix4f.M30], m[Matrix4f.M31], m[Matrix4f.M32], m[Matrix4f.M33]);
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
	
	public static void drawRay(PApplet p, sgRayf r) {
		p.line(r.p1().x, r.p1().y, r.p1().z, r.p2().x, r.p2().y, r.p2().z);
	}
	
	public static void drawPoint(PApplet p, SGVec_3f pt) {
		p.point(pt.x, pt.y, pt.z);
	}





}
