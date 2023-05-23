import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import IK.doubleIK.AbstractIKPin;
import ewbik.processing.doublePrecision.dArmature;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dKusudama;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import g4p_controls.GViewListener;
import math.doubleV.AbstractAxes;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;

public class SceneView extends View3D {

	boolean multipass = false;
	PGraphics3D stencil;
	PShader blurshader;
	public SceneView(PApplet p, G4PUI ui, BiConsumer<PGraphics3D, Integer> drawCall, boolean multipassAllowed) {
		super(p, ui, drawCall);
		multipass = multipassAllowed;
		reloadShaders();
	}

	public void mouseClicked() {
		pa.println("sceneView");
	}

	public void update() {
		PGraphics3D v = (PGraphics3D)getGraphics();
		dKusudama.frame = pa.frameCount;
		if(multipass) {
			if(stencil == null) {
				stencil = (PGraphics3D)pa.createGraphics(v.width, v.height, pa.P3D);
				stencil.noSmooth();
			}
			stencil.resetMatrix();
			stencil.beginDraw();
			setCamera(stencil, (float)ui.zoom, ui.STENCIL);
			if(predrawCall != null)
				predrawCall.accept(stencil, ui.STENCIL);
			((dArmature)ui.armature).drawMe(stencil, 100, ui.zoom);
			stencil.endDraw();
		} else {
			dKusudama.enableMultiPass(false);
		}
		v.beginDraw();
		v.clear();
		v.resetMatrix();
		//stencil.save("stenciltest.png");
		setSceneAndCamera(v, (float)ui.zoom, ui.RENDER);	
		v.background(80, 150, 190);
		if(predrawCall != null)
			predrawCall.accept(v, ui.RENDER);
		((dArmature)ui.armature).drawMe(v, 100, ui.zoom);
		if(multipass) {
			blurshader.set("mask", stencil);
			blurshader.set("horizontalPass", 0); v.filter(blurshader);
			blurshader.set("horizontalPass", 1);  v.filter(blurshader);	
		}
		v.endDraw();
	}
	public void camera(PVector cp, PVector so, PVector up, PGraphics pg) {
		pg.camera(cp.x, cp.y, cp.z, so.x, so.y, so.z, up.x, up.y, up.z);
	}

	public void setSceneAndCamera(PGraphics pg, float zoomScalar, int mode) {
		setCamera(pg, zoomScalar, mode);	
		pg.directionalLight(148, 148, 148, 0, -100, 100);
		pg.directionalLight(148, 148, 148, 0, -100, -100);
		pg.directionalLight(148, 148, 148, 100, -100, 0);
		pg.directionalLight(148, 148, 148, -100, -100, 0);
		pg.directionalLight(48, 48, 48, 100, 10, 100);
		pg.directionalLight(48, 48, 48, 100, 10, -100);
		pg.directionalLight(48, 48, 48, -100, 10, 100);
		pg.directionalLight(48, 48, 48, -100, 10, -100);
	}
	
	public void reloadShaders() {
		dKusudama.kusudamaShader = pa.loadShader("ewbIK/src/ewbik/processing/kusudama.glsl", "ewbIK/src/ewbik/processing/kusudama_vert.glsl");
		dKusudama.kusudamaStencil = pa.loadShader("ewbIK/src/ewbik/processing/kusudama_stencil.glsl", "ewbIK/src/ewbik/processing/kusudama_vert.glsl");
		dKusudama.twistShader = pa.loadShader("ewbIK/src/ewbik/processing/fan.glsl", "ewbIK/src/ewbik/processing/kusudama_vert.glsl");
		if(multipass) {			
			System.out.println(pa.sketchPath());
			blurshader = pa.loadShader( "ewbIK/src/ewbik/processing/blur-sep.glsl");
			blurshader.set("blurSize", 60);
			blurshader.set("sigma", 40f); 
			dKusudama.enableMultiPass(false);
			dKusudama.enableMultiPass(true);
		}
	}

}
