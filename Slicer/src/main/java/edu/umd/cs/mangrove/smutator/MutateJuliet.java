package edu.umd.cs.mangrove.smutator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.ibm.wala.cast.ipa.callgraph.AstSSAPropagationCallGraphBuilder.AstInterestingVisitor;

public class MutateJuliet {
	public static String warningStats = "/Users/ukoc/mangroveL/sa-lstm/data/java/CWE89JulietStatsBalanced.txt";
	public static String julietDir = "/Users/ukoc/workspace/Juliet4J/src/testcases/CWE89_SQL_Injection/";
	public static String mutantDir = "/Users/ukoc/workspace/Juliet4J/mutants/";

	public void createMutants() {
		try {
			List<String> warnings = Files.readAllLines(Paths.get(warningStats), StandardCharsets.UTF_8);
			for (String warning : warnings) {
				// System.out.print(warning + ",");
				String[] pair = warning.split(",");
				Mutator mutator = new Mutator(mutantDir, 6);
				int lineNumber = Integer.parseInt(pair[1]);
				mutator.mutateFunction(julietDir + pair[0], lineNumber, pair[3], pair[2]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MutateJuliet julietHandler = new MutateJuliet();
		julietHandler.createMutants();
	}

	public void printDP(String mutantFile, int warningLine, String label) throws IOException {
		String prog = new String(Files.readAllBytes(Paths.get(mutantFile)), StandardCharsets.UTF_8);
		CompilationUnit cu = getCompilationUnit(prog);
		MyAstFlatten astVisitor = new MyAstFlatten(0);
		//cu.accept(astVisitor); TODO

		// System.out.println(cu.toString());
	}

	public CompilationUnit getCompilationUnit(String source) {
		return JavaParser.parse(source);
	}
}

class MyAstFlatten extends AstInterestingVisitor {
	public MyAstFlatten(int vn) {
		super(vn);
		// TODO Auto-generated constructor stub
	}

	List<Integer> indexes = new ArrayList<Integer>();
	CompilationUnit cu;
	HashMap<Integer, Integer> functionPositions = new HashMap<Integer, Integer>();
	String label;
	int warningLine;

	public boolean visit(MethodDeclaration node) {
		return true;
	}
}