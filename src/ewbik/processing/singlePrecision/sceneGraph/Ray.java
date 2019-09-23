package ewbik.processing.singlePrecision.sceneGraph;

import processing.core.PVector;

/**
 * 
 * extremely basic implementation of a Ray 
 * object to serve as an example for how to 
 * extend this library
 * 
 * @author Eron Gjoni
 *
 */
public class Ray {

	public PVector p1, p2; 
	
	public Ray(PVector p1, PVector p2) {
		this.p1 = p1; 
		this.p2 = p2; 
	}

	public PVector p1() {
		if(p1 == null) p1 = new PVector();
		return this.p1;
	}
	
	public PVector p2() {
		if(p2 == null) p2 = new PVector();
		return this.p1;
	}
	
}
