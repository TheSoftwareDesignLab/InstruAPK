package uniandes.tsdl.instruapk.detectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import uniandes.tsdl.instruapk.model.MutationType;
import uniandes.tsdl.instruapk.model.location.MutationLocation;

public class MutationLocationListBuilder {


	public static List<MutationLocation> buildList(HashMap<MutationType, List<MutationLocation>> locations){
		List<MutationLocation> mutationLocationList = new ArrayList<MutationLocation>();

		for(Entry<MutationType, List<MutationLocation>> entry : locations.entrySet()){
			assignMutationType(entry.getValue(), entry.getKey());
			mutationLocationList.addAll(entry.getValue());
		}

		return mutationLocationList;
	}

	private static void assignMutationType(List<MutationLocation> list, MutationType type){
		for(MutationLocation loc : list){
			if(loc != null){
				loc.setType(type);
			}
		}
	}

}
