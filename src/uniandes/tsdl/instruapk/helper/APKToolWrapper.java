package uniandes.tsdl.instruapk.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class APKToolWrapper {
	//Same than openAPK but won't decode the resource files. Faster decoding. It will be use when there is no need for decoding the resources because they are not going to be edited.
	public static void openAPKWithNoResources(String path, String extraPath) throws IOException, InterruptedException{
		String decodedPath = Helper.getInstance().getCurrentDirectory();
		File tempFolder = new File(decodedPath+File.separator+"temp");
		if(tempFolder.exists()) {
			tempFolder.delete();
		}
		tempFolder.mkdirs();

		Process ps = Runtime.getRuntime().exec(new String[]{"java","-jar",Paths.get(decodedPath,extraPath,"apktool.jar").toAbsolutePath().toString(),"d",Paths.get(decodedPath,path).toAbsolutePath().toString(),"-o",Paths.get(decodedPath,"temp").toAbsolutePath().toString(),"-f","-r"});
		System.out.println("Processing your APK...");
		ps.waitFor();
		System.out.println("Wow... that was an amazing APK to process!!! :D");
	}
	
	
	public static boolean buildAPK(String path, String extraPath, String appName, int mutantIndex) throws IOException, InterruptedException{
		String decodedPath = Helper.getInstance().getCurrentDirectory();
		File tempFolder = new File(decodedPath+File.separator+"apk");
		if(tempFolder.exists()) {
			tempFolder.delete();
		}
		tempFolder.mkdirs();
//		Process ps = Runtime.getRuntime().exec(new String[]{"java","-jar",Paths.get(decodedPath,extraPath,"apktool.jar").toAbsolutePath().toString(),"b",Paths.get(decodedPath,path,"src").toAbsolutePath().toString(),"-o",Paths.get(decodedPath,path,decodedPath,appName).toAbsolutePath().toString(),"-f"});
		String firstArg = Paths.get(decodedPath,extraPath,"apktool.jar").toAbsolutePath().toString();
		//System.out.println("decodedPath: " + decodedPath + " path: " + path);
		String secondArg = Paths.get(decodedPath,path,"src").toAbsolutePath().toString();
		String thirdArg = Paths.get(decodedPath,path,appName).toAbsolutePath().toString();
		//System.out.println("first: " + extraPath + ":"+ firstArg + " second " + path + ":" +secondArg + " third: " + appName +":"+ thirdArg);
		Process ps = Runtime.getRuntime().exec(new String[]{"java","-jar",firstArg,"b",secondArg,"-o",thirdArg,"-f"});
		System.out.println("Building mutant "+mutantIndex+"...");
		ps.waitFor();
		firstArg = Paths.get(decodedPath,extraPath,"uber-apk-signer.jar").toAbsolutePath().toString();
		secondArg = Paths.get(decodedPath,path).toAbsolutePath().toString();
		thirdArg = Paths.get(decodedPath,path).toAbsolutePath().toString();
		//System.out.println("APK SIGNER VALUES");
		//System.out.println("first: " + extraPath + ":"+ firstArg + " second " + path + ":" +secondArg + " third: " + appName +":"+ thirdArg);
		Process pss = Runtime.getRuntime().exec(new String[]{"java","-jar",firstArg,"-a",secondArg,"-o",thirdArg});
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