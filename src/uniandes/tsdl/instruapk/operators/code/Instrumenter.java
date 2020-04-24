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
	private boolean greaterthan2 = true;
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

		if(
				/*Neither the methods with $ in their names because they are probably synthetic methods which are created when there is a nested class and one or more of its parameters are accessed*/
				t.getChild(0).toStringTree().contains("$")){
			System.out.println("Mutation was intended to be in synthetic method: \nName: " + t.getChild(0).toStringTree());
		}else{
			String cLine = lines.get(iter);
			String parameters = extractParameters(t.getChild(1).toStringTree());

			//This lines are for log and they help with debug stuff.
			//		System.out.println("child: " + t.getChild(0).toStringTree());
			//		System.out.println("paramst: " + parameters );
			//		System.out.println("file: "+ (new File(mLocation.getFilePath())).getName().split("\\.")[0]);

			//System.out.println("Line before: " + cLine + " Parameters: " + parameters);
			//When one or more variables are used inside the method, the line .locals should indicate how many variables are going to be use.
			//In case the line indicates that zero or one variable is going to be used, we change the line to indicate that two is the right number of variables.
			greaterThan2(cLine);
			cLine = checkMethodLocals(cLine);
			takeMaxParam(cLine);
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
				greaterThan2(cLine);
				takeMaxParam(cLine);
				cLine = checkMethodLocals(cLine);
			}
			//System.out.println("Line After: " + cLine + " Parameters: " + parameters);

			for (int i = iter; i < (iter+(tt.getLine()-t.getLine())); i++) {
				String line = lines.get(i);
				line = checkMethodLocals(line);
				greaterThan2(line);
				takeMaxParam(line);
				newLines.add(line);
			}
			iter=(iter+(tt.getLine()-t.getLine()));
			newLines.add(lines.get(iter++));
			//newLines.add((lines.get(iter++)));

			// The method System.out.println("RIP:...") was changed for a Log.d("","RIP:...")
			// because the latter makes a static call and it seems to be the right way when instrumenting like this.
//			if(!greaterthan2) {
//				moveParametersSafeZone(newLines);
//			}
			newLines.add("");
			newLines.add("    const-string v0, \"InstruAPK\"");
			newLines.add("");
			newLines.add("    const-string v1, \"InstruAPK:" + mutantIndex + ":" + (new File(mLocation.getFilePath())).getName().split("\\.")[0] + ":" + t.getChild(0).toStringTree()+"\"");
			newLines.add("");
			newLines.add("    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I");
			newLines.add("");
//			if(!greaterthan2){
//				moveParametersInitialState(newLines);
//			}
		}

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
	private String checkMethodLocals(String cLine){
//		if(cLine.contains(".locals 0") || cLine.contains(".locals 1")){
//			cLine = "	.locals 2";
//		}
		return cLine;
	}
	private void greaterThan2(String cLine){
		if(cLine.contains(".locals 0") || cLine.contains(".locals 1")){
			String localString = cLine.replace(".locals ","").replace(" ", "");
			startParamsPosition = Integer.parseInt(localString);
			greaterthan2 = false;
		}
	}
	private void takeMaxParam(String cLine){
		if(cLine.contains(".param p")){
			String param = cLine.replace(".param p","").replace(" ","");
			param = param.split(",")[0].trim();
			int paramValue = Integer.parseInt(param);
			maxParam = paramValue;
		}
	}
	private void moveParametersSafeZone(List<String> newLines){
		//It should start for the last parameter and start moving them to the safe locations depending on the startParamsPosition.
		// If it was 0 then move it 2 to reach v2 and so on, if was 1 move 1 with the same purpose
		//It can be change to always move them to 2 registers forward no mater the startParamsPosition?
		newLines.add("");
		for(int i = maxParam+1; i >= startParamsPosition; i--){
			String originalPosition = "v"+i;
			String safePosition = "";
			if(startParamsPosition ==0){
				safePosition = "v"+(i+2);
			}else{
				safePosition = "v"+(i+1);
			}
			newLines.add("	move-object " + safePosition+ "," + originalPosition);
		}
	}

	private void moveParametersInitialState(List <String> newLines){
		//It starts for the first param and starts moving them back to its original position depending on the startParamsPosition.
		//If it was 0 then move it 2 backwards to reach v0 and so on, if was 1 move 1 with the same purpose
		newLines.add("");
		for(int i = startParamsPosition; i <= maxParam+1; i++){
			String originalPosition = "v"+i;
			String safePosition = "";
			if(startParamsPosition ==0){
				safePosition = "v"+(i+2);
			}else{
				safePosition = "v"+(i+1);
			}
			newLines.add("	move-object " + originalPosition + "," + safePosition);
		}
	}
}
