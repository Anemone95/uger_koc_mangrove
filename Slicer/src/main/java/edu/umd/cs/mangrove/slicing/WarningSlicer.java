package edu.umd.cs.mangrove.slicing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

public class WarningSlicer {
	static final Charset utf8 = StandardCharsets.UTF_8;
	static String julietExclusionsFile = "data/scopeFiles/JulietSpecificExclusions.txt";
	static String owaspExclusionsFile = "data/scopeFiles/OwaspSpecificExclusions.txt";
	static String julietDom = "Ltestcases/CWE89_SQL_Injection/";
	static String owaspDom = "Lorg/owasp/benchmark/testcode/";
	static String julietDir = "/Users/ukoc/workspace/Juliet4J/src/testcases/CWE89_SQL_Injection/";
	static String owaspDir = "/Users/ukoc/Documents/workspace/owasp-1.1/src/main/java/org/owasp/benchmark/testcode/";
	static String owaspScopeFile = "data/scopeFiles/owaspdata.txt";

	public static void owaspSingleRun()
			throws IllegalArgumentException, IOException, CancelException, WalaException, InvalidClassFileException {
		String className = "Lorg/owasp/benchmark/testcode/BenchmarkTest09682";
		String funcDesc = "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V";
		String exclusionsFile = "data/OwaspSpecificExclusions.txt";
		WALASlicer slicer = new WALASlicer(owaspScopeFile, Files.readAllLines(Paths.get(exclusionsFile)));
		System.out.println(slicer.sliceOwasp(className, "doPost", funcDesc, "execute", 54));
	}

	public static void julietSingleRun()
			throws IllegalArgumentException, IOException, CancelException, WalaException, InvalidClassFileException {
		String className, funcDesc, functionName, callee, scopeFileName;
		className = "Ltestcases/CWE89_SQL_Injection/s04/CWE89_SQL_Injection__Property_executeBatch_75b";
		// testcases\/CWE89_SQL_Injection\/s01\/(?!(CWE89_SQL_Injection__Property_executeBatch_75)$).*
		funcDesc = "([B)V";
		callee = "addBatch";
		functionName = "goodG2BSink";
		scopeFileName = "data/juliet/CWE89_s04.txt";
		WALASlicer slicer = new WALASlicer(scopeFileName, Files.readAllLines(Paths.get(julietExclusionsFile)));
		String sliceJuliet = slicer.sliceJuliet(className, functionName, funcDesc, callee, 164);
		System.out.println(sliceJuliet);
	}

	public static void main(String[] args)
			throws IOException, IllegalArgumentException, CancelException, WalaException, InvalidClassFileException {
		if (args.length >= 1) {
			processWarningOwasp(args[0]);
		} else {
			// julietSingleRun();
			//runExp("/Users/ukoc/mangrove/phase2/lstm/data/java/CWE89JulietStatsBalanced.txt");
			runExp("/Users/ukoc/Documents/workspace/mangrove/mangrove/data/java/CWE89OwaspStatsAll.txt");
			// owaspSingleRun();
		}
	}

	public static void processWarningOwasp(String warning)
			throws IOException, FileNotFoundException, UnsupportedEncodingException, CancelException, WalaException,
			IllegalArgumentException, InvalidClassFileException {
		List<String> specificExclusions = Files.readAllLines(Paths.get(owaspExclusionsFile), StandardCharsets.UTF_8);
		String className, callee, exlcusion;
		String[] pair = warning.split(",");
		className = pair[0];
		int lineNumber = Integer.parseInt(pair[1]);
		String[] detail = pair[2].split(" ");
		callee = detail[0].substring(detail[0].indexOf('.') + 1);
		exlcusion = "org\\/owasp\\/benchmark\\/testcode\\/(?!(" + className + "(\\$(\\w*|\\d))*)$).*";
		specificExclusions.add(exlcusion);
		try {
			String[] fNameAndDesc = findEnclosingMethod(lineNumber, owaspDir + className + ".java",
					"Lorg/owasp/benchmark/testcode/");
			WALASlicer slicer = new WALASlicer(owaspScopeFile, specificExclusions);
			System.out.println("className:" + className + ", callee:" + callee + ", function:" + fNameAndDesc[0]
					+ ", desc:" + fNameAndDesc[1] + ", lineNumber:" + lineNumber);
			String sliceJuliet = slicer.sliceOwasp(owaspDom + className, fNameAndDesc[0], fNameAndDesc[1], callee,
					lineNumber);
			printToFile("/Users/ukoc/Documents/workspace/mangrove/mangrove/data/controlflowgraphs/owasp/" + className + pair[3] + ".txt",
				sliceJuliet);
		} catch (com.ibm.wala.util.debug.UnimplementedError e) {
			System.err.println(e.getLocalizedMessage());
		} catch (IllegalArgumentException e) {
			System.err.println(e.getLocalizedMessage());
		} finally {
			specificExclusions.remove(exlcusion);
		}
	}

	public static void processWarningJuliet(String warning)
			throws IOException, FileNotFoundException, UnsupportedEncodingException, CancelException, WalaException,
			IllegalArgumentException, InvalidClassFileException {
		List<String> specificExclusions = Files.readAllLines(Paths.get(julietExclusionsFile), StandardCharsets.UTF_8);
		String className, callee, subFolder, exlcusion;
		String[] pair = warning.split(",");
		String filePath = pair[0].substring(0, pair[0].length() - 5);
		int lineNumber = Integer.parseInt(pair[1]);
		String[] subFolderAndClassName = filePath.split("/");
		String[] warningDesc = pair[2].split(" ");
		callee = warningDesc[0].split("\\.")[1];
		subFolder = subFolderAndClassName[0];
		className = subFolderAndClassName[1];
		exlcusion = "testcases\\/CWE89_SQL_Injection\\/" + subFolder + "\\/(?!(" + className + ")$).*";
		boolean isDigit = Character.isDigit(filePath.charAt(filePath.length() - 1));
		if (!isDigit) {
			String substring = className.substring(0, className.length() - 1);
			exlcusion = "testcases\\/CWE89_SQL_Injection\\/" + subFolder + "\\/(?!(" + substring + "\\w(\\$\\w+)*)$).*";
		}
		specificExclusions.add(exlcusion);
		try {
			String[] fNameAndDesc = findEnclosingMethod(lineNumber, julietDir + pair[0],
					"Ltestcases/CWE89_SQL_Injection/" + subFolder + "/");
			String scopeFileName = "data/juliet/CWE89_" + subFolder + ".txt";
			System.out.println("className:" + className + ", callee:" + callee + ", function:" + fNameAndDesc[0]
					+ ", desc:" + fNameAndDesc[1] + ", lineNumber:" + lineNumber);
			WALASlicer slicer = new WALASlicer(scopeFileName, specificExclusions);
			String sliceJuliet = slicer.sliceJuliet(julietDom + filePath, fNameAndDesc[0], fNameAndDesc[1], callee,
					lineNumber);
			printToFile("/Users/ukoc/mangrove/phase2/slices/juliet/" + className + pair[3] + ".txt", sliceJuliet);
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

	public static void runExp(String warningFile)
			throws IOException, FileNotFoundException, UnsupportedEncodingException, CancelException, WalaException,
			IllegalArgumentException, InvalidClassFileException {
		List<String> warnings = Files.readAllLines(Paths.get(warningFile), utf8);
		for (String warning : warnings) {
			processWarningOwasp(warning);
			//processWarningJuliet(warning);
		}
	}

	public static String[] findEnclosingMethod(int lineNumber, String fileName, String subFolder) throws IOException {
		List<String> statements = Files.readAllLines(Paths.get(fileName), utf8);
		HashMap<String, String> typeMap = new HashMap<>();
		typeMap.put("void", "V");
		String prog = String.join("\n", statements);
		CompilationUnit cu = JavaParser.parse(prog);
		MethodDeclaration methodDeclaration = cu.getNodesByType(MethodDeclaration.class).stream()
				.filter(c -> (c.getBegin().get().line <= lineNumber && c.getEnd().get().line >= lineNumber)).findFirst()
				.get();
		System.out.println(methodDeclaration.getDeclarationAsString());
		String signature = "(";
		for (Parameter param : methodDeclaration.getParameters()) {
			signature += "Ljavax/servlet/http/" + param.getType() + ";";
		}
		signature += ")" + typeMap.get(methodDeclaration.getType().toString());
		System.out.println(signature);
		//cu.accept(astVisitor);//TODO
		String[] rslt = { methodDeclaration.getNameAsString(), signature };
		return rslt;
	}
}