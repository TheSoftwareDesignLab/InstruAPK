package uniandes.tsdl.instruapk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import uniandes.tsdl.instruapk.detectors.MutationLocationListBuilder;
import uniandes.tsdl.instruapk.helper.APKToolWrapper;
import uniandes.tsdl.instruapk.helper.Helper;
import uniandes.tsdl.instruapk.model.MutationType;
import uniandes.tsdl.instruapk.model.location.MutationLocation;
import uniandes.tsdl.instruapk.operators.OperatorBundle;
import uniandes.tsdl.instruapk.processors.MutationsProcessor;
import uniandes.tsdl.instruapk.processors.SourceCodeProcessor;

public class InstruAPK {

	public static final String MUTANTS_PATH = ".\\instruAPKOutput\\";
	public static final String EXTRA_PATH = ".\\extra\\";
	public static void main(String[] args) {
		try {
			long initialTime = System.currentTimeMillis();
			runInstruAPK(args);
			System.out.println("Started at: " + new Date(initialTime));
			long finalTime = System.currentTimeMillis();
			System.out.println("Finalized at: " + new Date(finalTime));
			System.out.println("Total Time: "+ ((finalTime-initialTime)/1000)+" seconds");

		} catch (NumberFormatException e) {
			System.out.println("Amount of mutants parameter is not a number!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void runInstruAPK(String[] args) throws NumberFormatException, Exception {
		//Usage Error
		if (args.length < 5) {
			System.out.println("******* ERROR: INCORRECT USAGE *******");
			System.out.println("Argument List:");
			System.out.println("1. APK path");
			System.out.println("2. Package Name");
			System.out.println("3. Mutants path");
			System.out.println("4. Binaries path");
			System.out.println("5. Directory containing the operator.properties file");
			return;
		}

		//Getting arguments
		String apkName;
		String apkPath = args[0];
		String appName = args[1];
		String mutantsFolder = args[2];
		String extraPath = args[3];
		String operatorsDir = args[4];

		// Fix params based in OS
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0) {
			mutantsFolder = mutantsFolder.replaceAll("[/]", "\\\\")+File.separator;
			extraPath = extraPath.replaceAll("[/]", "\\\\")+File.separator;
			apkPath = apkPath.replaceAll("[/]", "\\\\");
			apkName = apkPath.substring(apkPath.lastIndexOf("\\"));
		} else {
			apkName = apkPath.substring(apkPath.lastIndexOf("/"));
		}
		//Read selected operators
		OperatorBundle operatorBundle = new OperatorBundle(operatorsDir);
		System.out.println(operatorBundle.printSelectedOperators());

		Helper.getInstance();
		Helper.setPackageName(appName);
		// Decode the APK
		APKToolWrapper.openAPKWithNoResources(apkPath, extraPath);

		//1. Create hashmap for locations
		HashMap<MutationType, List<MutationLocation>> locations = new HashMap<>();

		// //2. Run detection phase for AST-based detectors
		// //2.1 Preprocessing: Find locations to target API calls
		SourceCodeProcessor scp = new SourceCodeProcessor(operatorBundle);
		locations.putAll( scp.processFolder("temp", extraPath, appName));

		Set<MutationType> keys = locations.keySet();
		List<MutationLocation> list = null;
		System.out.println("Amount Mutants	Mutation Operator");
		for (MutationType mutationType : keys) {
			list = locations.get(mutationType);
			System.out.println(list.size()+"		"+mutationType);
		}
		//
		//	//3. Build MutationLocation List
		List<MutationLocation> mutationLocationList = MutationLocationListBuilder.buildList(locations);
		printLocationList(mutationLocationList, mutantsFolder, appName);
		System.out.println("Total Locations: "+mutationLocationList.size());

		//	//4. Run mutation phase
		MutationsProcessor mProcessor = new MutationsProcessor("temp", appName, mutantsFolder);
		mProcessor.process(mutationLocationList, extraPath, apkName);

	}

	private static void printLocationList(List<MutationLocation> mutationLocationList, String mutantsFolder, String appName) {

		try {
			System.out.println(mutantsFolder + File.separator + appName + "-locations.json");
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(mutantsFolder + File.separator + appName + "-locations.json"));
			writer.write("{");
			writer.newLine();
			writer.flush();
			for (int i = 0; i < mutationLocationList.size(); i++) {
				MutationLocation temp = mutationLocationList.get(i);
				writer.write("	\""+(i+1)+"\":{");
				writer.newLine();
				writer.write("		\"mutationTypeID\":\""+temp.getType().getId()+"\",");
				writer.newLine();
				writer.write("		\"mutationTypeName\":\""+temp.getType().getName()+"\",");
				writer.newLine();
				writer.write("		\"filePath\":\""+temp.getFilePath()+"\",");
				writer.newLine();
				writer.write("		\"line\":\""+temp.getLine()+"\",");
				writer.newLine();
				writer.write("		\"startLine\":\""+temp.getStartLine()+"\",");
				writer.newLine();
				writer.write("		\"endLine\":\""+temp.getEndLine()+"\",");
				writer.newLine();
				writer.write("		\"startColumn\":\""+temp.getStartColumn()+"\",");
				writer.newLine();
				writer.write("		\"endColumn\":\""+temp.getEndColumn()+"\",");
				writer.newLine();
				writer.write("		\"length\":\""+temp.getLength()+"\"");
				writer.newLine();
				writer.write((i==mutationLocationList.size()-1)?"	}":"	},");
				writer.newLine();
				writer.flush();
			}
			writer.write("}");
			writer.newLine();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
