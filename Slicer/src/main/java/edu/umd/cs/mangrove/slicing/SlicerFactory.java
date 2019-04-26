package edu.umd.cs.mangrove.slicing;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.slicer.Slicer;
import edu.kit.joana.ifc.sdg.graph.slicer.SummarySlicerBackward;

public class SlicerFactory {

	public static Slicer makeSlicer(final SDG graph, final String kind, final boolean isbackward) {
		Slicer s = new SummarySlicerBackward(graph);
		//		if (isbackward) {
		//			if (kind.equals("Adhoc")) {
		//				s = new AdhocBackwardSlicer(graph);
		//			} else if (kind.equals("Summary")) {
		//				s = new SummarySlicerBackward(graph);
		//			} else if (kind.equals("Context")) {
		//				s = new ContextSlicerBackward
		//			} else if (kind.equals("ContextInsensitive")) {
		//				s = new ContextInsensitiveBackward(graph);
		//			} else if (kind.equals("Step")) {
		//				Set<Kind> omit = new HashSet<Kind>();
		//				s = new StepSlicerBackward(graph, omit);
		//			} else if (kind.equals("IPDG")) {
		//				s = new IPDGSlicerBackward(graph, null);
		//			} else if (kind.equals("Intraprocedural")) {
		//				s = new IntraproceduralSlicerBackward(graph);
		//			} else if (kind.equals("NandaI2P")) {
		//				s = new NandaI2PBackward(graph);
		//			} else if (kind.equals("IPDGSlicer")) {
		//				s = new IPDGSlicerBackward(graph, false);
		//			}
		//		} else {
		//			s = new SummarySlicerForward(graph);
		//		}
		return s;

	}
}
