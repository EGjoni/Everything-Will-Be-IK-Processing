import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import IK.doubleIK.AbstractIKPin;
import ewbik.processing.doublePrecision.dArmature;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dIKPin;
import ewbik.processing.doublePrecision.dKusudama;
import g4p_controls.GViewListener;
import math.doubleV.AbstractAxes;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;
import ewbik.processing.doublePrecision.sceneGraph.*;

public class WidgetView extends View3D {

	public WidgetView(PApplet p, G4PUI ui, BiConsumer<PGraphics3D, Integer> drawCall) {
		super(p, ui, drawCall);
	}
	
	
	public void mouseDragged() {
		if(this.pressedHere) {
			dIKPin activePin = (dIKPin) ui.activePin;
			dAxes actvePinAxes = activePin.getAxes();
			DVector translation = (DVector) actvePinAxes.getGlobalMBasis().translate;
			PVector pTrans = new PVector((float)translation.x, (float)translation.y, (float)translation.z);
			PVector screen = screenOf((PGraphics3D)this.getGraphics(), pTrans, 1f);
			PVector unproject = getUnProjectedPointOnPlane((PGraphics3D)this.getGraphics(), this.mouseX(), this.mouseY(), pTrans);
			activePin.translateTo(new DVector(unproject));
			ui.doSolve = true;
		}
	}
	
	public void mouseReleased() {
		super.mouseReleased();
		ui.doSolve = false;
	}

	public void update() {
		PGraphics3D v = (PGraphics3D)getGraphics();
		v.beginDraw();
		v.clear();
		v.background(0, 150);
		this.setCamera(v, (float)ui.zoom, 1);
		if(predrawCall != null)
			predrawCall.accept(v, ui.WIDGET_3D);
		((dArmature)ui.armature).drawWidgets(v, ui.zoom*30);
		v.endDraw();
	}
}
