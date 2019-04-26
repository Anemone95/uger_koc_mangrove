package edu.umd.cs.mangrove.cte;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;

public class OwaspTranformationExp {
	static final String srcDir = "../owasp-1.1/src/main/java/org/owasp/benchmark/testcode/";
	static final String outDir = "../owasp-1.1Reduced/src/main/java/org/owasp/benchmark/testcode/";
	static final String datasetFileName = "/Users/ukoc/mangrove/phase2/lstm/data/java/CWE89OwaspCTE_2.java";
	static final String reportsPath = "/Users/ukoc/mangrove/findsecbugs/reports_owasp/";
	static final String fp = " falsepositive";
	static final String tp = " truepositive";

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		OwaspTranformationExp.processOwaspReports();
		//OwaspTranformationExp.singleRun();
	}

	public static void singleRun() throws IOException, ParserConfigurationException, SAXException {
		String sampleFile = "BenchmarkTest09682.java";
		BugReport report = BugReportUtil.parseXML(reportsPath + "09682TP.xml");
		System.out.println(CodeTransformer.process(srcDir, sampleFile, report));
	}

	public static void processOwaspReports() throws IOException, ParserConfigurationException, SAXException {
		File[] reports = new File(reportsPath).listFiles();
		PrintWriter writer = new PrintWriter(datasetFileName);
		for (File r : reports) {
			if (r.isFile() && r.getName().endsWith(".xml")) {
				//System.out.println(r.getName());
				BugReport report = BugReportUtil.parseXML(r.getAbsolutePath());
				//System.out.println(report);
				String prog = CodeTransformer.process(srcDir, report.getClassName(), report);
				//System.out.println(prog);
				writer.print(prog + (r.getName().contains("FP.") ? fp : tp) + "\n");
			}
		}
		writer.close();
	}
}