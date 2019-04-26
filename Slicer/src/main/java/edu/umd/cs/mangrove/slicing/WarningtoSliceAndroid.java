package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jgrapht.traverse.BreadthFirstIterator;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.umd.cs.mangrove.util.BugReportAOL;
import edu.umd.cs.mangrove.util.BugReportUtil;
import edu.umd.cs.mangrove.util.DirectoryUtil;
import edu.umd.cs.mangrove.util.PrintUtil;
import edu.umd.cs.mangrove.util.SDGUtils;

public class WarningtoSliceAndroid {
	static final String OUTPATH = DirectoryUtil.PROJECT_DIR + "data/slices/android/";
	static final String BENCHPATH = DirectoryUtil.BENCHDIR_ANDROID + "benchmarks/DroidBenchExtended/benchmark/apks/";
	public static final Charset charset = Charset.forName("UTF-8");
	private static String SLICER = "Summary";

	public static void main(String[] args) throws Exception {
		String groundTruthDir = "benchmarks/DroidBenchExtended/benchmark/groundtruth/";
		Collection<File> files = FileUtils.listFiles(new File(DirectoryUtil.BENCHDIR_ANDROID + groundTruthDir),
				new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
		for (File file : files) {
			String outFileName = DirectoryUtil.PROJECT_DIR + "/data/slices/amandroid/" + file.getName() + ".txt";
			if (new File(outFileName).exists()) {
				//System.out.println("Slice Exists");
				continue;
			}
			ArrayList<BugReportAOL> parseSingleAOLXML = BugReportUtil.parseSingleAOLXML(file);
			for (BugReportAOL report : parseSingleAOLXML) {
				SDG sdg = getSDG(report.appTo);
				if (sdg == null) {
					System.out.println("No SDG");
					continue;
				}
				JoanaLineSlicer jSlicer = new JoanaLineSlicer(sdg, SLICER, true);
				System.out.print("Done...\n");
				HashSet<SDGNode> errorNodes = mapStatement2Node(sdg, report);
				if (errorNodes.isEmpty()) {
					System.err.println(file.getName());
					continue;
				}
				Collection<SDGNode> slice = jSlicer.slice(errorNodes);
				String errorNodesStr = "[";
				for (SDGNode sdgNode : errorNodes) {
					if (!jSlicer.toAbstract.contains(sdgNode) && !JoanaLineSlicer.isRemoveNode(sdgNode)) {
						errorNodesStr += sdgNode.getId() + ", ";
					}
				}
				String result = //errorNodesStr.substring(0, errorNodesStr.lastIndexOf(",")) + "]\n" + 
						SDGUtils.prepareSliceForEncoding(sdg, slice, jSlicer.toAbstract);

				PrintUtil.printToFile(outFileName, result, false);
				//System.out.println(result);
			}
		}
	}

	private static HashSet<SDGNode> mapStatement2Node(SDG sdg, BugReportAOL report) {
		HashSet<SDGNode> nodes = new HashSet<SDGNode>();
		final BreadthFirstIterator<SDGNode, SDGEdge> it = new BreadthFirstIterator<SDGNode, SDGEdge>(sdg);
		while (it.hasNext()) {
			final SDGNode node = it.next();
			if (node.getSource().contains("wala")) {
				continue;
			}
			//			System.out.println(node.getId() + ":" + node.getType() + ":" + node.getSr() + ":" + node.getLabel() + ":"
			//					+ node.getUnresolvedCallTarget() + ":" + node.getBytecodeMethod());
			if (node.getSource().contains(report.classTo) && node.getBytecodeMethod().contains(report.mthdTo)
					&& node.getType() != null && node.getType().equals(report.typeTo)
					&& ((node.getUnresolvedCallTarget() != null && node.getUnresolvedCallTarget().contains(report.stmtTo))
							|| node.getLabel().contains(report.sink + "("))) {
				nodes.add(node);
			}
		}
		if (nodes.isEmpty()) {
			System.err.println("No node: " + report);
			//throw new IllegalArgumentException("No node");
		}
		return nodes;
	}

	private static SDG getSDG(String pdgFile) throws IOException {
		SDG localSdg = null;
		String pathname = BENCHPATH + pdgFile.replace("\\", "/") + ".pdg";
		System.out.print("SDF file: " + pathname + " :: ");
		File file = new File(pathname);
		if (file.exists()) {//&& file.length() < SIZE_LIMIT
			System.out.print("Reading SDG from...");
			localSdg = SDG.readFrom(pathname);
			System.out.print("SDG file read done! ");
		}
		return localSdg;
	}
}