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

import IK.floatIK.AbstractArmature;
import IK.floatIK.AbstractBone;
import IK.floatIK.AbstractKusudama;
import IK.floatIK.AbstractLimitCone;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.Axes;
import math.floatV.MRotation;
import math.floatV.Rot;
import math.floatV.SGVec_3f;
import math.floatV.Vec3f;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;

/**
 * Note, this class is a concrete implementation of the abstract class AbstractKusudama. Please refer to the {@link AbstractKusudama AbstractKusudama docs.} 
 */	
public class Kusudama extends AbstractKusudama {
	
	public static PShader kusudamaShader; 
	public static PShader kusudamaStencil;
	public static int renderMode = 1; 
	protected static boolean multiPass = false;
	public static PShader currentShader; 

	float[] coneSequence; 
	int coneCount; 
	
	//default constructor required for file loading to work
	public Kusudama() {	}

	/**
	 * Kusudamas are a sequential collection of reach cones, forming a path by their tangents. <br><br>
	 *  
	 * A reach cone is essentially a cone bounding the rotation of a ball-and-socket joint.
	 * A reach cone is defined as a vector pointing in the direction which the cone is opening,
	 * and a radius (in radians) representing how much the cone is opening up. 
	 * <br><br>
	 * You can think of a Kusudama (taken from the Japanese word for "ball with a bunch of cones sticking out of it") as a ball with 
	 * with a bunch of reach-cones sticking out of it. Except that these reach cones are arranged sequentially, and a smooth path is 
	 * automatically inferred leading from one cone to the next.  
	 * 
	 * @param forBone the bone this kusudama will be attached to.
	 */
	public Kusudama(Bone forBone) {
		super(forBone);
	}


	/**
	 * {@inheritDoc}
	 **/
	@Override
	public AbstractLimitCone createLimitConeForIndex(int insertAt, Vec3f newPoint, float radius) {
		return new LimitCone(Axes.toPVector(newPoint), radius, this);		
	}
	
	/**
	 * Adds a LimitCone to the Kusudama. LimitCones are reach cones which can be arranged sequentially. The Kusudama will infer
	 * a smooth path leading from one LimitCone to the next. 
	 * 
	 * Using a single LimitCone is functionally equivalent to a classic reachCone constraint. 
	 * 
	 * @param insertAt the intended index for this LimitCone in the sequence of LimitCones from which the Kusudama will infer a path. @see IK.AbstractKusudama.limitCones limitCones array. 
	 * @param newPoint where on the Kusudama to add the LimitCone (in Kusudama's local coordinate frame defined by its bone's majorRotationAxes))
	 * @param radius the radius of the limitCone
	 */
	public void addLimitConeAtIndex(int insertAt, PVector newPoint, float radius) {
		super.addLimitConeAtIndex(insertAt, Axes.toSGVec(newPoint), radius);
	}
	

	public boolean isInLimits(PVector inPoint) {
		return super.isInLimits_(
				Axes.toSGVec(inPoint));
	}
	
	public void drawMe(PGraphics p, int boneCol, float pinSize) {		
		
		updateShaderTexture();
		
		PMatrix localMat = limitingAxes().getLocalPMatrix();
		p.applyMatrix(localMat);
		float circumference = (float) (attachedTo().getBoneHeight()/2.5f); 
		SGVec_3f min = new SGVec_3f(0f,0f,circumference);
		SGVec_3f current = new SGVec_3f(0f, 0f, circumference); 
		Rot minRot = new Rot(new SGVec_3f(0,1,0), minAxialAngle()); 
		float absAngle =minAxialAngle+range;
		Rot maxRot = new Rot(new SGVec_3f(0,1,0), absAngle); 

		float pieces = 20f; 
		float granularity =1f/pieces;  
		p.beginShape(PConstants.TRIANGLE_FAN);
		p.noStroke();	
		p.fill(0, 150, 0, 120);
		p.vertex(0,0,0);
		for(float i=0; i<=pieces+(3*granularity); i++) {
			MRotation interp = Rot.slerp(i*granularity, minRot.rotation, maxRot.rotation);
			current = interp.applyTo(min);
			p.vertex((float)current.x, (float)current.y, (float)(current.z));
		}		
		p.endShape();
		float r = p.red(System.identityHashCode(this));
		float g = p.green(System.identityHashCode(this));
		float b = p.blue(System.identityHashCode(this));
		p.fill(p.color(r,g,b));//p.color(255, 0, 255, 100));
		p.textureMode(p.NORMAL);
		p.shader(currentShader);
		if(renderMode == 0) 
			p.fill(p.color(r,g,b)); 
		else 
			p.fill(p.color(200, 0, 200, 255));

		currentShader.set("modelViewInv", ((PGraphicsOpenGL)p).modelviewInv);
		currentShader.set("coneSequence", coneSequence, 4);
		currentShader.set("coneCount", coneCount);
		p.sphereDetail(30);
		p.sphere((float)attachedTo().getBoneHeight()/3.5f);
		p.resetShader();
		Rot alignRot = limitingAxes.getGlobalMBasis().getInverseRotation().applyTo(attachedTo().localAxes().getGlobalMBasis().rotation);


		Rot[] decomposition = alignRot.getSwingTwist(new SGVec_3f(0,1,0));
		float angle = decomposition[1].getAngle() * decomposition[1].getAxis().y;
		Rot zRot = new Rot(new SGVec_3f(0,1,0), angle);
		SGVec_3f yaw = new SGVec_3f(0, 0, circumference); 
		yaw = zRot.applyToCopy(yaw);
		p.stroke(25, 25,195); 
		p.strokeWeight(4);
		p.line(0f,  0f,  0f,  (float)yaw.x, (float)yaw.y, (float)yaw.z);
		
		limitingAxes().drawMe(p, pinSize*2);

	}


	int lastRenderMode = -1; 
	protected void updateShaderTexture() {
		
		if(coneSequence == null || coneSequence.length != getLimitCones().size()*12 || coneCount != getLimitCones().size()) {
			coneSequence = new float[getLimitCones().size()*12];
			coneCount = getLimitCones().size();
		}
		
			int idx =0;
			for(LimitCone lc : getLimitCones()) {
				PVector controlPoint = Axes.toPVector(lc.getControlPoint()); 
				PVector leftTangent = Axes.toPVector(lc.tangentCircleCenterNext1);				
				PVector rightTangent =Axes.toPVector(lc.tangentCircleCenterNext2);
				leftTangent = leftTangent.normalize(); controlPoint = controlPoint.normalize(); rightTangent = rightTangent.normalize(); 
				float tanRan = (float) lc.tangentCircleRadiusNext; 
				float controlRan = (float) lc.getRadius();				 
				coneSequence[idx] = controlPoint.x; 
				coneSequence[idx+1]=controlPoint.y; 
				coneSequence[idx+2] =controlPoint.z; 
				coneSequence[idx+3] = controlRan;
				idx+= 4; 
				coneSequence[idx] = leftTangent.x; 
				coneSequence[idx+1]=leftTangent.y; 
				coneSequence[idx+2] =leftTangent.z; 
				coneSequence[idx+3] = tanRan;
				idx += 4;
				coneSequence[idx] = rightTangent.x; 
				coneSequence[idx+1]=rightTangent.y; 
				coneSequence[idx+2] =rightTangent.z; 
				coneSequence[idx+3] = tanRan;
				idx += 4; 
			}
		
				
			if(renderMode == 0) 
				currentShader = kusudamaStencil;				
			else
				currentShader = kusudamaShader;			
		
	}
	
	@Override
	public Axes limitingAxes() {
		return (Axes)super.limitingAxes();
	}
	
	@Override
	public void updateTangentRadii() {
		super.updateTangentRadii();
		updateShaderTexture();
	}
	
	@Override
	public ArrayList<LimitCone> getLimitCones() {
		return (ArrayList<LimitCone>)super.getLimitCones();
	}
	
	public static void enableMultiPass(boolean multipass) {
		if(multipass != multiPass) {
			multiPass = multipass;
			kusudamaShader.set("multiPass", multiPass);
		}
	}
	
}
