package edu.umd.cs.mangrove.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.base.CharMatcher;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.SDGNode.Operation;

public class SDGUtils {

	public static String nodeDecorator(SDGNode n) {
		String constVal, replaceValue, rslt = "";
		String value = CharMatcher.ascii().retainFrom(n.getLabel());
		value = value.replaceAll("\r\n", " NEWLINE ").replace("\n", " NEWLINE ");
		if (value.contains("#(")) {
			constVal = value.substring(value.indexOf("#(") + 2);
			if (constVal.endsWith(").length()")) {
				replaceValue = "" + constVal.substring(0, constVal.indexOf(")")).length();
				value = value.replace("#(" + constVal, " " + replaceValue + " ");
			} else {
				constVal = constVal.substring(0, constVal.indexOf(")"));
				replaceValue = constVal.matches("\\d+") || constVal.length() == 1 ? constVal : "STRING";
				value = value.replace("#(" + constVal + ")", " " + replaceValue + " ");
			}
		}
		Operation operation = n.getOperation();
		if (operation.equals(SDGNode.Operation.CALL) && value.contains(" = ")) {
			rslt += operation + " " + n.getType() + " " + value;
		} else if (operation.equals(SDGNode.Operation.IF)) {
			rslt += value;
		} else {
			rslt += operation + " " + value;
		}
		return rslt;
	}

	public static String prepareSlicePruned(final SDG sdg, Collection<SDGNode> slice, ArrayList<SDGNode> prune) {
		HashMap<Integer, HashSet<SDGNode>> lookupTable = new HashMap<>();
		HashSet<Integer> seenAll = new HashSet<>();
		prune.stream().forEach(node -> lookupTable.put(node.getId(), new HashSet<SDGNode>()));
		for (SDGNode node : prune) {
			if (seenAll.contains(node.getId())) continue;
			Queue<SDGNode> absQueue = new ConcurrentLinkedQueue<>();
			HashSet<Integer> seenNew = new HashSet<>();
			seenNew.add(node.getId());
			absQueue.add(node);
			while (!absQueue.isEmpty()) {
				SDGNode head = absQueue.poll();
				sdg.outgoingEdgesOf(head).stream().filter(e -> slice.contains(e.getTarget())).forEach(e -> {
					SDGNode target = e.getTarget();
					if (prune.contains(target)) {
						if (!seenNew.contains(target.getId())) {
							absQueue.add(target);
							seenNew.add(target.getId());
						}
					} else {
						seenNew.stream().forEach(id -> lookupTable.get(id).add(target));
					}
				});
			}
			seenAll.addAll(seenNew);
		}
		StringBuilder result = new StringBuilder("");
		for (SDGNode node : slice) {
			if (prune.contains(node)) continue;
			result.append("\n" + node.getId() + " :: ");
			HashSet<SDGNode> visited = new HashSet<>();
			String edgesStr = "";
			HashSet<SDGNode> successors = new HashSet<>();
			Set<SDGEdge> outgoingEdges = sdg.outgoingEdgesOf(node);
			outgoingEdges.stream().forEach(e -> successors.add(e.getTarget()));
			for (SDGEdge edge : outgoingEdges) {
				SDGNode target = edge.getTarget();
				if (!visited.contains(target) && slice.contains(target)) {
					if (prune.contains(target)) {
						for (SDGNode target2 : lookupTable.get(target.getId())) {
							String estr = "<JM, " + target2.getId() + ">";
							edgesStr += edgesStr.contains(estr) || successors.contains(target2) ? "" : estr;
						}
					} else {
						String label = edge.getLabel() == null ? "" : edge.getLabel() + ", ";
						result.append("<" + edge.getKind() + ", " + label + target + ">");
					}
					visited.add(target);
				}
			}
			result.append(edgesStr);
		}
		result.append("\n");
		//slice.stream().filter(n -> !prune.contains(n)).forEach(n -> result.append(nodeDecorator(n)));
		for (SDGNode sdgNode : slice) {
			if (!prune.contains(sdgNode)) {
				result.append(sdgNode.getId() + ":" + nodeDecorator(sdgNode) + "\n");
			}
		}
		return result.toString();
	}

	public static String prepareSliceForEncoding(final SDG sdg, Collection<SDGNode> slice, ArrayList<SDGNode> prune) {
		HashMap<Integer, HashSet<SDGNode>> lookupTable = new HashMap<>();
		HashSet<Integer> seenAll = new HashSet<>();
		prune.stream().forEach(node -> lookupTable.put(node.getId(), new HashSet<SDGNode>()));
		for (SDGNode node : prune) {
			if (seenAll.contains(node.getId())) continue;
			Queue<SDGNode> absQueue = new ConcurrentLinkedQueue<>();
			HashSet<Integer> seenNew = new HashSet<>();
			seenNew.add(node.getId());
			absQueue.add(node);
			while (!absQueue.isEmpty()) {
				SDGNode head = absQueue.poll();
				sdg.outgoingEdgesOf(head).stream().filter(e -> slice.contains(e.getTarget())).forEach(e -> {
					SDGNode target = e.getTarget();
					if (prune.contains(target)) {
						if (!seenNew.contains(target.getId())) {
							absQueue.add(target);
							seenNew.add(target.getId());
						}
					} else {
						seenNew.stream().forEach(id -> lookupTable.get(id).add(target));
					}
				});
			}
			seenAll.addAll(seenNew);
		}
		StringBuilder result = new StringBuilder("");
		for (SDGNode node : slice) {
			if (prune.contains(node)) continue;
			result.append("\n" + node+" :: "+node.getKind() + " :: " + node.getOperation() + " :: " + node.getType() + " :: "
					+ node.getLabel() + "::");
			HashSet<SDGNode> visited = new HashSet<>();
			String edgesStr = "";
			HashSet<SDGNode> successors = new HashSet<>();
			Set<SDGEdge> outgoingEdges = sdg.outgoingEdgesOf(node);
			outgoingEdges.stream().forEach(e -> successors.add(e.getTarget()));
			for (SDGEdge edge : outgoingEdges) {
				SDGNode target = edge.getTarget();
				if (!visited.contains(target) && slice.contains(target)) {
					if (prune.contains(target)) {
						for (SDGNode target2 : lookupTable.get(target.getId())) {
							String estr = "JM," + target2.getId() + ":";
							if (!successors.contains(target2) && !edgesStr.contains(estr)) {
								edgesStr += estr;
							}
						}
					} else {
						edgesStr += edge.getKind() + "," + target.getId() + ":";
					}
					visited.add(target);
				}
			}
			if (!edgesStr.equals("")) {
				result.append(edgesStr.substring(0, edgesStr.length() - 1));
			}
		}
		return result.toString();
	}

	public static String prepareSliceAbstracted(final SDG sdg, Collection<SDGNode> slice, ArrayList<SDGNode> prune) {
		String result = "";
		for (SDGNode sdgNode : slice) {
			result += "\n" + sdgNode + " :: ";
			Set<SDGEdge> outgoingEdgesOf = sdg.outgoingEdgesOf(sdgNode);
			boolean hopinTrue = prune.contains(sdgNode);
			for (SDGEdge sdgEdge : outgoingEdgesOf) {
				if (slice.contains(sdgEdge.getTarget())) {
					boolean hopoutTrue = prune.contains(sdgEdge.getTarget());
					if (hopinTrue && hopoutTrue) {
						result += new String("<HOP-INTER, " + sdgEdge.getTarget() + ">");
					} else if (hopinTrue) {
						result += new String("<HOP-OUT, " + sdgEdge.getTarget() + ">");
					} else if (hopoutTrue) {
						result += new String("<HOP-IN, " + sdgEdge.getTarget() + ">");
					} else {
						result += new String("<" + sdgEdge.getKind() + ", "
								+ (sdgEdge.getLabel() == null ? "" : sdgEdge.getLabel() + ", ") + sdgEdge.getTarget() + ">");
					}
				}
			}
		}
		StringBuilder result2 = new StringBuilder(result + "\n");
		for (SDGNode sdgNode : slice) {
			if (prune.contains(sdgNode)) {
				result2.append(sdgNode.getId() + ":HOP-NODE\n");
			} else {
				result2.append(sdgNode.getId() + ":" + nodeDecorator(sdgNode) + "\n");
			}
		}
		return result2.toString();
	}
}
