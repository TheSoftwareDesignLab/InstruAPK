package uniandes.tsdl.instruapk.operators.code;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;

import uniandes.tsdl.antlr.smaliParser;
import uniandes.tsdl.instruapk.helper.ASTHelper;
import uniandes.tsdl.instruapk.helper.FileHelper;
import uniandes.tsdl.instruapk.helper.Helper;
import uniandes.tsdl.instruapk.model.location.ASTMutationLocation;
import uniandes.tsdl.instruapk.model.location.MutationLocation;
import uniandes.tsdl.instruapk.operators.MutationOperator;

public class Instrumenter implements MutationOperator {

	@Override
	public boolean performMutation(MutationLocation location, BufferedWriter writer, int mutantIndex) throws Exception {


		ASTMutationLocation mLocation = (ASTMutationLocation) location;
		CommonTree tt = (CommonTree) mLocation.getTree().getFirstChildWithType(smaliParser.I_ORDERED_METHOD_ITEMS);
		CommonTree t = mLocation.getTree();

		List<String> newLines = new ArrayList<String>();
		List<String> lines = FileHelper.readLines(location.getFilePath());

		//Add lines before the MutationLocation
		for(int i=0; i < t.getLine()-1; i++){
			newLines.add(lines.get(i));
		}
		int iter = t.getLine()-1;
		String cLine = lines.get(iter);
		while(!cLine.startsWith(".method") && !cLine.contains(t.getChild(0).getText())) {
			newLines.add(lines.get(iter));
			iter++;
			cLine = lines.get(iter);
		}
		for (int i = iter; i < (iter+(tt.getLine()-t.getLine())); i++) {
			newLines.add(lines.get(i));
		}
		iter=(iter+(tt.getLine()-t.getLine()));
		
		newLines.add("		sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;");
		newLines.add("");
		newLines.add("    const-string v1, \"RIP:" + mutantIndex + ":" + (new File(mLocation.getFilePath())).getName() + ":" + t.getChild(0).toStringTree()+"\"");
		newLines.add("");
		newLines.add("    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V");
		newLines.add("");


		for(int i=iter; i < lines.size() ; i++){
			newLines.add(lines.get(i));
		}
		//
		FileHelper.writeLines(location.getFilePath(), newLines);
		Helper.mutationSuccess(mutantIndex);
		Helper.writeBasicLogInfo(mutantIndex, location.getFilePath(), location.getType().getName(), new int[] {mLocation.getLine(),tt.getLine()}, writer);
		writer.write("	For mutant "+mutantIndex+" Bluetooth Adapter has been set to null");
		writer.newLine();
		writer.flush();

		return true;
	}

}
