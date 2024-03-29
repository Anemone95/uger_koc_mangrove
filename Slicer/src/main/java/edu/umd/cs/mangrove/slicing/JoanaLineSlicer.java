package edu.umd.cs.mangrove.slicing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.traverse.BreadthFirstIterator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNode.Kind;
import edu.kit.joana.ifc.sdg.graph.slicer.Slicer;

public class JoanaLineSlicer {
	private SDG sdg;
	private Slicer slicer;

	private Map<Line, Set<SDGNode>> line2nodes;
	private SortedSet<Line> lines;
	public ArrayList<SDGNode> toAbstract;

	public JoanaLineSlicer(final SDG sdg) {
		this(sdg, "Summary", true);
		toAbstract = new ArrayList<SDGNode>();
	}

	public JoanaLineSlicer(final SDG sdg, final String slicerKind, final boolean isBackward) {
		this.sdg = sdg;
		this.slicer = SlicerFactory.makeSlicer(sdg, slicerKind, isBackward);
		toAbstract = new ArrayList<SDGNode>();
	}

	public SortedSet<Line> getLines() {
		return Collections.unmodifiableSortedSet(lines);
	}

	public HashSet<SDGNode> getNodesAtLine(Line line) {
		HashSet<SDGNode> nodes = new HashSet<SDGNode>();
		final BreadthFirstIterator<SDGNode, SDGEdge> it = new BreadthFirstIterator<SDGNode, SDGEdge>(sdg);
		while (it.hasNext()) {
			final SDGNode node = it.next();
			if (node.getSource().equals(line.filename)) {
				//System.out.println(node.getId() + ":" + node.getSource() + ":" + node.getSr() + ":" + node.getLabel());
				if (node.getSr() == line.line) {//  && !isAbstractNode(node) && !isAbstractNode(node)) {
					nodes.add(node);
				}
			}
		}
		if (nodes.isEmpty()) { throw new IllegalArgumentException("No node"); }
		return nodes;
	}

	public Collection<SDGNode> slice(Line line) {
		Set<Line> mylines = new HashSet<Line>();
		mylines.add(line);
		return slice(mylines);
	}

	public Collection<SDGNode> slice(Set<Line> line) {
		if (sdg == null) { throw new IllegalStateException("Run readIn first to load sdg from file."); }
		HashSet<SDGNode> crit = new HashSet<>();
		for (Line l : line) {
			Set<SDGNode> nodes = line2nodes.get(l);
			if (nodes != null) {
				crit.addAll(nodes);
			}
		}
		return slice(crit);
	}

	public Collection<SDGNode> slice(HashSet<SDGNode> crit) {
		Collection<SDGNode> result = slicer.slice(crit);
		List<SDGNode> toRemove = new ArrayList<SDGNode>();
		boolean verbose = false;
		for (SDGNode n : result) {
			if (isRemoveNode(n)) {
				//System.out.println(n.getId() + "\t" + n.getLabel() + "\t" + n.getType() + "\t" + n.getKind() + "\t"
				//	+ n.getOperation() + "\t" + n.getSr() + "\t" + n.getSource() + "\t" + n.getBytecodeIndex() + "\t"
				//	+ n.getClassLoader());
				toRemove.add(n);
			} else if (isAbstractNode(n)) {
				toAbstract.add(n);
			} else if (verbose) {
				System.out.println(n.getId() + "\t" + n.getLabel() + "\t" + n.getType() + "\t" + n.getKind() + "\t"
						+ n.getOperation() + "\t" + n.getSr() + "\t" + n.getSource() + "\t" + n.getBytecodeIndex());
			}
		}
		result.removeAll(toRemove);
		return result;
	}

	public HashMap<String, String> computeVariableNameMap(String fileName, Collection<SDGNode> slice)
			throws IOException {
		String codeStr = String.join("\n", Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));
		CompilationUnit cu = JavaParser.parse(codeStr);

		HashMap<String, String> variableNameMap = new HashMap<String, String>();

		//LHS of assignment operations
		cu.getNodesByType(AssignExpr.class).stream().forEach(c -> {
			SDGNode latestNode = getLatestNode(slice, c.getBegin().get().line, fileName);
			if (latestNode != null) {
				String[] assignmentPair = latestNode.getLabel().split(" = ");
				if (!variableNameMap.containsKey(assignmentPair[0])) {
					variableNameMap.put(assignmentPair[0], c.getTarget().toString());
				}
			}
		});

		// Collect all VariableDeclarators
		List<VariableDeclarator> varDec = cu.getNodesByType(VariableDeclarator.class);
		for (VariableDeclarator vd : varDec) {
			List<SDGNode> nodes = getRelevantNodes(slice, vd.getBegin().get().line, fileName);
			for (SDGNode n : nodes) {
				String[] assignmentPair = n.getLabel().split(" = ");
				if (!variableNameMap.containsKey(assignmentPair[0])) {
					if ((n.getKind().equals(SDGNode.Kind.CALL) && typeComperator(n, vd))
							|| (n.getKind().equals(SDGNode.Kind.EXPRESSION) && n.getType().equals("Ljava/lang/Object"))) {
						variableNameMap.put(assignmentPair[0], vd.getIdentifierAsString());
					} else if (n.getType().equals("Ljava/lang/StringBuilder")) {
						variableNameMap.put(assignmentPair[0], "sb");
					}
				}
			}
		}
		return variableNameMap;
	}

	private boolean typeComperator(SDGNode n, VariableDeclarator variableDeclarator) {
		String string = variableDeclarator.getType().toString().replaceAll("<.*>|\\[\\]", "");
		String replace = n.getType().replace("/", ".");
		replace = replace.substring(replace.indexOf("L") + 1);
		return replace.endsWith(string)
				|| (string.contains(".") && replace.startsWith(string.substring(0, string.lastIndexOf(".") + 1))
						&& replace.endsWith(string.substring(string.lastIndexOf(".") + 1)));
	}

	public Set<Line> getLinesForNodes(Collection<SDGNode> nodes) {
		TreeSet<Line> lines = new TreeSet<Line>();

		for (SDGNode node : nodes) {
			if (isRemoveNode(node)) {
				lines.add(new Line(node.getSource(), node.getSr()));
			}
		}

		return lines;
	}

	public SDGNode getLatestNode(Collection<SDGNode> nodes, int line, String fileName) {
		SDGNode rslt = null;
		for (SDGNode n : nodes) {
			if (fileName.endsWith(n.getSource()) && line == n.getSr() && (rslt == null || rslt.getId() < n.getId())) {
				rslt = n;
			}
		}
		return rslt;
	}

	public List<SDGNode> getRelevantNodes(Collection<SDGNode> nodes, int line, String fileName) {
		List<SDGNode> rslt = new ArrayList<SDGNode>();
		for (SDGNode n : nodes) {
			if (n.getSource().equals(fileName) && line == n.getSr() && !n.getKind().equals(SDGNode.Kind.PREDICATE)) {
				rslt.add(n);
			}
		}
		return rslt;
	}

	public static class LineInSlice implements Comparable<LineInSlice> {
		public int count;
		public final Line line;

		public LineInSlice(final Line line) {
			this.line = line;
		}

		public int hashCode() {
			return line.hashCode() * 4177;
		}

		public boolean equals(Object o) {
			if (this == o) { return true; }

			if (o instanceof LineInSlice) {
				LineInSlice lis = (LineInSlice) o;
				return line.equals(lis.line);
			}

			return false;
		}

		public int compareTo(LineInSlice other) {
			int diff = count - other.count;

			if (diff != 0) { return diff; }

			return line.compareTo(other.line);
		}

		public String toString() {
			return count + "\t" + line;
		}

	}

	public static boolean isRemoveNode(final SDGNode node) {
		return node.getSource() == null || (node.getClassLoader() != null && node.getClassLoader().equals("Primordial"))
				|| node.getSr() < 0 || isExcluded(node.getSource()) || node.getLabel().contains("_exception_")
				|| node.getLabel().contains("fake")
				|| (node.getBytecodeIndex() < -2 && node.getOperation() != SDGNode.Operation.ASSIGN);
	}

	private static boolean isExcluded(final String str) {
		return str.contains("java/lang") || str.contains("java/io") || str.contains("java/util")
				|| str.contains("com/ibm/wala") || str.contains("sun/reflect") || str.contains("java/security")
				|| str.contains("sun/") || str.contains("javax/servlet");
	}

	public boolean isAbstractNode(SDGNode sdgNode) {
		return sdgNode.getKind().equals(Kind.FORMAL_IN) || sdgNode.getKind().equals(Kind.FORMAL_OUT)
				|| sdgNode.getKind().equals(Kind.ACTUAL_IN) || sdgNode.getKind().equals(Kind.ACTUAL_OUT)
				|| sdgNode.getLabel().equals("many2many") || sdgNode.getLabel().contains("UNIQ(")
				|| sdgNode.getLabel().contains("<init>") || sdgNode.getLabel().equals("immutable");
	}

	public static String getClassName(String file) {
		String result;

		int indexOfSlash = file.lastIndexOf('/');
		if (indexOfSlash > 0) {
			result = file.substring(indexOfSlash + 1);
		} else {
			result = file;
		}
		result = result.substring(0, result.lastIndexOf('.'));
		return result;
	}

	public static class Line implements Comparable<Line> {
		public final String filename;
		public final int line;

		public Line(String filename, int line) {
			this.filename = filename;
			this.line = line;
		}

		public String toString() {
			return filename + ":" + line;
		}

		public String getRealLine(String base) throws IOException {
			String real = "<not found>";

			BufferedReader bIn = new BufferedReader(new FileReader(base + filename));
			for (int i = 1; i < line && bIn.ready(); i++) {
				bIn.readLine();
			}

			if (bIn.ready()) {
				real = bIn.readLine();
			}
			bIn.close();
			return real;
		}

		public boolean equals(Object obj) {
			if (obj instanceof Line) {
				Line l = (Line) obj;
				return filename.equals(l.filename) && line == l.line;
			}
			return false;
		}

		public int hashCode() {
			return filename.hashCode() * line;
		}

		public int compareTo(Line l) {
			int cmp = filename.compareTo(l.filename);
			if (cmp == 0) {
				cmp = line - l.line;
			}
			return cmp;
		}
	}
}