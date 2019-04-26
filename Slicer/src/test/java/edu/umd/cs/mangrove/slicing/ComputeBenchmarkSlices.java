package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.mhpoptimization.CSDGPreprocessor;
import edu.kit.joana.wala.core.SDGBuilder;
import edu.kit.joana.wala.core.SDGBuilder.SDGBuilderConfig;
import edu.umd.cs.mangrove.slicing.JoanaLineSlicer.Line;
import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;

public class ComputeBenchmarkSlices {

	private static final String HOME_DIR = "/Users/ukoc/";
	private static final String BENCH_DIR = HOME_DIR + "workspace-bench/";
	private static final String exclusionsFile = "data/scopeFiles/H2SpecificExclusions.txt";

	private static String SLICER = "Summary";

	public static void main(final String[] args) throws IOException, ClassHierarchyException, UnsoundGraphException,
			CancelException, ParserConfigurationException, SAXException {
		String jarFile = BENCH_DIR + "jackrabbit/jackrabbit.jar";
		//h2db/h2/bin/h2-1.4.196.jar
		//BENCH_DIR + "CoreNLP/target/stanford-corenlp-3.7.0.jar";
		//BENCH_DIR + "mybatis-3/mybatis-3.jar";
		//BENCH_DIR + "jackrabbit/jackrabbit.jar";

		String callHierarchyFile = "callers/LockManagerImpl.reapplyLock.239.FP.txt";
		//SieveCoreferenceSystem.runConllEval.1074.txt
		//BuildBase.exec.txt";
		//CoreScorer.getEvalSummary.23.txt;
		//CoNLL2011DocumentReader.CoNLL2011DocumentReader.93.txt
		//CoNLLDocumentReader.CoNLLDocumentReader.141.txt
		//IOUtils.getBZip2PipedInputStream.1578.txt
		//SieveCoreferenceSystem.runConllEval.1074.txt
		//SieveCoreferenceSystem.getConllEvalSummary.1084.txt
		//PooledDataSource.pingConnection.527.FP.txt
		//FtpServer.startTask.455.txt
		//LockManagerImpl.reapplyLock.239.FP.txt

		String sourceFile = BENCH_DIR
				+ "jackrabbit/jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockManagerImpl.java";
		//h2db/h2/src/tools/org/h2/build/BuildBase.java
		//h2db/h2/src/tools/org/h2/dev/ftp/server/FtpServer.java
		//CoreNLP/src/edu/stanford/nlp/coref/CorefScorer.java
		//CoreNLP/src/edu/stanford/nlp/dcoref/CoNLLDocumentReader.java
		//CoreNLP/src/edu/stanford/nlp/dcoref/CoNLL2011DocumentReader.java
		//CoreNLP/src/edu/stanford/nlp/io/IOUtils.java
		//CoreNLP/src/edu/stanford/nlp/dcoref/SieveCoreferenceSystem.java
		//mybatis-3/src/main/java/org/apache/ibatis/datasource/pooled/PooledDataSource.java
		//jackrabbit/jackrabbit-core/src/main/java/org/apache/jackrabbit/core/lock/LockManagerImpl.java

		if (args.length > 1) {
			jarFile = args[0];
			callHierarchyFile = args[1];
			sourceFile = args[2];
			if (args.length == 4) SLICER = args[3];
		}
		System.out.println(jarFile + ":" + sourceFile + ":" + callHierarchyFile);
		computeSlicesFromCallHierarchy(jarFile, callHierarchyFile, sourceFile);
	}

	public static String computeSlice(String jarFile, String sourceFile, String entryClass, String entryMethod,
			Set<Line> mylines, HashMap<String, ClassLoaderReference> libsMap)
			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {

		SDGBuilderConfig conf = JoanaSDGHelper.getSDGBuilder(jarFile, libsMap, exclusionsFile);
		conf.entry = JoanaSDGHelper.findMethod(conf, entryClass, entryMethod);// "L" TODO
		System.out.println(conf.entry);
		final SDG sdg = SDGBuilder.build(conf);
		CSDGPreprocessor.preprocessSDG(sdg);
		JoanaLineSlicer joanaLineSlicer = new JoanaLineSlicer(sdg, SLICER, true);
		HashSet<SDGNode> theNodesAtLine = null;
		for (Line line : mylines) {
			theNodesAtLine = joanaLineSlicer.getNodesAtLine(line);
			break;
		}
		Collection<SDGNode> slice = joanaLineSlicer.slice(theNodesAtLine);
		for (SDGNode sdgNode : slice) {
			System.out.print("\n" + sdgNode + " :: ");
			Set<SDGEdge> outgoingEdgesOf = sdg.outgoingEdgesOf(sdgNode);
			for (SDGEdge sdgEdge : outgoingEdgesOf) {
				if (slice.contains(sdgEdge.getTarget())) {
					String string = new String("<" + sdgEdge.getKind() + ", "
							+ (sdgEdge.getLabel() == null ? "" : sdgEdge.getLabel() + ", ") + sdgEdge.getTarget() + ">");
					System.out.print(string + " ");
				}
			}
		}
		System.err.println();
		for (SDGNode n : slice) {
			String nodeStr = n.getType() == null ? n.getLabel() + " " : n.getType() + " " + n.getLabel() + " ";
			System.out.println(n + ":" + nodeStr);
		}

		HashMap<String, String> variableNameMap = null;
		if (!(new File(sourceFile).exists())) {
			System.out.println("Could not find source file: " + sourceFile);
		} else {
			variableNameMap = joanaLineSlicer.computeVariableNameMap(sourceFile, slice);
			System.out.println(variableNameMap);
		}

		String sliceStr = postProcessSlice(slice, variableNameMap);
		return sliceStr;
	}

	private static String computeSlice(String jarFile, String warningClass, String sourceFile, String entryClass,
			String entryMethod, int lineNumber, HashMap<String, ClassLoaderReference> libsMap)
			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {
		Set<Line> lines = new HashSet<Line>();
		lines.add(new Line(warningClass + ".java", lineNumber));
		return computeSlice(jarFile, sourceFile, entryClass, entryMethod, lines, libsMap);
	}

	public static void computeSliceBulk(String bench, String jarFile, String outFile, String sourceBase,
			String warningFile, HashMap<String, ClassLoaderReference> libsMap) throws IOException,
			ParserConfigurationException, SAXException, ClassHierarchyException, UnsoundGraphException, CancelException {
		String classPath, sourceFile, entryMethod;
		SDGBuilderConfig conf = JoanaSDGHelper.getSDGBuilder(jarFile, libsMap, exclusionsFile);
		HashMap<String, BugReport> parseXML2Map = BugReportUtil.parseXML2Map(new File(warningFile));
		Set<Entry<String, BugReport>> entrySet = parseXML2Map.entrySet();
		for (Entry<String, BugReport> entry : entrySet) {
			BugReport report = entry.getValue();
			if (report.getClassName().startsWith("javax.") || report.getClassName().startsWith("java.lang")
					|| report.getClassName().startsWith("org.apache.tools") || report.getClassName().startsWith("org.gradle")
					|| report.getClassName().startsWith("org.h2.test") || report.getClassName().startsWith("org.h2.samples")
					|| report.getClassName().startsWith("org.hsqldb.test")) {
				continue;
			}
			classPath = report.getClassName().replaceAll("\\.", "/");
			sourceFile = sourceBase + classPath + ".java";
			if (!(new File(sourceFile).exists())) {
				sourceFile = findFile(BENCH_DIR + bench, classPath + ".java");
			}
			entryMethod = report.getMethodName() + report.getMethodSignature();
			String runCmd = "java -jar -Xmx8g mangrove.jar '" + jarFile + "' '" + classPath + "' '" + sourceFile + "' '"
					+ entryMethod + "' '" + report.getLineNumber() + "' '" + libsMap.keySet().toString() + "'";
			System.out.println(runCmd.replaceAll(", ", "' '"));
			if (report.getClassName() != null) {
				continue;
			}
			try {
				conf.entry = JoanaSDGHelper.findMethod(conf, "L" + classPath, entryMethod);
				final SDG sdg = SDGBuilder.build(conf);
				CSDGPreprocessor.preprocessSDG(sdg);
				JoanaLineSlicer joanaLineSlicer = new JoanaLineSlicer(sdg, SLICER, true);
				Collection<SDGNode> slice = joanaLineSlicer.slice(new Line(classPath + ".java", report.getLineNumber()));
				HashMap<String, String> variableNameMap = null;
				if (sourceFile == null) {
					System.out.println("Could not find source file: " + sourceFile);
				} else {
					variableNameMap = joanaLineSlicer.computeVariableNameMap(sourceFile, slice);
					System.out.println(variableNameMap);
				}

				String sliceStr = postProcessSlice(slice, variableNameMap);
				System.out.println("\n" + sliceStr);
			} catch (Exception e) {
				System.out.println("FAILED:" + e);
			}
		}
	}

	public static void computeSlicesFromCallHierarchy(String jarFile, String callHierarchyFile, String sourceFile)
			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {
		String entryClass = null, entryMethod = null;
		int currentDepth, lineNumber, lastDepth = 0;
		HashMap<String, ClassLoaderReference> libsMap = new HashMap<String, ClassLoaderReference>();
		libsMap.put("lib/servlet-api.jar", ClassLoaderReference.Primordial);

		ArrayList<Line> mylines = new ArrayList<Line>();
		List<String> readAllLines = Files.readAllLines(Paths.get(callHierarchyFile));
		String[] split = readAllLines.remove(0).split(":");
		entryClass = split[1];
		lineNumber = Integer.parseInt(split[2].trim());
		mylines.add(new Line(entryClass.substring(1) + ".java", lineNumber));
		for (String caller : readAllLines) {
			currentDepth = caller.lastIndexOf("|") + 1;
			if (currentDepth <= lastDepth) {
				System.out.println("\n" + mylines);
				String slice = null;
				try {
					slice = computeSlice(jarFile, sourceFile, entryClass, entryMethod, new HashSet<>(mylines), libsMap);
					//break;
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				System.out.println("\n" + slice);
				//				List<Line> subList = mylines.subList(currentDepth, lastDepth - 1);
				//				mylines.removeAll(subList);
			}
			if (caller.equals("END")) {
				break;
			}
			String[] pair = caller.split(":");
			entryMethod = pair[0].substring(currentDepth);
			entryClass = pair[1].replace(".", "/");
			lineNumber = Integer.parseInt(pair[2].trim());
			//mylines.add(new Line(entryClass.substring(1) + ".java", lineNumber));
			lastDepth = currentDepth;
		}
	}

	public static String findFile(String dir, String file) throws IOException {
		String srcFile = file.replaceAll("\\$.*\\.java", ".java");
		Optional<Path> findFirst = Files.walk(Paths.get(dir)).filter(n -> n.toString().endsWith(srcFile)).findFirst();
		if (findFirst.isPresent()) { return findFirst.get().toString(); }
		return null;
	}

	public static String postProcessSlice(Collection<SDGNode> slice, HashMap<String, String> varMap) {
		String rslt = "";
		for (SDGNode n : slice) {
			rslt += n.getType() == null ? n.getLabel() + " " : n.getType() + " " + n.getLabel() + " ";
			//System.out.println(nodeStr);
		}
		if (varMap != null) {
			for (Entry<String, String> entry : varMap.entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				rslt = rslt.replace(k + " ", v + " ").replace(k + ".", v + ".").replace(k + ",", v + ",")
						.replace(k + ")", v + ")").replace(k + "(", v + "(").replace(k + "]", v + "]")
						.replace(k + "[", v + "[");
			}
		}
		return rslt;
	}

	public static void run(final String[] args) throws IOException, ClassHierarchyException, UnsoundGraphException,
			CancelException, ParserConfigurationException, SAXException {
		String jarFile, classPath, sourceFile, entryClass, entryMethod;
		int lineNumber;
		HashMap<String, ClassLoaderReference> libsMap = new HashMap<String, ClassLoaderReference>();
		libsMap.put("lib/servlet-api.jar", ClassLoaderReference.Primordial);
		jarFile = args[0];
		classPath = args[1];
		sourceFile = args[2];
		entryClass = args[3];
		entryMethod = args[5];
		lineNumber = Integer.parseInt(args[5]);
		for (int i = 6; i < args.length; i++) {
			libsMap.put(args[i], ClassLoaderReference.Application);
		}
		String sliceStr = computeSlice(jarFile, classPath, sourceFile, entryClass, entryMethod, lineNumber, libsMap);
		System.out.println("\n" + sliceStr);
	}

	public static void runForAll(int option) throws IOException, ClassHierarchyException, UnsoundGraphException,
			CancelException, ParserConfigurationException, SAXException {
		String benchSrc = null, jarFile = null, outFile = null, sourceBase = null, wFileNameXML = null;
		HashMap<String, ClassLoaderReference> libsMap = new HashMap<String, ClassLoaderReference>();
		for (int i = 0; i < 10; i++) {
			//libsMap.put("lib/servlet-api.jar", ClassLoaderReference.Primordial);
			switch (i) {
			case 0:
				benchSrc = "h2db/h2/src";
				outFile = "data/java/H2SlicesAll.txt";
				jarFile = "findsecbugs/jars/h2-all.jar";
				sourceBase = BENCH_DIR + "h2db/h2/src/main/";
				wFileNameXML = "findsecbugs/h2-all.xml";
				break;
			case 1:
				benchSrc = "hsqldb/src";
				outFile = "data/java/HSQLDBSlices.txt";
				jarFile = BENCH_DIR + "hsqldb/lib/hsqldb.jar";
				sourceBase = BENCH_DIR + "hsqldb/src/";
				wFileNameXML = "findsecbugs/hsqldb.xml";
				libsMap.put(BENCH_DIR + "hsqldb/lib/sqltool.jar", ClassLoaderReference.Application);
				libsMap.put(BENCH_DIR + "hsqldb/lib/hsqldbutil.jar", ClassLoaderReference.Application);
				break;
			case 2:
				benchSrc = "mybatis-3/src";
				sourceBase = BENCH_DIR + "mybatis-3/src/main/java/";
				outFile = "data/java/MybatisSlices.txt";
				jarFile = BENCH_DIR + "mybatis-3/target/mybatis-3.4.5.jar";
				wFileNameXML = "findsecbugs/mybatis.xml";
				break;
			case 3:
				benchSrc = "biojava/biojava-structure/src";
				sourceBase = BENCH_DIR + "biojava/biojava-structure/src/main/java/";
				outFile = "data/java/BiojavaSlices.txt";
				jarFile = BENCH_DIR + "biojava/biojava-structure/target/biojava-structure-4.2.8.jar";
				wFileNameXML = "findsecbugs/biojava.xml";
				break;
			case 4:
				benchSrc = "CoreNLP/src";
				sourceBase = BENCH_DIR + "CoreNLP/src/";
				outFile = "data/java/CoreNLPSlices.txt";
				jarFile = BENCH_DIR + "CoreNLP/target/stanford-corenlp-3.7.0.jar";
				wFileNameXML = "findsecbugs/CoreNLP.xml";
				break;
			case 5:
				benchSrc = "jackrabbit";
				sourceBase = BENCH_DIR + "jackrabbit/jackrabbit-core/src/main/java/";
				outFile = "data/java/JackrabbitSlices.txt";
				jarFile = BENCH_DIR + "jackrabbit/jackrabbit-core/target/jackrabbit-core-2.15.6.jar";
				wFileNameXML = "findsecbugs/jackrabbit.xml";
				libsMap.put(BENCH_DIR + "jackrabbit/jackrabbit-webdav/target/jackrabbit-webdav-2.15.6.jar",
						ClassLoaderReference.Application);
				libsMap.put(BENCH_DIR + "jackrabbit/jackrabbit-webapp/target/jackrabbit-webapp-2.15.6.jar",
						ClassLoaderReference.Application);

				break;
			case 6:
				benchSrc = "jetty/";
				String mainJar = "jetty/jetty-server/target/jetty-server-9.4.8-SNAPSHOT.jar";
				sourceBase = BENCH_DIR + "jetty/jetty-server/src/main/java/";
				outFile = "data/java/JettySlices.txt";
				jarFile = BENCH_DIR + mainJar;
				wFileNameXML = "findsecbugs/jetty.xml";
				libsMap.put(BENCH_DIR + mainJar.replace("server", "jaas"), ClassLoaderReference.Application);
				libsMap.put(BENCH_DIR + mainJar.replace("server", "security"), ClassLoaderReference.Application);
				libsMap.put(BENCH_DIR + mainJar.replace("server", "plus"), ClassLoaderReference.Application);
				libsMap.put(BENCH_DIR + mainJar.replace("server", "http"), ClassLoaderReference.Application);
				break;
			case 7:
				benchSrc = "jenkins";
				sourceBase = BENCH_DIR + "jenkins/src/main/java/";
				outFile = "data/java/JenkinsSlices.txt";
				jarFile = BENCH_DIR + "jenkins/core/target/jenkins-core-2.17-SNAPSHOT.jar";
				wFileNameXML = "findsecbugs/jenkins.xml";
				break;
			case 8:
				benchSrc = "accumulo/core/src";
				sourceBase = BENCH_DIR + "accumulo/core/src/main/java/";
				outFile = "data/java/AccumuloSlices.txt";
				jarFile = BENCH_DIR + "accumulo/core/target/accumulo-core-1.8.1.jar";
				wFileNameXML = "findsecbugs/accumulo.xml";
				break;
			case 9:
				benchSrc = "hadoop/hadoop-common-project/hadoop-common/src";
				sourceBase = BENCH_DIR + "hadoop/hadoop-common-project/hadoop-common/src/main/java/";
				outFile = "data/java/HadoopSlices.txt";
				jarFile = "findsecbugs/jars/hadoop.jar";
				wFileNameXML = "findsecbugs/hadoop.xml";
				break;
			default:
				break;
			}
			computeSliceBulk(benchSrc, jarFile, outFile, sourceBase, wFileNameXML, libsMap);
			libsMap.clear();
		}
	}
}