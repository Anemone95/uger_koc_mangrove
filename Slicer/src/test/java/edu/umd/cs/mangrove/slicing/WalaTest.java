package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;
import com.ibm.wala.util.strings.Atom;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.mhpoptimization.CSDGPreprocessor;
import edu.kit.joana.wala.core.SDGBuilder;
import edu.kit.joana.wala.core.SDGBuilder.SDGBuilderConfig;
import edu.umd.cs.mangrove.slicing.JoanaLineSlicer.Line;

public class WalaTest {

	public static CHACallGraph doSlice(String appJar)
			throws WalaException, IOException, IllegalArgumentException, CancelException, UnsoundGraphException {
		String scopeFile = "data/scopeFiles/h2data.txt";
		String fileName = "Lorg/h2/dev/ftp/server/FtpServer";
		File exclusionsFile = new File("data/Java60RegressionExclusions.txt");
		AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFile, exclusionsFile,
				CallGraphTestUtil.class.getClassLoader());
		//AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, exclusionsFile);

		ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		//debugPrint(cha);

		System.out.println("AllApplicationEntrypoints");
		AllApplicationEntrypoints applicationEntrypoints = new AllApplicationEntrypoints(scope, cha);
		ArrayList<Entrypoint> toRemove = new ArrayList<>();
		for (Entrypoint entrypoint : applicationEntrypoints) {
			if (!entrypoint.getMethod().getDeclaringClass().toString().contains("h2")) {
				toRemove.add(entrypoint);
			}
		}
		applicationEntrypoints.removeAll(toRemove);
		//applicationEntrypoints.clear();

		System.out.println("CHACallGraph");
		//CHACallGraph chaCallGraph = new CHACallGraph(cha);
		//chaCallGraph.init(applicationEntrypoints);

		//computeEntryPoints(fileName, "startTask", "(Ljava/lang/String;)V", scope, cha);
		Iterable<Entrypoint> entrypoints = computeEntryPoints("Lorg/h2/dev/ftp/server/FtpControl", "processConnected",
				"(Ljava/lang/String;Ljava/lang/String;)V", scope, cha);
		//IMethod method = entrypoints.iterator().next().getMethod();
		//System.out.println(method);
		System.out.println("AnalysisOptions");
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);

		CallGraphBuilder<InstanceKey> cgb = Util.makeZeroCFABuilder(options, new AnalysisCacheImpl(), cha, scope, null,
				null);
		System.out.println("CallGraph");
		CallGraph cg = cgb.makeCallGraph(options, null);
		for (CGNode cgNode : cg) {
			if (cgNode.getMethod().getDeclaringClass().getClassLoader().toString().equals("Application")) {
				System.err.println(cgNode);
			}
		}
		PointerAnalysis<InstanceKey> pa = cgb.getPointerAnalysis();

		System.out.println("findMethod");
		CGNode mainMethod = findMethod(cg, fileName, "startTask", "(Ljava/lang/String;)V", scope);

		Statement statement = findCallTo(mainMethod, "exec");

		SDGBuilderConfig conf = JoanaSDGHelper.getSDGBuilder(scope);
		final SDG sdg = SDGBuilder.build(conf);
		CSDGPreprocessor.preprocessSDG(sdg);
		JoanaLineSlicer slicer = new JoanaLineSlicer(sdg);
		Line line = null;//new Line(filename, line);
		slicer.slice(line);
		Collection<Statement> slice;

		// context-sensitive traditional slice
		System.out.println("Slicer");
		slice = Slicer.computeBackwardSlice(statement, cg, pa);
		dumpSlice(slice);

		return null;
	}

	private static Iterable<Entrypoint> computeEntryPoints(final String fileName, final String funcName,
			final String desc, final AnalysisScope scope, final ClassHierarchy cha) {
		return new Iterable<Entrypoint>() {
			public Iterator<Entrypoint> iterator() {
				final Atom mainMethod = Atom.findOrCreateAsciiAtom(funcName);
				return new Iterator<Entrypoint>() {
					private int index = 0;

					public boolean hasNext() {
						return index < 1;
					}

					public Entrypoint next() {
						index++;
						TypeReference T = TypeReference.findOrCreate(scope.getApplicationLoader(),
								TypeName.string2TypeName(fileName));
						MethodReference mainRef = MethodReference.findOrCreate(T, mainMethod,
								Descriptor.findOrCreateUTF8(desc));

						return new DefaultEntrypoint(mainRef, cha);
					}
				};
			}
		};
	}

	public static CGNode findMethod(CallGraph cg, String fileName, String methodName, String desc,
			final AnalysisScope scope) {
		Descriptor d = Descriptor.findOrCreateUTF8(desc);
		Atom name = Atom.findOrCreateUnicodeAtom(methodName);
		//TypeReference T = TypeReference.findOrCreate(scope.getApplicationLoader(), TypeName.string2TypeName(fileName));
		//final Atom method = Atom.findOrCreateAsciiAtom(methodName);
		//MethodReference mainRef = MethodReference.findOrCreate(T, method, Descriptor.findOrCreateUTF8(desc));
		for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();) {
			CGNode n = it.next();
			IMethod m = n.getMethod();
			if (m.getDeclaringClass().getClassLoader().toString().equals("Application")) {
				if (m.getName().equals(name) && m.getDescriptor().equals(d)) { return n; }
			}
		}
		Assertions.UNREACHABLE("failed to find method:" + fileName + "." + methodName + desc);
		return null;
	}

	public static Statement findCallTo(CGNode n, String methodName) {
		IR ir = n.getIR();
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof com.ibm.wala.ssa.SSAAbstractInvokeInstruction) {
				com.ibm.wala.ssa.SSAAbstractInvokeInstruction call = (com.ibm.wala.ssa.SSAAbstractInvokeInstruction) s;
				if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
					com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1,
							"expected 1 but got " + indices.size());
					return new com.ibm.wala.ipa.slicer.NormalStatement(n, indices.intIterator().next());
				}
			}
		}
		Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
		return null;
	}

	public static void dumpSlice(Collection<Statement> slice) {
		for (Statement s : slice) {
			System.err.println(s);
		}
	}

	public static void main(String[] args) throws UnsoundGraphException {
		String appJar = "findsecbugs/jars/h2-all.jar";
		try {
			//CallGraphBuilder cgb = doSlicing(appJar);
			CHACallGraph chaCallGraph = doSlice(appJar);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WalaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CancelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
