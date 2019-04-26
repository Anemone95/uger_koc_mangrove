package edu.umd.cs.mangrove.util;

public class BugReportAOL {
	public String stmtFrom;
	public String classFrom;
	public String mthdFrom;
	public String stmtTo;
	public String classTo;
	public String mthdTo;
	public String appFrom;
	public String appTo;
	public String typeFrom;
	public String typeTo;
	public String fullreport;
	public String sink;
	public String source;

	public void setFullReport(String fullreport) {
		this.fullreport = fullreport;
	}

	public void setSink(String sink) {
		this.sink = sink;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setFroms(String stmtFrom, String classFrom, String mthdFrom, String appFrom, String typeFrom) {
		this.stmtFrom = stmtFrom;
		this.classFrom = classFrom;
		this.mthdFrom = mthdFrom;
		this.appFrom = appFrom;
		this.typeFrom = typeFrom;
	}

	public void setTos(String stmtTo, String classTo, String mthdTo, String appTo, String typeTo) {
		this.stmtTo = stmtTo;
		this.classTo = classTo;
		this.mthdTo = mthdTo;
		this.appTo = appTo;
		this.typeTo = typeTo;
	}

	@Override
	public String toString() {
		return "BugReportAOL [stmtFrom=" + stmtFrom + ", classFrom=" + classFrom + ", mthdFrom=" + mthdFrom + ", stmtTo="
				+ stmtTo + ", classTo=" + classTo + ", mthdTo=" + mthdTo + ", appFrom=" + appFrom + ", appTo=" + appTo
				+ "]";
	}
}