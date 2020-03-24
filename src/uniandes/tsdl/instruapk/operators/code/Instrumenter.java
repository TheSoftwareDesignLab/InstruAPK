package uniandes.tsdl.instruapk.operators.code;

import java.io.BufferedWriter;
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
		
//		ASTMutationLocation mLocation = (ASTMutationLocation) location;
//		CommonTree tree = mLocation.getTree();
//		CommonTree treee = ASTHelper.getFirstBrotherNamedOfType(smaliParser.I_STATEMENT_FORMAT11x, "move-result-object", tree);
//		String varName = treee.getChild(1).toString();
		
		List<String> newLines = new ArrayList<String>();
		List<String> lines = FileHelper.readLines(location.getFilePath());

		//Add lines before the MutationLocation
		for(int i=0; i < tt.getLine()-1; i++){
			newLines.add(lines.get(i));
		}

		System.out.println(((ASTMutationLocation) location).getTree().getClass());
		newLines.add("		sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;\n" +
				"\n" +
				"    const-string v1, \"RIP:" + mutantIndex + ":" + "" + ":" + ""+ ":" + new Date(System.currentTimeMillis()) + "\"\n" +
				"\n" +
				"    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V \n");
		
		for(int i=tt.getLine(); i < lines.size() ; i++){
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
