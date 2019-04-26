package edu.umd.cs.mangrove.slicing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.ibm.wala.cfg.exc.intra.MethodState;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarStreamModule;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.pruned.ApplicationLoaderPolicy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.CFG;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.VirtualNode;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.building.ICFGBuilder;
import edu.kit.joana.ifc.sdg.irlsod.RegionClusterBasedCDomOracle;
import edu.kit.joana.ifc.sdg.util.BytecodeLocation;
import edu.kit.joana.wala.core.ExternalCallCheck;
import edu.kit.joana.wala.core.Main;
import edu.kit.joana.wala.core.SDGBuilder;
import edu.kit.joana.wala.core.SDGBuilder.DynamicDispatchHandling;
import edu.kit.joana.wala.core.SDGBuilder.ExceptionAnalysis;
import edu.kit.joana.wala.core.SDGBuilder.FieldPropagation;
import edu.kit.joana.wala.core.SDGBuilder.PointsToPrecision;
import edu.kit.joana.wala.core.SDGBuilder.StaticInitializationTreatment;
import edu.umd.cs.mangrove.util.DirectoryUtil;

public class JoanaSDGHelper {

	private static final String exclusionsFile = DirectoryUtil.PROJECT_DIR
			+ "/scopeFiles/Java60RegressionExclusions.txt";

	static IMethod findMethod(SDGBuilder.SDGBuilderConfig scfg, final String mainClass, final String entryMethod) {
		//debugPrint(scfg.cha);
		final IClass cl = scfg.cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, mainClass));
		if (cl == null) { throw new RuntimeException("Class not found: " + mainClass); }
		final IMethod m = cl.getMethod(Selector.make(entryMethod));
		if (m == null) { throw new RuntimeException("Method not found:" + cl + "." + entryMethod); }
		return m;
	}

	private static AnalysisScope makeMinimalScope(final String jarFile, HashMap<String, ClassLoaderReference> libs,
			final String specificExclusions) throws IOException {
		final AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
		String[] appJars = jarFile.split(":");
		for (String appjar : appJars) {
			scope.addToScope(ClassLoaderReference.Application, new JarStreamModule(new FileInputStream(appjar)));//App jar			
		}
		JarStreamModule jreStubJar = new JarStreamModule(new FileInputStream("lib/stubs/jSDG-stubs-jre1.5.jar"));
		scope.addToScope(ClassLoaderReference.Primordial, jreStubJar);
		//JarStreamModule rtJar = new JarStreamModule(new FileInputStream("stubs/rt.jar"));
		//scope.addToScope(ClassLoaderReference.Primordial, rtJar);
		//		JarStreamModule classesStubJar = new JarStreamModule(new FileInputStream("stubs/classes.jar"));
		//		scope.addToScope(ClassLoaderReference.Primordial, classesStubJar);
		Set<Entry<String, ClassLoaderReference>> entrySet = libs.entrySet();
		for (Entry<String, ClassLoaderReference> entry : entrySet) {
			scope.addToScope(entry.getValue(), new JarStreamModule(new FileInputStream(entry.getKey())));
		}
		scope.setExclusions(new FileOfClasses(new FileInputStream(exclusionsFile)));
		if (specificExclusions != null) {
			List<String> exclusions = Files.readAllLines(Paths.get(specificExclusions));
			for (String exc : exclusions) {
				scope.getExclusions().add(exc);
			}
		}
		return scope;
	}

	public static SDGBuilder.SDGBuilderConfig getSDGBuilder(final String jarFile,
			HashMap<String, ClassLoaderReference> libsMap, final String exclusionsFile)
			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {
		SDGBuilder.SDGBuilderConfig scfg = new SDGBuilder.SDGBuilderConfig();
		scfg.out = System.out;
		scfg.scope = makeMinimalScope(jarFile, libsMap, exclusionsFile);
		scfg.cache = new AnalysisCacheImpl();
		scfg.cha = ClassHierarchyFactory.make(scfg.scope);
		scfg.ext = ExternalCallCheck.EMPTY;
		scfg.immutableNoOut = Main.IMMUTABLE_NO_OUT;
		scfg.immutableStubs = Main.IMMUTABLE_STUBS;
		scfg.ignoreStaticFields = Main.IGNORE_STATIC_FIELDS;
		scfg.exceptions = ExceptionAnalysis.IGNORE_ALL;
		scfg.pruneDDEdgesToDanglingExceptionNodes = true;
		scfg.defaultExceptionMethodState = MethodState.DEFAULT;
		scfg.accessPath = false;
		scfg.sideEffects = null;
		scfg.prunecg = 0;
		scfg.pruningPolicy = ApplicationLoaderPolicy.INSTANCE;
		scfg.pts = PointsToPrecision.INSTANCE_BASED;
		scfg.customCGBFactory = null;
		scfg.staticInitializers = StaticInitializationTreatment.SIMPLE;
		scfg.fieldPropagation = FieldPropagation.OBJ_GRAPH;
		scfg.computeInterference = false;
		scfg.computeAllocationSites = false;
		scfg.computeSummary = false;
		scfg.cgConsumer = null;
		scfg.additionalContextSelector = null;
		scfg.dynDisp = DynamicDispatchHandling.IGNORE;
		scfg.debugCallGraphDotOutput = false;
		scfg.debugManyGraphsDotOutput = false;
		scfg.debugAccessPath = false;
		scfg.debugStaticInitializers = false;
		return scfg;
	}

	public static SDGBuilder.SDGBuilderConfig getSDGBuilder(final AnalysisScope scope)
			throws ClassHierarchyException, IOException, UnsoundGraphException, CancelException {
		SDGBuilder.SDGBuilderConfig scfg = new SDGBuilder.SDGBuilderConfig();

		scfg.out = System.out;
		scfg.scope = scope;
		scfg.cache = new AnalysisCacheImpl();
		scfg.cha = ClassHierarchyFactory.make(scfg.scope);
		scfg.ext = ExternalCallCheck.EMPTY;
		scfg.immutableNoOut = Main.IMMUTABLE_NO_OUT;
		scfg.immutableStubs = Main.IMMUTABLE_STUBS;
		scfg.ignoreStaticFields = Main.IGNORE_STATIC_FIELDS;
		scfg.exceptions = ExceptionAnalysis.IGNORE_ALL;
		scfg.pruneDDEdgesToDanglingExceptionNodes = true;
		scfg.defaultExceptionMethodState = MethodState.DEFAULT;
		scfg.accessPath = false;
		scfg.sideEffects = null;
		scfg.prunecg = 1;
		scfg.pruningPolicy = ApplicationLoaderPolicy.INSTANCE;
		scfg.pts = PointsToPrecision.INSTANCE_BASED;
		scfg.customCGBFactory = null;
		scfg.staticInitializers = StaticInitializationTreatment.SIMPLE;
		scfg.fieldPropagation = FieldPropagation.OBJ_GRAPH;
		scfg.computeInterference = true;
		scfg.computeAllocationSites = true;
		scfg.cgConsumer = null;
		scfg.additionalContextSelector = null;
		scfg.dynDisp = DynamicDispatchHandling.PRECISE;
		scfg.debugCallGraphDotOutput = false;
		scfg.debugManyGraphsDotOutput = false;
		scfg.debugAccessPath = false;
		scfg.debugStaticInitializers = false;
		return scfg;
	}

	public static DirectedGraph<VirtualNode, SDGEdge> unfoldVirtualCFGFor(final SDG sdg, final int thread) {
		final CFG icfg = ICFGBuilder.extractICFG(sdg);
		final DirectedGraph<VirtualNode, SDGEdge> ret = new DefaultDirectedGraph<VirtualNode, SDGEdge>(SDGEdge.class);
		for (final SDGNode n : icfg.vertexSet()) {
			if (!RegionClusterBasedCDomOracle.possiblyExecutesIn(n, thread)) {
				continue;
			}
			for (final SDGEdge e : icfg.outgoingEdgesOf(n)) {
				if (!RegionClusterBasedCDomOracle.possiblyExecutesIn(e.getTarget(), thread)) {
					continue;
				}
				final VirtualNode vn = new VirtualNode(n, thread);
				final VirtualNode vm = new VirtualNode(e.getTarget(), thread);
				ret.addVertex(vn);
				ret.addVertex(vm);
				ret.addEdge(vn, vm, e);
			}
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private static boolean isCallCallRetEdge(final SDGEdge e) {
		// @formatter:off
		return (e.getKind() == SDGEdge.Kind.CONTROL_FLOW) && (e.getSource().getKind() == SDGNode.Kind.CALL)
				&& BytecodeLocation.isCallRetNode(e.getTarget());
		// @formatter:on
	}

	private static void debugPrint(IClassHierarchy cha) {
		if (true) { // debug
			for (Iterator<IClass> it = cha.iterator(); it.hasNext();) {
				ShrikeClass next = (ShrikeClass) it.next();
				if (!next.getClassLoader().getName().toString().equals("Primordial")) {
					Collection<IMethod> allMethods = next.getAllMethods();
					for (IMethod iMethod : allMethods) {
						if (!iMethod.toString().contains("Primordial")) System.out.println(" " + iMethod);
						for (IField iField : next.getAllFields()) {
							if (!iField.toString().contains("Primordial") && !iField.toString().contains("this$0"))
								System.out.println("\t" + iField.getName());
						}
					}
				}
			}
		}
	}
}