import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.print.attribute.standard.OutputDeviceAssigned;

import java.util.HashMap;

import IK.doubleIK.AbstractArmature;
import IK.doubleIK.AbstractBone;
import IK.doubleIK.AbstractIKPin;
import IK.doubleIK.AbstractKusudama;
import IK.doubleIK.AbstractLimitCone;
import data.EWBIKSaver;
import ewbik.processing.EWBKIO;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dIKPin;
import ewbik.processing.doublePrecision.dKusudama;
import g4p_controls.*;
import math.Vec;
import math.doubleV.AbstractAxes;
import math.doubleV.Vec3d;
import math.doubleV.Rot;
import math.doubleV.RotationOrder;
import math.doubleV.SGVec_3d;
import processing.opengl.PGraphics3D;
import processing.core.*;
import processing.event.MouseEvent;

public class G4PUI {
	
	//SOLVER MODES
	public static final int PERPETUAL = 0, INTERACTION = 1, ITERATION = 2, STEP = 3;  
	//DRAW PASS TYPES
	public static final int STENCIL = 0, RENDER = 1, WIDGET_2D = 2, WIDGET_3D = 3;
	
	
	AbstractIKPin activePin;
	AbstractAxes worldAxes;
	AbstractBone selectedBone;
	AbstractLimitCone selectedCone;
	ArrayList<AbstractIKPin> pins = new ArrayList<>();
	boolean doSolve = false;
	float zoom;
	protected PVector cameraPosition = new PVector(0, 100, 200); 
	protected PVector lookAt = new PVector(0, 150, 0);
	
	GSpinner boneHeight;
	private ArrayList<? extends AbstractLimitCone> limitConeCollection;
	ArrayList<AbstractBone> boneCollection = new ArrayList<>();
	HashMap<String, AbstractBone> boneMap = new HashMap<String, AbstractBone>();
	HashMap<AbstractBone, Integer> revBoneMap = new HashMap<AbstractBone, Integer>();
	//HashMap<String, AbstractBone> childMap = new HashMap<String, AbstractBone>();
	AbstractArmature armature;
	GPanel constraintProperties, targetProperties, childBonesGroup;
	GPanel solverPanel;
	GPanel addLimitCone;
	GSpinner newLimitConeIndex;
	GSlider2D coneOrientation;
	GSlider coneSpread;
	GKnob coneSpreadVisualizer;
	GSlider zoomSlider;
	GButton confirmAddCone;
	GToggleGroup solverMode;
	GOption perpetual, onInteraction, byIteration, byStep; 
	GPanel turntablePanel;// childBonesPanel;
	GPanel addChildPanel;
	GButton addConstraintBtn, addTargetBtn;
	GSlider dampening, stiffness, painfulness; 
	GSpinner iterations;
	GKnob boneTwistVisualizer;
	PApplet pa;
	public int solveMode = 1;
	private GKnob turntableKnob;
	public GPanel bonePanel;
	private GDropList bonelist, limitConeList;
	private GTextField newBoneName;

	private GView view3DScene;
	private GView view3DWidget;
	public SceneView sceneView;
	public WidgetView widgetView;
	private int fixCount = 0;
	private GView debugView;

	PApplet constraintWindow; 
	
	private Object eventOwner; 
	
	public G4PUI(PApplet p, boolean multipassEnabled, 
			AbstractArmature armature, AbstractAxes worldAxes,
			BiConsumer<PGraphics3D, Integer> drawScene, BiConsumer<PGraphics3D, Integer> drawWidgets) {
		super();
		this.armature = armature;
		this.pa = p;
		this.worldAxes = worldAxes;
		this.zoom = pa.PI/2f;
		try {
			this.lastOpenedDir = pa.loadStrings("lastDir.txt")[0];
		} catch (Exception e) {
			this.lastOpenedDir = pa.sketchPath();
		}
		
		this.updatePinList();
		this.activePin = activePin;
		
		view3DScene = new GView(pa, 300, 0, pa.width-300, pa.height, PApplet.P3D);
		view3DWidget = new GView(pa, 300, 0, pa.width-300, pa.height, PApplet.P3D);
		view3DWidget.setAlpha(155);
		sceneView = new SceneView(pa, this, drawScene, multipassEnabled);
		widgetView = new WidgetView(pa, this, drawWidgets);
		view3DScene.addListener(sceneView);
		view3DWidget.addListener(widgetView);
		//debugView = new GView(pa, 300, 0, pa.width-300, pa.height, PApplet.P2D);
		//debugView.addListener(new VisualDebug2D(pa, this, null));
		
		this.populateGUI();
	}
	public void update() {
		pa.println("ui frame " +pa.frameCount);
	}
	
	public void updateUI() {
		boneCollection = (ArrayList<AbstractBone>)armature.getBoneList();
		bonelist.setItems(getBoneStringArr(armature.getRootBone(), 9999, true, boneMap, revBoneMap), 0);
		bonelist.setSelected(revBoneMap.get(this.selectedBone));
		newBoneName.setText("");
		boneHeight.setValue((int)this.selectedBone.getBoneHeight());
		addChildPanel.setCollapsed(true);
		boolean hasTarget = selectedBone.getIKPin() != null;
		boolean hasConstraint = selectedBone.getConstraint() != null;
		if(selectedBone == armature.getRootBone()) {
			addConstraintBtn.setLocalColor(4, pa.color(150,150,150,100));
			addConstraintBtn.setEnabled(false);
		} else {
			addConstraintBtn.setEnabled(true);
			addConstraintBtn.setLocalColorScheme(GCScheme.BLUE_SCHEME);
			if (!hasConstraint){
				addConstraintBtn.setText("Add Constraint");
				constraintProperties.setVisible(false);
			} else {
				addConstraintBtn.setVisible(false);		
				constraintProperties.setVisible(true);
			}
		}
		addConstraintBtn.setVisible(!hasConstraint);
		constraintProperties.setVisible(hasConstraint);
		if(hasTarget) {
			
		}
		if(hasConstraint) {
			AbstractKusudama k =  ((AbstractKusudama)selectedBone.getConstraint());
			painfulness.setValue((float)selectedBone.getConstraint().getPainfulness());
			limitConeCollection =k.getLimitCones();
			String[] cones = new String[limitConeCollection.size()];
			for(int i=0; i<limitConeCollection.size(); i++)
				cones[i] = "Cone "+i;
			limitConeList.setItems(cones, 0);
			this.selectedCone = limitConeCollection.get(0);
			limitConeList.setSelected(0);
			coneSpread.setValue((float)this.selectedCone.getRadius());
			twistMin.setValue((float)k.minAxialAngle());
			twistRange.setValue((float)k.maxAxialAngle());
		} 
		updateTwistVisualizer(null);
		dampening.setValue((float)armature.getDampening());
		stiffness.setValue((float)selectedBone.getStiffness());
		iterations.setValue(armature.getDefaultIterations());
		updateTransformsList();
		updatePinList();
		updateTargetPanel();
	}

	public void populateGUI() {
		GCScheme.changePaletteColor(GCScheme.PURPLE_SCHEME, 3, pa.color(0,0,0,255));
		GCScheme.changePaletteColor(GCScheme.CYAN_SCHEME, 3, pa.color(0,0,0,255));
		this.selectedBone = armature.getRootBone();
		
		ConstraintPanel_init();
		ChildPanel_init();
		BonePanel_init();
		SolverPanel_init();
		targetPanel_init();
		
		TurntablePanel_init();		
		updateTransformsList();
		updateUI();
	}
	
	public void panel_cancel(GPanel panel, GEvent event) {
		mouseReleased();
	}
	public void dialog_cancel(GButton btn, GEvent eventt) {
		((GPanel)btn.getParent()).setCollapsed(true);
	}
	
	public void set_solver_mode(GOption opt, GEvent event) {
		if(opt.isSelected()) {
			if(opt == perpetual) solveMode = PERPETUAL;
			if(opt == onInteraction) solveMode = INTERACTION;
			if(opt == byIteration) solveMode = ITERATION;
			if(opt == byStep) solveMode = STEP;
		}
	}
	
	public void add_child(GButton btn, GEvent event) {
		String newName = newBoneName.getText();
		if(newName == null || newName.equals("")) newName = "child of "+this.selectedBone.getTag();
		String nameAttempt = newName;
		int attempt = 1;
		while(boneMap.get(nameAttempt) != null) {
			attempt++;
			nameAttempt = newName + attempt;
		}
		try {
			AbstractBone newB = this.selectedBone.getClass().getDeclaredConstructor(AbstractBone.class, String.class, double.class).newInstance(this.selectedBone, nameAttempt, this.boneHeight.getValue());
			this.selectedBone = newB;
			updateUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateForEffectorSelectList();
		updateTargetSelectList();
		updateTransformsList();
		target_forEffector_selectlist.setSelected(revBoneMap.get(this.selectedBone));
	}
	
	public void edit_painfulness(GSlider pain, GEvent event) {
		pa.println("pain event: " + event.name() + " : " +  pain.getValueF());
		if(this.selectedBone.getConstraint() != null) {
			this.selectedBone.getConstraint().setPainfulness(pain.getValueF());
		}
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
	
	public void addConstraint_click(GButton btn, GEvent envet) {
		if(this.selectedBone.getConstraint() == null) {
			dKusudama newConstraint = new dKusudama((dBone) this.selectedBone);
			newConstraint.addLimitCone(new SGVec_3d(0f,1f,0f), Math.toRadians(60), null, null);
			newConstraint.setAxialLimits(-2d, 4d);
			this.selectedBone.addConstraint(newConstraint);
			newConstraint.optimizeLimitingAxes();
			this.updateUI();
		}
	}
	
	public void confirm_addCone(GButton btn, GEvent event) {
		double defaultRad = 0.5d;
		((AbstractKusudama)this.selectedBone.getConstraint()).addLimitConeAtIndex(
				this.newLimitConeIndex.getValue(), null, -1d);
		this.addLimitCone.setCollapsed(true);
		this.limitConeList.setSelected(this.newLimitConeIndex.getValue());
		this.updateUI();
	}
	
	
	
	long lastUIUpdate = 0;
	private GSlider twistMin;
	private GSlider twistRange;
	private GDropList targetList;
	private GDropList target_forEffector_selectlist;
	private GDropList target_parentTransform_selectlst;
	private ArrayList<AbstractBone> availableEffectorList;
	private GSlider weight;
	private GSlider x_priority;
	private GSlider y_priority;
	private GSlider z_priority;
	private HashMap<String, AbstractAxes> extraTransforms;
	private GSlider depthFalloff;
	private HashMap <String, AbstractAxes>transformsMap;
	private HashMap<AbstractAxes, Integer> indexedTransformMap = new HashMap<>();
	private String lastOpenedDir;
	private int nextProgress;
	
	public float[] latLonToVec(float lat, float lon) {
		float z = (float)Math.cos(lat) * (float)Math.cos(lon);
		float x = (float)Math.cos(lat) * (float)Math.sin(lon);
		float y = (float)Math.sin(lat);
		pa.println("("+x+", "+y+", "+z);
		return new float[] {x,y,z};
	}
	public float[] vecToLatLon (float x, float y, float z) {
		double theta = Math.asin(y);
        double latitude = theta;
        
        double phi = Math.atan2(x, z);
        double longitude = phi;
        if (longitude < 0) {
            longitude += Math.PI*2d;
        }
		pa.println("lat: "+latitude+", lon: "+ longitude);
		return new float[] {(float)latitude, (float)longitude};
	}
	
	public void reorient_cone(GSlider2D g2d, GEvent event) {
		pa.println("reorient event: "+event.name());
		if(this.selectedCone == null)
			return;
		
		long current = System.currentTimeMillis();
		long delta = current - lastUIUpdate;
		if(event.equals(GEvent.RELEASED) || delta > 25) {
			// Calculate Cartesian coordinates
			float latitude = g2d.getValueYF();
			float longitude = g2d.getValueXF();
			pa.println("--lat: "+latitude+", lon: "+ longitude);
			float[] toV1 = latLonToVec(latitude, longitude);
			float[] ll1 = vecToLatLon(toV1[0], toV1[1], toV1[2]);
			float[] toV2 = latLonToVec(ll1[0], ll1[1]);
			float[] ll2 = vecToLatLon(toV2[0], toV2[1], toV2[2]);
			
			double z = Math.cos(latitude) * Math.cos(longitude);
		    double x = Math.cos(latitude) * Math.sin(longitude);
		    double y = Math.sin(latitude);
	        this.selectedCone.setControlPoint(new SGVec_3d(x, y, z));
	        this.edit_conespread(coneSpread, event);
	        this.selectedCone.getParentKusudama().optimizeLimitingAxes();
	        lastUIUpdate = current;
		}
		//if(event.equals(GEvent.RELEASED))
			
	}
	
	public void bone_select(GDropList gd, GEvent event) {
		String selected = gd.getSelectedText();
		AbstractBone toSelect = boneMap.get(selected);
		if(this.selectedBone != toSelect) {
			constraintProperties.setCollapsed(true);
			this.selectedBone = toSelect;
			updateUI();
			target_forEffector_selectlist.setSelected(revBoneMap.get(this.selectedBone));
		}
	}
	
	public void set_dampening(GSlider gs, GEvent event) {
		if(event.equals(GEvent.RELEASED)) {
			armature.setDefaultDampening(dampening.getValueF());
		}
	}
	
	public void set_stiffness(GSlider gs, GEvent event) {
		if(event.equals(GEvent.RELEASED)) {
			selectedBone.setStiffness(stiffness.getValueF());
		}
	}
	public void set_iterations(GSpinner gs, GEvent event) {
		if(event.equals(GEvent.CHANGED)) {
			armature.setDefaultIterations(iterations.getValue());
		}
	}
	
	public void addTarget_click(GButton btn, GEvent event) {
		String bstr = target_forEffector_selectlist.getSelectedText();
 		AbstractBone b = boneMap.get(bstr);
 		updateForEffectorSelectList();
 		if(b == null || b.getIKPin() != null) {
 			G4P.showMessage(pa, "The effector you selected already has a target. It can't have two.", "No luck", G4P.ERROR_MESSAGE);
 		} else {
 			b.enablePin_(b.localAxes().origin_());
 			this.updatePinList();
 			int newidx = this.pins.indexOf(this.activePin);
 			updateTargetSelectList();
 			this.targetList.setSelected(newidx);
 			select_target(this.targetList, GEvent.CHANGED); 			
 			updateForEffectorSelectList();
 			updateTransformsList();
 			armature.regenerateShadowSkeleton();
 			this.dialog_cancel(btn, event);
 		}
	}
	
	
	public void turntable_handle(GKnob gk, GEvent event) {
		worldAxes.rotateToParent();
		worldAxes.rotateAboutY(Math.toRadians(gk.getValueF()*360d), false);
		if(gk.getValueF() >= 0.9999f) 
			gk.setValue(0.00002f);
		if(gk.getValueF() <= 0.00001f)
			gk.setValue(0.9998f);
	}
	
	private String[] getBoneStringArr(AbstractBone startFrom, int maxDepth, boolean inclusive, HashMap<String, AbstractBone> trackMap, HashMap<AbstractBone, Integer> revtrackMap) {
		trackMap.clear();
		revtrackMap.clear();
		ArrayList<String> boneStrings = new ArrayList<String>();
		String[] result;
		boneStrings = getBoneStringList(startFrom, 0, maxDepth, inclusive, trackMap);
		if(boneStrings.size() == 0) boneStrings.add("None");
		result = boneStrings.toArray(new String[0]);
		for(int i=0; i<result.length; i++) {
			AbstractBone b = trackMap.get(result[i]);
			revtrackMap.put(b, i);
		}
		
		return result;
 	}
	private ArrayList<String> getBoneStringList(AbstractBone currentBone, int parentCount, int maxDepth, boolean inclusive, HashMap<String, AbstractBone> trackMap) {
		ArrayList<String> boneStrings = new ArrayList<String>();
		if(maxDepth < 0) return boneStrings;
		String depthStr = "-";
		String prepend = "";
		if(inclusive) {
			int attempt = 1; 
			for(int d = 0; d < parentCount; d++) {
				prepend += depthStr;
			}
			String thisBoneString = prepend + currentBone.getTag();
			String currentAttemptString = thisBoneString;
		
			while(trackMap.get(currentAttemptString) != null) {
				attempt++;
				currentAttemptString = thisBoneString + attempt;
			}
		
			trackMap.put(currentAttemptString, currentBone);
			boneStrings.add(currentAttemptString);
		}
		for(AbstractBone b : currentBone.getChildren()) {
			boneStrings.addAll(getBoneStringList(b, parentCount+1, maxDepth-1, true, trackMap));
		}
		return boneStrings;
 	}
	
	public GPanel BonePanel_init() {
		bonePanel = new GPanel(pa, 0,  pa.height-520, 300, 520, "Bone Info");
		bonePanel.setCollapsed(false);
		bonePanel.setCollapsible(false);
		bonePanel.setDraggable(false);
		bonePanel.setOpaque(true);
		bonePanel.setLocalColorScheme(GCScheme.BLUE_SCHEME, false);
		  
		bonelist = new GDropList(pa, 100, 20, 200, 150, 8, 20);		
		bonelist.addEventHandler(this, "bone_select");
		bonelist.setLocalColorScheme(GCScheme.PURPLE_SCHEME, true);
		stiffness = new GSlider(pa, 220, 40, 70, 50, 12);
		stiffness.setShowValue(true);
		stiffness.setShowTicks(true);
		stiffness.setLimits(0, 1f);
		stiffness.setLocalColorScheme(GCScheme.CYAN_SCHEME);
		stiffness.addEventHandler(this, "set_stiffness");
		GLabel stif = new GLabel(pa, 140, 40, 75, 50);
		stif.setText("Stiffness:");
		stif.setTextAlign(GAlign.RIGHT, GAlign.CENTER);
		
		addConstraintBtn = new GButton(pa, 10, 200, 120, 30, "Add Constraint");
		addConstraintBtn.addEventHandler(this, "addConstraint_click");
		
		bonePanel.addControls(bonelist, stiffness, stif, addChildPanel, addConstraintBtn, constraintProperties);
		
		boneTwistVisualizer = new GKnob(pa, 10, 60,  bonePanel.getWidth()/4f,  bonePanel.getWidth()/4f, 1.0f);
		boneTwistVisualizer.setShowDecor(false, true, false, true);
		boneTwistVisualizer.setEnabled(true);
		boneTwistVisualizer.setTurnMode(GKnob.CTRL_ANGULAR);
		boneTwistVisualizer.addEventHandler(this, "transform_bone_twist");
		bonePanel.addControl(boneTwistVisualizer);
		
		
		
		return bonePanel;
	}
	public GPanel ChildPanel_init() {
		addChildPanel = new GPanel(pa, 10, 20, 300, 80, "Add Child");
		addChildPanel.setLocalColorScheme(GCScheme.CYAN_SCHEME);
		addChildPanel.setDraggable(false);
		addChildPanel.setFont(new Font((String)null, 0, 14));
		GButton confirmAddChild = new GButton(pa, 260, 35, 30, 30, "OK");
		confirmAddChild.addEventHandler(this, "add_child");
		newBoneName = new GTextField(pa, 0,40, 150, 20);
		newBoneName.tag = "boneName";
		newBoneName.setPromptText("Bone Name");		
		boneHeight = new GSpinner(pa, 160, 40, 40, 20, 0.1f);
		boneHeight.setLimits(5, 0, 1000, 1);	
		GLabel lbl1 = new GLabel(pa, 0, -30, 150, 38);
		lbl1.setText("Height");
		lbl1.setTextAlign(GAlign.SOUTH, GAlign.SOUTH);
		boneHeight.addControl(lbl1);
		addChildPanel.addControl(newBoneName);
		addChildPanel.addControl(boneHeight);
		addChildPanel.addControl(confirmAddChild);
		insertCloseButton(addChildPanel, confirmAddChild);
		return addChildPanel;
	}
	
	public void transform_bone_twist(GKnob gk, GEvent event) {
		AbstractKusudama constraint = (AbstractKusudama) selectedBone.getConstraint();
		if(constraint != null && constraint.constrainsTwist()) {
			float currentVal = (float) gk.getValueF();
			constraint.setTwist(currentVal);
		} else {
			Vec3d<?> targY = selectedBone.localAxes().y_().heading();
			selectedBone.localAxes().alignOrientationTo(selectedBone.getMajorRotationAxes());
			selectedBone.rotAboutFrameY(gk.getValueF());
			Vec3d<?> tempY = selectedBone.localAxes().y_().heading();
			Rot swingBack = new Rot(tempY, targY);
			selectedBone.localAxes().rotateBy(swingBack);
		}
	}
	
	public void edit_twistMin(GSlider twistmin, GEvent event) {
		AbstractKusudama constraint = (AbstractKusudama) selectedBone.getConstraint();
		if(constraint != null && constraint.constrainsTwist()) {
			constraint.setAxialLimits(twistmin.getValueF(), constraint.maxAxialAngle());
		}
		this.updateTwistVisualizer(event);
	}
	
	public void edit_twistRange(GSlider twistrange, GEvent event) {
		AbstractKusudama constraint = (AbstractKusudama) selectedBone.getConstraint();
		if(constraint != null && constraint.constrainsTwist()) {
			constraint.setAxialLimits(constraint.minAxialAngle(),twistrange.getValueF());
		}
		this.updateTwistVisualizer(event);
	}
	
	private void updateTwistVisualizer(GEvent event) {
		AbstractKusudama constraint = (AbstractKusudama) selectedBone.getConstraint();
		float range = pa.PI*2;
		//range = pa.min(pa.max(range, 0.001f), pa.PI-.00001f);
		float base = 0;
		float currentVal = 0.1f;
		if(constraint != null && constraint.constrainsTwist()) {
			range = twistRange.getValueF();
			base = twistMin.getValueF();
			currentVal = (float) constraint.getTwistRatio();
		}
		
		float min = Math.min(base+range, base);
		float max = Math.max(base+range, base);
		//int baseYellow = GCScheme.getPalette(boneTwistVisualizer.getLocalColorScheme())[14];
		//int baseBlue = GCScheme.getPalette(boneTwistVisualizer.getLocalColorScheme())[2];
		//boneTwistVisualizer.setLocalColor(2, baseBlue);
		
		boneTwistVisualizer.setLimits(0, 1f);
		boneTwistVisualizer.setValue(currentVal);
		boneTwistVisualizer.setShowDecor(false, true, true, true);
		boneTwistVisualizer.setShowArcOnly(true);
		boneTwistVisualizer.setGripAmount(1f);
		//boneTwistVisualizer.setShowTrack(true);
		//boneTwistVisualizer.setShowLimits(true);
		boneTwistVisualizer.setShowValue(true);
		boneTwistVisualizer.setTurnRange(pa.degrees(min), pa.degrees(max));
	}
	
	private GPanel ConstraintPanel_init() {
		constraintProperties = new GPanel(pa, 10, 170, 300, 350, "Constraint Properties");
		constraintProperties.setLocalColorScheme(GCScheme.CYAN_SCHEME);
		constraintProperties.setFont(new Font((String)null, 0, 16));
		constraintProperties.setDraggable(false);
		//constraintProperties.setDragArea(10, 50, 120, 30);
		constraintProperties.setCollapsed(false);
		constraintProperties.setVisible(false);
		insertCloseButton(constraintProperties, null);
		painfulness = new GSlider(pa, 80, constraintProperties.getHeight()-50, 75, 50, 12);
		painfulness.setShowValue(true);
		painfulness.setShowTicks(true);
		painfulness.setLimits(0, 1f);
		painfulness.setLocalColorScheme(GCScheme.CYAN_SCHEME);
		painfulness.addEventHandler(this, "edit_painfulness");
		
		twistMin = new GSlider(pa, 10, 10, 140, 60, 12);
		twistMin.setShowDecor(false, false, true, false);
		twistMin.setLimits(-pa.PI/4, -pa.PI, pa.PI);
		GLabel twm = new GLabel(pa, 160f, 10f, 100f, 65f, "Twist Minimum");
		twistMin.addEventHandler(this, "edit_twistMin");
		twm.setTextAlign(GAlign.LEFT, GAlign.CENTER);
		twistRange = new GSlider(pa, 10, 50, 140, 60, 12);
		twistRange.addEventHandler(this, "edit_twistRange");
		twistRange.setShowDecor(false, false, true, false);
		twistRange.setLimits(pa.PI/2, -pa.PI*2, pa.PI*2);
		GLabel twr = new GLabel(pa, 160f, 50, 100, 65, "Twist Range");
		twr.setTextAlign(GAlign.LEFT, GAlign.CENTER);
		//GKnob twistVisual = new GKnob(pa, )
		GLabel pain = new GLabel(pa, 0, constraintProperties.getHeight()-50, 75, 50);
		pain.setText("Painfulness:");
		pain.setTextAlign(GAlign.RIGHT, GAlign.CENTER);
		GPanel conesPanel = conesPanel_init();
		constraintProperties.addControls(painfulness, pain, conesPanel, twistMin, twistRange, twm, twr);
		
		return constraintProperties;
	}
	
	public GPanel conesPanel_init() {
		GPanel conesPanel = new GPanel(pa, 10, constraintProperties.getHeight()-250, constraintProperties.getWidth()-20, 200, "Limit Cones");
		conesPanel.setCollapsible(false);
		conesPanel.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
		conesPanel.setDraggable(false);
		limitConeList = new GDropList(pa, 100, 25, 160, 70);	
		limitConeList.setFont(new Font((String)null, 0, 14));
		limitConeList.addEventHandler(this, "limitCone_select");
		addLimitCone = new GPanel(pa, 10, 25, 100, 100, "Add Cone");
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
		coneSpread = new GSlider(pa, conesPanel.getWidth() - 70, 185f, coneOrientation.getHeight(), 80f, 15f);//)rightSection  - 40, 50, 12);
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
		confirmAddCone = new GButton(pa, 60, 60, 30, 30, "OK");
		confirmAddCone.addEventHandler(this, "confirm_addCone");
		addLimitCone.addControls(newLimitConeIndex, confirmAddCone);
		addLimitCone.setCollapsed(true);
		conesPanel.addControls(addLimitCone, limitConeList, coneSpread, /*coneSpreadVisualizer,*/ sprd, lon, lat);
		insertCloseButton(addLimitCone, confirmAddCone);
		return conesPanel;
	}
	public GPanel targetPanel_init() {
		GPanel addTargetPanel = new GPanel(pa, 2, 20, 250, 80, "Add Target");
		addTargetPanel.setLocalColorScheme(GCScheme.RED_SCHEME);
		
		addTargetPanel.setCollapsed(true);
		target_forEffector_selectlist = new GDropList(pa, 5, 20, 230, 400, 20, 20);	
		target_forEffector_selectlist.setItems(new String[]{"No Effector Selected"}, 0);
		target_forEffector_selectlist.addEventHandler(this, "change_effector");
		
		targetList = new GDropList(pa, 95, 20, 200, 400, 20, 20);	
		targetList.addEventHandler(this, "select_target");
		targetList.setItems(new String[]{"None"}, 0);
		
		target_parentTransform_selectlst = new GDropList(pa,  145, 50, 150, 80);	
		target_parentTransform_selectlst.setItems(new String[]{"World"}, 0);
		target_parentTransform_selectlst.addEventHandler(this, "set_target_parent");
		GLabel par = new GLabel(pa, -150, 0, 150, 20);
		par.setText("Parent Transform: ");
		par.setTextAlign(GAlign.RIGHT, GAlign.NORTH);
		target_parentTransform_selectlst.addControl(par);
		
		addTargetBtn = new GButton(pa, addTargetPanel.getWidth()-32, addTargetPanel.getHeight()-32, 30, 30, "OK");
		addTargetBtn.addEventHandler(this, "dialog_cancel");
		addTargetBtn.addEventHandler(this, "addTarget_click");
		
		
		targetProperties = new GPanel(pa, 0, solverPanel.getY()+solverPanel.getHeight(), 300, 135, "Target Properties");
		targetProperties.setLocalColorScheme(GCScheme.GOLD_SCHEME);
		targetProperties.setDraggable(false);
		targetProperties.setCollapsible(false);
		targetProperties.addControls(targetList, target_parentTransform_selectlst, addTargetPanel);
		addTargetPanel.addControls(target_forEffector_selectlist, addTargetBtn);
		
		insertCloseButton(addTargetPanel, addTargetBtn);
		
		GDropList tps = target_parentTransform_selectlst;
		weight = new GSlider(pa, tps.getX() + 50, tps.getY() + 25, 100, 20, 12);
		weight.addEventHandler(this, "target_weight_change");		
		 x_priority = new GSlider(pa, 0,0, 80, 20, 12);
		 x_priority.setLimits(0.5f, 0f, 5f );
		 y_priority = new GSlider(pa, 0,0, 80, 20, 12);
		 y_priority.setLimits(0.5f, 0f, 5f );
		 z_priority = new GSlider(pa, 0,0, 80, 20, 12);
		 z_priority.setLimits(0.5f, 0f, 5f );
		 depthFalloff = new GSlider(pa, 0, tps.getY() + 25, 100, 20, 12);
		 depthFalloff.addEventHandler(this, "depth_falloff_change");
		 x_priority.addEventHandler(this, "target_dir_priority_change");
		 y_priority.addEventHandler(this, "target_dir_priority_change");
		 z_priority.addEventHandler(this, "target_dir_priority_change");
		 targetProperties.addControl(weight);
		 
		 insertLeftOf(depthFalloff, weight);
		 depthFalloff.moveTo(depthFalloff.getX()-50, depthFalloff.getY());
		addLeftLabel("depth falloff", depthFalloff, 0);
		 
		 insertBelow(z_priority, weight);
		 insertLeftOf(y_priority, z_priority);
		 insertLeftOf(x_priority, y_priority);
		 
		 addLeftLabel("weight", weight, 0);
		 addUnderLabel("x priority", x_priority, -80);
		 addUnderLabel("y priority", y_priority, -80);
		 addUnderLabel("z priority", z_priority, -80);	 
		
		updateTargetSelectList();
		updateForEffectorSelectList();
		return targetProperties;
	}
	
	public void set_target_parent(GDropList parList, GEvent event) {
		this.activePin.getAxes().setParent(transformsMap.get(parList.getSelectedText()));
	}
	
	public void depth_falloff_change(GSlider depth, GEvent event) {
		if(event.equals(GEvent.RELEASED)) {
			this.activePin.setDepthFalloff(depth.getValueF());
			armature.regenerateShadowSkeleton();
		}
	}
	
	public void updateTransformsList() {		
		String[] bonestrArr = getBoneStringArr(armature.getRootBone(), 9999, true, boneMap, revBoneMap);
		transformsMap = new HashMap<>(); 
		//HashMap<AbstractAxes, String> inverseTransformsMap = new HashMap<>();
		indexedTransformMap = new HashMap<>();
		
		ArrayList<String> parstrings = new ArrayList<>();
		parstrings.add("World");
		transformsMap.put("World", this.worldAxes);
		indexedTransformMap.put(this.worldAxes, parstrings.size());
		parstrings.add("Armature");
		transformsMap.put("Armature", this.armature.localAxes());
		indexedTransformMap.put(this.armature.localAxes(), parstrings.size());
		if(this.extraTransforms != null) {
			for(String s : this.extraTransforms.keySet()) {
				AbstractAxes val = this.extraTransforms.get(s);
				parstrings.add(s);
				transformsMap.put(s, val);
				indexedTransformMap.put(val,  parstrings.size());
			}
		}		
		for(int i = 0; i < bonestrArr.length; i++) {
			AbstractBone candidate = boneMap.get(bonestrArr[i]);
			String base = bonestrArr[i];
			transformsMap.put(base, candidate.localAxes());
			parstrings.add(base);
			indexedTransformMap.put(candidate.localAxes(), parstrings.size());
			if(candidate.getIKPin() != null && candidate.getIKPin() != activePin) {
				String targS = "targ of "+base;
				parstrings.add(targS);
				transformsMap.put(targS, candidate.getIKPin().getAxes());
				indexedTransformMap.put(candidate.getIKPin().getAxes(),  parstrings.size());
			}
		}
		int selected = parstrings.indexOf(target_parentTransform_selectlst.getSelectedText());
		String[] result = parstrings.toArray(new String[0]);
		target_parentTransform_selectlst.setItems(result, selected);
	}
	
	public void target_weight_change(GSlider gw, GEvent event) {
		if(this.activePin != null) {
			this.activePin.setPinWeight(gw.getValueF());
			this.armature.updateShadowSkelRateInfo();
		}
	}
	
	public void target_dir_priority_change(GSlider gs, GEvent event) {
		if(this.activePin != null) {
			double xPriority = gs == x_priority ? gs.getValueF() : this.activePin.getXPriority();
			double yPriority = gs == y_priority ? gs.getValueF() : this.activePin.getYPriority();
			double zPriority = gs == z_priority ? gs.getValueF() : this.activePin.getZPriority();
			this.activePin.setTargetPriorities(xPriority, yPriority, zPriority);
			this.armature.updateShadowSkelRateInfo();
		}
	}
	
	/**
	 * place  @param elem below @param under/ 
	 * @return @param elem for chaining
	 */
	public GAbstractControl insertBelow(GAbstractControl elem, GAbstractControl under) {
		float x = under.getX();
		float y = under.getY();
		float height = under.getHeight();
		GAbstractControl par = under.getParent();
		par.addControl(elem);
		elem.moveTo(x, y+height+2);
		return elem;
	}
	
	/**
	 * place  @param elem above @param above
	 * @return @param elem for chaining
	 */
	public GAbstractControl insertAbove(GAbstractControl elem, GAbstractControl above) {
		float x = above.getX();
		float y = above.getY();
		GAbstractControl par = above.getParent();
		par.addControl(elem);
		elem.moveTo(x, y-elem.getHeight() -2);
		return elem;
	}
	
	/**
	 * place  @param elem left of @param leftof
	 * @return @param elem for chaining
	 */
	public GAbstractControl insertLeftOf(GAbstractControl elem, GAbstractControl leftof) {
		float x = leftof.getX();
		float y = leftof.getY();
		GAbstractControl par = leftof.getParent();
		par.addControl(elem);
		elem.moveTo(x - elem.getWidth()-2, y);
		return elem;
	}
	
	/**
	 * place  @param elem right of @param rightof
	 * @return @param elem for chaining
	 */
	public GAbstractControl insertRightOf(GAbstractControl elem, GAbstractControl rightof) {
		float x = rightof.getX();
		float y = rightof.getY();
		GAbstractControl par = rightof.getParent();
		par.addControl(elem);
		elem.moveTo(x + rightof.getWidth()+2, y);
		return elem;
	}
	
	public void addLeftLabel(String text, GAbstractControl elem, float offY) {
		float x = elem.getX();
		float y = elem.getY();
		GLabel lbl = new GLabel(pa, x - 200, y + offY, 200, 25, text+": ");
		elem.getParent().addControl(lbl);
		lbl.setTextAlign(GAlign.RIGHT, GAlign.TOP);
	}
	
	public void addUnderLabel(String text, GAbstractControl elem, float offY) {
		float x = elem.getX();
		float y = elem.getY();
		GLabel lbl = new GLabel(pa, x, y, elem.getWidth(), y+30 +offY, text+": ");
		elem.getParent().addControl(lbl);
		//lbl.setTextAlign(GAlign.CENTER, GAlign.TOP);
	}
	
	public void updateTargetSelectList() {
		String[] pinnames = getTargetNames();
		targetList.setItems(pinnames, 0);
	}
	
	public String[] getTargetNames() {
		ArrayList<String> pinNames = new ArrayList<>();
		for(int i = 0; i<pins.size(); i++) {
			AbstractIKPin pin = pins.get(i);
			String pinname = pin.forBone().getTag()+"'s target";
			pinNames.add(pinname);
		}
		String[] pinnames = pinNames.toArray(new String[0]);
		return pinnames;
	}
	
	public void updateForEffectorSelectList() {
		String[] bonestrArr = getBoneStringArr(armature.getRootBone(), 9999, true, boneMap, revBoneMap);
		for(int i = 0; i < bonestrArr.length; i++) {
			AbstractBone candidate = boneMap.get(bonestrArr[i]);
			if(candidate.getIKPin() != null) {
				bonestrArr[i] += " (already pinned)";
				boneMap.put(bonestrArr[i], candidate);
			}
		}
		target_forEffector_selectlist.setItems(bonestrArr, 0);
	}
	
	public void select_target(GDropList dl, GEvent event) {
		this.activePin = this.pins.get(dl.getSelectedIndex());
		updateTargetPanel();
	}
	
	public void updateTargetPanel() {
		updateTransformsList();		
		weight.setValue((float)this.activePin.getPinWeight());
		depthFalloff.setValue((float)this.activePin.getDepthFalloff());
		x_priority.setValue((float)this.activePin.getXPriority());
		y_priority.setValue((float)this.activePin.getYPriority());
		z_priority.setValue((float)this.activePin.getZPriority());
		AbstractAxes parAxes = this.activePin.getAxes().getParentAxes();
		int a =0;
		int paridx = (int) indexedTransformMap.get(parAxes);
		target_parentTransform_selectlst.setSelected(paridx);
	}
	public void insertCloseButton (GPanel panel, GAbstractControl leftOf) {
		float sansLeft = 0;
		float y = panel.getHeight()-45;
		float height = 30;
		if(leftOf != null) {
			sansLeft = leftOf.getWidth();
			y = leftOf.getY();
			height = leftOf.getHeight();
		}
		GButton closePanel = new GButton(pa, (panel.getWidth()- sansLeft)-60, y, 50, height, "Close");
		closePanel.addEventHandler(this, "dialog_cancel");
		panel.addControl(closePanel);
	}
	public GPanel SolverPanel_init() {
		solverPanel = new GPanel(pa, 0, 140, 300, 100, "Solver options");
		solverPanel.setCollapsed(false);
		solverPanel.setCollapsible(false);
		solverPanel.setDraggable(false);
		solverPanel.setOpaque(true);
		GLabel modelbl = new GLabel(pa, 0, 10, 150, 38);
		modelbl.setText("Mode: ");
		modelbl.setTextAlign(GAlign.WEST, GAlign.NORTH);
		
		solverMode = new GToggleGroup();
		perpetual = 	new GOption(pa, 5, 35, 150, 15, "Perpetual");		
		onInteraction = new GOption(pa, 5, 50, 150, 15, "on interaction");
		byIteration = 	new GOption(pa, 5, 65, 150, 15, "by iteration");
		byStep = 		new GOption(pa, 5, 80, 150, 15, "by step");
		solverMode.addControls(perpetual, onInteraction, byIteration, byStep);
		perpetual.addEventHandler(this, "set_solver_mode");
		onInteraction.addEventHandler(this, "set_solver_mode");
		onInteraction.setSelected(true);
		byIteration.addEventHandler(this, "set_solver_mode");
		byStep.addEventHandler(this, "set_solver_mode");
		
		GLabel dmp = new GLabel(pa, 140, 20, 75, 50);
		dmp.setText("Dampening");
		dmp.setTextAlign(GAlign.RIGHT, GAlign.CENTER);
		dampening = new GSlider(pa, 220, 20, 70, 50, 12);
		dampening.setShowValue(true);
		dampening.setShowTicks(true);
		dampening.setLimits(0, pa.PI/2);
		dampening.addEventHandler(this, "set_dampening");
		dampening.setLocalColorScheme(GCScheme.CYAN_SCHEME);
		iterations = new GSpinner(pa, 220, 70, 50, 25, 1f);
		iterations.setLimits(10, 0, 1000, 1);
		iterations.addEventHandler(this, "set_iterations");
		GLabel itr = new GLabel(pa, -85, 5, 75, 10);
		itr.setText("Iterations: ");
		itr.setTextAlign(GAlign.RIGHT, GAlign.NORTH);
		iterations.addControl(itr);
		
		solverPanel.addControls(modelbl,perpetual,onInteraction,byIteration,byStep,dampening, dmp, iterations);
		return solverPanel;
	}

	
	public GPanel TurntablePanel_init() {
		turntablePanel = new GPanel(pa, 0, 0, 300, 140, "Turntable");
		turntableKnob = new GKnob(pa, 100f, 20f, 80f, 80f, 1.0f);
		turntableKnob.setTurnRange(0, 360);
		turntableKnob.setTurnMode(GKnob.CTRL_ANGULAR);
		turntableKnob.setShowArcOnly(true);
		turntablePanel.setCollapsible(false);
		turntablePanel.setDraggable(false);
		turntablePanel.addEventHandler(this, "panel_cancel");
		turntableKnob.setIncludeOverBezel(false);
		turntableKnob.setShowTrack(true);
		turntableKnob.addEventHandler(this, "turntable_handle");
		turntableKnob.setLocalColorScheme(GCScheme.BLUE_SCHEME);
		turntablePanel.addControl(turntableKnob);
		
		zoomSlider = new GSlider(pa, turntablePanel.getWidth() - 40, turntablePanel.getHeight()-10, turntablePanel.getHeight()-30, 60, 12f);
		zoomSlider.setLimits(zoom, 0.05f, 0.8f*pa.PI);
		zoomSlider.setRotation(-pa.PI/2f);
		zoomSlider.setShowDecor(false, false, true, true);
		turntablePanel.addControl(zoomSlider);
		zoomSlider.addEventHandler(this, "edit_zoom");
		
		GButton savebtn = new GButton(pa, 0, 0, 50, 30, "Save");
		insertLeftOf(savebtn, turntableKnob);
		savebtn.moveTo(savebtn.getX()-30, savebtn.getY()+10);
		GButton loadbtn = new GButton(pa, 0, 0, 50, 30, "Load");
		insertBelow(loadbtn, savebtn);
		loadbtn.moveTo(loadbtn.getX(), loadbtn.getY() + 20);
		savebtn.addEventHandler(this, "save_armature");
		loadbtn.addEventHandler(this, "load_armature");
		return turntablePanel;
	}
	public void save_armature(GButton save, GEvent event ) {
		String  outDir;
		if(lastOpenedDir != null) {
			outDir = G4P.selectOutput("Save", lastOpenedDir);
		} else {
			outDir = G4P.selectOutput("Save");
		}
		if(outDir != null) {
			int lastsep = outDir.lastIndexOf(File.pathSeparator);
			lastOpenedDir = lastsep == -1 ? outDir : outDir.substring(0, lastsep);
			pa.saveStrings("lastDir.txt", new String[] {lastOpenedDir});
			if(!outDir.endsWith(".arm")) {
				outDir += ".arm";
			}
		}
		EWBIKSaver newSaver = new EWBIKSaver();
		newSaver.saveArmature(armature, outDir); 
	}
	public void load_armature(GButton save, GEvent event ) {
		String inDir;
		if(lastOpenedDir != null)
			inDir = G4P.selectInput("Input Dialog", "arm", "EWB-IK Armature files", lastOpenedDir);
		else 
			inDir = G4P.selectInput("Input Dialog", "arm", "EWB-IK Armature files");
		 
		try {
			if(inDir != null) {
				int lastsep = inDir.lastIndexOf(File.pathSeparator);
				lastOpenedDir = lastsep == -1 ? inDir : inDir.substring(0, lastsep);
				pa.saveStrings("lastDir.txt", new String[] {lastOpenedDir});
			}
			this.armature =  EWBKIO.LoadArmature_doublePrecision(inDir);
			armature.localAxes().setRelativeToParent(worldAxes);
			Vec<?> origin = armature.localAxes().getGlobalMBasis().getOrigin();
			lookAt.set(origin.getXf(), origin.getYf(), origin.getZf());
			armature.regenerateShadowSkeleton(true);
			selectedBone = armature.getRootBone();
			armature.localAxes().setRelativeToParent(worldAxes);
			this.updatePinList();
			this.activePin = this.pins.get(0);
			this.updateTransformsList();
			for(AbstractIKPin pin : pins) {
				if(indexedTransformMap.get(pin.getAxes().getParentAxes()) == null) {
					pin.getAxes().setRelativeToParent(this.worldAxes);
				}				
			}
			this.updateTargetSelectList();
			this.updateForEffectorSelectList();
			this.updateTargetPanel();
			this.updateUI();
		} catch (Exception e) {
			e.printStackTrace();
			if(inDir != null) {
				G4P.showMessage(pa, "BAD FILE. Boo.", "Nope" , G4P.ERROR_MESSAGE);
			}
		}
	}
	
	public void edit_zoom(GSlider zoom, GEvent event) {
		this.zoom = zoom.getValueF();
	}
	
	public void reloadShaders() {
		sceneView.reloadShaders();
		widgetView.reloadShaders();
	}
	
	public void updatePinList() {
		pins.clear();
		this.recursivelyAddToPinnedList(pins, armature.getRootBone());
		if(pins .size() > 0) {
			this.activePin = pins.get(pins.size()-1);
		}
	}
	
	public void incrementSelectedPin() {
		int currentPinIndex =(pins.indexOf(activePin) + 1) % pins.size();
		activePin  = pins.get(currentPinIndex);
	}
	
	public void decrementSelectedPin() {
		int idx = pins.indexOf(activePin);
		int currentPinIndex =  (pins.size()-1) -(((pins.size()-1) - (idx - 1)) % pins.size());
		activePin  = pins.get(currentPinIndex);
	}
	
	public void recursivelyAddToPinnedList(ArrayList<AbstractIKPin> pins, AbstractBone descendedFrom) {
		ArrayList<AbstractBone> pinnedChildren = (ArrayList<AbstractBone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
		for(AbstractBone b : pinnedChildren) {
			pins.add(b.getIKPin());
			AbstractAxes parAx = b.getIKPin().getAxes().getParentAxes();
			if(parAx == null || indexedTransformMap.get(parAx) == null) 
				b.getIKPin().getAxes().setRelativeToParent(worldAxes);
		}
		for(AbstractBone b : pinnedChildren) {
			ArrayList<AbstractBone> children = (ArrayList<AbstractBone>) b.getChildren(); 
			for(AbstractBone b2 : children) {
				recursivelyAddToPinnedList(pins, b2);
			}
		}
	}
	
	public void mousePressed(MouseEvent event) {
		pa.println("pressss");
	}
	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		if(event.isShiftDown()) {
			activePin.getAxes().rotateAboutZ(e/pa.TAU, true);
		}else if (event.isControlDown()) {
			activePin.getAxes().rotateAboutX(e/pa.TAU, true);
		}  else {
			activePin.getAxes().rotateAboutY(e/pa.TAU, true);
		}
		doSolve = true;
	}
	
	public void keyPressed() {
		if (pa.key == pa.CODED) {
			if (pa.keyCode == pa.DOWN) {
				incrementSelectedPin();
			} else if (pa.keyCode == pa.UP) {
				decrementSelectedPin();
			} else if(pa.keyCode == pa.RIGHT) {
				nextProgress = 1;
			} else if(pa.keyCode == pa.LEFT) {
				nextProgress = -1;
			}
		} else {
			if(pa.key == 'c') {
				doSolve = true;
			} else if (pa.key == 'r') {
				reloadShaders();
			} else if (pa.key == 's') {
				//guiView.sceneView.stencil.save("stenciltest.png");
			}
		}
	}
	
	public void mouseReleased() {
		this.widgetView.mouseReleased();
		this.sceneView.mouseReleased();
		this.eventOwner = null;
	}
	
	public void setEventOwner(Object o) {
		this.eventOwner = o;
	}
	public void releaseEventOwnership() {
		this.eventOwner = null;
	}
	public Object eventOwner() {
		return this.eventOwner;
	}
	
	/**returns true if a request to run the solver on the next frame is pending**/
	public boolean doSolve() {
		if(solveMode == this.PERPETUAL) 
			return true; 
		else {
			boolean toReturn = doSolve;
			doSolve = false;
			return toReturn;
		}			
	}
	public void requestSolve() {
		if(solveMode == INTERACTION) {
			this.doSolve = true;
		}
	}

}
