package edu.umd.cs.mangrove.slicing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.HeapStatement;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.ParamCallee;
import com.ibm.wala.ipa.slicer.ParamCaller;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.NodeDecorator;

public class WALASlicer {
	private AnalysisScope cachedScope;
	private IClassHierarchy cachedCHA;
	private DataDependenceOptions ddOpt = DataDependenceOptions.NO_BASE_NO_HEAP_NO_EXCEPTIONS;
	private ControlDependenceOptions cdOpt = ControlDependenceOptions.FULL;
	private final String exclusionFile = "data/scopeFiles/Java60RegressionExclusions.txt";
	private String scopeFile;
	private List<String> specificExclusions;
	private boolean debug = false;

	public WALASlicer(String scopeFile, List<String> exclusion) {
		this.scopeFile = scopeFile;
		this.specificExclusions = exclusion;
	}

	private Iterable<Entrypoint> computeEntryPoints(final String fileName, final String funcName, final String desc,
			final AnalysisScope scope, final ClassHierarchy cha) {
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
						// fileName.endsWith("b")
						// ? fileName.substring(0, fileName.length() - 1) + "a"
						// :
						MethodReference mainRef = MethodReference.findOrCreate(T, mainMethod,
								Descriptor.findOrCreateUTF8(desc));

						return new DefaultEntrypoint(mainRef, cha);
					}
				};
			}
		};
	}

	public Statement findCallTo(CGNode n, String methodName, int lineNumber) throws InvalidClassFileException {
		IR ir = n.getIR();
		ArrayList<NormalStatement> callStmts = new ArrayList<NormalStatement>();
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next();
			if (s instanceof SSAInvokeInstruction) {
				CallSiteReference callSite = ((SSAInvokeInstruction) s).getCallSite();
				if (callSite.getDeclaredTarget().getName().toString().equals(methodName)) {
					IntSet indices = ir.getCallInstructionIndices(callSite);
					Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
					callStmts.add(new NormalStatement(n, indices.intIterator().next()));
				}
			}
		}
		int resultIndx = -1;
		int distance = 999;
		for (int i = 0; i < callStmts.size(); i++) {
			int firstLine = callStmts.get(i).getNode().getMethod()
					.getSourcePosition(callStmts.get(i).getInstructionIndex()).getFirstLine();
			int dist = Math.abs(firstLine - lineNumber);
			if (distance > dist) {
				distance = dist;
				resultIndx = i;
			}
		}
		if (resultIndx == -1) {
			Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
		}
		return callStmts.get(resultIndx);
	}

	private CGNode findMethod(CallGraph cg, String methodSignature, String methodName, String className)
			throws InvalidClassFileException {

		Descriptor d = Descriptor.findOrCreateUTF8(methodSignature);
		Atom name = Atom.findOrCreateUnicodeAtom(methodName);

		for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();) {
			CGNode n = it.next();
			IMethod method = n.getMethod();
			if (method.getDeclaringClass().getClassLoader().toString().equals("Application")
					&& method.getName().equals(name) && method.getDescriptor().equals(d)) {
				return n;
			}
		}
		for (CGNode n : cg) {
			IMethod method = n.getMethod();
			if (method.getDeclaringClass().getClassLoader().toString().equals("Application")
					&& method.getName().equals(name) && method.getDescriptor().equals(d)) {
				String declaringClass = method.getDeclaringClass().getName().toString();
				if (declaringClass.equals(className)) { return n; }
			}
		}
		return null;
	}

	private AnalysisScope findOrCreateAnalysisScope() throws IOException {
		if (cachedScope == null) cachedScope = CallGraphTestUtil.makeJ2SEAnalysisScope(scopeFile, exclusionFile);
		if (specificExclusions != null) {
			for (String exc : specificExclusions) {
				cachedScope.getExclusions().add(exc);
			}
		}
		return cachedScope;
	}

	private ClassHierarchy findOrCreateCHA(AnalysisScope scope) throws ClassHierarchyException {
		if (cachedCHA == null) {
			cachedCHA = ClassHierarchyFactory.make(scope);
		}
		return (ClassHierarchy) cachedCHA;
	}

	public NodeDecorator<Statement> makeNodeDecorator() {
		return new NodeDecorator<Statement>() {
			public String getLabel(Statement s) throws WalaException {
				switch (s.getKind()) {
				case HEAP_PARAM_CALLEE:
				case HEAP_PARAM_CALLER:
				case HEAP_RET_CALLEE:
				case HEAP_RET_CALLER:
					HeapStatement h = (HeapStatement) s;
					return s.getKind() + "\\n" + h.getNode() + "\\n" + h.getLocation();
				case NORMAL:
					NormalStatement n = (NormalStatement) s;
					SSAInstruction instruction = n.getInstruction();

					String lineStr = "";
					try {
						IMethod method = n.getNode().getMethod();
						lineStr += "in method:" + method.getName() + ", at line:"
								+ method.getSourcePosition(n.getInstructionIndex()).getFirstLine();
					} catch (InvalidClassFileException e) {
						// TODO
					}

					lineStr += ", inst:" + instruction;
					return lineStr;
				case PARAM_CALLEE:
					ParamCallee paramCallee = (ParamCallee) s;
					return s.getKind() + " " + paramCallee.getValueNumber() + "\\n" + s.getNode().getMethod().getName();
				case PARAM_CALLER:
					ParamCaller paramCaller = (ParamCaller) s;
					return s.getKind() + " " + paramCaller.getValueNumber() + "\\n" + s.getNode().getMethod().getName()
							+ "\\n" + paramCaller.getInstruction().getCallSite().getDeclaredTarget().getName();
				case EXC_RET_CALLEE:
				case EXC_RET_CALLER:
				case NORMAL_RET_CALLEE:
				case NORMAL_RET_CALLER:
				case PHI:
				default:
					return s.toString();
				}
			}
		};
	}

	public String sliceJuliet(String fileName, String funcName, String desc, String callee, int lineNumber)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		AnalysisScope scope = findOrCreateAnalysisScope();
		ClassHierarchy cha = findOrCreateCHA(scope);
		//debugPrint(cha);
		String entryMthdName = funcName.contains("good") ? "good" : "bad";
		CallGraphBuilder<InstanceKey> builder;
		CallGraph cg;
		try {

			Iterable<Entrypoint> entrypoints = computeEntryPoints(fileName, entryMthdName, "()V", scope, cha);
			AnalysisOptions opts = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroOneCFABuilder(opts, new AnalysisCacheImpl(), cha, scope);
			cg = builder.makeCallGraph(opts, null);
		} catch (com.ibm.wala.util.debug.UnimplementedError e) {
			Iterable<Entrypoint> entrypoints = computeEntryPoints(fileName, entryMthdName,
					"(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", scope, cha);
			AnalysisOptions opts = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
			builder = Util.makeZeroOneCFABuilder(opts, new AnalysisCacheImpl(), cha, scope);
			cg = builder.makeCallGraph(opts, null);
		}
		CGNode mthd = findMethod(cg, desc, funcName, fileName);

		PointerAnalysis<InstanceKey> pA = builder.getPointerAnalysis();
		Collection<Statement> slice = Slicer.computeBackwardSlice(findCallTo(mthd, callee, lineNumber), cg, pA, ddOpt,
				cdOpt);
		return printSDG(cg, pA, slice);

	}

	public String computeCFG(String fileName, String funcName, String desc, String sink, int lineNumber)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		AnalysisScope scope = findOrCreateAnalysisScope();
		ClassHierarchy cha = findOrCreateCHA(scope);
		Iterable<Entrypoint> entrypoints = computeEntryPoints(fileName, funcName, desc, scope, cha);
		AnalysisOptions opts = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
		AnalysisCache cache = new AnalysisCacheImpl();
		debugPrint(cha, cache, opts);
		IMethod iMethod = findIMethod(cha, cache, opts, fileName, funcName, desc);
		IR ir = cache.getSSACache().findOrCreateIR(iMethod, Everywhere.EVERYWHERE, opts.getSSAOptions());
		if (ir == null) {
			PropagationCallGraphBuilder builder = Util.makeZeroOneCFABuilder(opts, cache, cha, scope);
			CGNode mthd = findMethod(builder.makeCallGraph(opts), desc, funcName, fileName);
			iMethod = mthd.getMethod();
			ir = mthd.getIR();
		}
		String targets = "[";
		Iterator<SSAInstruction> iterateNormalInstructions = ir.iterateNormalInstructions();
		while (iterateNormalInstructions.hasNext()) {
			SSAInstruction ssaIns = (SSAInstruction) iterateNormalInstructions.next();
			System.out.println(ssaIns);
			int firstLine = iMethod.getSourcePosition(ssaIns.iindex).getFirstLine();
			System.out.println(firstLine);
			if (ssaIns.toString().contains(sink) && firstLine == lineNumber) {
				BasicBlock blockForInstruction = ir.getControlFlowGraph().getBlockForInstruction(ssaIns.iindex);
				targets += blockForInstruction.getNumber() + ", ";
			}
		}
		targets = targets.substring(0, targets.lastIndexOf(",")) + "]\n";
		return targets;
	}

	public String sliceRW(String fileName, String funcName, String desc, int lineNumber)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		AnalysisScope scope = findOrCreateAnalysisScope();
		ClassHierarchy cha = findOrCreateCHA(scope);
		AllApplicationEntrypoints applicationEntrypoints = new AllApplicationEntrypoints(scope, cha);
		ArrayList<Entrypoint> toRemove = new ArrayList<>();
		for (Entrypoint entrypoint : applicationEntrypoints) {
			if (!entrypoint.getMethod().getDeclaringClass().toString().contains("h2")) {
				toRemove.add(entrypoint);
			}
		}
		applicationEntrypoints.removeAll(toRemove);
		AnalysisOptions opts = CallGraphTestUtil.makeAnalysisOptions(scope, applicationEntrypoints);
		AnalysisCache cache = new AnalysisCacheImpl();
		debugPrint(cha, cache, opts);
		IMethod iMethod = findIMethod(cha, cache, opts, fileName, funcName, desc);

		IR ir = cache.getSSACache().findOrCreateIR(iMethod, Everywhere.EVERYWHERE, opts.getSSAOptions());

		if (ir == null) {
			PropagationCallGraphBuilder builder = Util.makeZeroOneCFABuilder(opts, cache, cha, scope);
			CGNode mthd = findMethod(builder.makeCallGraph(opts), desc, funcName, fileName);
			ir = mthd.getIR();
		}
		return ir.toString();
	}

	public String sliceOwasp(String fileName, String funcName, String desc, String callee, int lineNumber)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		AnalysisScope scope = findOrCreateAnalysisScope();
		ClassHierarchy cha = findOrCreateCHA(scope);
		AnalysisCache cache = new AnalysisCacheImpl();
		Iterable<Entrypoint> entrypoints = computeEntryPoints(fileName, funcName, desc, scope, cha);
		AnalysisOptions opts = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
		debugPrint(cha, cache, opts);
		CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneCFABuilder(opts, cache, cha, scope);
		CallGraph cg = builder.makeCallGraph(opts, null);
		CGNode mthd = findMethod(cg, desc, funcName, fileName);

		PointerAnalysis<InstanceKey> pA = builder.getPointerAnalysis();
		Collection<Statement> slice = Slicer.computeBackwardSlice(findCallTo(mthd, callee, lineNumber), cg, pA, ddOpt,
				cdOpt);
		return printSDG(cg, pA, slice);
	}

	public String slice(String fileName, String funcName, String desc, String callee, int lineNumber)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		System.out.println("[WarningSlicer=>slice=>fileName:" + fileName + ",funcName:" + funcName + ",desc:" + desc
				+ ",callee:" + callee + ",lineNumber:" + lineNumber + "]");
		AnalysisScope scope = findOrCreateAnalysisScope();
		ClassHierarchy cha = findOrCreateCHA(scope);
		//debugPrint(cha);
		Iterable<Entrypoint> entrypoints = computeEntryPoints(fileName, funcName, desc, scope, cha);
		AnalysisOptions opts = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
		CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneCFABuilder(opts, new AnalysisCacheImpl(), cha, scope);
		CallGraph cg = builder.makeCallGraph(opts, null);

		CGNode mthd = findMethod(cg, desc, funcName, fileName);

		PointerAnalysis<InstanceKey> pA = builder.getPointerAnalysis();
		Collection<Statement> slice = Slicer.computeBackwardSlice(findCallTo(mthd, callee, lineNumber), cg, pA, ddOpt,
				cdOpt);
		return printSDG(cg, pA, slice);
	}

	public String dumpSlice(Collection<Statement> slice, String delimiter) {
		String rslt = "";
		int i = 1;
		for (Statement s : slice)
			rslt += (i++) + " " + s + delimiter;
		return rslt.trim();
	}

	public IMethod findIMethod(ClassHierarchy cha, AnalysisCache cache, AnalysisOptions opts, String clazz,
			String method, String desc) {
		for (Iterator<IClass> it = cha.iterator(); it.hasNext();) {
			ShrikeClass next = (ShrikeClass) it.next();
			if (!next.getClassLoader().getName().toString().equals("Primordial")) {
				if (next.getReference().getName().toString().equals(clazz)) {
					for (IMethod iMethod : next.getAllMethods()) {
						if (iMethod.getName().toString().contains(method)
								&& iMethod.getDescriptor().toString().contains(desc)) {
							return iMethod;
						}
					}
				}
			}
		}
		return null;
	}

	private void debugPrint(ClassHierarchy cha, AnalysisCache cache, AnalysisOptions opts) {
		if (debug) { // debug
			for (Iterator<IClass> it = cha.iterator(); it.hasNext();) {
				ShrikeClass next = (ShrikeClass) it.next();
				if (!next.getClassLoader().getName().toString().equals("Primordial")) {
					Collection<IMethod> allMethods = next.getAllMethods();
					for (IMethod iMethod : allMethods) {
						System.out.println(" " + iMethod);
						for (IField iField : next.getAllFields()) {
							if (!iField.toString().contains("Primordial")) {
								System.out.println("\t" + iField.getName());
							}
						}
					}
				}
			}
		}
	}

	public void writeToPdf(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, Collection<Statement> slice,
			String fileName, String functionName) throws WalaException {
		Graph<Statement> g = pruneSDG(new SDG<InstanceKey>(cg, pointerAnalysis, ddOpt, cdOpt), slice);
		DotUtil.dotify(g, makeNodeDecorator(), PDFTypeHierarchy.DOT_FILE,
				"/Users/ukoc/pdfs/" + fileName + "_" + functionName + ".pdf", "/opt/local/bin/dot");
	}

	public Graph<Statement> pruneSDG(SDG<?> sdg, final Collection<Statement> slice) {
		Predicate<Statement> f = new Predicate<Statement>() {
			@Override
			public boolean test(Statement o) {
				return slice.contains(o) && !o.toString().contains("Primordial") && o.getKind() == Statement.Kind.NORMAL;
			}
		};
		return GraphSlicer.prune(sdg, f);
	}

	public String printSDG(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, Collection<Statement> slice)
			throws WalaException {
		Graph<Statement> g = pruneSDG(new SDG<InstanceKey>(cg, pointerAnalysis, ddOpt, cdOpt), slice);
		NodeDecorator<Statement> decorator = makeNodeDecorator();
		Iterator<Statement> iter = g.iterator();
		String sdgStr = "";
		while (iter.hasNext()) {
			Statement stmt = iter.next();
			sdgStr += decorator.getLabel(stmt) + "\n";
		}
		return sdgStr;
	}
}