package edu.umd.cs.mangrove.miscellaneous;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

import edu.umd.cs.mangrove.util.PrintUtil;

public class CweXML2SQL {
	public static final String OUTDIR = "/Users/ukoc/workspace-mangrove/mangroveweb/sql/";

	public static void main(final String[] args) throws IOException, ClassHierarchyException, UnsoundGraphException,
			CancelException, ParserConfigurationException, SAXException {
		String wFileNameXML = "data//1000CWE.xml";
		String insertSQL = "insert into Mitre_CWEs values(";
		String outfile = OUTDIR + "CWEList.sql";
		String insertReport = null;
		ArrayList<String[]> reports = parseXML2(new File(wFileNameXML));
		for (String[] report : reports) {
			insertReport = insertSQL + report[0] + ", '" + report[1] + ",'" + report[2] + "', " + report[3] + ");";
			// System.out.println(insertReport);
			PrintUtil.printToFile(outfile, insertReport, true);
		}

	}

	public static ArrayList<String[]> parseXML2(File xmlFile)
			throws IOException, ParserConfigurationException, SAXException {
		ArrayList<String[]> cweArr = new ArrayList<String[]>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();

		NodeList CWEs = doc.getElementsByTagName("Categories");
		for (int i = 0; i < CWEs.getLength(); i++) {
			if (CWEs.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;
			// TODO
		}
		return cweArr;
	}
}
