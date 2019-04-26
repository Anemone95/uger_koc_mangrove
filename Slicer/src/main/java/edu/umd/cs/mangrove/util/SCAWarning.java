package edu.umd.cs.mangrove.util;

public class SCAWarning {
	private int lineNumber;
	private String fileName;
	String type;
	private String description;
	private String label;
	
	public SCAWarning(int lineNumber, String fileNme, String type, String description, String label) {
		super();
		this.setLineNumber(lineNumber);
		this.setFileName(fileNme);
		this.type = type;
		this.setDescription(description);
		this.setLabel(label);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
}
