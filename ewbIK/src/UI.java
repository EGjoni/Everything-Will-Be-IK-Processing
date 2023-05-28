import IK.doubleIK.AbstractBone;
import ewbik.processing.doublePrecision.dArmature;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dIKPin;
import ewbik.processing.doublePrecision.dKusudama;
import ewbik.processing.doublePrecision.sceneGraph.DVector;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import ewbik.processing.singlePrecision.Armature;
import ewbik.processing.singlePrecision.Bone;
import ewbik.processing.singlePrecision.IKPin;
import ewbik.processing.singlePrecision.Kusudama;
import ewbik.processing.singlePrecision.sceneGraph.Axes;
import math.doubleV.Vec3d;
import math.floatV.SGVec_3f;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

public class UI {
	PApplet pa; 
	PGraphics display, stencil;
	PShader blurshader;
	public boolean multipass = false; 

	public UI(PApplet p, boolean multipassAllowed) {
		pa = p;
		currentDrawSurface = pa.g; 
		if(multipassAllowed) {
			stencil = pa.createGraphics(p.width, p.height, pa.P3D);
			display = pa.createGraphics(p.width, p.height, pa.P3D);
			stencil.noSmooth();
			display.smooth(8);
			System.out.println(p.sketchPath());
			blurshader = pa.loadShader( "ewbIK/src/ewbik/processing/blur-sep.glsl");
			blurshader.set("blurSize", 20);
			blurshader.set("sigma", 9f);
			multipass = true;  			
		}
		dKusudama.kusudamaShader = pa.loadShader("ewbIK/src/ewbik/processing/kusudama.glsl", "ewbIK/src/ewbik/processing/kusudama_vert.glsl");
		dKusudama.kusudamaStencil = pa.loadShader("ewbIK/src/ewbik/processing/kusudama_stencil.glsl", "ewbIK/src/ewbik/processing/kusudama_vert.glsl");
		Kusudama.kusudamaShader = dKusudama.kusudamaShader; 
		Kusudama.kusudamaStencil = dKusudama.kusudamaStencil;
		
	}	

	public void drawBoneInfo(PGraphics pg, dBone bone, int idx) {
		if(bone.isPinned()) {
			pg.strokeWeight(10); 
			pg.stroke(255,0,0); 
			point(pg, bone.getPinLocation());
		}

		String boneAngles = "";
		try {
			double[] angleArr = bone.getXYZAngle();
			boneAngles += " D ( " + pa.degrees((float)angleArr[0]) + ",   " + pa.degrees((float)angleArr[1]) + ",   " + pa.degrees((float)angleArr[2]) + "  )";
			pg.fill(0);
			pg.text(boneAngles, (-pa.width/2) +10,  (-pa.height/2) + (10*idx)); 
		} catch (Exception e) {
		}        
	}

	public void drawBoneInfo(PGraphics pg, Bone bone, int idx) {
		if(bone.isPinned()) {
			pg.strokeWeight(10); 
			pg.stroke(255,0,0); 
			point(pg, bone.getPinLocation());
		}

		String boneAngles = "";
		try {
			float[] angleArr = bone.getXYZAngle();
			boneAngles += " D ( " + pa.degrees((float)angleArr[0]) + ",   " + pa.degrees((float)angleArr[1]) + ",   " + pa.degrees((float)angleArr[2]) + "  )";
			pg.fill(0);
			pg.text(boneAngles, (-pa.width/2) +10,  (-pa.height/2) + (10*idx)); 
		} catch (Exception e) {
		}        
	}


	public void drawInstructions(PGraphics pg, String append, float zoomScalar) {
		String appended = append == null ? "" : "-"+append;
		
		String instructionText =
				"-Click and drag to move the selected pin.\n"
						+"-To select a different pin, use the Up and Down arrows.\n"
						+ "-Use the mouse wheel to rotate the pin about its (red) Y axis.\n" 
						+ "-Hold shift while using the mouse wheel to rotate the pin about its (blue) Z axis.\n"
						+ "-Hold ctrl while using the mouse wheel to rotate the pin about its (green) X axis. \n" 
						+ appended;
		//pg.textMode(pa.MODEL);
		if(pg.textFont == null)
			pg.textSize(12);
		else {
			pg.textSize(12);
		}
		//PFont instructionFont = pa.createFont(pg.textFont.getName(), 42);
		pg.fill(0,0,0, 90);
		float boxW = pg.textWidth(instructionText); 
		float boxH = (pg.textAscent() + pg.textDescent()) * (instructionText.split("\n").length);
		pg.rect((-pa.width/2f)+30, (-pa.height/2f)+15, boxW+25, boxH+40);
		pg.fill(255, 255, 255, 255);
		pg.emissive(255, 255, 255);
		pg.text(instructionText, (-pa.width/2f) + 50f, -pa.height/2f + 40f);
		
	}

	public void printXY(PGraphics pg, PVector p) {
		System.out.println(pg.screenX(p.x, p.y, p.z)
				+", " + pg.screenY(p.x, p.y, p.z));
	}
	public void line(PGraphics pg, PVector p1, PVector p2) {
		pg.line(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
	}

	public void point(PGraphics pg, PVector p) {
		pg.point(p.x, p.y, p.z);
	}

	public void printXY(PGraphics pg, DVector pd) {
		PVector p = pd.toPVec();
		System.out.println(pg.screenX(p.x, p.y, p.z)
				+", " + pg.screenY(p.x, p.y, p.z));
	}
	public void line(PGraphics pg, DVector p1, DVector p2) {
		PVector p1f = p1.toPVec();
		PVector p2f = p2.toPVec(); 
		pg.line(p1f.x, p1f.y, p1f.z, p2f.x, p2f.y, p2f.z);
	}

	public void point(PGraphics pg, DVector pd) {
		PVector p = pd.toPVec();
		pg.point(p.x, p.y, p.z);
	}	

	public void drawPins(PGraphics pg, dIKPin activePin, 
			float zoomScalar, float drawSize,
			boolean cubeMode, dAxes cubeAxes) {

		if(activePin != null) {
			dAxes ellipseAx = cubeMode ? cubeAxes : (dAxes) activePin.getAxes();
			PVector pinLoc =  screenOf(pg, ellipseAx.origin_(), zoomScalar) ;
			PVector pinX = screenOf(pg, ellipseAx.x_().getScaledTo(drawSize), zoomScalar);
			PVector pinY = screenOf(pg, ellipseAx.y_().getScaledTo(drawSize), zoomScalar);
			PVector pinZ = screenOf(pg, ellipseAx.z_().getScaledTo(drawSize), zoomScalar);
			pg.fill(255,255,255, 150);
			pg.stroke(255, 0, 255);
			float totalpriorities = (float)(activePin.getXPriority() + activePin.getYPriority() + activePin.getZPriority()); 
			pg.ellipse(pinLoc.x, pinLoc.y, zoomScalar*50, zoomScalar*50);

			PVector effectorO = screenOf(pg, activePin.forBone().localAxes().origin_(), zoomScalar);
			PVector effectorX = screenOf(pg, activePin.forBone().localAxes().x_().getScaledTo(drawSize), zoomScalar);
			PVector effectorY = screenOf(pg, activePin.forBone().localAxes().y_().getScaledTo(drawSize), zoomScalar);
			PVector effectorZ = screenOf(pg, activePin.forBone().localAxes().z_().getScaledTo(drawSize), zoomScalar);
			pg.stroke(255,255,255,150);

			if(!cubeMode) {
				float xPriority = (float) activePin.getXPriority();
				float yPriority = (float) activePin.getYPriority();
				float zPriority = (float) activePin.getZPriority();
				drawPinEffectorHints(
						pg,
						pinLoc, 
						pinX, pinY, pinZ, 
						effectorO, 
						effectorX, effectorY, effectorZ,
						xPriority, yPriority, zPriority, totalpriorities
						);
			}
		}
	}
	
	public void drawPins(PGraphics pg, IKPin activePin, 
			float zoomScalar, float drawSize,
			boolean cubeMode, Axes cubeAxes) {

		if(activePin != null) {
			Axes ellipseAx = cubeMode ? cubeAxes : (Axes) activePin.getAxes();
			PVector pinLoc =  screenOf(pg, ellipseAx.origin(), zoomScalar) ;
			PVector pinX = screenOf(pg, Axes.toPVector(ellipseAx.x_().getScaledTo(drawSize)), zoomScalar);
			PVector pinY = screenOf(pg, Axes.toPVector(ellipseAx.y_().getScaledTo(drawSize)), zoomScalar);
			PVector pinZ = screenOf(pg, Axes.toPVector(ellipseAx.z_().getScaledTo(drawSize)), zoomScalar);
			pg.fill(255,255,255, 150);
			pg.stroke(255, 0, 255);
			float totalpriorities = (float)(activePin.getXPriority() + activePin.getYPriority() + activePin.getZPriority()); 
			pg.ellipse(pinLoc.x, pinLoc.y, zoomScalar*50, zoomScalar*50);

			PVector effectorO = screenOf(pg, Axes.toPVector(activePin.forBone().localAxes().origin_()), zoomScalar);
			PVector effectorX = screenOf(pg, Axes.toPVector(activePin.forBone().localAxes().x_().getScaledTo(drawSize)), zoomScalar);
			PVector effectorY = screenOf(pg, Axes.toPVector(activePin.forBone().localAxes().y_().getScaledTo(drawSize)), zoomScalar);
			PVector effectorZ = screenOf(pg, Axes.toPVector(activePin.forBone().localAxes().z_().getScaledTo(drawSize)), zoomScalar);
			pg.stroke(255,255,255,150);

			if(!cubeMode) {
				float xPriority = (float) activePin.getXPriority();
				float yPriority = (float) activePin.getYPriority();
				float zPriority = (float) activePin.getZPriority();
				drawPinEffectorHints(
						pg,
						pinLoc, 
						pinX, pinY, pinZ, 
						effectorO, 
						effectorX, effectorY, effectorZ,
						xPriority, yPriority, zPriority, totalpriorities
						);
			}
		}
	}

	public void drawPinEffectorHints(PGraphics pg, 
			PVector pinLoc, 
			PVector pinX, PVector pinY, PVector pinZ, 
			PVector effectorO, 
			PVector effectorX, PVector effectorY, PVector effectorZ,
			float xPriority, float yPriority, float zPriority, float totalpriorities) {
		
			pg.line(pinLoc.x, pinLoc.y, pinLoc.z, effectorO.x, effectorO.y, effectorO.z);
			pg.stroke(0,255,0,150);
			pg.strokeWeight(2f*xPriority / totalpriorities);
			pg.line(pinX.x, pinX.y, pinX.z,  effectorX.x, effectorX.y, effectorX.z);
			pg.stroke(255,0,0,150);
			pg.strokeWeight(2f*yPriority/ totalpriorities);
			pg.line(pinY.x, pinY.y, pinY.z, effectorY.x, effectorY.y, effectorY.z);
			pg.stroke(0, 0,255,150);
			pg.strokeWeight(2f*zPriority / totalpriorities);
			pg.line(pinZ.x, pinZ.y, pinZ.z, effectorZ.x, effectorZ.y, effectorZ.z);

	}

	public void drawPass(int mode, float drawSize, Runnable preArmatureDraw, PGraphics buffer, Armature armature) {
		Kusudama.renderMode = mode;
		Bone.renderMode = mode;
		Axes.renderMode = mode;
		if(preArmatureDraw != null)
			preArmatureDraw.run();
		armature.drawMe( buffer, 100, drawSize);
	}	

	public void drawPass(int mode, float drawSize, Runnable preArmatureDraw, PGraphics buffer, dArmature armature) {
		dKusudama.renderMode = mode;
		dBone.renderMode = mode;
		dAxes.renderMode = mode;
		if(preArmatureDraw != null)
			preArmatureDraw.run();
		armature.drawMe( buffer, 100, drawSize, null);
	}	

	public  PVector screenOf(PGraphics pg, PVector pt, float zoomScalar) {
		return new PVector(
				(pg.screenX((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar) - orthoWidth/2f,
				(pg.screenY((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar)- orthoHeight/2f);
	}

	public<V extends Vec3d<?>>  PVector screenOf(PGraphics pg, V pt, float zoomScalar) {
		return new PVector(
				(pg.screenX((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar)- orthoWidth/2f,
				(pg.screenY((float)pt.x, (float)pt.y, (float)pt.z)*zoomScalar)- orthoHeight/2f);
	}


	private PGraphics currentDrawSurface = null;
	
	public void drawScene(float zoomScalar, float drawSize,
			Runnable additionalDraw, 
			dArmature armature, 
			String usageInstructions,
			dIKPin activePin, dAxes cubeAxes, boolean cubeEnabled) {
		if(multipass) {
			dKusudama.enableMultiPass(true);
			currentDrawSurface = stencil;
			
			stencil.beginDraw();
				setCamera(stencil, zoomScalar);
				drawPass(0, drawSize, additionalDraw, stencil, armature);  
			stencil.endDraw();
			
			currentDrawSurface = display;
			display.beginDraw();
				setSceneAndCamera(display, zoomScalar);
				drawPass(1, drawSize, additionalDraw, display, armature);		
				blurshader.set("mask", stencil);
				blurshader.set("horizontalPass", 0); display.filter(blurshader);
				blurshader.set("horizontalPass", 1);  display.filter(blurshader);			
			display.endDraw();

			currentDrawSurface = pa.g;
			setCamera(pa.g, zoomScalar);
			pa.background(80, 150, 190);
			pa.imageMode(pa.CENTER);
			pa.image(display, 0, 0, orthoWidth, orthoHeight);			
			pa.resetMatrix();
			drawPins(pa.g, activePin, zoomScalar, drawSize, cubeEnabled, cubeAxes);
			pa.resetMatrix();	float cx =pa.width; 	float cy =pa.height;
			pa.ortho(-cx/2f, cx/2f,  -cy/2f, cy/2f, -1000, 1000);
			drawInstructions(pa.g, usageInstructions, zoomScalar); 
			drawPins(pa.g, activePin, drawSize, zoomScalar, cubeEnabled, cubeAxes);
			drawInstructions(pa.g, usageInstructions, zoomScalar); 
		}	else {			
			dKusudama.enableMultiPass(false);
			currentDrawSurface = pa.g;
			setSceneAndCamera(pa.g, zoomScalar);
			pa.background(80, 150, 190);
			drawPass(1, drawSize, additionalDraw, pa.g, armature);
			pa.resetMatrix();
			drawPins(pa.g, activePin, zoomScalar, drawSize, cubeEnabled, cubeAxes);
			pa.resetMatrix();	float cx =pa.width; 	float cy =pa.height;
			pa.ortho(-cx/2f, cx/2f,  -cy/2f, cy/2f, -1000, 1000);
			drawInstructions(pa.g, usageInstructions, zoomScalar); 
		}
	}


	public void drawScene(float zoomScalar, float drawSize, 
			Runnable additionalDraw, 
			Armature armature, 
			String usageInstructions,
			IKPin activePin, Axes cubeAxes, boolean cubeEnabled) {
		if(multipass) {
			Kusudama.enableMultiPass(true);
			currentDrawSurface = stencil;
			
			stencil.beginDraw();
				setCamera(stencil, zoomScalar);
				drawPass(0, drawSize, additionalDraw, stencil, armature);  
			stencil.endDraw();
			
			currentDrawSurface = display;
			display.beginDraw();
				setSceneAndCamera(display, zoomScalar);
				drawPass(1, drawSize, additionalDraw, display, armature);		
				blurshader.set("mask", stencil);
				blurshader.set("horizontalPass", 0); display.filter(blurshader);
				blurshader.set("horizontalPass", 1);  display.filter(blurshader);			
			display.endDraw();

			currentDrawSurface = pa.g;
			setCamera(pa.g, zoomScalar);
			pa.background(80, 150, 190);
			pa.imageMode(pa.CENTER);
			pa.image(display, 0, 0, orthoWidth, orthoHeight);			
			pa.resetMatrix();
			drawPins(pa.g, activePin, zoomScalar, drawSize, cubeEnabled, cubeAxes);
			pa.resetMatrix();	float cx =pa.width; 	float cy =pa.height;
			pa.ortho(-cx/2f, cx/2f,  -cy/2f, cy/2f, -1000, 1000);
			drawInstructions(pa.g, usageInstructions, zoomScalar); 
			drawPins(pa.g, activePin, drawSize, zoomScalar, cubeEnabled, cubeAxes);
			drawInstructions(pa.g, usageInstructions, zoomScalar); 
		}	else {
			Kusudama.enableMultiPass(false);
			currentDrawSurface = pa.g;
			setSceneAndCamera(pa.g, zoomScalar);
			pa.background(80, 150, 190);
			drawPass(1, drawSize, additionalDraw, pa.g, armature);
			pa.resetMatrix();
			drawPins(pa.g, activePin, zoomScalar, drawSize, cubeEnabled, cubeAxes);
			pa.resetMatrix();	float cx =pa.width; 	float cy =pa.height;
			pa.ortho(-cx/2f, cx/2f,  -cy/2f, cy/2f, -1000, 1000);
			drawInstructions(pa.g, usageInstructions, zoomScalar); 
		}
	}


	PVector mouse = new PVector(0,0,0);				
	PVector cameraPosition = new PVector(0, 0, 70); 
	PVector lookAt = new PVector(0, 0, 0);
	PVector up = new PVector(0, 1, 0);


	public void toggleMultipass() {
		multipass = !multipass;
		if(stencil == null && multipass) {
			stencil = pa.createGraphics(1200, 900, pa.P3D);
			display = pa.createGraphics(1200, 900, pa.P3D);
			stencil.noSmooth();
			display.smooth(8);
			blurshader = pa.loadShader( "src/ewbik/processing/blur-sep.glsl");
			blurshader.set("blurSize", 20);
			blurshader.set("sigma", 9f);
		}
	}

	public void camera(PVector cp, PVector so, PVector up, PGraphics pg) {
		pg.camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
	}

	public void setSceneAndCamera(PGraphics pg, float zoomScalar) {
		setCamera(pg, zoomScalar);		
		pg.directionalLight(148, 148, 148, 0, 100, 100);
		pg.directionalLight(148, 148, 148, 0, 100, -100);
		pg.directionalLight(148, 148, 148, 100, 100, 0);
		pg.directionalLight(148, 148, 148, -100, 100, 0);
		pg.directionalLight(48, 48, 48, 100, -10, 100);
		pg.directionalLight(48, 48, 48, 100, -10, -100);
		pg.directionalLight(48, 48, 48, -100, -10, 100);
		pg.directionalLight(48, 48, 48, -100, -10, -100);	

	}

	float orthoHeight, orthoWidth;

	public void setCamera(PGraphics pg, float zoomScalar) {
		pg.clear();
		orthoHeight = pa.height*zoomScalar;
		orthoWidth = ((float)pa.width/(float)pa.height) * orthoHeight; 
		mouse.x =  (pa.mouseX - (pa.width/2f)) * (orthoWidth/pa.width); mouse.y = (pa.mouseY - (pa.height/2f)) *  (orthoHeight/pa.height);
		camera(cameraPosition, lookAt, up, pg);
		pg.ortho(-orthoWidth/2f, orthoWidth/2f, -orthoHeight/2f, orthoHeight/2f, -1000, 1000); 
	}
	
	/**
	 * @return the draw surface this class is currently operating on. 
	 * This is used as kind of a hack so I don't have to bother writing interfaces  just to render a box when using multipass. 
	 */
	public PGraphics getCurrentDrawSurface() {
		return currentDrawSurface;
	}

}
