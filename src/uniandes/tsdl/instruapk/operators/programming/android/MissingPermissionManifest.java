package uniandes.tsdl.instruapk.operators.programming.android;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import uniandes.tsdl.instruapk.helper.FileHelper;
import uniandes.tsdl.instruapk.helper.Helper;
import uniandes.tsdl.instruapk.model.location.MutationLocation;
import uniandes.tsdl.instruapk.operators.MutationOperator;

public class MissingPermissionManifest implements MutationOperator{

	@Override
	public boolean performMutation(MutationLocation location, BufferedWriter writer, int mutantIndex) throws Exception {

		List<String> newLines = new ArrayList<String>();
		List<String> lines = FileHelper.readLines(location.getFilePath());
		
		for(int i=0; i < lines.size(); i++){
			
			String currLine = lines.get(i);
			
			//Discard lines to remove in the new source code file
			if(i < location.getStartLine() || i > location.getEndLine()){
				newLines.add(currLine);
			}
			
		}
		
		FileHelper.writeLines(location.getFilePath(), newLines);
		Helper.mutationSuccess(mutantIndex);
		Helper.writeBasicLogInfo(mutantIndex, location.getFilePath(), location.getType().getName(), new int[] {location.getStartLine()}, writer);
		writer.write("	For mutant "+mutantIndex+" the lines between "+(location.getStartLine()+1)+" and "+ (location.getEndLine()+1)+" have been deleted.");
		writer.newLine();
		writer.flush();

		return true;
	}

}
