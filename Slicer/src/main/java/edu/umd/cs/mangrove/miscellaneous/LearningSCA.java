package edu.umd.cs.mangrove.miscellaneous;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import edu.umd.cs.mangrove.slicing.WALASlicer;

public class LearningSCA {
	static final Charset utf8 = StandardCharsets.UTF_8;
	static String owaspExclusionsFile = "data/OwaspSpecificExclusions.txt";
	static String owaspDom = "Lorg/owasp/benchmark/testcode/";
	static String owaspDir = "/Users/ukoc/workspace/owasp-1.1/src/main/java/org/owasp/benchmark/testcode/";
	static String scopeFileName = "data/owaspdata.txt";
	static HashMap<String, ArrayList<String>> sinksMap = new HashMap<String, ArrayList<String>>();
	static List<String> specificExclusions;
	private static int lineNumber;
	static String[] injections = { "sqli", "cmdi", "xpathi", "ldapi" };
	static String[] sinkFiles = { "sql-jdbc", "command", "xpath-javax", "ldap" };
	static String desc = "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V";
	static String functionName = "doPost";

	public static void main(String[] args)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		String injectionSinkFiles = "/Users/ukoc/Documents/workspace/find-sec-bugs/plugin/src/main/resources/injection-sinks";
		specificExclusions = Files.readAllLines(Paths.get(owaspExclusionsFile), StandardCharsets.UTF_8);
		createSinks(injectionSinkFiles);
		runExp("/Users/ukoc/owasp-1.1/expectedresults-1.1.csv");
	}

	private static void createSinks(String injectionSinkFiles) throws IOException {
		for (int i = 0; i < sinkFiles.length; i++) {
			ArrayList<String> lines = (ArrayList<String>) Files
					.readAllLines(Paths.get(injectionSinkFiles + "/" + sinkFiles[i] + ".txt"), StandardCharsets.UTF_8);
			while (lines.remove(""))
				;
			ArrayList<String> sinks = new ArrayList<String>();
			for (String line : lines) {
				String sink = line.substring(line.lastIndexOf(".") + 1, line.indexOf("("));
				if (!sinks.contains(sink)) {
					sinks.add(sink);
				}
			}
			sinksMap.put(injections[i], sinks);
		}
	}

	public static void runExp(String labelFile) throws IOException, FileNotFoundException, UnsupportedEncodingException,
			CancelException, WalaException, IllegalArgumentException, InvalidClassFileException {
		List<String> labels = Files.readAllLines(Paths.get(labelFile), utf8);
		labels.remove(0);/// removing headers
		for (String l : labels) {
			String[] tuple = l.split(",");
			if (tuple[1].equals("sqli")) {
				// tuple[1].equals("cmdi") || tuple[1].equals("xpathi") ||
				// tuple[1].equals("ldapi")
				processOwasp(tuple[0], tuple[1], tuple[2], tuple[3]);
			}
		}
	}

	public static void processOwasp(String className, String category, String label, String cwe)
			throws IOException, FileNotFoundException, UnsupportedEncodingException, CancelException, WalaException,
			IllegalArgumentException, InvalidClassFileException {

		String exlcusion = "org\\/owasp\\/benchmark\\/testcode\\/(?!(" + className + "(\\$(\\w*|\\d))*)$).*";
		specificExclusions.add(exlcusion);
		try {
			String callee = findSinkPoint(owaspDir + className + ".java", functionName, category);
			WALASlicer slicer = new WALASlicer(scopeFileName, specificExclusions);
			String debugStr = "className:" + className + ", callee:" + callee + ", category:" + category
					+ ", lineNumber:" + lineNumber;
			System.out.println(debugStr);
			String sliceJuliet = slicer.sliceOwasp(owaspDom + className, functionName, desc, callee, lineNumber);
			String fileName = "/Users/ukoc/mangrove/phase2/slices/owaspAll/" + className + "_" + category + "_" + label
					+ ".txt";
			printToFile(fileName, sliceJuliet);
		} catch (com.ibm.wala.util.debug.UnimplementedError e) {
			System.err.println(e.getLocalizedMessage());
		} catch (IllegalArgumentException e) {
			System.err.println(e.getLocalizedMessage());
		} finally {
			specificExclusions.remove(exlcusion);
		}
	}

	public static void printToFile(String fName, String slice) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(fName);
		writer.print(slice);
		writer.flush();
		writer.close();
	}

	public static String findSinkPoint(String fileName, String functionName, String category) throws IOException {
		String codeStr = String.join("\n", Files.readAllLines(Paths.get(fileName), utf8));
		CompilationUnit cu = JavaParser.parse(codeStr);

		MethodDeclaration doPostDec = cu.getNodesByType(MethodDeclaration.class).stream()
				.filter(c -> c.getNameAsString().equals(functionName)).findFirst().get();
		MethodCallExpr callee = doPostDec.getBody().get().getNodesByType(MethodCallExpr.class).stream().filter(
				call -> sinksMap.get(category).contains(call.getNameAsString()) && !call.getArguments().isEmpty())
				.findFirst().get();
		lineNumber = callee.getBegin().get().line;
		return callee.getNameAsString();
	}
}