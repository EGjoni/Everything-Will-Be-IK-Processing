import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

import IK.doubleIK.AbstractArmature;
import IK.doubleIK.AbstractBone;
import IK.doubleIK.AbstractIKPin;
import IK.doubleIK.AbstractKusudama;
import IK.doubleIK.AbstractLimitCone;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dKusudama;
import g4p_controls.GAlign;
import g4p_controls.GButton;
import g4p_controls.GCScheme;
import g4p_controls.GDropList;
import g4p_controls.GEvent;
import g4p_controls.GKnob;
import g4p_controls.GLabel;
import g4p_controls.GOption;
import g4p_controls.GPanel;
import g4p_controls.GSlider;
import g4p_controls.GSlider2D;
import g4p_controls.GSpinner;
import g4p_controls.GTextField;
import g4p_controls.GToggleGroup;
import g4p_controls.GView;
import math.Vec;
import math.doubleV.AbstractAxes;
import math.doubleV.Rot;
import math.doubleV.SGVec_3d;
import math.doubleV.Vec3d;
import processing.core.PApplet;
import processing.opengl.PGraphics3D;

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
	
	
	GSpinner boneHeight;
	ArrayList<AbstractBone> boneCollection = new ArrayList<>();
	HashMap<String, AbstractBone> boneMap = new HashMap<String, AbstractBone>();
	HashMap<AbstractBone, Integer> revBoneMap = new HashMap<AbstractBone, Integer>();
	//HashMap<String, AbstractBone> childMap = new HashMap<String, AbstractBone>();
	AbstractArmature armature;
	
	
	
	GSlider zoomSlider;
	GToggleGroup solverMode;
	GOption perpetual, onInteraction, byIteration, byStep;
	GPanel targetProperties;
	GPanel turntablePanel;// childBonesPanel;
	GPanel addChildPanel;
	GButton addConstraintBtn, addTargetBtn;
	GSlider dampening, stiffness;
	GSpinner iterations;
	GKnob boneTwistVisualizer;
	PApplet pa;
	
	
	public Dialogs dialogs;
	
	public int solveMode = 1;
	private GKnob turntableKnob;
	public GPanel bonePanel;
	private GDropList bonelist;
	private GTextField newBoneName;
	GPanel solverPanel;
	
	
	private GView view3DScene;
	private GView view3DWidget;
	public SceneView sceneView;
	public WidgetView widgetView;
	private int fixCount = 0;
	private GView debugView;
	
	HashMap<AbstractBone, PApplet> boneCWindowMap = new HashMap<>();
	
	
	
	public G4PUI(PApplet p, Number zoom, boolean multipassEnabled, 
			AbstractArmature armature, AbstractAxes worldAxes,
			BiConsumer<PGraphics3D, Integer> drawScene, BiConsumer<PGraphics3D, Integer> drawWidgets) {
		super();
		this.armature = armature;
		this.pa = p;
		this.worldAxes = worldAxes;
		this.zoom = (float)zoom;
		this.updatePinList();
		this.activePin = activePin;
		this.dialogs = new Dialogs(this);
		
		view3DScene = new GView(pa, 300, 0, pa.width-300, pa.height, PApplet.P3D);
		view3DWidget = new GView(pa, 300, 0, pa.width-300, pa.height, PApplet.P3D);
		view3DWidget.setAlpha(255);
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
			if (!hasConstraint){
				addConstraintBtn.setLocalColorScheme(GCScheme.BLUE_SCHEME);
				addConstraintBtn.setText("Add Constraint");
			} else {
				addConstraintBtn.setLocalColorScheme(GCScheme.CYAN_SCHEME);
				addConstraintBtn.setText("Edit Constraint");
			}
		}
		
		
		//constraintProperties.setVisible(hasConstraint);
		addTargetBtn.setVisible(!hasTarget);
		targetProperties.setVisible(hasTarget);
		
		if(hasTarget) {
			
		}
		if(hasConstraint) {
			Dialogs.KusudamaDialog dialog = dialogs.getDialogFor((AbstractKusudama)selectedBone.getConstraint());
			dialog.updateUI();
		} else {
			updateTwistVisualizer(null);
		}
		
		dampening.setValue((float)armature.getDampening());
		stiffness.setValue((float)selectedBone.getStiffness());
		
		iterations.setValue(armature.getDefaultIterations());
		
	}

	public void populateGUI() {
		GCScheme.changePaletteColor(GCScheme.PURPLE_SCHEME, 3, pa.color(0,0,0,255));
		GCScheme.changePaletteColor(GCScheme.CYAN_SCHEME, 3, pa.color(0,0,0,255));
		this.selectedBone = armature.getRootBone();
		
		//ConstraintPanel_init();
		TargetPanel_init();
		ChildPanel_init();
		BonePanel_init();
		
		SolverPanel_init();
		
		TargetPanel_init();
		
		TurntablePanel_init();		
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
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public void edit_painfulness(GSlider pain, GEvent event) {
		pa.println("pain event: " + event.name() + " : " +  pain.getValueF());
		if(this.selectedBone.getConstraint() != null) {
			this.selectedBone.getConstraint().setPainfulness(pain.getValueF());
		}
	}
	
	
	
	public void addConstraint_click(GButton btn, GEvent envet) {
		if(this.selectedBone.getConstraint() == null) {
			dKusudama newConstraint = new dKusudama((dBone) this.selectedBone);
			newConstraint.addLimitCone(new SGVec_3d(0f,1f,0f), Math.toRadians(60), null, null);
			newConstraint.setAxialLimits(-2d, 4d);
			this.selectedBone.addConstraint(newConstraint);
			newConstraint.optimizeLimitingAxes();
			this.updateUI();
		} else {
			
		}
	}
	
	long lastUIUpdate = 0;
	
	
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

	
	public void bone_select(GDropList gd, GEvent event) {
		String selected = gd.getSelectedText();
		AbstractBone toSelect = boneMap.get(selected);
		if(this.selectedBone != toSelect) {
			targetProperties.setCollapsed(true);
			this.selectedBone = toSelect;
			updateUI();
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
		pa.println("clicked");
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
		result = boneStrings.toArray(String[]::new);
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
	
	public void mouseReleased() {
		this.widgetView.mouseReleased();
		this.sceneView.mouseReleased();
	}
	
	public GPanel BonePanel_init() {
		bonePanel = new GPanel(pa, 0, 0, 300, 600, "Bone Info");
		bonePanel.setCollapsed(false);
		bonePanel.setCollapsible(false);
		bonePanel.setDraggable(false);
		bonePanel.setOpaque(true);
		bonePanel.setLocalColorScheme(GCScheme.BLUE_SCHEME, false);
		  
		bonelist = new GDropList(pa, 100, 20, 200, 400, 20, 20);		
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
		
		bonePanel.addControls(bonelist, stiffness, stif, addChildPanel, addTargetBtn, targetProperties, addConstraintBtn);
		
		boneTwistVisualizer = new GKnob(pa, bonePanel.getWidth()/2.5f, 70,  bonePanel.getWidth()/2.5f,  bonePanel.getWidth()/2, 1.0f);
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
		GButton cancelAddChild = new GButton(pa, 205, 35, 50, 30, "Cancel");
		cancelAddChild.addEventHandler(this, "dialog_cancel");		
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
		addChildPanel.addControl(cancelAddChild);
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
			Dialogs.KusudamaDialog kusuDialog = dialogs.getDialogFor((AbstractKusudama)selectedBone.getConstraint());
			range = kusuDialog.twistRange.getValueF();
			base = kusuDialog.twistMin.getValueF();
			currentVal = (float) constraint.getTwistRatio();
		}
		
		float min = Math.min(base+range, base);
		float max = Math.max(base+range, base);
		//int baseYellow = GCScheme.getPalette(boneTwistVisualizer.getLocalColorScheme())[14];
		//int baseBlue = GCScheme.getPalette(boneTwistVisualizer.getLocalColorScheme())[2];
		//boneTwistVisualizer.setLocalColor(2, baseBlue);
		
		boneTwistVisualizer.setLimits(-pa.PI, pa.PI);
		boneTwistVisualizer.setValue(currentVal);
		boneTwistVisualizer.setShowDecor(false, true, true, true);
		boneTwistVisualizer.setShowArcOnly(true);
		boneTwistVisualizer.setGripAmount(1f);
		//boneTwistVisualizer.setShowTrack(true);
		//boneTwistVisualizer.setShowLimits(true);
		boneTwistVisualizer.setShowValue(true);
		boneTwistVisualizer.setTurnRange(pa.degrees(min), pa.degrees(max));
	}
	
	
	public GPanel TargetPanel_init() {
		addTargetBtn = new GButton(pa, 10, 85, 120, 30, "Add Target");
		addTargetBtn.addEventHandler(this, "addTarget_click");
		targetProperties = new GPanel(pa, 0, 85, 300, 80, "Target Properties");
		targetProperties.setLocalColorScheme(GCScheme.CYAN_SCHEME);
		targetProperties.setDraggable(false);
		targetProperties.setCollapsed(true);
		targetProperties.setVisible(false);
		insertCloseButton(targetProperties);
		return targetProperties;
	}
	
	
	public void insertCloseButton (GPanel panel) {
		GButton closePanel = new GButton(pa, panel.getWidth()-60, panel.getHeight()-40, 50, 30, "Close");
		closePanel.addEventHandler(this, "dialog_cancel");
		panel.addControl(closePanel);
	}
	public GPanel SolverPanel_init() {
		solverPanel = new GPanel(pa, 0, 620, 300, 140, "Solver options");
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
		iterations = new GSpinner(pa, 230, 80, 50, 25, 1f);
		iterations.setLimits(10, 0, 1000, 1);
		iterations.addEventHandler(this, "set_iterations");
		GLabel itr = new GLabel(pa, -60, 5, 60, 10);
		itr.setText("Iterations: ");
		itr.setTextAlign(GAlign.EAST, GAlign.NORTH);
		iterations.addControl(itr);
		
		solverPanel.addControls(modelbl,perpetual,onInteraction,byIteration,byStep,dampening, dmp, iterations);
		return solverPanel;
	}

	
	public GPanel TurntablePanel_init() {
		turntablePanel = new GPanel(pa, 20, 780, 200, 140, "Turntable");
		turntableKnob = new GKnob(pa, 60f, 20f, 80f, 80f, 1.0f);
		turntableKnob.setTurnRange(0, 360);
		turntableKnob.setTurnMode(GKnob.CTRL_ANGULAR);
		turntableKnob.setShowArcOnly(true);
		turntablePanel.setCollapsible(false);
		turntablePanel.addEventHandler(this, "panel_cancel");
		turntableKnob.setIncludeOverBezel(false);
		turntableKnob.setShowTrack(true);
		turntableKnob.addEventHandler(this, "turntable_handle");
		turntableKnob.setLocalColorScheme(GCScheme.BLUE_SCHEME);
		turntablePanel.addControl(turntableKnob);
		
		zoomSlider = new GSlider(pa, turntablePanel.getWidth() - 40, turntablePanel.getHeight()-10, turntablePanel.getHeight()-30, 60, 12f);
		zoomSlider.setLimits(zoom, 0.25f, 0.8f*pa.PI);
		zoomSlider.setRotation(-pa.PI/2f);
		zoomSlider.setShowDecor(false, false, true, true);
		turntablePanel.addControl(zoomSlider);
		zoomSlider.addEventHandler(this, "edit_zoom");
		return turntablePanel;
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
	
	public void recursivelyAddToPinnedList(ArrayList<AbstractIKPin> pins, AbstractBone descendedFrom) {
		ArrayList<AbstractBone> pinnedChildren = (ArrayList<AbstractBone>) descendedFrom.getMostImmediatelyPinnedDescendants(); 
		for(AbstractBone b : pinnedChildren) {
			pins.add(b.getIKPin());
			if(b.getIKPin().getAxes().getParentAxes() == null)
				b.getIKPin().getAxes().setRelativeToParent(worldAxes);
		}
		for(AbstractBone b : pinnedChildren) {
			ArrayList<AbstractBone> children = (ArrayList<AbstractBone>) b.getChildren(); 
			for(AbstractBone b2 : children) {
				recursivelyAddToPinnedList(pins, b2);
			}
		}
	}
}
