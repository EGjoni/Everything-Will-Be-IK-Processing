import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import IK.doubleIK.AbstractIKPin;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dKusudama;
import ewbik.processing.doublePrecision.sceneGraph.DVector;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import g4p_controls.GViewListener;
import math.doubleV.AbstractAxes;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;

public class View3D extends GViewListener {

	PApplet pa;
	G4PUI ui;
	protected BiConsumer<PGraphics3D, Integer> predrawCall;
	protected AbstractAxes worldAxes;
	//protected double zoom = 1d;
	
	protected float orthoHeight, orthoWidth;
	protected PVector mouse = new PVector(0,0,0);				
	protected PVector cameraPosition = new PVector(0, 150, 500); 
	protected PVector lookAt = new PVector(0, 150, 0);
	protected PVector up = new PVector(0, -1, 0);
	

	protected boolean pressedHere = false;
	
	public View3D(PApplet p, G4PUI ui, BiConsumer<PGraphics3D, Integer> drawCall) {
		super();
		this.pa = p;
		this.ui = ui;
		this.predrawCall = drawCall;
	}
	
	public void mouseEntered() {
		if(!this.isMousePressed()) {
			this.pressedHere = false;
		}
	}
	public void mousePressed() {
		this.pressedHere = true;
	}
	
	public void mouseReleased() {
		this.pressedHere = false;
	}
	
	public void camera(PVector cp, PVector so, PVector up, PGraphics pg) {
		pg.camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
	}
	
	public void printXY(PGraphics pg, DVector pd) {
		PVector p = pd.toPVec();
		System.out.println(pg.screenX(p.x, p.y, p.z)
				+", " + pg.screenY(p.x, p.y, p.z));
	}

	public  PVector screenOf(PGraphics pg, PVector pt, float zoomScalar) {
		return new PVector(
				(pg.screenX((float)pt.x, (float)pt.y, (float)pt.z)),//*zoomScalar) - orthoWidth/2f,
				(pg.screenY((float)pt.x, (float)pt.y, (float)pt.z)));//*zoomScalar)- orthoHeight/2f);
	}

	public void setCamera(PGraphics pg, float zoomScalar, int mode) {
		dKusudama.renderMode = mode;
		dBone.renderMode = mode;
		dAxes.renderMode = mode;
		pg.clear();
		orthoHeight = pg.height*zoomScalar;
		orthoWidth = ((float)pg.width/(float)pg.height) * orthoHeight;
		//pg.ortho(-pg.width/(2f/zoomScalar), pg.width/(2f/zoomScalar), -pg.height/(2f/zoomScalar), pg.height/(2f/zoomScalar), -1000f, 1000f);
		float cameraZ = (pg.height/2f) / pa.tan(zoomScalar/2f);
		pg.perspective(zoomScalar, (float)pg.width/(float)pg.height, 
	            cameraZ/10f, cameraZ*10f);
		camera(cameraPosition, lookAt, up, pg);
		
		mouse.x =  (pg.modelX(this.mouseX(), this.mouseY(), 20f));// - (pg.width/2f)) * (orthoWidth/pg.width); mouse.y = (this.mouseY() - (pg.height/2f)) *  (orthoHeight/pg.height);
		mouse.y = (pg.modelY(this.mouseX(), this.mouseY(), 20f));
		mouse.z = (pg.modelZ(this.mouseX(), this.mouseY(), 20f));
		//pg.ortho(0, orthoWidth/2f, pa.width, pa.height, -1000, 1000); 
	}
	

	  // Function that calculates the coordinates on the floor surface corresponding to the screen coordinates
	  PVector getUnProjectedPointOnPlane(PGraphics3D pg, float screen_x, float screen_y, PVector planePosition) {

	  PVector f = planePosition.copy(); // Position of the floor
	  PVector n = PVector.sub(cameraPosition, lookAt);
	  n = n.normalize();/// The direction of the floor ( normal vector )
	  PVector w = unProject(pg, screen_x, screen_y, -1.0f); // 3 -dimensional coordinate corresponding to a point on the screen
	  PVector e = getEyePosition(pg); // Viewpoint position

	  // Computing the intersection of  
	  f.sub(e);
	  w.sub(e);
	  w.mult( n.dot(f)/n.dot(w) );
	  w.add(e);

	  return w;
	}

	// Function to get the position of the viewpoint in the current coordinate system
	PVector getEyePosition(PGraphics3D pg) {
	  PMatrix3D mat = (PMatrix3D)pg.getMatrix(); //Get the model view matrix
	  mat.invert();
	  return new PVector( mat.m03, mat.m13, mat.m23 );
	}
	//Function to perform the conversion to the local coordinate system ( reverse projection ) from the window coordinate system
	PVector unProject(PGraphics3D pg, float winX, float winY, float winZ) {
	  PMatrix3D mat = getMatrixLocalToWindow(pg);  
	  mat.invert();

	  float[] in = {winX, winY, winZ, 1.0f};
	  float[] out = new float[4];
	  mat.mult(in, out);  // Do not use PMatrix3D.mult(PVector, PVector)

	  if (out[3] == 0 ) {
	    return null;
	  }

	  PVector result = new PVector(out[0]/out[3], out[1]/out[3], out[2]/out[3]);  
	  return result;
	}

	//Function to compute the transformation matrix to the window coordinate system from the local coordinate system
	PMatrix3D getMatrixLocalToWindow(PGraphics3D pg) {
	  PMatrix3D projection = ((PGraphics3D)pg).projection; 
	  PMatrix3D modelview = ((PGraphics3D)pg).modelview;   

	  // viewport transf matrix
	  PMatrix3D viewport = new PMatrix3D();
	  viewport.m00 = viewport.m03 = pg.width/2;
	  viewport.m11 = -pg.height/2;
	  viewport.m13 =  pg.height/2;

	  // Calculate the transformation matrix to the window coordinate system from the local coordinate system
	  viewport.apply(projection);
	  viewport.apply(modelview);
	  return viewport;
	}
	
	public void reloadShaders() {
		
	}
}
