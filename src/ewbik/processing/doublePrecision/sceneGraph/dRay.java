package ewbik.processing.doublePrecision.sceneGraph;


import sceneGraph.math.doubleV.SGVec_3d;

/**
 * 
 * extremely basic implementation of a Ray 
 * object to serve as an example for how to 
 * extend this library
 * 
 * @author Eron Gjoni
 *
 */
public class dRay {

	public DVector p1, p2; 
	
	public dRay(DVector p1, DVector p2) {
		this.p1 = p1; 
		this.p2 = p2; 
	}

	public DVector p1() {
		if(p1 == null) p1 = new DVector();
		return this.p1;
	}
	
	public DVector p2() {
		if(p2 == null) p2 = new DVector();
		return this.p1;
	}
	
}
