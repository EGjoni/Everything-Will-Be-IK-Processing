import processing.core.PApplet;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

import IK.doubleIK.AbstractKusudama;
import IK.doubleIK.AbstractLimitCone;
import IK.doubleIK.Constraint;
import g4p_controls.GAlign;
import g4p_controls.GButton;
import g4p_controls.GCScheme;
import g4p_controls.GDropList;
import g4p_controls.GEvent;
import g4p_controls.GKnob;
import g4p_controls.GLabel;
import g4p_controls.GPanel;
import g4p_controls.GSlider;
import g4p_controls.GSlider2D;
import g4p_controls.GSpinner;
import g4p_controls.GWindow;
import math.Vec;
import math.doubleV.SGVec_3d;

public class Dialogs {
	
	
	HashMap<Constraint, KusudamaDialog> kusudamaDialogMap = new HashMap<>();
	G4PUI ui;
	public Dialogs(G4PUI ui) {
		this.ui = ui;
	}
	
	public KusudamaDialog getDialogFor(AbstractKusudama forKusudama) {
		if(forKusudama != null) {
			KusudamaDialog result = kusudamaDialogMap.get(forKusudama);
			if(result == null) {
				result = new KusudamaDialog(forKusudama);
			}
			return result;
		}
		return null;
	}
	
	public class KusudamaDialog {
		AbstractKusudama kusudama;
		PApplet pa;
		GPanel constraintProperties, childBonesGroup;
		GPanel addLimitCone;
		GSpinner newLimitConeIndex;
		GSlider2D coneOrientation;
		GSlider coneSpread, painfulness;
		GKnob coneSpreadVisualizer;
		GButton cancelAddCone, confirmAddCone;
		public ArrayList<? extends AbstractLimitCone> limitConeCollection;
		public AbstractLimitCone selectedCone;
		public GSlider twistMin;
		public GSlider twistRange;
		
		public GDropList  limitConeList;
		
		public KusudamaDialog(AbstractKusudama forConstraint) {
			this.kusudama = forConstraint;
			pa = GWindow.getWindow(pa, "Kusudama Properties for " + kusudama.attachedTo().getTag(), 
					10, 200, 300, 400, PApplet.P2D);
			
		}
		
		private GPanel ConstraintPanel_launch() {
			constraintProperties = new GPanel(pa, 10, 200, 300, 400, "Constraint Properties");
			constraintProperties.setLocalColorScheme(GCScheme.CYAN_SCHEME);
			constraintProperties.setFont(new Font((String)null, 0, 16));
			constraintProperties.setDraggable(false);
			//constraintProperties.setDragArea(10, 50, 120, 30);
			constraintProperties.setCollapsed(false);
			constraintProperties.setVisible(false);
			ui.insertCloseButton(constraintProperties);
			painfulness = new GSlider(pa, 80, constraintProperties.getHeight()-50, 70, 50, 12);
			painfulness.setShowValue(true);
			painfulness.setShowTicks(true);
			painfulness.setLimits(0, 1f);
			painfulness.setLocalColorScheme(GCScheme.CYAN_SCHEME);
			painfulness.addEventHandler(this, "edit_painfulness");
			
			twistMin = new GSlider(pa, 10, 30, 140, 60, 12);
			twistMin.setShowDecor(false, false, true, false);
			twistMin.setLimits(-pa.PI/4, -pa.PI, pa.PI);
			GLabel twm = new GLabel(pa, 160f, 30f, 100f, 65f, "Twist Minimum");
			twistMin.addEventHandler(this, "edit_twistMin");
			twm.setTextAlign(GAlign.LEFT, GAlign.CENTER);
			twistRange = new GSlider(pa, 10, 80, 140, 60, 12);
			twistRange.addEventHandler(this, "edit_twistRange");
			twistRange.setShowDecor(false, false, true, false);
			twistRange.setLimits(pa.PI/2, -pa.PI*2, pa.PI*2);
			GLabel twr = new GLabel(pa, 160f, 80, 100, 65, "Twist Range");
			twr.setTextAlign(GAlign.LEFT, GAlign.CENTER);
			//GKnob twistVisual = new GKnob(pa, )
			GLabel pain = new GLabel(pa, 0, constraintProperties.getHeight()-50, 75, 50);
			pain.setText("Painfulness:");
			pain.setTextAlign(GAlign.RIGHT, GAlign.CENTER);
			GPanel conesPanel = conesPanel_init();
			constraintProperties.addControls(painfulness, pain, conesPanel, twistMin, twistRange, twm, twr);
			
			return constraintProperties;
		}
		
		public void updateUI() {
			this.painfulness.setValue((float)ui.selectedBone.getConstraint().getPainfulness());
			this.limitConeCollection = ((AbstractKusudama)ui.selectedBone.getConstraint()).getLimitCones();
			String[] cones = new String[this.limitConeCollection.size()];
			for(int i=0; i<this.limitConeCollection.size(); i++)
				cones[i] = "Cone "+i;
			this.limitConeList.setItems(cones, 0);
			this.selectedCone = this.limitConeCollection.get(0);
			this.limitConeList.setSelected(0);
			this.coneSpread.setValue((float)this.selectedCone.getRadius());
			
		}
		
		public GPanel conesPanel_init() {
			GPanel conesPanel = new GPanel(pa, 10, constraintProperties.getHeight()-250, constraintProperties.getWidth()-20, 200, "Limit Cones");
			conesPanel.setCollapsible(false);
			conesPanel.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
			conesPanel.setDraggable(false);
			limitConeList = new GDropList(pa, 20, 25, 160, 70);	
			limitConeList.setFont(new Font((String)null, 0, 14));
			limitConeList.addEventHandler(this, "limitCone_select");
			addLimitCone = new GPanel(pa, 185, 25, 100, 100, "Add Cone");
			addLimitCone.setDraggable(false);
			addLimitCone.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
			coneOrientation = new GSlider2D(pa, 10, 45, 200, 140);
			coneOrientation.setLimitsX(-pa.PI*2, pa.PI*2);
			coneOrientation.setLimitsY(pa.PI, -pa.PI);
			coneOrientation.setValueX(0);
			coneOrientation.setValueY(0);
			coneOrientation.setAlpha(230);
			GLabel lon = new GLabel(pa, 0, (coneOrientation.getHeight() + coneOrientation.getY()) - 5, coneOrientation.getWidth(), 40, "Longitude");
			lon.setTextAlign(GAlign.CENTER, GAlign.TOP);
			lon.setFont(new Font((String)null, 0, 14));
			GLabel lat = new GLabel(pa, -8, coneOrientation.getHeight() + coneOrientation.getY(), coneOrientation.getHeight(), 40, "Latitute");
			lat.setTextAlign(GAlign.CENTER, GAlign.TOP);
			lat.setRotation(-pa.PI/2);
			lat.setFont(new Font((String)null, 0, 14));
			coneOrientation.addEventHandler(this, "reorient_cone");
			conesPanel.addControl(coneOrientation);
			float rightSection = conesPanel.getWidth() - coneOrientation.getWidth();
			coneSpread = new GSlider(pa, conesPanel.getWidth() - 70, 180f, coneOrientation.getHeight(), 80f, 15f);//)rightSection  - 40, 50, 12);
			coneSpread.setLimits(pa.PI/4, 0, pa.PI);
			coneSpread.setShowDecor(false, false, true, false);
			coneSpread.setRotation(-pa.PI/2);
			coneSpread.addEventHandler(this, "edit_conespread");
					
			/*coneSpreadVisualizer = new GKnob(pa, rightSection + 15, 60, (rightSection/1.5f), (rightSection/1.5f), 0);
			coneSpreadVisualizer.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
			coneSpreadVisualizer.setGripAmount(0.1f);
			coneSpreadVisualizer.setShowDecor(true, false, false, true);
			coneSpreadVisualizer.setEnabled(false);*/
			GLabel sprd = new GLabel(pa, coneSpread.getX()+50, coneSpread.getY(), coneSpread.getWidth(), 20);
			sprd.setText("Spread angle", GAlign.CENTER, GAlign.TOP);
			sprd.setFont(new Font((String)null, 0, 12));
			sprd.setRotation(-pa.PI/2);
			
			newLimitConeIndex = new GSpinner(pa, 50, 30, 35, 20);
			GLabel newlc = new GLabel(pa, -50, 5, 45, 20);
			newlc.setText("index");
			newlc.setTextAlign(GAlign.RIGHT, GAlign.TOP);
			newLimitConeIndex.addControl(newlc);
			newLimitConeIndex.setLimits(1, 0, 16, 1);
			cancelAddCone = new GButton(pa, 5, 60, 50, 30, "Cancel");
			cancelAddCone.addEventHandler(this, "dialog_cancel");
			confirmAddCone = new GButton(pa, 60, 60, 30, 30, "OK");
			confirmAddCone.addEventHandler(this, "confirm_addCone");
			addLimitCone.addControls(newLimitConeIndex, cancelAddCone, confirmAddCone);
			addLimitCone.setCollapsed(true);
			conesPanel.addControls(addLimitCone, limitConeList, coneSpread, /*coneSpreadVisualizer,*/ sprd, lon, lat);
			
			return conesPanel;
		}
		
		public void edit_conespread(GSlider spread, GEvent event) {
			pa.println("conespread event: " + event.name() + " : " +  spread.getValueF());
			float range = pa.PI - spread.getValueF();
			range = pa.min(pa.max(range, 0.001f), pa.PI-.00001f);
			float base = coneOrientation.getValueYF() + 2*pa.PI;
			/**hack to fix weird z-index focus issue*/
			this.selectedCone.setRadius(spread.getValueF());
			this.selectedCone.getParentKusudama().optimizeLimitingAxes();
		}
		
		public void limitCone_select(GDropList gd, GEvent event) {
			this.selectedCone = limitConeCollection.get(gd.getSelectedIndex());
			double lat = 0d;
			Vec<?> cpoint = this.selectedCone.getControlPoint();
			
	        double theta = Math.asin(cpoint.getYd());
	        double latitude = theta;
	       
	        double phi = Math.atan2(cpoint.getXd(), cpoint.getZd());
	        double longitude = phi;

	        if (longitude < 0) {
	            longitude += Math.PI*2d;
	        }
			coneOrientation.setValueY((float)latitude);
			coneOrientation.setValueX((float)longitude);
			coneSpread.setValue((float)this.selectedCone.getRadius());
		}
		
		public void confirm_addCone(GButton btn, GEvent event) {
			double defaultRad = 0.5d;
			((AbstractKusudama)ui.selectedBone.getConstraint()).addLimitConeAtIndex(
					this.newLimitConeIndex.getValue(), null, -1d);
			this.addLimitCone.setCollapsed(true);
			//AbstractLimitCone selected = 
			this.limitConeList.setSelected(this.newLimitConeIndex.getValue());
			this.updateUI();
		}
		
		public void reorient_cone(GSlider2D g2d, GEvent event) {
			pa.println("reorient event: "+event.name());
			if(this.selectedCone == null)
				return;
			
			long current = System.currentTimeMillis();
			long delta = current - ui.lastUIUpdate;
			if(event.equals(GEvent.RELEASED) || delta > 25) {
				// Calculate Cartesian coordinates
				float latitude = g2d.getValueYF();
				float longitude = g2d.getValueXF();
				double z = Math.cos(latitude) * Math.cos(longitude);
			    double x = Math.cos(latitude) * Math.sin(longitude);
			    double y = Math.sin(latitude);
		        this.selectedCone.setControlPoint(new SGVec_3d(x, y, z));
		        this.edit_conespread(coneSpread, event);
		        this.selectedCone.getParentKusudama().optimizeLimitingAxes();
		        ui.lastUIUpdate = current;
			}
			//if(event.equals(GEvent.RELEASED))
				
		}
	}

}