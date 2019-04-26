package edu.umd.cs.mangrove.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.github.javaparser.ast.CompilationUnit;

import edu.kit.joana.ifc.sdg.graph.SDG;
import edu.kit.joana.ifc.sdg.graph.SDGEdge;
import edu.kit.joana.ifc.sdg.graph.SDGSerializer;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.VirtualNode;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.threads.ThreadsInformation.ThreadInstance;
import edu.kit.joana.ifc.sdg.util.graph.ThreadInformationUtil;
import edu.kit.joana.ifc.sdg.util.graph.io.dot.MiscGraph2Dot;
import edu.kit.joana.wala.core.graphs.Dominators;
import edu.umd.cs.mangrove.slicing.JoanaSDGHelper;

public class PrintUtil {

	public static String postprocess(CompilationUnit cu) {
		return postprocess(cu.toString());
	}

	public static String postprocess(String data) {
		data = data.substring(data.indexOf("Instructions:"));
		data = data.replaceAll("\\(", " ( ");
		data = data.replaceAll("\\)", " ) ");
		data = data.replaceAll("\\{", " { ");
		data = data.replaceAll("\\}", " } ");
		data = data.replaceAll("\\[", " [ ");
		data = data.replaceAll("\\]", " ] ");
		data = data.replaceAll("<", " < ");
		data = data.replaceAll(";", " ");
		data = data.replaceAll(">", " > ");
		data = data.replaceAll("@", " @ ");
		data = data.replaceAll("#", " # ");
		data = data.replaceAll(",", " , ");
		data = data.replaceAll("'", "");
		data = data.replaceAll("\"", "");
		data = data.replaceAll("=", " = ");
		data = data.replaceAll("BB\\d+", "");
		data = data.replaceAll("\\d+\\s\\s\\s", "");
		data = data.replaceAll("\\s+", " ");
		//		data = data.replaceAll("throws \\w+(,\\s\\w+\\s)?", "");
		//		data = data.replaceAll("try \\{", "\\{");
		//		data = data.replaceAll("/\\*\\*\\*/\n", "");
		//		data = data.replaceAll("/\\*\\*/\n", "");
		//		data = data.replaceAll("\\s*//(\\s\\w+)*\\s?\n", "\n");
		//		data = data.replaceAll("\\s\\.", " ");
		//		data = data.replaceAll("\\(\\.", "(");
		//		data = data.replaceAll("injectableMethod\\(\\)", "injectableMethod(paramTaint)");
		data = data.replaceAll("\\=\\.", "=");
		data = data.replaceAll("\n", " ");
		data = data.replaceAll("    ", " ");
		data = data.replaceAll("   ", " ");
		data = data.replaceAll("  ", " ");
		data = data.replaceAll(":", " : ");
		data = data.replaceAll(" \\d+", " N ");
		return data.replaceAll("  ", " ");
	}

	public static void printToFile(String fileName, String str, boolean append) throws IOException {
		FileWriterWithEncoding writer = new FileWriterWithEncoding(new File(fileName), StandardCharsets.UTF_8, append);
		writer.write(str + '\n');
		writer.close();
	}

	public static void printSDG2File(final String fileName, final SDG sdg) throws FileNotFoundException {
		System.out.println(sdg.getThreadsInfo());
		final DirectedGraph<ThreadInstance, DefaultEdge> tct = ThreadInformationUtil
				.buildThreadCreationTree(sdg.getThreadsInfo());
		MiscGraph2Dot.export(tct, MiscGraph2Dot.tctExporter(), "tct.dot");
		for (final ThreadInstance ti : sdg.getThreadsInfo()) {
			final DirectedGraph<VirtualNode, SDGEdge> threadGraph = JoanaSDGHelper.unfoldVirtualCFGFor(sdg, ti.getId());
			MiscGraph2Dot.export(threadGraph, MiscGraph2Dot.threadGraphExporter(),
					String.format("thread-%d.dot", ti.getId()));
			final Dominators<VirtualNode, SDGEdge> threadDom = Dominators.compute(threadGraph,
					new VirtualNode(ti.getEntry(), ti.getId()));
			MiscGraph2Dot.export(threadDom.getDominationTree(), MiscGraph2Dot.genericExporter(),
					String.format("thread-%d-dom.dot", ti.getId()));
		}
		final PrintWriter pw = new PrintWriter(fileName);
		SDGSerializer.toPDGFormat(sdg, pw);
		pw.close();
	}
	
	
}