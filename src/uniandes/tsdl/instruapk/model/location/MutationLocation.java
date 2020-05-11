package uniandes.tsdl.instruapk.model.location;

import uniandes.tsdl.instruapk.model.MutationType;

public class MutationLocation {
	
	private String filePath;
	private int line;
	private int startLine;
	private int endLine;
	private int startColumn;
	private int endColumn;
	private int length;
	private String methodName;
	private String className;
	private String methodParameters;
	
	private MutationType type;
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public int getLine() {
		return line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public void setStartColumn(int startCoumn) {
		this.startColumn = startCoumn;
	}

	public int getEndColumn() {
		return endColumn;
	}

	public void setEndColumn(int endColumn) {
		this.endColumn = endColumn;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}


	public MutationType getType() {
		return type;
	}

	public void setType(MutationType type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}


	public String getMethodName() {
		return methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodParameters() {
		return methodParameters;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setMethodParameters(String methodParameters) {
		this.methodParameters = methodParameters;
	}

	public static MutationLocation buildLocation(String path, int startLine, int endLine, int startCol, int endCol, int lineNum, int length, MutationType mType) {
		MutationLocation location = new MutationLocation();
		location.setFilePath(path);
		location.setStartLine(startLine);
		location.setEndLine(endLine);
		location.setStartColumn(startCol);
		location.setEndColumn(endCol);
		location.setType(mType);
		location.setLine(lineNum);
		location.setLength(length);
		return location;
	}
	
	
}
