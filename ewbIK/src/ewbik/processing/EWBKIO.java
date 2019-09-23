package ewbik.processing;


import ewbik.processing.doublePrecision.dArmature;
import ewbik.processing.doublePrecision.dBone;
import ewbik.processing.doublePrecision.dIKPin;
import ewbik.processing.doublePrecision.dKusudama;
import ewbik.processing.doublePrecision.dLimitCone;
import ewbik.processing.doublePrecision.sceneGraph.dAxes;
import ewbik.processing.singlePrecision.*;
import ewbik.processing.singlePrecision.sceneGraph.Axes;

import java.util.Collection;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import data.EWBIKLoader;
import data.EWBIKSaver;

public final class EWBKIO {
	
	/**
	 * Return a single precision (float) version of the armature in stored in the specified filepath
	 * @param path
	 * @return the Armature, or null if the file does not specify an armature
	 */
	public static Armature LoadArmature_singlePrecision(String path) {
		EWBIKLoader newLoader = new EWBIKLoader(); 
		Collection<Armature> ArmatureList =
				(Collection<Armature>)newLoader.importSinglePrecisionArmatures(path, 
				Axes.class, Bone.class, Armature.class, Kusudama.class, LimitCone.class, IKPin.class);
		for(Armature a : ArmatureList) {
			return a;
		}
		return null;
	}
		
	/**
	 * Return a double precision (double) version of the armature in stored in the specified filepath
	 * @param path
	 * @return the dArmature, or null if the file does not specify an armature
	 */
	public static dArmature LoadArmature_doublePrecision(String path) {
		EWBIKLoader newLoader = new EWBIKLoader(); 
		Collection<dArmature> dArmatureList =
				(Collection<dArmature>)newLoader.importDoublePrecisionArmatures(	path, 
				dAxes.class, dBone.class, dArmature.class, dKusudama.class, dLimitCone.class, dIKPin.class);
		for(dArmature a : dArmatureList) {
			return a;	
		}
		return null;
	}	
	
	
	/**
	 * save the given armature into the specified filepath
	 * @param path
	 * @param loadInto
	 */
	public static void SaveArmature(String path, dArmature toSave) {
		EWBIKSaver newSaver = new EWBIKSaver();
		newSaver.saveArmature(toSave, path);
	}
	
	
	/**
	 * save the given armature into the specified filepath
	 * @param path
	 * @param loadInto
	 */
	public static void SaveArmature(String path, Armature toSave) {
		EWBIKSaver newSaver = new EWBIKSaver();
		newSaver.saveArmature(toSave, path);
	}

}
