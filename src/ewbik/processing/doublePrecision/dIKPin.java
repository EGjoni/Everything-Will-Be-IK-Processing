package ewbik.processing.doublePrecision;

import IK.doubleIK.AbstractIKPin;
import IK.floatIK.AbstractBone;
import data.SaveManager;
import  ewbik.processing.doublePrecision.sceneGraph.*;


/**
 * Note, this class is a concrete implementation of the abstract class AbstractIKPin. Please refer to the {@link AbstractIKPin AbstractIKPin docs.} 
 */	
public class dIKPin extends AbstractIKPin{
	
	public dIKPin(){}
	
	public dIKPin(dAxes inAxes, boolean enabled, dBone bone) {
		super(inAxes, enabled, bone);
	}
	
	public dIKPin(dAxes inAxes, dBone bone) {
		super(inAxes, bone);
	}
	
	
	
	///WRAPPER FUNCTIONS. Basically just ctrl+f and replace these with the appropriate class names and 
	//any conversion functions you modified in AxesExample and you should be good to go. 
	public DVector getLocation() {
		return dAxes.toDVector(super.getLocation_());
	}
	

	public void translateTo(DVector v) {
		 super.translateTo_(dAxes.toSGVec(v));
	}
	
	public void translateBy(DVector v) {
		 super.translateBy_(dAxes.toSGVec(v));
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
