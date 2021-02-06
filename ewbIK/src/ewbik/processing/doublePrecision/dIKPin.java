package ewbik.processing.doublePrecision;

import IK.doubleIK.AbstractIKPin;
import asj.SaveManager;
import IK.doubleIK.AbstractBone;
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
		return (DVector) super.getLocation_();
	}	

	public void translateTo(DVector v) {
		 super.translateTo_(v);
	}
	
	public void translateBy(DVector v) {
		 super.translateBy_(v);
	}
	
	/**rotate this pin about its X axis**/
	public void rotateAboutX(double radians) {
		axes.rotateAboutX(radians, true);
	}	
	/**rotate this pin about its X axis**/
	public void rotateAboutY(double radians) {
		axes.rotateAboutY(radians, true);
	}
	/**rotate this pin about its X axis**/
	public void rotateAboutZ(double radians) {
		axes.rotateAboutZ(radians, true);
	}
	@Override
	public dAxes getAxes()  {
		return (dAxes) axes;
	}
	
	@Override
	public dBone forBone() {
		return (dBone)super.forBone();
	}
}
