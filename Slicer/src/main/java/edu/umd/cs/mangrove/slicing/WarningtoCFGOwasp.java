package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import edu.umd.cs.mangrove.util.PrintUtil;

public class WarningtoCFGOwasp {
	private static final String MANGROVEDIR = "/Users/ukoc/Documents/workspace/mangrove/mangrove/";
	private static final String OUTPATH = MANGROVEDIR + "data/controlflowgraphs/owasp/";
	private static final String ENTRY_METHOD = "doPost";
	private static final String ENTRY_DESC = "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V";
	private static final String spoceFile = MANGROVEDIR + "data/scopeFiles/owaspdata.txt";
	private static final String classFileDir = "org/owasp/benchmark/testcode/";
	private static final String classPath = "L" + classFileDir;
	static WALASlicer slicer;

	public static void main(String[] args) throws IOException, IllegalArgumentException, CancelException, WalaException,
			InvalidClassFileException, ParserConfigurationException, SAXException {
		slicer = new WALASlicer(spoceFile, null);
		String warningFile = "/Users/ukoc/Documents/workspace/data_mangrove/java/CWE89OwaspStatsAll.txt";
		List<String> warnings = Files.readAllLines(Paths.get(warningFile), StandardCharsets.UTF_8);
		for (String warning : warnings) {
			System.err.println(warning);
			String[] fields = warning.split(",");
			String sink = fields[2].substring(0, fields[2].indexOf(' ')).replace(".", ", ");
			computeCFG(fields[0], Integer.parseInt(fields[1]), sink, fields[3]);
			//break;
		}
	}

	private static void computeCFG(String className, int lineNumber, String sink, String label)
			throws IOException, CancelException, WalaException, InvalidClassFileException {

		String outFile = OUTPATH + className + "_" + label + ".txt";
		if (new File(outFile).exists()) {
			System.out.println("CFG Exists");
			return;
		}
		System.out.println(outFile);
		try {
			String cfg = slicer.computeCFG(classPath + className, ENTRY_METHOD, ENTRY_DESC, sink, lineNumber);
			//System.out.println(cfg + " " + label);
			PrintUtil.printToFile(outFile, cfg, true);
		} catch (com.ibm.wala.util.debug.UnimplementedError e) {
			throw e;//System.err.println(e.getLocalizedMessage());
		} catch (IllegalArgumentException e) {
			throw e;//System.err.println(e.getLocalizedMessage());
		}
	}
}