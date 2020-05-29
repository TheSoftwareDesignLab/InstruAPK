package uniandes.tsdl.instruapk.processors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import uniandes.tsdl.instruapk.helper.APKToolWrapper;
import uniandes.tsdl.instruapk.model.location.MutationLocation;
import uniandes.tsdl.instruapk.operators.MutationOperator;
import uniandes.tsdl.instruapk.operators.MutationOperatorFactory;

public class MutationsProcessor {

	private String appFolder;
	private String appName;
	private String mutantsRootFolder;
	private String mutantRootFolder;

	public MutationsProcessor(String appFolder, String appName, String mutantsRootFolder) {
		super();
		this.appFolder = appFolder;
		this.appName = appName;
		this.mutantsRootFolder = mutantsRootFolder;
	}

	private String setupMutantFolder(int mutantIndex) throws IOException {
		System.out.println("Creating folder for mutant "+ mutantIndex);
		String path = getMutantsRootFolder() + File.separator + getAppName() + "-mutant" + mutantIndex;
		System.out.println("Copying app information into mutant "+ mutantIndex+" folder");
		FileUtils.copyDirectory(new File(getAppFolder()), new File(path + File.separator + "src"));
		return path;

	}

	public void process(List<MutationLocation> locations, String extraPath, String apkName) throws IOException {
		MutationOperatorFactory factory = MutationOperatorFactory.getInstance();
		MutationOperator operator = null;
		int mutantIndex = 1;
		String mutantFolder = null;
		String newMutationPath = null;
		System.out.println(getMutantsRootFolder() + File.separator + getAppName() + "-mutants.log");
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(getMutantsRootFolder() + File.separator + getAppName() + "-mutants.log"));
		BufferedWriter wwriter = new BufferedWriter(
				new FileWriter(getMutantsRootFolder() + File.separator + getAppName() + "-times.csv"));
		wwriter.write("mutantIndex;mutantType;mutationTime;buildingTime");
		wwriter.newLine();
		wwriter.flush();
		//crete just one copy of the folder
		setupMutantFolder(0);
		boolean errorFound = false;
		for (MutationLocation mutationLocation : locations) {
			try {
				Long mutationIni = System.currentTimeMillis();
				//setupMutantFolder(mutantIndex);
				System.out.println("Mutant: " + mutantIndex + " - Type: " + mutationLocation.getType());
				operator = factory.getOperator(mutationLocation.getType().getId());

				//Same file name, same folder modified
				setMutantRootFolder(getMutantsRootFolder() + File.separator + getAppName() + "-mutant" + 0 + File.separator);
				mutantFolder = getMutantRootFolder() + "src" + File.separator;
				// The mutant should be written in mutantFolder
				newMutationPath = mutationLocation.getFilePath().replace(appFolder, mutantFolder);
				// System.out.println(newMutationPath);
				mutationLocation.setFilePath(newMutationPath);
				operator.performMutation(mutationLocation, writer, mutantIndex);
				Long mutationEnd = System.currentTimeMillis();
				File mutatedFile = new File(newMutationPath);
				String fileName = (new File(newMutationPath)).getName();
				File mutantRootFolderDir = new File(getMutantRootFolder()+fileName);
				FileUtils.copyFile(mutatedFile, mutantRootFolderDir);
				Long buildEnd = System.currentTimeMillis();
				Long mutationTime = mutationEnd-mutationIni;
				Long buildingTime = buildEnd - mutationEnd;
				wwriter.write(mutantIndex+";"+mutationLocation.getType().getId()+";"+mutationTime+";"+buildingTime);
				wwriter.newLine();
				wwriter.flush();
			} catch (Exception e) {
				errorFound = true;
				Logger.getLogger(MutationsProcessor.class.getName())
						.warning("- Error generating mutant  " + mutantIndex);
				System.out.println("Error generating mutant: " + mutantIndex + " location :" + mutationLocation);
				e.printStackTrace();
			}
			mutantIndex++;
		}
		try {
			if(!errorFound){
				System.out.println("Mutant Root Folder: " + getMutantsRootFolder());
				boolean result = APKToolWrapper.buildAPK(getMutantRootFolder(), extraPath, apkName, 0);
				if(result) {FileUtils.deleteDirectory(new File(mutantRootFolder + "src" + File.separator));}
			}
		}catch (Exception e ){
			System.out.println("There was an exception: \n stack trace: " + Arrays.toString(e.getStackTrace()) + "\n message: " + e.getMessage());
			e.printStackTrace();
		}
		writer.close();
		wwriter.close();
	}

	public String getAppFolder() {
		return appFolder;
	}

	public String getAppName() {
		return appName;
	}

	public void setMutantsRootFolder(String mutantsRootFolder){
		this.mutantsRootFolder = mutantsRootFolder;
	}

	public String getMutantsRootFolder() {
		return mutantsRootFolder;
	}

	public void setMutantRootFolder(String mutantRootFolder){
		this.mutantRootFolder = mutantRootFolder;
	}
	public String getMutantRootFolder(){
		return  this.mutantRootFolder;
	}
}
