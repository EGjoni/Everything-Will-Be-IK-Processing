import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import IK.doubleIK.AbstractIKPin;
import IK.doubleIK.AbstractKusudama;
import ewbik.processing.doublePrecision.dArmature;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dIKPin;
import ewbik.processing.doublePrecision.dKusudama;
import g4p_controls.GViewListener;
import math.Vec;
import math.doubleV.AbstractAxes;
import math.doubleV.SGVec_3d;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;
import ewbik.processing.doublePrecision.sceneGraph.*;

public class VisualDebug2D extends GViewListener {

	G4PUI ui;
	PApplet pa;
	public VisualDebug2D(PApplet p, G4PUI ui, BiConsumer<PGraphics3D, Integer> drawCall) {
		super();
		this.ui = ui;
		this.pa = p;
	}
	
	public void update() {
		PGraphics v = getGraphics();
		v.beginDraw();
		if(ui.selectedBone.getConstraint() != null) {
			v.clear();
			v.background(pa.color(0,0,0,100));
			v.resetMatrix();
			v.translate(v.width/2, v.height/2);
			v.pushMatrix();
			
			v.strokeWeight(0.015f);
			
			v.scale(100f);
			
		
			
			AbstractKusudama k = (AbstractKusudama) ui.selectedBone.getConstraint();
			Vec<?> min = k.twistMinVec;
			Vec<?> max = k.twistMaxVec;
			Vec<?> cent = k.twistCenterVec;
			Vec<?>[] twistlocs =  k.getTwistLocVecs(ui.selectedBone.localAxes());		
			Vec<?> twistLocZ = twistlocs[1];
			Vec<?> twistLocX = twistlocs[0];
			v.stroke(pa.color(0,0,255));
			v.line(0, 0, 0f, 1f);
			v.stroke(pa.color(0,255,0));
			v.line(0, 0, 1f, 0f);
			v.strokeWeight(0.035f);
			v.stroke(pa.color(255));
			v.line(0, 0, min.getXf(), min.getZf());
			v.stroke(pa.color(155,155,0));
			v.line(0, 0, cent.getXf(), cent.getZf());
			v.stroke(pa.color(0,0,0));
			v.line(0, 0, max.getXf(), max.getZf());
			v.stroke(pa.color(0,250,250));
			v.line(0, 0, twistLocZ.getXf()*2f, twistLocZ.getZf()*2f);
			v.stroke(pa.color(150,250,0));
			v.line(0, 0, twistLocX.getXf()*2f, twistLocX.getZf()*2f);
			
			v.popMatrix();
		}
		v.endDraw();
	}
}
