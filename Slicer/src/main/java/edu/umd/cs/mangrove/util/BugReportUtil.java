package edu.umd.cs.mangrove.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class BugReportUtil {
	private static Pattern p = Pattern.compile("(U|S|T)(\\d+)");
	private static LSSerializer serializer;
	private static HashMap<String, String> primitiveTypes = new HashMap<>();
	//	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
	//		String filePath = DirectoryUtil.MANGROVEDIR + "data/findsecbugs/reports/owasp_sqli_reports.xml";
	//		HashMap<String, BugReport> parseXML2Map = parseXML2Map(new File(filePath));
	//		System.out.println(parseXML2Map);
	//		//parseXML(filePath);
	//	}

	public static BugReport parseXML(String filePath) throws IOException, ParserConfigurationException, SAXException {
		return parseXML(new File(filePath));
	}

	public static BugReport parseXML(File fXmlFile) throws IOException, ParserConfigurationException, SAXException {
		boolean isFP = fXmlFile.getName().endsWith("gP.xml") | fXmlFile.getName().endsWith("FP.xml");
		BugReport bugReport = new BugReport("");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		Node bugInstNode = null;

		NodeList methods = doc.getElementsByTagName("Method");
		for (int i = 0; i < methods.getLength(); i++) {
			if (methods.item(i).getNodeType() == Node.ELEMENT_NODE) {
				org.w3c.dom.Element m = (org.w3c.dom.Element) methods.item(i);
				String name = m.getAttribute("name");
				if ((isFP && !name.contains("bad")) || (!isFP && !name.contains("good"))) {
					bugInstNode = methods.item(i).getParentNode();
					bugReport.methodName = name;
					Node methodSourceLine = m.getElementsByTagName("SourceLine").item(0);
					bugReport.className = ((org.w3c.dom.Element) methodSourceLine).getAttribute("sourcefile");
					break;
				}
			}
		}
		if (bugInstNode == null) return null;

		org.w3c.dom.Element bugInstElement = (org.w3c.dom.Element) bugInstNode;
		bugReport.bugType = bugInstElement.getAttribute("type");
		NodeList childNodes = bugInstNode.getChildNodes();
		HashMap<Integer, String> localVariables = new HashMap<Integer, String>();
		for (int j = 0; j < childNodes.getLength(); j++) {
			if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
				org.w3c.dom.Element item = (org.w3c.dom.Element) childNodes.item(j);
				//				if (item.getTagName().equals("SourceLine")) { TODO
				//					int lineNumber = Integer.parseInt(item.getAttribute("start"));
				//					if (!bugReport.linenumbers.contains(lineNumber)) {
				//						bugReport.linenumbers.add(lineNumber);
				//					}
				//				} else 
				if (item.getTagName().equals("String")) {
					String role = item.getAttribute("role");
					String name = item.getAttribute("value");
					if (role.equals("Parameter Taint") && name.length() > 0) {
						bugReport.paramTaint = name.substring(0, name.contains(" ") ? name.indexOf(' ') : 1);
					} else if (role.equals("Taint Frame") && name.length() > 0) {
						Matcher m = p.matcher(name.split("\\|")[0]);
						while (m.find())
							localVariables.put(Integer.parseInt(m.group(2)), m.group(1));
					} else if (role.equals("Sink method")) {
						bugReport.injectableMethod = name.substring(name.indexOf('.') + 1, name.indexOf('('));
					} else if (role.equals("Method Bytecode")) {
						String[] variables = name.split("LocalVariable");
						for (int i = 1; i < variables.length; i++) {
							String var = variables[i].substring(1, variables[i].indexOf(')'));
							// String[] split = var.split(" ");
							String index = var.substring(var.indexOf("index = ") + 8, var.indexOf(":"));
							if (!bugReport.taintFrame.containsKey(index))
								bugReport.taintFrame.put(localVariables.get(index) + index, var);
						}
					}
				}
			}
		}
		String vardetail = bugReport.taintFrame.get(bugReport.paramTaint);
		if (vardetail != null) {
			bugReport.paramTaint = vardetail.replaceAll(".*, index = \\d:.*\\s", "");
		} else {

		}
		//Collections.sort(bugReport.linenumbers);
		// System.out.println(bugReport);
		return bugReport;
	}

	public static HashMap<String, BugReport> parseXML2Map(File xmlFile)
			throws IOException, ParserConfigurationException, SAXException {
		HashMap<String, BugReport> reports = new HashMap<String, BugReport>();
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		DOMImplementationLS domImplLS = (DOMImplementationLS) doc.getImplementation();
		serializer = domImplLS.createLSSerializer();
		Node firstChild = doc.getFirstChild();
		long time = 0;
		if (firstChild.getNodeType() == Node.ELEMENT_NODE) {
			String analysisTimestamp = ((org.w3c.dom.Element) firstChild).getAttribute("analysisTimestamp");
			String timestamp = ((org.w3c.dom.Element) firstChild).getAttribute("timestamp");
			time = TimeUnit.NANOSECONDS.toSeconds((Long.parseLong(analysisTimestamp) - Long.parseLong(timestamp)));
			//TODO test the conversion above 
		}
		NodeList bugReports = doc.getElementsByTagName("BugInstance");
		for (int i = 0; i < bugReports.getLength(); i++) {
			if (bugReports.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			BugReport bugReport = createBugReportObject((org.w3c.dom.Element) bugReports.item(i));
			bugReport.time = time;
			reports.put(bugReport.className + "_" + bugReport.lineNumber, bugReport);
		}

		return reports;
	}

	public static BugReport parseSingleBugXML(File xmlFile)
			throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		DOMImplementationLS domImplLS = (DOMImplementationLS) doc.getImplementation();
		serializer = domImplLS.createLSSerializer();
		Node firstChild = doc.getFirstChild();
		long time = 0;
		if (firstChild.getNodeType() == Node.ELEMENT_NODE) {
			String analysisTimestamp = ((org.w3c.dom.Element) firstChild).getAttribute("analysisTimestamp");
			String timestamp = ((org.w3c.dom.Element) firstChild).getAttribute("timestamp");
			time = TimeUnit.NANOSECONDS.toSeconds((Long.parseLong(analysisTimestamp) - Long.parseLong(timestamp)));
			//TODO test the conversion above 
		}
		BugReport bugReport = null;
		NodeList bugReports = doc.getElementsByTagName("BugInstance");
		for (int i = 0; i < bugReports.getLength(); i++) {
			if (bugReports.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			bugReport = createBugReportObject((org.w3c.dom.Element) bugReports.item(i));
			bugReport.time = time;
			break;
		}
		return bugReport;
	}

	public static ArrayList<BugReportAOL> parseSingleAOLXML(File xmlFile)
			throws IOException, ParserConfigurationException, SAXException {
		primitiveTypes.put("int", "I");
		primitiveTypes.put("void", "V");
		primitiveTypes.put("double", "D");
		primitiveTypes.put("boolean", "Z");
		primitiveTypes.put("char", "C");
		primitiveTypes.put("byte", "B");
		primitiveTypes.put("float", "F");
		primitiveTypes.put("short", "S");
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		DOMImplementationLS domImplLS = (DOMImplementationLS) doc.getImplementation();
		serializer = domImplLS.createLSSerializer();
		ArrayList<BugReportAOL> reports = new ArrayList<BugReportAOL>();
		NodeList bugReports = doc.getElementsByTagName("flow");
		for (int i = 0; i < bugReports.getLength(); i++) {
			if (bugReports.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			BugReportAOL createBugReportAOL = createBugReportAOL((org.w3c.dom.Element) bugReports.item(i));
			reports.add(createBugReportAOL);
			break;
		}
		return reports;
	}

	private static BugReportAOL createBugReportAOL(org.w3c.dom.Element reportXML) {
		BugReportAOL bugReport = new BugReportAOL();
		bugReport.setFullReport(serializer.writeToString(reportXML));
		NodeList list = reportXML.getElementsByTagName("reference");
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			org.w3c.dom.Element refNode = (org.w3c.dom.Element) list.item(i);
			String stmtValue = refNode.getElementsByTagName("statementgeneric").item(0).getTextContent();
			String[] split = stmtValue.split(" ");
			String type = primitiveTypes.containsKey(split[1]) ? primitiveTypes.get(split[1]) : "L" + split[1];
			type = type.replace(".", "/");
			String sinkorSource = split[2].substring(0, split[2].indexOf("("));
			stmtValue = split[0].substring(0, split[0].indexOf(":")) + "." + sinkorSource;
			String mthdValue = refNode.getElementsByTagName("method").item(0).getTextContent();
			String[] pair = mthdValue.split(" ");
			mthdValue = pair[2].substring(0, pair[2].indexOf("("));
			//com.example.collector.MainActivity.onCreate(Landroid/os/Bundle;)V
			String classValue = refNode.getElementsByTagName("classname").item(0).getTextContent().replace('.', '/');
			classValue = classValue.contains("$") ? classValue.substring(0, classValue.indexOf("$")) : classValue;
			String appValue = refNode.getElementsByTagName("file").item(0).getTextContent();
			//System.out.println(stmtValue + " :: " + mthdValue + " :: " + classValue + " :: " + appValue);
			if (refNode.getAttribute("type").equals("to")) {
				bugReport.setTos(stmtValue, classValue, mthdValue, appValue, type);
				bugReport.setSink(sinkorSource);
			} else {
				bugReport.setFroms(stmtValue, classValue, mthdValue, appValue, type);
				bugReport.setSource(sinkorSource);
			}
		}
		return bugReport;
	}

	private static BugReport createBugReportObject(org.w3c.dom.Element reportXML) {
		BugReport bugReport = new BugReport(new HashMap<String, ArrayList<Integer>>());
		bugReport.setFullReport(serializer.writeToString(reportXML));

		bugReport.bugType = reportXML.getAttribute("type");
		bugReport.priority = Integer.parseInt(reportXML.getAttribute("priority"));
		bugReport.rank = Integer.parseInt(reportXML.getAttribute("rank"));
		bugReport.className = ((org.w3c.dom.Element) reportXML.getChildNodes().item(1)).getAttribute("classname");

		org.w3c.dom.Element methodNode = (org.w3c.dom.Element) reportXML.getChildNodes().item(3);
		bugReport.methodName = methodNode.getAttribute("name");
		bugReport.methodSignature = methodNode.getAttribute("signature");

		bugReport.lineNumber = Integer
				.parseInt(((org.w3c.dom.Element) reportXML.getChildNodes().item(5)).getAttribute("start"));

		org.w3c.dom.Element injNode = (org.w3c.dom.Element) reportXML.getChildNodes().item(7);

		if (injNode != null) { // Sinkidentifier
			bugReport.injectableMethod = injNode.getAttribute("value");
		}

		HashSet<String> files = new HashSet<String>();
		for (int j = 8; j < reportXML.getChildNodes().getLength(); j++) {
			if (reportXML.getChildNodes().item(j).getNodeType() != Node.ELEMENT_NODE) continue;
			org.w3c.dom.Element traceLineNode = (org.w3c.dom.Element) reportXML.getChildNodes().item(j);
			if (!traceLineNode.getNodeName().equals("SourceLine")) continue;
			String sourcefile = traceLineNode.getAttribute("sourcefile");
			files.add(sourcefile);
			String start = traceLineNode.getAttribute("start");
			if (!bugReport.linenumbers.containsKey(sourcefile))
				bugReport.linenumbers.put(sourcefile, new ArrayList<Integer>());
			bugReport.linenumbers.get(sourcefile).add(Integer.parseInt(start));
			bugReport.witnessLenght++;
		}
		for (String file : files) {
			ArrayList<Integer> lines = bugReport.linenumbers.get(file);
			Collections.sort(lines);
			bugReport.linenumbers.put(file, lines);
		}
		return bugReport;
	}

	public static HashMap<String, Integer> parseXMLForStats(File xmlFile)
			throws IOException, ParserConfigurationException, SAXException {
		HashMap<String, Integer> fileStats = new HashMap<String, Integer>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		NodeList classStats = doc.getElementsByTagName("ClassStats");
		for (int i = 0; i < classStats.getLength(); i++) {
			if (classStats.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			org.w3c.dom.Element classStat = (org.w3c.dom.Element) classStats.item(i);
			String sourcefile = classStat.getAttribute("sourceFile");
			int numBugs = Integer.parseInt(classStat.getAttribute("bugs"));
			if (numBugs != 0) fileStats.put(sourcefile, numBugs);
		}
		return fileStats;
	}

	public static BugReport parseHTML(String filePath) throws IOException {
		File input = new File(filePath);
		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
		Element extraElement = doc.getElementsMatchingOwnText(filePath.contains("gP.") ? "good" : "bad").first();

		BugReport bugReport = new BugReport(new HashMap<String, ArrayList<Integer>>());
		String[] lines = extraElement.toString().split("<br>");
		// for (String line : lines) {
		// System.out.println(line);
		// }
		bugReport.bugType = lines[0].substring(lines[0].lastIndexOf("Bug type ") + 9, lines[0].lastIndexOf('(') - 1);
		bugReport.className = lines[1].substring(lines[1].lastIndexOf('.') + 1);
		bugReport.methodName = lines[2].substring(lines[2].lastIndexOf('.') + 1, lines[2].lastIndexOf('('));
		bugReport.injectableMethod = lines[4].substring(lines[4].indexOf('.') + 1, lines[4].lastIndexOf('('));
		bugReport.paramTaint = "U";
		// lines[7].substring(lines[7].lastIndexOf("Parameter Taint") + 16,
		// lines[7].lastIndexOf(' '));
		for (int i = 9; i < lines.length; i++) {
			if (lines[i].contains("[line ")) {
				String fileName = lines[i].substring(lines[i].lastIndexOf(' '));
				String lineNumberStr = lines[i].substring(lines[i].lastIndexOf(' ') + 1, lines[i].lastIndexOf(']'));
				if (!bugReport.linenumbers.containsKey(fileName)) {
					bugReport.linenumbers.put(fileName, new ArrayList<Integer>());
				}
				bugReport.linenumbers.get(fileName).add(Integer.parseInt(lineNumberStr));

			}
		}
		return bugReport;
	}

	static BugReport example1() {
		int priority = 3;
		int confidence = 5;
		int severity = 5;
		String bugType = "SQL_INJECTION_JDBC";
		String warningMethod = "doPost";
		String injectableMethod = "prepareStatement";
		String taintParam = "sql";
		HashMap<String, String> taintFrame = new HashMap<String, String>();
		String[] tf = { "U0,[2] {?}", "U1,[1](U) {[?]+[?]}", "U2,[0] {?}", "U3, {&lt;init&gt;()}", "U4, {getTheValue()}",
				"U5, {toString()}", "U6, {getSqlConnection()}", "U7, {?}|", "U6, {getSqlConnection()}", "U5, {toString()}",
				"S, {?}", "S, {?}", "S, {2}" };
		for (String pair : tf) {
			String keyvalue[] = pair.split(",");
			taintFrame.put(keyvalue[0], keyvalue[1]);
		}
		ArrayList<String> injectableArgs = null;
		return new BugReport(priority, confidence, severity, bugType, "fileName", warningMethod, injectableMethod,
				taintParam, taintFrame, null, injectableArgs);
	}

	static BugReport example2() {
		int priority = 3;
		int confidence = 5;
		int severity = 5;
		String bugType = "SQL_INJECTION_JDBC";
		String warningMethod = "doPost";
		String injectableMethod = "prepareCall";
		String taintParam = "sql";
		HashMap<String, String> taintFrame = new HashMap<String, String>();
		String[] tf = { "U0,[2] {?}", "U1,[1](U) {[?]+[?]}", "U2,[0] {?}", "U3, {&lt;init&gt;()}", "U4, {getTheValue()}",
				"U5, {toString()}", "U6, {getSqlConnection()}", "U7, {?}|", "U6, {getSqlConnection()}", "U5, {toString()}",
				"S, {?}", "S, {?}", "S, {2}" };
		for (String pair : tf) {
			String keyvalue[] = pair.split(",");
			taintFrame.put(keyvalue[0], keyvalue[1]);
		}
		ArrayList<String> injectableArgs = null;
		return new BugReport(priority, confidence, severity, bugType, "fileName", warningMethod, injectableMethod,
				taintParam, taintFrame, null, injectableArgs);
	}
}
