package edu.umd.cs.mangrove.smutator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

public class Mutator {
	private static final int HUNDRED = 100;
	private static final int BOOL = 0;
	public static final Charset utf8 = StandardCharsets.UTF_8;

	private static final int emptystmt = 0;
	private static final int printStmt = 1;
	private static final int classFieldDecStmt = 2;
	private static final int localVariableDecStmt = 3;
	private static final int incrementStmt = 4;
	private static final int deadCode = 5;
	private static int mutationCount = 6;

	private Random random = new Random();
	private CodeGenerator codeGenerator = new CodeGenerator();
	private int classFirstLine = 0;
	private String mutantDir;

	public void findFirstLine(List<String> statementList) {
		for (String string : statementList) {
			if (string.contains("class ")) {
				break;
			} else classFirstLine++;
		}
	}

	public Mutator(String mutantDir, int mutantCount) {
		this.mutantDir = mutantDir;
		mutationCount = mutantCount;
	}

	public CompilationUnit getCompilationUnit(String source) {
		return JavaParser.parse(source);
	}

	public String createSideEffectFreeStmt() {
		String stmt = "System.out.println(\" STRING \");";
		if (random.nextInt(HUNDRED) % 2 == 0)
			stmt = "" + codeGenerator.types[BOOL] + " x" + random.nextInt(HUNDRED) + " = false;";
		return stmt;
	}

	public void mutateClass(String sourceFile, int warningLine, String label, String description) throws IOException {
		String fileName = sourceFile.substring(sourceFile.lastIndexOf("/"));
		List<String> statements = Files.readAllLines(Paths.get(sourceFile), utf8);
		int LoC = statements.size();
		String prog = String.join("\n", statements);
		findFirstLine(statements);
		CompilationUnit cu = getCompilationUnit(prog);
		MyAstVisitor astVisitor = new MyAstVisitor(cu, true, warningLine);
		//cu.accept(astVisitor);TODO

		int fCount = astVisitor.indexes.size();

		int index = 0;
		String newStmt = ";";
		int mutantCount = random.nextInt(mutationCount - 3) + 3;
		// System.out.println("mutantCount:" + mutantCount);
		while (mutantCount-- > 0) {
			int rMutation = random.nextInt(mutationCount);
			switch (rMutation) {
			case emptystmt:
				index = classFirstLine + random.nextInt(LoC - classFirstLine) + 1;
				newStmt = ";";
				break;
			case printStmt:
				index = astVisitor.indexes.get(random.nextInt(fCount)) + 1;
				newStmt = "System.out.println(\" STRING \")";
			case classFieldDecStmt:
				index = classFirstLine + 1;
				newStmt = codeGenerator.accessModifiers[random.nextInt(codeGenerator.accessModifiers.length)] + " "
						+ codeGenerator.types[random.nextInt(codeGenerator.types.length)] + " x" + random.nextInt(HUNDRED)
						+ ";";
				break;
			case localVariableDecStmt:
				index = astVisitor.indexes.get(random.nextInt(fCount)) + 1;
				newStmt = codeGenerator.types[random.nextInt(codeGenerator.types.length)] + " x" + random.nextInt(HUNDRED)
						+ ";";
				break;
			case deadCode:
				index = astVisitor.indexes.get(random.nextInt(fCount)) + 1;
				newStmt = "if(!true) { " + createSideEffectFreeStmt() + " } ";
				break;
			case incrementStmt:
				index = astVisitor.indexes.get(random.nextInt(fCount)) + 1;
				int xxx = random.nextInt(HUNDRED);
				newStmt = codeGenerator.types[random.nextInt(codeGenerator.types.length - 3) + 3] + " x" + xxx + "=0;"
						+ " x" + xxx + "++;";
				break;
			default:
				break;
			}
			saveMutant(fileName, warningLine, statements, newStmt, index, mutantCount, label, description);
		}
	}

	public void mutateFunction(String sourceFile, int warningLine, String label, String description) throws IOException {
		String fileName = sourceFile.contains("/") ? sourceFile.substring(sourceFile.lastIndexOf("/")) : sourceFile;
		// System.out.println(fileName);
		List<String> statements = Files.readAllLines(Paths.get(mutantDir + sourceFile), utf8);
		String prog = String.join("\n", statements);
		findFirstLine(statements);
		CompilationUnit cu = getCompilationUnit(prog);
		MethodDeclaration doPostMethod = cu.getNodesByType(MethodDeclaration.class).stream()
				.filter(m -> m.getNameAsString().equals("doPost")).findFirst().get();
		Position startPosition = doPostMethod.getBody().get().getBegin().get();
		Position endPosition = doPostMethod.getBody().get().getEnd().get();

		MyAstVisitor astVisitor = new MyAstVisitor(cu, label == null, warningLine);
		saveMutant(fileName, warningLine, statements, "", 0, 0, label, description);
		Collections.sort(astVisitor.indexes);
		String newStmt = ";";
		int mutantCount = mutationCount;//random.nextInt(mutationCount - 3) + 7;
		while (--mutantCount > 0) {
			int index = startPosition.line + 1 + random.nextInt(endPosition.line - startPosition.line - 1);
			int rMutation = random.nextInt(mutationCount);
			switch (rMutation) {
			case emptystmt:
				newStmt = ";";
				break;
			case printStmt:
				newStmt = "System.out.println(\" STRING \")";
			case classFieldDecStmt:
				newStmt = codeGenerator.types[random.nextInt(codeGenerator.types.length)] + " x" + random.nextInt(HUNDRED)
						+ ";";
				break;
			case localVariableDecStmt:
				newStmt = codeGenerator.types[random.nextInt(codeGenerator.types.length)] + " x" + random.nextInt(HUNDRED)
						+ ";";
				break;
			case deadCode:
				newStmt = "if(!true) { " + createSideEffectFreeStmt() + " } ";
				break;
			case incrementStmt:
				int xxx = random.nextInt(HUNDRED);
				newStmt = codeGenerator.types[random.nextInt(codeGenerator.types.length - 3) + 3] + " x" + xxx + "=0;"
						+ " x" + xxx + "++;";
				break;
			default:
				break;
			}
			saveMutant(fileName, warningLine, statements, newStmt, index, mutantCount, label, description);
		}
	}

	public void saveMutant(String fileName, int warningLine, List<String> statements, String stmt, int index,
			int mutantid, String label, String description) throws FileNotFoundException, UnsupportedEncodingException {
		int line = 0;
		String mutantFile = new String(fileName);
		while ((new File(mutantFile)).isFile()) {
			mutantid++;
			mutantFile = fileName.replaceAll(".java", "" + mutantid + ".java");
		}
		PrintWriter writer = new PrintWriter(mutantDir + mutantFile, "UTF-8");
		for (String string : statements) {
			if (line++ == index) writer.println(stmt);
			if (line == warningLine) writer.println("WARNINGLINE " + description);
			writer.println(string);
		}
		writer.println(label);
		writer.close();
	}
}

class CodeGenerator {
	String[] accessModifiers = { "", "public", "protected", "private" };
	String[] types = { "boolean", "byte", "char", "short", "int", "long", "float", "double" };
}

class MyAstVisitor {//TODO
	List<Integer> indexes = new ArrayList<Integer>();
	CompilationUnit cu;
	HashMap<Integer, Integer> functionPositions = new HashMap<Integer, Integer>();
	boolean findLabel;
	String label;
	int warningLine;

	public MyAstVisitor(CompilationUnit cu, boolean findLabel, int warningLine) {
		this.cu = cu;
		this.findLabel = findLabel;
		this.warningLine = warningLine;
	}

	public String getLabel() {
		return label;
	}

	public boolean visit(MethodDeclaration node) {
		return true;
	}
}