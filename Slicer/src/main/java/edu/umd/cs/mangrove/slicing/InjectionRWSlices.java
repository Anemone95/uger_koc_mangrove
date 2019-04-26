package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.umd.cs.mangrove.util.BugReport;

public class InjectionRWSlices {
	static final Charset utf8 = StandardCharsets.UTF_8;

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		String appName = "accumulo";
		String outputPath = "/Users/ukoc/mangrove/phase2/slices/" + appName + "/";
		String warningXML = "/Users/ukoc/mangrove/findsecbugs/real_world_reports/" + appName + ".xml";
		String scopeFile = "data/" + appName + "-data.txt";
		ArrayList<String> specificExclusions = new ArrayList<String>();
		specificExclusions.add("Lorg\\/apache\\/hadoop\\/*");
		ArrayList<BugReport> parseXML = parseXML(new File(warningXML));
		for (BugReport bugReport : parseXML) {
			System.out.println(bugReport);
			WALASlicer slicer = new WALASlicer(scopeFile, specificExclusions);
			String callee = bugReport.getArtifact("callee");
			if (callee.contains("detected"))
				continue;
			callee = callee.substring(0, callee.indexOf("("));
			callee = callee.substring(callee.indexOf(".") + 1);
			String outFile = outputPath
					+ bugReport.getClassName().substring(bugReport.getClassName().lastIndexOf("/") + 1) + "_"
					+ bugReport.getLineNumber() + "_" + bugReport.getBugType() + "label.txt";
			if (new File(outFile).exists()) {
				continue;
			}
			try {
				String slice = slicer.slice("L" + bugReport.getClassName(), bugReport.getMethodName(),
						bugReport.getMethodSignature(), callee, bugReport.getLineNumber());
				System.out.println(slice);
				//printToFile(outFile, slice);
				break;
			} catch (Exception e) {
				System.err.println(e.getClass());
			} catch (Error e) {
				System.err.println(e.getClass());
			}
		}
	}

	public static void printToFile(String fName, String slice) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(fName);
		writer.print(slice);
		writer.flush();
		writer.close();
	}

	public static ArrayList<BugReport> parseXML(File fXmlFile)
			throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		ArrayList<BugReport> reports = new ArrayList<BugReport>();
		NodeList bugInstances = doc.getElementsByTagName("BugInstance");
		for (int i = 0; i < bugInstances.getLength(); i++) {
			if (bugInstances.item(i).getNodeType() == Node.ELEMENT_NODE) {
				NamedNodeMap attributes = bugInstances.item(i).getAttributes();
				BugReport bugReport = new BugReport(attributes.getNamedItem("abbrev").getNodeValue());
				bugReport.addArtifact("priority", attributes.getNamedItem("priority").getNodeValue());
				bugReport.addArtifact("category", attributes.getNamedItem("category").getNodeValue());
				NodeList childNodes = bugInstances.item(i).getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++) {
					if (childNodes.item(j).getNodeType() != Node.ELEMENT_NODE)
						continue;
					org.w3c.dom.Element node = (org.w3c.dom.Element) childNodes.item(j);
					if (node.getTagName().equals("Method")) {
						bugReport.setMethodName(node.getAttribute("name"));
						bugReport.setMethodSignature(node.getAttribute("signature"));
						bugReport.setClassName(node.getAttribute("classname").replaceAll("\\.", "/"));
					} else if (node.getTagName().equals("SourceLine")) {
						if (bugReport.getLineNumber() < 0) {
							bugReport.setLineNumber(Integer.parseInt(node.getAttribute("start")));
						} else {
							bugReport.addArtifact("lineNumber", node.getAttribute("start"));
						}
					} else if (node.getTagName().equals("String")) {
						bugReport.addArtifact("callee", node.getAttribute("value"));
					}
				}
				reports.add(bugReport);
			}
		}
		return reports;
	}
}