package edu.umd.cs.mangrove.miscellaneous;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.github.javaparser.utils.Pair;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;
import edu.umd.cs.mangrove.util.PrintUtil;

public class BugReportSQLGenerator {
	public static final String OUTDIR = "/Users/ukoc/Documents/workspace/mangrove/mangroveweb/sql/";

	public static void main(final String[] args) throws IOException, ClassHierarchyException, UnsoundGraphException,
			CancelException, ParserConfigurationException, SAXException {
		// generateSQLforXML();
		generateSQLforHTML();
	}

	private static void generateSQLforXML() throws IOException, ParserConfigurationException, SAXException {
		String wFileNameXML = "data/reports/dacapo-reports/h2-reports.xml";
		final String insertSQL = "insert into SCA_Reports values(0, 1, 1, 89, '";
		final String delimiter = "|";
		String outfile = OUTDIR + "FindSecBugsReports.sql";
		String insertReport = null;
		HashMap<String, BugReport> parseXML2Map = BugReportUtil.parseXML2Map(new File(wFileNameXML));
		Set<Entry<String, BugReport>> entrySet = parseXML2Map.entrySet();
		for (Entry<String, BugReport> entry : entrySet) {
			BugReport report = entry.getValue();
			insertReport = insertSQL + report.getFullReport() + delimiter + report.getClassName() + delimiter
					+ report.getMethodName() + report.getMethodSignature() + delimiter + report.getLineNumber();
			// System.out.println(insertReport);
			PrintUtil.printToFile(outfile, insertReport, true);
		}
	}

	private static void generateSQLforHTML() throws IOException, ParserConfigurationException, SAXException {
		HashMap<String, Pair<String, String>> cweMap = new HashMap<String, Pair<String, String>>();
		;
		cweMap.put("COMMAND_INJECTION",
				new Pair<String, String>("CMDi-78", "https://cwe.mitre.org/data/definitions/78.html\" target=\"_blank\""));
		cweMap.put("XSS_SERVLET",
				new Pair<String, String>("XSS-80", "https://cwe.mitre.org/data/definitions/80.html\" target=\"_blank\""));
		cweMap.put("SQL_INJECTION_JDBC",
				new Pair<String, String>("SQLi-89", "https://cwe.mitre.org/data/definitions/89.html\" target=\"_blank\""));
		cweMap.put("LDAP_INJECTION",
				new Pair<String, String>("LDAPi-90", "https://cwe.mitre.org/data/definitions/90.html\" target=\"_blank\""));
		cweMap.put("CRLF_INJECTION_LOGS", new Pair<String, String>("CRLF-117",
				"https://cwe.mitre.org/data/definitions/117.html\" target=\"_blank\""));
		cweMap.put("XPATH_INJECTION", new Pair<String, String>("XPATH-643",
				"https://cwe.mitre.org/data/definitions/643.html\" target=\"_blank\""));
		cweMap.put("EL_INJECTION",
				new Pair<String, String>("ELi-917", "https://cwe.mitre.org/data/definitions/917.html\" target=\"_blank\""));

		String benchmarkName = "Jackrabbit";
		String wFileNameXML = "findsecbugs/jackrabbit-all.html";
		final String insertSQL = "insert into SCA_Reports values(0, 1, 2, 89, '";
		final String delimiter = "|";
		String outfile = OUTDIR + benchmarkName + "ReportsHTMLAAA.sql";
		String insertReport = null;
		HashMap<String, BugReport> parseXML2Map = BugReportUtil.parseXML2Map(new File(wFileNameXML));
		Set<Entry<String, BugReport>> entrySet = parseXML2Map.entrySet();
		for (Entry<String, BugReport> entry : entrySet) {
			BugReport report = entry.getValue();
			Pair<String, String> pair = cweMap.get(report.getBugType());
			insertReport = benchmarkName + delimiter + report.getBugType() + delimiter + pair.a + delimiter
					+ report.getClassName() + delimiter + report.getMethodName() + delimiter + report.getLineNumber()
					+ delimiter + report.getFullReport();
			insertReport = insertReport.replace("#" + report.getBugType() + "\"", pair.b);
			System.out.println(insertReport);
			if (!insertReport.contains("org.h2.test.") && !insertReport.contains("org.h2.samples.")) {
				PrintUtil.printToFile(outfile, insertReport, true);
			}
		}
	}
}
