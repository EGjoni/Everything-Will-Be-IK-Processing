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

import IK.doubleIK.AbstractKusudama;
import IK.doubleIK.AbstractLimitCone;
import IK.floatIK.AbstractBone;
import data.SaveManager;
import ewbik.processing.doublePrecision.sceneGraph.*;
import processing.core.PVector;
import sceneGraph.math.doubleV.SGVec_3d;
import sceneGraph.math.floatV.MathUtils;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractKusudama. Please refer to the {@link AbstractKusudama AbstractKusudama docs.} 
 */	
public class dKusudama extends AbstractKusudama {
	
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
	public AbstractLimitCone createLimitConeForIndex(int insertAt, SGVec_3d newPoint, double radius) {
		return new dLimitCone(dAxes.toDVector(newPoint), radius, this);		
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
	public void addLimitConeAtIndex(int insertAt, DVector newPoint, double radius) {
		super.addLimitConeAtIndex(insertAt, dAxes.toSGVec(newPoint), radius);
	}

	public boolean isInLimits(DVector inPoint) {
		return super.isInLimits_(
				dAxes.toSGVec(inPoint));
	}
}
