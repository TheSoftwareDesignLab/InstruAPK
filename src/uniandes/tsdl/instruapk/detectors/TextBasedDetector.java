package uniandes.tsdl.instruapk.detectors;

import java.util.List;

import uniandes.tsdl.instruapk.model.location.MutationLocation;

public abstract class TextBasedDetector  extends MutationLocationDetector {

	
	public abstract List<MutationLocation> analyzeApp(String rootPath) throws Exception;

}
