package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;
import edu.umd.cs.mangrove.util.PrintUtil;

public class WarningtoCFG {
	static final String MANGROVEDIR = "/Users/ukoc/Documents/workspace/mangrove/mangrove/";
	static final String OUTPATH = MANGROVEDIR + "data/controlflowgraphs/rw/";
	static final String xmlFile = MANGROVEDIR + "data/findsecbugs/reports/AllReports.xml";
	static HashMap<String, BugReport> reportMap;
	static HashMap<String, Integer> wordMap;

	public static void main(String[] args) throws IOException, IllegalArgumentException, CancelException, WalaException,
			InvalidClassFileException, ParserConfigurationException, SAXException {
		reportMap = BugReportUtil.parseXML2Map(new File(xmlFile));
		wordMap = new HashMap<String, Integer>();
		String scopeFilesDir = "data/scopeFiles/benchmark/";//CWE89OwaspStatsAll
		String warningFile = "/Users/ukoc/Documents/workspace/mangrove/mangrove/data/labels/LabeledRW.txt";
		List<String> warnings = Files.readAllLines(Paths.get(warningFile), StandardCharsets.UTF_8);
		for (String warning : warnings) {
			System.out.println(warning);
			String[] fields = warning.split(",");
			computeCFG(reportMap.get(fields[0] + "_" + fields[1]), scopeFilesDir + fields[2], fields[3], fields[5]);
		}
		for (Entry<String, Integer> entry : wordMap.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	public static void computeCFG(BugReport bugReport, String scopefile, String label, String prog)
			throws IOException, CancelException, WalaException, InvalidClassFileException {
		System.out.println(bugReport);
		String clazz = "L" + bugReport.getClassName().replaceAll("\\.", "/");
		String fileName = clazz.substring(clazz.lastIndexOf("/") + 1);
		String outFile = OUTPATH + prog + "." + fileName + "." + bugReport.getLineNumber() + "." + label + ".txt";
		if (new File(outFile).exists()) {
			System.out.println("CFG Exists");
			return;
		}
		System.out.println(outFile);
		try {
			WALASlicer slicer = new WALASlicer(scopefile, null);
			String injectableMethod = bugReport.getInjectableMethod();
			injectableMethod = injectableMethod.substring(0, injectableMethod.indexOf("("));
			System.out.println(injectableMethod);
			
			String cfg = slicer.computeCFG(clazz, bugReport.getMethodName(), bugReport.getMethodSignature(), "",
					bugReport.getLineNumber());
			//cfg = PrintUtil.postprocess(cfg);
			//System.out.println(cfg + " " + label);
			//updateDict(cfg);
			PrintUtil.printToFile(outFile, cfg, true);
		} catch (com.ibm.wala.util.debug.UnimplementedError e) {
			System.out.println(e.getLocalizedMessage());
		} catch (IllegalArgumentException e) {
			System.out.println(e.getLocalizedMessage());
		} catch (NullPointerException e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
}