package edu.umd.cs.mangrove.cte;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;

public class JulietTranformationExp {
	static String srcDir = "../Juliet4J/src/testcases/CWE89_SQL_Injection/";
	static String outDir = "../Juliet4JReduced/src/testcases/CWE89_SQL_Injection/";
	static final String datasetFileName = "/Users/ukoc/mangrove/phase2/lstm/data/java/CWE89JulietCTE_2.java";
	static final String reportsPath = "/Users/ukoc/mangrove/findsecbugs/reports_juliet/";
	static final String fp = " falsepositive";
	static final String tp = " truepositive";

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		JulietTranformationExp.processJulietReports();
		//JulietTranformationExp.singleRun();
	}

	private static void singleRun() throws IOException, ParserConfigurationException, SAXException {
		String sampleFile = "s01/CWE89_SQL_Injection__console_readLine_execute_45.java";
		BugReport report = BugReportUtil.parseXML(reportsPath + sampleFile.replace(".java", "gP.xml"));
		System.out.println(CodeTransformer.process(srcDir, sampleFile, report));

	}

	private static void processJulietReports() throws IOException, ParserConfigurationException, SAXException {
		String[] subFolders = { "s01/", "s02/", "s03/", "s04/" };
		PrintWriter writer = new PrintWriter(datasetFileName);
		for (String subFolder : subFolders) {
			File[] reports = new File(reportsPath + subFolder).listFiles();
			for (File r : reports) {
				if (r.getName().contains(".xml")) {
					try {
						System.out.println(r.getName());
						System.out.flush();
						BugReport report = BugReportUtil.parseXML(r.getAbsolutePath());
						if (report.getParamTaint() != null) {
							String prog = CodeTransformer.process(srcDir + subFolder, report.getClassName(), report);
							writer.print(prog + (r.getName().contains("gP.xml") ? fp : tp) + "\n");
						}else{
							System.out.println("report.paramTaint is null!");
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
						System.err.flush();
						e.printStackTrace();
					}
				}
			}
		}
		writer.close();
	}
}