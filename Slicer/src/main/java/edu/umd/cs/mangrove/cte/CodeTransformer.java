package edu.umd.cs.mangrove.cte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.PrintUtil;

public class CodeTransformer {

	public static String process(String srcDir, String srcFile, BugReport report) throws IOException {
		String codeStr = String.join("\n", Files.readAllLines(Paths.get(srcDir + srcFile), StandardCharsets.UTF_8));
		CompilationUnit cu = JavaParser.parse(codeStr);

		List<Comment> allContainedComments = cu.getAllContainedComments();
		for (Comment comment : allContainedComments)
			comment.setContent("");

		ASTVisitor tv = new ASTVisitor(report);

		tv.visitBreadthFirst(cu);
		tv.processReportLines();
		inlineMethodCalls(cu, tv, report.getLinenumbers().get(srcDir + srcFile));//TODO test this
		processRemoves(cu, tv);
		removeIrrelevantFields(cu, report, tv);
		removeIrrelevantMethods(cu, report, findFields(cu), tv);
		removeUnusedVariables(tv.warningMethod, report, tv);
		inLineIntoCaller(cu, tv);
		renameVariables(cu, tv.renameMap);

		// tv.visitDepthFirstDebug(cu);

		String postprocess = PrintUtil.postprocess(cu);
		// PrintUtil.printToFile(srcFile.replace(srcDir, outDir), postprocess);

		return postprocess;
	}

	private static void inLineIntoCaller(CompilationUnit cu, ASTVisitor tv) {
		cu.getNodesByType(MethodCallExpr.class).stream()
				.filter(c -> c.getNameAsString().equals(tv.warningMethod.getNameAsString()) && c.getArguments().size() == 0)
				.forEach(c -> {
					ExpressionStmt expStmt = c.getAncestorOfType(ExpressionStmt.class);
					MethodDeclaration md = c.getAncestorOfType(MethodDeclaration.class);
					BlockStmt blockStmt = md.getBody().get();
					NodeList<Statement> statements = blockStmt.getStatements();
					for (int i = 0; i < statements.size(); i++) {
						Statement stmt = statements.get(i);
						if (stmt.hashCode() == expStmt.hashCode()) {
							stmt.remove();
							blockStmt.addStatement(i, tv.warningMethod.getBody().get());
							tv.warningMethod.remove();
							tv.warningMethod = md;
							tv.renameMap.put(md.getNameAsString(), "warningMethod");
							break;
						}
					}
				});
	}

	public static ArrayList<String> findFields(CompilationUnit cu) {
		ArrayList<String> fields = new ArrayList<String>();
		cu.getNodesByType(FieldDeclaration.class).stream().forEach(c -> {
			for (VariableDeclaratorId vdi : c.getNodesByType(VariableDeclaratorId.class))
				fields.add(vdi.getName().getIdentifier());
		});
		return fields;
	}

	public static void inlineMethodCalls(CompilationUnit cu, ASTVisitor tv, ArrayList<Integer> lineNumbers) {
		for (int i = 0; i < lineNumbers.size() - 1; i++) {
			List<MethodCallExpr> calls = tv.findMethodCallsInLines(cu, lineNumbers.get(i));
			for (MethodCallExpr call : calls) {
				MethodDeclaration md = tv.findMethodBody(cu, call);
				if (md == null || !md.getBody().isPresent()) {
					// callee is not defined in the project
					continue;
				}
				BlockStmt bs = (BlockStmt) md.getBody().get().clone();
				if (bs.getChildNodes().size() == 1) {
					// inline only if bs has just one child (e.g. just a return
					// stmt)
					Node parent = call.getParentNode().get();
					if (parent.getClass().equals(VariableDeclarator.class)) {
						Expression returnExp = bs.getNodesByType(ReturnStmt.class).get(0).getExpression().get();
						returnExp.setRange((new Range(parent.getBegin().get(), parent.getEnd().get())));
						((VariableDeclarator) parent).setInitializer(returnExp);
					} else if (parent.getClass().equals(ExpressionStmt.class)) {
						MethodDeclaration mdParent = parent.getAncestorOfType(MethodDeclaration.class);
						BlockStmt blockStmt = mdParent.getBody().get();
						NodeList<Statement> statements = blockStmt.getStatements();
						for (int j = 0; j < statements.size(); j++) {
							Statement stmt = statements.get(j);
							if (stmt.hashCode() == parent.hashCode()) {
								stmt.remove();
								blockStmt.addStatement(j, bs);
								break;
							}
						}
						if (md.equals(tv.warningMethod)) {
							tv.warningMethod = mdParent;
							tv.bugreport.setMethodName(mdParent.getNameAsString());
							tv.renameMap.put("data", "taintSrc"); // TODO
						}
					}
					call.getNodesByType(SimpleName.class).get(0).setIdentifier("removed");
					call.remove();
				} else if (call.getScope() != null) {
					// copy the declaration into src file
					ClassOrInterfaceDeclaration classDef = call.getAncestorOfType(ClassOrInterfaceDeclaration.class);
					Position begin = classDef.getEnd().get();
					int len = md.getEnd().get().line - md.getBegin().get().line;
					md.setRange(new Range(begin, new Position(begin.line + len, md.getBegin().get().column)));
					classDef.addMember(md);
					classDef.setRange(new Range(classDef.getBegin().get(),
							new Position(md.getEnd().get().line + 1, classDef.getBegin().get().column)));
					for (Node node : call.getChildNodes()) {
						if (node.getClass().equals(NameExpr.class)) {
							NameExpr ne = (NameExpr) node;
							if (ne.getNameAsString().equals(call.getScope().toString())) {
								ne.getNodesByType(SimpleName.class).get(0).setIdentifier("removed");
								ne.remove();
								break;
							}
						}
					}
				}
			}
		}
	}

	private static void processRemoves(CompilationUnit cu, ASTVisitor tv) {
		for (Node node : tv.nodesToRemove) {
			node.getNodesByType(SimpleName.class).stream().forEach(c -> c.setIdentifier("removed"));
			node.remove();
		}
	}

	/**
	 * This method removes class fields which are not directly used in the
	 * warning method
	 */
	public static void removeIrrelevantFields(CompilationUnit cu, BugReport bugreport, ASTVisitor tv) {
		cu.getNodesByType(FieldDeclaration.class).stream().forEach(c -> {
			int size = c.getVariables().size();
			int removed = 0;
			for (int i = 0; i < size; i++) {
				if (!tv.isUsedByWM(c.getVariable(i).getIdentifierAsString(), SimpleName.class)) {
					c.getVariable(i).setIdentifier("removed");
					c.getVariable(i).remove();
					removed++;
				}
			}
			if (size == removed) {
				c.remove();
			}
		});
	}

	public static void removeIrrelevantMethods(CompilationUnit cu, BugReport report, ArrayList<String> fields,
			ASTVisitor tv) {
		cu.getNodesByType(MethodDeclaration.class).stream().filter(c -> {
			if (c.getNameAsString().equals(report.getMethodName()) || tv.isCalledByWM(c.getNameAsString())) {
				return false;
			} else for (SimpleName sn : c.getNodesByType(SimpleName.class)) {
				if (fields.contains(sn.getIdentifier())) return false;
			}
			return true;
		}).forEach(c -> c.remove());
	}

	public static void removeUnusedVariables(Node cu, BugReport report, ASTVisitor tv) {
		cu.getNodesByType(VariableDeclarationExpr.class).stream()
				.filter(c -> c.getAncestorOfType(ExpressionStmt.class) != null).forEach(c -> {
					MethodDeclaration m = c.getAncestorOfType(MethodDeclaration.class);
					int line = c.getBegin().isPresent() ? c.getBegin().get().line : 9999;
					NodeList<VariableDeclarator> variables = c.getVariables();
					int size = variables.size();
					int removed = 0;
					for (int i = 0; i < size; i++) {
						if (!report.getLinenumbers().get(report.getClassName()).contains(line)//TODO test this
								&& tv.isUnusedVariable(m, c.getVariable(i).getIdentifier().getNameAsString(), line)) {
							c.getVariable(i).setIdentifier("removed");
							c.getVariable(i).remove();
							removed++;
						}
					}
					if (size == removed) {
						c.getAncestorOfType(ExpressionStmt.class).remove();
					}
				});
	}

	// rename variables relevant to the bug report
	public static void renameVariables(Node cu, HashMap<String, String> renameMap) {
		cu.getNodesByType(SimpleName.class).stream().filter(c -> renameMap.containsKey(c.getIdentifier())).forEach(c -> {
			c.setIdentifier(renameMap.get(c.getIdentifier()));
		});
	}
}