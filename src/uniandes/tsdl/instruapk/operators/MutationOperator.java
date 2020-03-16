package uniandes.tsdl.instruapk.operators;

import java.io.BufferedWriter;

import uniandes.tsdl.instruapk.model.location.MutationLocation;

public interface MutationOperator {

	boolean performMutation(MutationLocation location, BufferedWriter writer, int mutantIndex) throws Exception;
	
}
