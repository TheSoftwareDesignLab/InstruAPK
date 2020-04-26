package uniandes.tsdl.instruapk.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jdt.core.dom.CompilationUnit;

import uniandes.tsdl.instruapk.detectors.code.visitors.APICallVO;
import uniandes.tsdl.instruapk.detectors.code.visitors.MethodCallVO;
import uniandes.tsdl.instruapk.detectors.code.visitors.MethodDeclarationVO;
import uniandes.tsdl.instruapk.detectors.code.visitors.TreeVisitorInstance;
import uniandes.tsdl.instruapk.helper.ASTHelper;
import uniandes.tsdl.instruapk.helper.Helper;
import uniandes.tsdl.instruapk.model.MutationType;
import uniandes.tsdl.instruapk.model.location.ASTMutationLocation;
import uniandes.tsdl.instruapk.model.location.MutationLocation;
import uniandes.tsdl.instruapk.operators.OperatorBundle;

public class SourceCodeProcessor {

	private HashSet<String> targetApis ;
	private HashSet<String> targetDeclarations ;

	private HashMap<String, List<Integer>> targetApisAndMutypes ;
	private HashMap<String, List<Integer>> targeDeclarationsAndMutypes ;

	private List<String> activities;
	private List<String> serializableClasses;
	private List<String> parcelableClasses;

	private OperatorBundle operatorBundle;

	private static SourceCodeProcessor instance = null;


	public static SourceCodeProcessor getInstance() {
		if(instance == null) {
			instance = new SourceCodeProcessor(null);
		}
		return instance;
	}


	public SourceCodeProcessor(OperatorBundle operatorBundle){
		this.operatorBundle = operatorBundle;
		targetApis = new HashSet<String>();
		targetDeclarations = new HashSet<String>();
		targetApisAndMutypes = new HashMap<>();
		targeDeclarationsAndMutypes = new HashMap<>();
		activities = new ArrayList<>();
		serializableClasses = new ArrayList<>();
		parcelableClasses = new ArrayList<>();
		instance = this;
	}

	public HashMap<MutationType, List<MutationLocation>> processFolder(String folderPath, String extrasFolder, String packageName) throws IOException{
		HashMap<MutationType, List<MutationLocation>> locations = new HashMap<>();
		folderPath = folderPath+File.separator+"smali";
		Collection<File> files = FileUtils.listFiles(new File(folderPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			if(file.getName().endsWith(".smali") && file.getCanonicalPath().contains(packageName.replace(".", Helper.isWindows()?"\\":"/"))
					&& !file.getName().contains("$")
					&& !file.getName().contains("EmmaInstrumentation.java") && !file.getName().contains("FinishListener.java") && !file.getName().contains("InstrumentedActivity.java") && !file.getName().contains("SMSInstrumentedReceiver.java")){
				HashMap<MutationType, List<MutationLocation>> fileLocations = processFile(file.getAbsolutePath(), folderPath, extrasFolder);
				appendLocations(fileLocations, locations);
			}
		}
		return locations;
	}

	private static void appendLocations(HashMap<MutationType, List<MutationLocation>> source, HashMap<MutationType, List<MutationLocation>> target){

		for(Entry<MutationType, List<MutationLocation>> entry : source.entrySet()){
			List<MutationLocation> sourceLocations = source.get(entry.getKey());
			List<MutationLocation> targetLocations = target.get(entry.getKey());

			if(targetLocations != null){
				targetLocations.addAll(sourceLocations);
			} else {
				targetLocations = sourceLocations;
			}

			target.put(entry.getKey(), targetLocations);
		}

	}

	private  HashMap<MutationType, List<MutationLocation>> processFile(String filePath, String projectPath, String extrasFolder){

		HashMap<MutationType, List<MutationLocation>> mutationLocations = new HashMap<>();

		List<String> lines = new ArrayList<String>();
		String unitName = filePath.substring(filePath.lastIndexOf(File.separator)+1).replace(".smali", "");

		MutationLocation location= null;
		MutationType muType = null;

		try {

			//Reading the source code (file)
			StringBuffer source  = readSourceFile(filePath, lines);

			//Getting AST from file
			CommonTree cu = ASTHelper.getAST(filePath);



			TreeVisitorInstance ttv = new TreeVisitorInstance(filePath);
			ttv.visit(cu, null);

			HashSet<APICallVO> calls = ttv.getCalls();

			Iterator<APICallVO> a = calls.iterator();
			while(a.hasNext()){
				APICallVO b = a.next();
				int[] c = b.getMuTypes();
				for (int i = 0; i < c.length; i++) {
					if(operatorBundle.isOperatorSelected(""+c[i])){
						muType = MutationType.valueOf(c[i]);

							location = ASTMutationLocation.buildLocation(b.getFilePath(), b.getLine(), -1, -1, -1, b.getLine(), -1, muType, b.getTree());
							if(!mutationLocations.containsKey(muType)){
								mutationLocations.put(muType, new ArrayList<MutationLocation>());
							}
							mutationLocations.get(muType).add(location);
					}
				}

			}
		} catch (FileNotFoundException e) {
			Logger.getLogger(SourceCodeProcessor.class.getName()).severe(
					" File not found " + filePath);


		} catch (IOException e) {
			Logger.getLogger(SourceCodeProcessor.class.getName()).severe(
					" Error reading/writing file " + filePath);
		} catch (ClassCastException e){
			Logger.getLogger(SourceCodeProcessor.class.getName()).severe(
					" Unable to cast TypeDeclaration " + filePath);
		} catch(Exception e){
			e.printStackTrace();
			Logger.getLogger(SourceCodeProcessor.class.getName()).severe(
					" Runtime Exception while casting TypeDeclaration " + filePath);
		}

		return mutationLocations;
	}

	private StringBuffer readSourceFile(String filePath,  List<String> lines)
			throws FileNotFoundException, IOException {
		StringBuffer source = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line = null;

		while((line = reader.readLine()) != null){
			lines.add(line);
			source.append(line).append("\n");
		}
		reader.close();
		return source;
	}

	private int computeEndLine(List<String> lines, int startLine, int startColumn, int length) {

		int endLine = startLine-1;
		int count = 0;

		do{
			endLine+=1;
			count += lines.get(endLine-1).length()-startColumn;	
			startColumn = 0;
		}while(count < length);


		return endLine;
	}
}