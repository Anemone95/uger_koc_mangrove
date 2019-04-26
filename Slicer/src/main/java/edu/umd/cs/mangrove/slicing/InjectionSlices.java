package edu.umd.cs.mangrove.slicing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

public class InjectionSlices {
	static final Charset utf8 = StandardCharsets.UTF_8;
	static String julietExclusionsFile = "data/JulietSpecificExclusions.txt";
	static String owaspExclusionsFile = "data/OwaspSpecificExclusions.txt";
	static String julietDom = "Ltestcases/CWE89_SQL_Injection/";
	static String owaspDom = "Lorg/owasp/benchmark/testcode/";
	static String owaspScopeFile = "data/owaspdata.txt";
	static String julietDir = "/Users/ukoc/workspace/Juliet4J/src/testcases/CWE89_SQL_Injection/";
	static String owaspDir = "/Users/ukoc/workspace/owasp-1.1/src/main/java/org/owasp/benchmark/testcode/";

	public static void main(String[] args)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		String warningFile = "/Users/ukoc/mangrove/phase2/lstm/data/java/InjectionOwaspBalanced.csv";
		String outputPath = "/Users/ukoc/mangrove/phase2/slices/injectionOwasp/";
		String className, callee, type, label, slice;
		int lineNumber;
		List<String> specificExclusions = Files.readAllLines(Paths.get(owaspExclusionsFile), utf8);
		List<String> warnings = Files.readAllLines(Paths.get(warningFile), utf8);
		while (!warnings.get(0).contains("BenchmarkTest09471"))
			warnings.remove(0);
		warnings.remove(0);// headers
		for (String warning : warnings) {
			String[] tokens = warning.split(",");
			className = tokens[0];
			lineNumber = Integer.parseInt(tokens[1]);
			callee = tokens[2].substring(tokens[2].indexOf('.') + 1);
			type = tokens[3];
			label = tokens[4];
			slice = processWarningOwasp(className, lineNumber, callee, label, specificExclusions);
			printToFile(outputPath + className + type + label + ".txt", slice);
		}
	}

	public static String processWarningOwasp(String className, int lineNumber, String callee, String label,
			List<String> specificExclusions) throws IOException, FileNotFoundException, UnsupportedEncodingException,
			CancelException, WalaException, IllegalArgumentException, InvalidClassFileException {
		String exlcusion = "org\\/owasp\\/benchmark\\/testcode\\/(?!(" + className + "(\\$(\\w*|\\d))*)$).*";
		specificExclusions.add(exlcusion);
		try {
			String[] nameAndDesc = findEnclosingMethod(lineNumber, owaspDir + className + ".java",
					"Lorg/owasp/benchmark/testcode/");
			WALASlicer slicer = new WALASlicer(owaspScopeFile, specificExclusions);
			System.out.println("className:" + className + ", callee:" + callee + ", function:" + nameAndDesc[0] + ", desc:"
					+ nameAndDesc[1] + ", lineNumber:" + lineNumber);
			return slicer.sliceOwasp(owaspDom + className, nameAndDesc[0], nameAndDesc[1], callee, lineNumber);
		} catch (com.ibm.wala.util.debug.UnimplementedError e) {
			System.err.println(e.getLocalizedMessage());
		} catch (IllegalArgumentException e) {
			System.err.println(e.getLocalizedMessage());
		} finally {
			specificExclusions.remove(exlcusion);
		}
		return null;
	}

	public static void printToFile(String fName, String slice) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(fName);
		writer.print(slice);
		writer.flush();
		writer.close();
	}

	public static String[] findEnclosingMethod(int lineNumber, String fileName, String subFolder) throws IOException {
		List<String> statements = Files.readAllLines(Paths.get(fileName), utf8);
		String prog = String.join("\n", statements);
		CompilationUnit cu = JavaParser.parse(prog);//TODO
		String[] rslt = {};
		return rslt;
	}
}