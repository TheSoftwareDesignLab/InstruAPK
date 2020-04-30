package uniandes.tsdl.instruapk.helper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Helper {

	public static Helper instance = null;
	public static String currDirectory = "";
	public static List<String> actNames = new ArrayList<String>();
	public static String mainActivity = "";
	public static String packageName = "";
	public final static String MANIFEST = "AndroidManifest.xml";
	public final static String MAIN_ACTION = "android.intent.action.MAIN";
	public static final int MIN_VERSION = 2;
	public static final int MAX_VERSION = 27;
	public static final String MIN_SDK_VERSION = "android:minSdkVersion";
	public static final String TARGET_SDK_VERSION = "android:targetSdkVersion";
	public static final String MAX_SDK_VERSION = "android:maxSdkVersion";
	public static final String STRINGS = "strings.xml";
	public static final String COLORS = "colors.xml";

	public static Helper getInstance() {
		if (instance == null) {
			instance = new Helper();
		}
		return instance;
	}

	public static String getPackageName() {
		return packageName;
	}

	public static void setPackageName(String packageName) {
		Helper.packageName = packageName;
	}

	public String getCurrentDirectory() throws UnsupportedEncodingException {
		String dir = System.getProperty("user.dir");
		return dir;
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0) {
			return true;
		}
		return false;
	}

	public static void mutationSuccess(int mutantIndex) {
		System.out.println("Mutant "+mutantIndex+" has survived the mutation process. Now its source code has been modified.");
	}

	public static void writeBasicLogInfo(int mutantIndex, String filePath, String name, int[] startLine,
			BufferedWriter writer) throws IOException {

		String mutatedLines = "{ ";
		for (int i = 0; i < startLine.length; i++) {
			mutatedLines += startLine[i]+", ";
		}
		mutatedLines = mutatedLines.substring(0,mutatedLines.length()-2)+" }";
		writer.write("Mutant "+mutantIndex+": "+filePath+"; "+name+" in "+((startLine.length>1)?"lines ":"line ")+mutatedLines);
		writer.newLine();
		writer.flush();
	}
}
