package uniandes.tsdl.instruapk.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import uniandes.tsdl.instruapk.helper.Helper;

import java.nio.file.Files;
import java.nio.file.Paths;

public class APKToolWrapper {

	public static void openAPK(String path, String extraPath) throws IOException, InterruptedException{
		String decodedPath = Helper.getInstance().getCurrentDirectory();
		// Creates folder for decoded app
//		System.out.println(decodedPath);
		File tempFolder = new File(decodedPath+File.separator+"temp");
		if(tempFolder.exists()) {
			tempFolder.delete();
		}
		tempFolder.mkdirs();
		Process ps = Runtime.getRuntime().exec(new String[]{"java","-jar",Paths.get(decodedPath,extraPath,"apktool.jar").toAbsolutePath().toString(),"d",Paths.get(decodedPath,path).toAbsolutePath().toString(),"-o",Paths.get(decodedPath,"temp").toAbsolutePath().toString(),"-f"});
		System.out.println("Processing your APK...");
		ps.waitFor();
		System.out.println("Wow... that was an amazing APK to proccess!!! :D");
	}

	//Same than openAPK but won't decode the resource files. Faster decoding. It will be use when there is no need for decoding the resources because they are not going to be edited.
	public static void openAPKWithNoResources(String path, String extraPath) throws IOException, InterruptedException{
		String decodedPath = Helper.getInstance().getCurrentDirectory();
		// Creates folder for decoded app
//		System.out.println(decodedPath);
		File tempFolder = new File(decodedPath+File.separator+"temp");
		if(tempFolder.exists()) {
			tempFolder.delete();
		}
		tempFolder.mkdirs();
		Process ps = Runtime.getRuntime().exec(new String[]{"java","-jar",Paths.get(decodedPath,extraPath,"apktool.jar").toAbsolutePath().toString(),"d",Paths.get(decodedPath,path).toAbsolutePath().toString(),"-o",Paths.get(decodedPath,"temp").toAbsolutePath().toString(),"-f","-r"});
		System.out.println("Processing your APK...");
		ps.waitFor();
		System.out.println("Wow... that was an amazing APK to proccess!!! :D");
	}
	public static boolean buildAPK(String path, String extraPath, String appName, int mutantIndex) throws IOException, InterruptedException{
		String decodedPath = Helper.getInstance().getCurrentDirectory();
		Process ps = Runtime.getRuntime().exec(new String[]{"java","-jar",Paths.get(decodedPath,extraPath,"apktool.jar").toAbsolutePath().toString(),"b",Paths.get(decodedPath,path,"src").toAbsolutePath().toString(),"-o",Paths.get(decodedPath,path,appName).toAbsolutePath().toString(),"-f"});
		System.out.println("Building mutant "+mutantIndex+"...");
		ps.waitFor();
		Process pss = Runtime.getRuntime().exec(new String[]{"java","-jar",Paths.get(decodedPath,extraPath,"uber-apk-signer.jar").toAbsolutePath().toString(),"-a",Paths.get(decodedPath,path).toAbsolutePath().toString(),"-o",Paths.get(decodedPath,path).toAbsolutePath().toString()});
		System.out.println("Signing mutant "+mutantIndex+"...");
		pss.waitFor();
		if(Files.exists(Paths.get(decodedPath,path,appName).toAbsolutePath())) {
			System.out.println("SUCCESS: The "+mutantIndex+" mutant APK has been generated.");
			return true;
		} else {
			System.out.println("ERROR: The " + mutantIndex + " mutant APK has not been generated.");
			return false;
		}
	}
}