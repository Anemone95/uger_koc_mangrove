package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.mhpoptimization.CSDGPreprocessor;
import edu.kit.joana.wala.core.SDGBuilder;
import edu.kit.joana.wala.core.SDGBuilder.SDGBuilderConfig;
import edu.umd.cs.mangrove.slicing.JoanaLineSlicer.Line;
import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;
import edu.umd.cs.mangrove.util.PrintUtil;
import edu.umd.cs.mangrove.util.SDGUtils;

public class WarningtoSliceSingleEntryBatch {
	static final String HOME_DIR = System.getProperty("user.home") + "/";
	static final String MANGROVEDIR = System.getProperty("user.dir") + "/";
	static final String BENCHDIR = HOME_DIR + "workspace-bench/";
	static final String OUTPATH = MANGROVEDIR + "data/slices/new/";
	static final String xmlFile = MANGROVEDIR + "findsecbugs/reports/AllReports.xml";//AllReports
	static HashMap<String, BugReport> reportMap;
	static HashMap<String, Integer> wordMap;
	static HashMap<String, String> jarMap;
	static HashMap<String, ClassLoaderReference> libsMap = new HashMap<String, ClassLoaderReference>();
	private static final String exclusionsFile = "data/scopeFiles/H2SpecificExclusions.txt";
	private static SDG sdg = null;
	private static String SLICER = "Summary";

	public static void main(String[] args) throws Exception {
		libsMap.put("lib/servlet-api.jar", ClassLoaderReference.Primordial);
		reportMap = BugReportUtil.parseXML2Map(new File(xmlFile));
		prepareMaps();
		String reportsFile = "data/SliceThese.txt";
		List<String> reports = Files.readAllLines(Paths.get(reportsFile), StandardCharsets.UTF_8);

		String jarFiles = BENCHDIR + "susi_server.jar";//
		String[] entrypair = { "Lai/susi/server/AbstractAPIHandler", "doPost(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V" };//Lai/susi/mind/SusiSkill Lai/susi/SusiServer Lai/susi/mind/SusiMind Lai/susi/tools/AIML2Susi
		SDGBuilderConfig config = JoanaSDGHelper.getSDGBuilder(jarFiles, libsMap, exclusionsFile);
		config.entry = JoanaSDGHelper.findMethod(config, entrypair[0], entrypair[1]);
		sdg = SDGBuilder.build(config);
		System.out.print("SDG completed! Optimizing...");
		CSDGPreprocessor.preprocessSDG(sdg);
		System.out.print("Optimizing SDG completed! Computing Slices...");
		for (String warning : reports) {
			System.out.println(warning);
			String[] fields = warning.split(",");
			processReport(reportMap.get(fields[0] + "_" + fields[1]), fields[2], fields[3], fields[4], fields[5]);
			//break;
		}
	}

	private static void processReport(BugReport bugReport, String scopefile, String label, String entry, String prog)
			throws Exception {
		String[] entryFields = entry.replaceAll("\\.", "/").split(":");
		if (!entryFields[1].startsWith("L")) {
			entryFields[1] = "L" + entryFields[1];
		}
		String clazz = "L" + bugReport.getClassName().replaceAll("\\.", "/");
		String fileName = clazz.substring(clazz.lastIndexOf("/") + 1);
		String entrySubstr = entryFields[1].substring(entryFields[1].lastIndexOf("/") + 1) + "_"
				+ entryFields[0].substring(0, entryFields[0].indexOf("("));
		String outFile = OUTPATH + prog + "." + fileName + "." + bugReport.getLineNumber() + "." + entrySubstr + "."
				+ label + ".txt";
		//SDGSerializer.toPDGFormat(sdg, new PrintWriter(outFile.replace(".txt", ".pdg")));
		if (new File(outFile).exists()) {
			System.out.println("Slice Exists");
			return;
		}
		String sourceFile = clazz.substring(1);
		if (sourceFile.contains("$")) {
			sourceFile = sourceFile.substring(0, sourceFile.indexOf("$"));
		}
		Line errorLine = new Line(sourceFile + ".java", bugReport.getLineNumber());
		try {
			String jarFile = jarMap.get(scopefile) == null ? BENCHDIR + scopefile : jarMap.get(scopefile);
			String slice = computeSlice(bugReport, jarFile, "", entryFields[1], entryFields[0], errorLine, libsMap, clazz);
			PrintUtil.printToFile(outFile, slice, false);
		} catch (Exception e) {
			//throw e;
			System.out.println(e.getLocalizedMessage());
		}
	}

	public static String computeSlice(BugReport bugReport, String jarFile, String sourceFile, String entryClass,
			String entryMethod, Line line, HashMap<String, ClassLoaderReference> libsMap, String clazz)
			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {
		JoanaLineSlicer jSlicer = new JoanaLineSlicer(sdg, SLICER, true);
		System.out.print("Slice done...\n");
		HashSet<SDGNode> errorNodes = jSlicer.getNodesAtLine(line);
		Collection<SDGNode> slice = jSlicer.slice(errorNodes);
		String errorNodesStr = "[";
		for (SDGNode sdgNode : errorNodes) {
			if (!jSlicer.toAbstract.contains(sdgNode) && !JoanaLineSlicer.isRemoveNode(sdgNode)) {
				errorNodesStr += sdgNode.getId() + ", ";
			}
		}
		String result = errorNodesStr.substring(0, errorNodesStr.lastIndexOf(", ")) + "]\n"
				+ SDGUtils.prepareSlicePruned(sdg, slice, jSlicer.toAbstract);
		return result;
	}

	private static void prepareMaps() {
		jarMap = new HashMap<String, String>();
	}
}