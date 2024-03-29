package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
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
import edu.kit.joana.ifc.sdg.graph.SDGSerializer;
import edu.kit.joana.ifc.sdg.mhpoptimization.CSDGPreprocessor;
import edu.kit.joana.wala.core.SDGBuilder;
import edu.kit.joana.wala.core.SDGBuilder.SDGBuilderConfig;
import edu.umd.cs.mangrove.slicing.JoanaLineSlicer.Line;
import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;
import edu.umd.cs.mangrove.util.DirectoryUtil;
import edu.umd.cs.mangrove.util.PrintUtil;
import edu.umd.cs.mangrove.util.SDGUtils;

public class WarningtoSlice {
	static final String OUTPATH = DirectoryUtil.PROJECT_DIR + "data/slices/rw/";//rwdataset-pruned/
	static final String xmlFile = DirectoryUtil.PROJECT_DIR + "data/findsecbugs/reports/AllReports.xml";
	static HashMap<String, BugReport> reportMap;
	static HashMap<String, Integer> wordMap;
	static HashMap<String, String> jarMap;
	static HashMap<String, ClassLoaderReference> libsMap = new HashMap<String, ClassLoaderReference>();
	private static final String exclusionsFile = "data/scopeFiles/H2SpecificExclusions.txt";
	public static final Charset charset = Charset.forName("UTF-8");
	private static String SLICER = "Summary";
	static HashSet<String> engDictionary = new HashSet<String>();

	public static void main(String[] args) throws Exception {
		libsMap.put("lib/servlet-api.jar", ClassLoaderReference.Primordial);
		libsMap.put(DirectoryUtil.BENCH_DIR + "/hsqldb/lib/ant-1.10.1.jar", ClassLoaderReference.Primordial);
		libsMap.put(DirectoryUtil.BENCH_DIR + "/hsqldb/lib/junit.jar", ClassLoaderReference.Primordial);
		libsMap.put(DirectoryUtil.HOME_DIR + ".m2/repository/io/airlift/airline/0.7/airline-0.7.jar",
				ClassLoaderReference.Primordial);
		libsMap.put(DirectoryUtil.HOME_DIR + ".m2/repository/junit/junit/4.12/junit-4.12.jar",
				ClassLoaderReference.Primordial);
		libsMap.put(DirectoryUtil.HOME_DIR + ".m2/repository/com/squareup/okio/okio/1.14.0/okio-1.14.0.jar",
				ClassLoaderReference.Primordial);

		reportMap = BugReportUtil.parseXML2Map(new File(xmlFile));
		prepareMaps();
		if (args.length > 10) {
			System.out.println(args[0]);
			String[] fields = args[0].split(",");
			processReport(reportMap.get(fields[0] + "_" + fields[1]), fields[2], fields[3], fields[4], fields[5]);
		} else {
			String reportsFile = "data/SliceThese.txt";
			List<String> reports = Files.readAllLines(Paths.get(reportsFile), StandardCharsets.UTF_8);
			for (String warning : reports) {
				try {
					//System.out.println(warning);
					String[] fields = warning.split(",");
					processReport(reportMap.get(fields[0] + "_" + fields[1]), fields[2], fields[3], fields[4], fields[5]);
					//break;
				} catch (Exception e) {
					//throw e;
					System.out.println(e.getMessage());
				}
			}
		}
	}

	private static void processReport(BugReport bugReport, String scopefile, String label, String entry, String prog)
			throws Exception {
		try {
			String[] entryFields = entry.replaceAll("\\.", "/").split(":");
			if (!entryFields[1].startsWith("L")) entryFields[1] = "L" + entryFields[1];

			String clazz = "L" + bugReport.getClassName().replaceAll("\\.", "/");
			String fileName = clazz.substring(clazz.lastIndexOf("/") + 1);
			String entrySubstr = entryFields[1].substring(entryFields[1].lastIndexOf("/") + 1) + "."
					+ entryFields[0].substring(0, entryFields[0].indexOf("("));
			String pdfFile = DirectoryUtil.HOME_DIR + "sdgs/" + entrySubstr + ".pdg";
			String outFile = OUTPATH + prog + "." + fileName + "." + bugReport.getLineNumber() + "." + entrySubstr + "."
					+ label + ".txt";
			System.out.println(outFile);
			if (new File(outFile).exists()) {
				System.out.println("Slice Exists");
				return;
			}
			String sourceFile = clazz.substring(1);
			if (sourceFile.contains("$")) {
				sourceFile = sourceFile.substring(0, sourceFile.indexOf("$"));
			}
			Line errorLine = new Line(sourceFile + ".java", bugReport.getLineNumber());
			String jarFile = jarMap.get(scopefile);
			if (jarFile == null) jarFile = DirectoryUtil.BENCH_DIR + scopefile;
			String slice = computeSlice(jarFile, entryFields[1], entryFields[0], errorLine, libsMap, clazz, pdfFile);
			PrintUtil.printToFile(outFile, slice, false);
		} catch (Exception e) {
			System.out.println(e.getMessage()); //throw e;
		}
	}

	public static String computeSlice(String jarFile, String entryClass, String entryMethod, Line line,
			HashMap<String, ClassLoaderReference> libsMap, String clazz, String pdgFile)
			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {
		SDG localSdg = null;
		//		File file = new File(pdgFile);
		//		if (sdg != null && pdgFile.equals(sdgName)) {
		//			localSdg = sdg;
		//		} else if (file.exists() && file.length() < SIZE_LIMIT) {
		//			System.out.print("Reading SDG from: " + pdgFile + " ... ");
		//			localSdg = SDG.readFrom(pdgFile);
		//			System.out.print("SDG file read done! ");
		//		} else {
		System.out.print("Building SDG... ");
		SDGBuilderConfig config = JoanaSDGHelper.getSDGBuilder(jarFile, libsMap, exclusionsFile);
		config.entry = JoanaSDGHelper.findMethod(config, entryClass, entryMethod);
		localSdg = SDGBuilder.build(config);
		System.out.print("SDG build done! Optimizing...");
		CSDGPreprocessor.preprocessSDG(localSdg);
		System.out.print("Done! Saving to: " + pdgFile);
		SDGSerializer.toPDGFormat(localSdg, new PrintWriter(pdgFile));
		System.out.print(" Done! ");
		//		}
		//		sdg = localSdg;
		//		sdgName = pdgFile;
		System.out.print("Computing Slice...");
		JoanaLineSlicer jSlicer = new JoanaLineSlicer(localSdg, SLICER, true);
		System.out.print("Done...\n");
		HashSet<SDGNode> errorNodes = jSlicer.getNodesAtLine(line);
		Collection<SDGNode> slice = jSlicer.slice(errorNodes);
		String errorNodesStr = "[";
		for (SDGNode sdgNode : errorNodes) {
			if (!jSlicer.toAbstract.contains(sdgNode) && !JoanaLineSlicer.isRemoveNode(sdgNode)) {
				errorNodesStr += sdgNode.getId() + ", ";
			}
		}
		String result = errorNodesStr.substring(0, errorNodesStr.lastIndexOf(",")) + "]\n"
				+ SDGUtils.prepareSliceForEncoding(localSdg, slice, jSlicer.toAbstract);
		return result;
	}

	private static void prepareMaps() throws IOException {
		jarMap = new HashMap<String, String>();
		String benchdir = DirectoryUtil.BENCH_DIR;
		jarMap.put("apollo_scope.txt",
				benchdir + "apollo/apollo-core.jar:" + benchdir + "apollo/apollo-demo.jar:" + benchdir
						+ "apollo/apollo-portal.jar:" + benchdir + "apollo/apollo-client.jar:" + benchdir
						+ "apollo/apollo-configservice.jar");
		jarMap.put("biojava_scope.txt", benchdir + "biojava/biojava-structure.jar");
		//jarMap.put("corenlp_scope.txt", BENCHDIR + "CoreNLP/target/stanford-corenlp-3.7.0.jar");
		jarMap.put("freecs_scope.txt", benchdir + "freecs-1.3.20111225/freecs.jar");
		jarMap.put("giraph_scope.txt", benchdir + "giraph/giraph-core.jar");
		jarMap.put("h2_scope.txt", benchdir + "h2db/h2/bin/h2-1.4.196.jar");
		jarMap.put("hsqldb_scope.txt", benchdir + "hsqldb/lib/hsqldb2.jar");
		jarMap.put("jackrabbit_scope.txt", benchdir + "jackrabbit-core.jar:" + benchdir
				+ "jackrabbit/jackrabbit-webdav/target/jackrabbit-webdav-2.15.7-SNAPSHOT.jar:");
		jarMap.put("jetty_scope.txt",
				benchdir + "jetty/jetty-server/target/jetty-server-9.4.8-SNAPSHOT.jar:" + benchdir
						+ "jetty/jetty-util/target/jetty-util-9.4.8-SNAPSHOT.jar:" + benchdir
						+ "jetty/jetty-jaas/target/jetty-jaas-9.4.8-SNAPSHOT.jar:" + benchdir
						+ "jetty/jetty-http/target/jetty-http-9.4.8-SNAPSHOT.jar");
		jarMap.put("jodatime_scope.txt", benchdir + "joda-time/joda-time.jar");
		jarMap.put("jpf_scope.txt", benchdir + "jpf-core/build/jpf.jar");
		jarMap.put("mybatis_scope.txt", benchdir + "mybatis-3/mybatis-3.jar");
		jarMap.put("okhttp_scope.txt",
				benchdir + "okhttp/okhttp.jar:" + benchdir + "okhttp/okcurl.jar:" + benchdir + "okhttp/benchmarks.jar:"
						+ benchdir + "okhttp/mockwebserver.jar:" + benchdir + "okhttp/okhttp-tests.jar:" + benchdir
						+ "okhttp/okhttp-urlconnection.jar:" + benchdir + "okhttp/static-server.jar");
		jarMap.put("upm_scope.txt", benchdir + "upm-swing/upm.jar");
		jarMap.put("susi_scope.txt", benchdir + "susi_server.jar");
	}
}