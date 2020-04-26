package uniandes.tsdl.instruapk.operators.code;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;

import uniandes.tsdl.antlr.smaliParser;
import uniandes.tsdl.instruapk.helper.FileHelper;
import uniandes.tsdl.instruapk.helper.Helper;
import uniandes.tsdl.instruapk.model.location.ASTMutationLocation;
import uniandes.tsdl.instruapk.model.location.MutationLocation;
import uniandes.tsdl.instruapk.operators.MutationOperator;

public class Instrumenter implements MutationOperator {
	private boolean greaterThan2 = true;
	private int maxParam = 0;
	private int startParamsPosition = 0;
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
		String parameters = extractParameters(t.getChild(1).toStringTree());

		//System.out.println("Line before: " + cLine + " Parameters: " + parameters);
		while( /*Line should be a method*/ !(cLine.startsWith(".method")
				/*Line should contain the name of the method*/
				&& cLine.contains(t.getChild(0).toStringTree())
				/*Line should contain the parameters.*/
				&& cLine.contains(parameters))
				&& iter < lines.size()
				) {
			//System.out.println("Line while "+  iter + " : " + cLine + " Parameters: " + parameters);
			newLines.add(cLine);
			iter++;
			cLine = lines.get(iter);
		}
		//At this point the method that is going to be instrumented was already found. If what to see it, uncomment the line below.
		//System.out.println("Line After: " + cLine + " Parameters: " + parameters);

		for (int i = iter; i < (iter+(tt.getLine()-t.getLine())); i++) {
			String line = lines.get(i);
			//Check the .locals lines and if it's less than 2, change it to be at least 2
			line = lessThan2(line,mutantIndex);
			//Not needed because the parameters aren't going to be moved.
			//takeMaxParam(line);
			newLines.add(line);
		}

		iter=(iter+(tt.getLine()-t.getLine()));
		newLines.add(lines.get(iter++));

		// The method System.out.println("RIP:...") was changed for a Log.d("","RIP:...")
		// because the latter makes a static call and it seems to be the right way when instrumenting like this.
		// Also, the same number of registers are needed when using sysout
		newLines.add("");
		newLines.add("    const-string v0, \"InstruAPK\"");
		newLines.add("");
		newLines.add("    const-string v1, \"InstruAPK:" + mutantIndex + ":" + (new File(mLocation.getFilePath())).getName().split("\\.")[0] + ":" + t.getChild(0).toStringTree()+ ":"+ parameters +"\"");
		newLines.add("");
		newLines.add("    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I");
		newLines.add("");

		for(int i=iter; i < lines.size() ; i++) {
			newLines.add(lines.get(i));
		}

		FileHelper.writeLines(location.getFilePath(), newLines);
		Helper.mutationSuccess(mutantIndex);
		Helper.writeBasicLogInfo(mutantIndex, location.getFilePath(), location.getType().getName(), new int[] {mLocation.getLine(),tt.getLine()}, writer);
		writer.write("	For mutant "+mutantIndex+" Bluetooth Adapter has been set to null");
		writer.newLine();
		writer.flush();
		return true;
	}

	private String extractParameters(String parameters){
		//Split it using the string I_METHOD_RETURN_TYPE and take the las part of it
		parameters = parameters.split("I_METHOD_RETURN_TYPE")[parameters.split("I_METHOD_RETURN_TYPE").length-1];
		//Add spaces between the ) because there could be some of them like this )) an the next steps don't work as we expect
		parameters = parameters.replace(")"," ) ");
		//Split the string by the )
		parameters = parameters.split("\\)")[1];
		//if(!parameters.contains(";")){parameters = "";}
		//Add () because there could be methods without arguments
		parameters = "(" + parameters + ")";
		//Delete all the spaces in the string because we add some spaces in previous steps and when there is no arguments the string ends like (  ) which is not a valid value.
		// Also, some times, there are spaces between words that makes the instrumenter never found the method with that arguments. (We do not yet were those spaces were added.)
		return parameters.replace(" ","");
	}

	private String lessThan2(String cLine, int mutantIndex){
		if(cLine.equals("    .locals 0") || cLine.equals("    .locals 1")){
			System.out.println("Register's number has been changed");
			return "	.locals 2";
		}
		return  cLine;
	}
}
