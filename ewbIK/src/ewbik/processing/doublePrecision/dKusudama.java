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


import IK.doubleIK.AbstractKusudama;
import IK.doubleIK.AbstractLimitCone;
import ewbik.processing.doublePrecision.sceneGraph.*;
import math.doubleV.AbstractAxes;
import math.doubleV.MRotation;
import math.doubleV.Rot;
import math.doubleV.SGVec_3d;
import math.doubleV.Vec3d;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractKusudama. Please refer to the {@link AbstractKusudama AbstractKusudama docs.} 
 */	
public class dKusudama extends AbstractKusudama {
	public static PShader kusudamaShader; 
	public static PShader kusudamaStencil;
	public static int renderMode = 1; 
	protected static boolean multiPass = false;
	public static PShader currentShader; 

	float[] coneSequence; 
	int coneCount; 

	public dKusudama() {}

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
	public dKusudama(dBone forBone) {
		super(forBone); 
	}


	/**
	 * {@inheritDoc}
	 **/
	@Override
	public AbstractLimitCone  createLimitConeForIndex(int insertAt, Vec3d<?> newPoint, double radius) {
		return new dLimitCone(newPoint, radius, this);		
	}

	public void drawMe(PGraphics p, int boneCol, float pinSize) {		
		
		updateShaderTexture();
		
		PMatrix localMat = ((dAxes)twistOrientationAxes()).getLocalPMatrix();
		p.applyMatrix(localMat);
		float circumference = (float) (attachedTo().getBoneHeight()/2.5f); 
		DVector min = new DVector(this.twistMinVec).setMag(circumference);
		DVector current = new DVector(min); 
		double absAngle =minAxialAngle+range;
		Rot maxRot = new Rot(new DVector(0,1,0), absAngle); 

		double pieces = 20d; 
		double granularity =1d/pieces;  
		p.beginShape(PConstants.TRIANGLE_FAN);
		p.noStroke();	
		p.fill(0, 150, 0, 120);
		p.vertex(0,0,0);
		for(double i=0; i<=pieces+(3*granularity); i++) {
			MRotation interp = new Rot(new DVector(0,1,0), i*granularity*range).rotation;//Rot.slerp(i*granularity, twistMinRot.rotation, twistMaxRot.rotation);
			current = interp.applyTo(min);
			p.vertex((float)current.x, (float)current.y, (float)(current.z));
		}		
		double twistRatio = getTwistRatio();
		MRotation interp = new Rot(new DVector(0,1,0), twistRatio*range).rotation;
		DVector roll = interp.applyTo(min);		
		p.endShape();
		p.stroke(25, 25,195); 
		p.strokeWeight(4);
		p.line(0f,  0f,  0f,  (float)roll.x, (float)roll.y, (float)roll.z);
		//p.strokeWeight(2);
		//p.line(0f,  0f,  0f, 0f, 0f, 100f);
		p.noStroke();
		p.popMatrix();
		p.pushMatrix();
		localMat = ((dAxes)swingOrientationAxes()).getLocalPMatrix();
		p.applyMatrix(localMat);
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
		Rot alignRot = twistAxes.getGlobalMBasis().getInverseRotation().applyTo(attachedTo().localAxes().getGlobalMBasis().rotation);

	}


	int lastRenderMode = -1; 
	protected void updateShaderTexture() {
		
		if(coneSequence == null || coneSequence.length != getLimitCones().size()*12 || coneCount != getLimitCones().size()) {
			coneSequence = new float[getLimitCones().size()*12];
			coneCount = getLimitCones().size();
		}
		
			int idx =0;
			for(dLimitCone lc : getLimitCones()) {
				PVector controlPoint = new DVector(lc.getControlPoint()).toPVec(); 
				PVector leftTangent = new DVector(lc.tangentCircleCenterNext1).toPVec();				
				PVector rightTangent = new DVector(lc.tangentCircleCenterNext2).toPVec();
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
	public void updateTangentRadii() {
		super.updateTangentRadii();
		updateShaderTexture();
	}

	@Override
	public ArrayList<dLimitCone> getLimitCones() {
		return (ArrayList<dLimitCone>)super.getLimitCones();
	}

	public static void enableMultiPass(boolean multipass) {
		if(multipass != multiPass) {
			multiPass = multipass;
			kusudamaShader.set("multiPass", multiPass);
		}
	}

}
