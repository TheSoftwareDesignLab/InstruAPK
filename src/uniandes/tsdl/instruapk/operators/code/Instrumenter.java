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
		String fileName = (new File(mLocation.getFilePath())).getName().split("\\.")[0];
		String methodAccessList = extractMethodAccessList(t,fileName);
		String methodName = t.getChild(0).toStringTree().trim();
		//print("Line before: Line: :" + cLine + ": Parameters: " + parameters + " access list: " + methodAccessList);
		while( /*Line should be a method*/
			/*Be aware that this enclosing negation !() changes the inside condition, may be a little bit confusing at first but it is just logic*/
				!(
				cLine.startsWith(".method")
				/*Line should contain the name of the method*/
				&& cLine.contains(methodName)
				/*Line should contain the parameters.*/
				&& cLine.contains(parameters)
				/*Line should contain the same access list values (public | private | protected | bridge | synthetic and so on)*/
				&& checkAccessList(cLine,methodName,methodAccessList)
		)
				&& iter < lines.size()
				) {
			//print("Line while: Iter: "+  iter + " : Line: :" + cLine + ": Parameters: " + parameters + " access list: " + methodAccessList);
			cLine = lines.get(iter);
			newLines.add(cLine);
			iter++;
		}
		//At this point the method that is going to be instrumented was already found. If you wanna see it, uncomment the line below.
		//print("Line after: iter: "+  iter + " Line: :" + cLine + ": Parameters: " + parameters + " access list: " + methodAccessList);
		int nextIter = (iter+(tt.getLine()-t.getLine()));
		for (int i = iter; i < nextIter && i < lines.size(); i++) {
			String line = lines.get(i);
			//Check the .locals lines and if it's less than 2, change it to be at least 2
			line = lessThan2(line);
			//print("line to be write before instrumentation: " + line);
			newLines.add(line);
		}
		/*Here there used to be a while trying to find the next line to be instrumented but this job is successfully done by the for above
		* now the tool is able to write the annotations without problems
		* */

		iter=nextIter;
		location.setMethodName(methodName);
		location.setClassName(fileName);
		location.setMethodParameters(parameters);
		location.setMethodAccessList(methodAccessList);
		newLines.add("    new-instance v0, Ljava/lang/StringBuilder;");
		newLines.add("");
		newLines.add("    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V");
		newLines.add("");
		newLines.add("    const-string v1, \"InstruAPK;;" + mutantIndex + ";;" +  fileName + ";;" + methodName + ";;"+ parameters + ";;"+ methodAccessList +";;"+ "\"");
		newLines.add("");
		newLines.add("    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;");
		newLines.add("");
		newLines.add("    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J");
		newLines.add("");
		newLines.add("    move-result-wide v1");
		newLines.add("");
		newLines.add("    invoke-virtual {v0, v1, v2}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;");
		newLines.add("");
		newLines.add("    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;");
		newLines.add("");
		newLines.add("    move-result-object v0");
		newLines.add("");
		newLines.add("    const-string v1, \"InstruAPK\"");
		newLines.add("");
		newLines.add("    invoke-static {v1, v0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I");
		newLines.add("");

		while(iter < lines.size()){
			newLines.add(lines.get(iter++));
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

	private String lessThan2(String cLine){
		if(cLine.equals("    .locals 0") || cLine.equals("    .locals 1") || cLine.equals("    .locals 2")){
			System.out.println("Register's number has been changed");
			return "	.locals 3";
		}
		return  cLine;
	}

//	private String extractMethod(int iter, List<String> lines){
//		int fakeIter = iter;
//		String method = "";
//		System.out.println("line in iter " + lines.get(iter));
//		System.out.println("line in fake iter: " + lines.get(fakeIter));
//		while(!lines.get(fakeIter).equals(".end method")){
//			method += " " + lines.get(fakeIter);
//			fakeIter++;
//		}
//		method += " " + lines.get(fakeIter);
//		return method;
//	}

	private String extractMethodAccessList(CommonTree t, String fileName){
		String accessList = t.getChild(2).toStringTree();
		String[] accessListArray = accessList.split("I_ACCESS_LIST");
		if(accessListArray.length > 0){
			accessList = accessListArray[1].trim();
			accessList = accessList.split("[)]")[0].trim();
		}else{
			accessList = "";
		}
		return accessList;
	}
	private boolean checkAccessList(String line, String searchMethodName, String expectedAccessList){
		String currentAccessList = line.split("\\.method")[1];
		currentAccessList = currentAccessList.split(searchMethodName)[0].trim();
		return currentAccessList.equals(expectedAccessList);
	}
	private void print(String value){
		System.out.println(value);
	}
}
