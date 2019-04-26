package edu.umd.cs.mangrove.slicing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
import edu.umd.cs.mangrove.util.PrintUtil;
import edu.umd.cs.mangrove.util.SDGUtils;

public class ComputeOwaspSlices {

	private static final String WORKSPACE = "/Users/ukoc/Documents/workspace/";
	private static final String PROJECT_BASE = WORKSPACE + "mangrove/mangrove/";
	private static final String OUT_DIR = PROJECT_BASE + "data/slices/owasp/";
	//private static final String SOURCE_BASE = "/Users/ukoc/workspace-bench/owasp-1.1/src/main/java/";
	private static final String JAR_FILE = "/Users/ukoc/workspace-bench/owasp-1.1/target/benchmark.jar";
	private static final String ENTRY_METHOD = "doPost(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V";
	private static final String slicer = "Summary";
	private static final String classFileDir = "org/owasp/benchmark/testcode/";
	private static final String classPath = "L" + classFileDir;

	public static void main(final String[] args)
			throws IOException, ClassHierarchyException, UnsoundGraphException, CancelException {
		String outFile = null;
		String exclusionsFile = PROJECT_BASE + "data/scopeFiles/OwaspSpecificExclusions.txt";

		HashMap<String, ClassLoaderReference> libs = new HashMap<String, ClassLoaderReference>();
		libs.put(PROJECT_BASE + "lib/servlet-api.jar", ClassLoaderReference.Primordial);

		String wFileName = PROJECT_BASE + "data/findsecbugs/reports/CWE89OwaspStatsAll.txt";
		ArrayList<String> warnings = (ArrayList<String>) Files.readAllLines(Paths.get(wFileName));
		for (String w : warnings) {
			System.out.println("\n" + w);
			String[] fields = w.split(",");
			SDGBuilderConfig buildSDG = JoanaSDGHelper.getSDGBuilder(JAR_FILE, libs, exclusionsFile);
			buildSDG.entry = JoanaSDGHelper.findMethod(buildSDG, classPath + fields[0], ENTRY_METHOD);
			final SDG sdg = SDGBuilder.build(buildSDG);
			CSDGPreprocessor.preprocessSDG(sdg);
			outFile = OUT_DIR + fields[0] + "_" + fields[5] + ".txt";
			String slice = computeSlice(fields[0] + ".java", classFileDir, Integer.parseInt(fields[1]), sdg);
			//System.out.println(slice);
			PrintUtil.printToFile(outFile, slice, false);
			//break;
		}
	}

	public static String computeSlice(String classFile, String classFileDir, int lineNumber, final SDG sdg)
			throws IOException {
		String javaFile = classFileDir + classFile;
		JoanaLineSlicer joanaLineSlicer = new JoanaLineSlicer(sdg, slicer, true);
		Line line = new Line(javaFile, lineNumber);
		HashSet<SDGNode> errorNodes = joanaLineSlicer.getNodesAtLine(line);
		Collection<SDGNode> slice = joanaLineSlicer.slice(errorNodes);
		//		HashMap<String, String> variableNameMap = joanaLineSlicer
		//				.computeVariableNameMap(SOURCE_BASE + classFileDir + classFile, slice);
		String errorNodesStr = "[";
		for (SDGNode sdgNode : errorNodes) {
			if (!joanaLineSlicer.toAbstract.contains(sdgNode)) {
				errorNodesStr += sdgNode.getId() + ", ";
			}
		}
		String sliceStr = errorNodesStr.substring(0, errorNodesStr.lastIndexOf(",")) + "]\n"
				+ SDGUtils.prepareSliceForEncoding(sdg, slice, joanaLineSlicer.toAbstract);

		//Abstracted(sdg, slice, errorNodes, joanaLineSlicer.toAbstract);
		//		Set<Entry<String, String>> entrySet = variableNameMap.entrySet();
		//		for (Entry<String, String> entry : entrySet) {
		//			String key = entry.getKey();
		//			String value = entry.getValue();
		//			sliceStr = sliceStr.replace(key + " ", value + " ");
		//			sliceStr = sliceStr.replace(key + ".", value + ".");
		//			sliceStr = sliceStr.replace(key + ",", value + ",");
		//			sliceStr = sliceStr.replace(key + ")", value + ")");
		//			sliceStr = sliceStr.replace(key + "(", value + "(");
		//			sliceStr = sliceStr.replace(key + "]", value + "]");
		//			sliceStr = sliceStr.replace(key + "[", value + "[");
		//		}
		return sliceStr;
	}
}