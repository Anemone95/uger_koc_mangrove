package edu.umd.cs.mangrove.slicing;

import java.io.IOException;
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
import edu.umd.cs.mangrove.util.DirectoryUtil;
import edu.umd.cs.mangrove.util.SDGUtils;

public class JoanaSlicingTest {

	private static final String JAR_FILE = DirectoryUtil.BENCH_DIR + "/owasp-1.1/target/benchmark.jar";
	private static final String MAIN_CLASS = "Lorg/owasp/benchmark/testcode/BenchmarkTest06570";
	private static final String ENTRY_METHOD = "doPost(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V";
	private static final String EXCLUSIONS_FILE = DirectoryUtil.PROJECT_DIR + "/scopeFiles/OwaspSpecificExclusions.txt";
	private static final int lineNumber = 71;

	public static void main(final String[] args)
			throws IOException, ClassHierarchyException, UnsoundGraphException, CancelException {
		//GraphViewer.main(null);
		HashMap<String, ClassLoaderReference> libs = new HashMap<String, ClassLoaderReference>();
		String servletApiJar = DirectoryUtil.PROJECT_DIR + "/lib/servlet-api.jar";
		libs.put(servletApiJar, ClassLoaderReference.Primordial);

		SDGBuilderConfig buildSDG = JoanaSDGHelper.getSDGBuilder(JAR_FILE, libs, EXCLUSIONS_FILE);
		buildSDG.entry = JoanaSDGHelper.findMethod(buildSDG, MAIN_CLASS, ENTRY_METHOD);
		final SDG sdg = SDGBuilder.build(buildSDG);
		CSDGPreprocessor.preprocessSDG(sdg);
		lineSlicerTest(sdg);
	}

	public static void lineSlicerTest(SDG sdg) throws IOException {
		String fileName = MAIN_CLASS.substring(1) + ".java";
		JoanaLineSlicer lineSlicer = new JoanaLineSlicer(sdg, "Summary", true);
		HashSet<SDGNode> nodesAtLine = lineSlicer.getNodesAtLine(new Line(fileName, lineNumber));
		Collection<SDGNode> slice = lineSlicer.slice(nodesAtLine);
		System.out.println(SDGUtils.prepareSlicePruned(sdg, slice, lineSlicer.toAbstract));
	}
}