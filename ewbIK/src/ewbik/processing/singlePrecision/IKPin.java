package ewbik.processing.singlePrecision;

import IK.floatIK.AbstractIKPin;
import data.JSONObject;
import data.LoadManager;
import data.SaveManager;
import ewbik.processing.singlePrecision.sceneGraph.Axes;
import processing.core.PVector;
import sceneGraph.math.floatV.AbstractAxes;

public class IKPin extends AbstractIKPin{
	
	//default constructor required for file loading to work
	public IKPin() {}
	
	public IKPin(Axes inAxes, boolean enabled, Bone bone) {
		super(inAxes, enabled, bone);
	}
	
	public IKPin(Axes inAxes, Bone bone) {
		super(inAxes, bone);
	}
	
	
	
	///WRAPPER FUNCTIONS. Basically just ctrl+f and replace these with the appropriate class names and 
	//any conversion functions you modified in AxesExample and you should be good to go. 
	public PVector getLocation() {
		return Axes.toPVector(super.getLocation_());
	}
	

	public void translateTo(PVector v) {
		 super.translateTo_(Axes.toSGVec(v));
	}
	
	public void translateBy(PVector v) {
		 super.translateBy_(Axes.toSGVec(v));
	}

	@Override
	public void notifyOfSaveIntent(SaveManager saveManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyOfSaveCompletion(SaveManager saveManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLoading() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLoading(boolean loading) {
		// TODO Auto-generated method stub
		
	}


}
