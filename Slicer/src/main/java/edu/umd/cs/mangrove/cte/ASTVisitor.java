package edu.umd.cs.mangrove.cte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.imports.ImportDeclaration;
import com.github.javaparser.ast.imports.SingleTypeImportDeclaration;
import com.github.javaparser.ast.imports.TypeImportOnDemandDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.TreeVisitor;

import edu.umd.cs.mangrove.util.BugReport;

class ASTVisitor extends TreeVisitor {
	ArrayList<Class<?>> toRemoveTypes;
	ArrayList<Node> nodesToRemove;
	HashMap<Integer, Expression> reportedNodes;
	HashMap<String, String> renameMap;
	BugReport bugreport;
	MethodDeclaration warningMethod;

	ASTVisitor(BugReport report) {
		toRemoveTypes = new ArrayList<Class<?>>();
		nodesToRemove = new ArrayList<Node>();
		this.bugreport = report;
		renameMap = new HashMap<String, String>();
		reportedNodes = new HashMap<Integer, Expression>();
		toRemoveTypes.add(AnnotationExpr.class);
		toRemoveTypes.add(SingleMemberAnnotationExpr.class);
		toRemoveTypes.add(MarkerAnnotationExpr.class);
		toRemoveTypes.add(PackageDeclaration.class);
		toRemoveTypes.add(ImportDeclaration.class);
		toRemoveTypes.add(SingleTypeImportDeclaration.class);
		toRemoveTypes.add(CatchClause.class);
		toRemoveTypes.add(TypeImportOnDemandDeclaration.class);
		toRemoveTypes.add(Comment.class);
		toRemoveTypes.add(BlockComment.class);
		toRemoveTypes.add(JavadocComment.class);
		toRemoveTypes.add(LineComment.class);
		renameMap.put(bugreport.getInjectableMethod(), "injectableMethod");
		renameMap.put(bugreport.getMethodName(), "warningMethod");
		renameMap.put(bugreport.getParamTaint(), "paramTaint");
	}

	void processReportLines() {

		Expression taintSrcNode = reportedNodes.get(bugreport.getLinenumbers().get(0));
		renameMap.put(taintSrcNode.getNodesByType(SimpleName.class).get(0).getIdentifier(), "taintSrc");

		int line = bugreport.getLinenumbers().get(bugreport.getLinenumbers().size() - 1).get(0);//TODO
		createParamTaint(line, (Expression) reportedNodes.get(line));

		// process each reported line except first
		for (int i = 1; i < bugreport.getLinenumbers().size(); i++) {
			Expression node = reportedNodes.get(bugreport.getLinenumbers().get(i));
			node.getNodesByType(SimpleName.class).stream().forEach(c -> {
				String name = c.getIdentifier();
				if (!renameMap.containsKey(name)) {
					for (Entry<String, String> entry : bugreport.getTaintFrame().entrySet()) {
						if (entry.getValue().contains(name)) {
							renameMap.put(name, entry.getKey());
							break;
						}
					}
				}
			});
		}
	}

	@Override
	public void process(Node node) {
		ArrayList<Integer> linenumbers = bugreport.getLinenumbers().get(bugreport.getClassName() + ".java");//TODO test this
		int indexOf = linenumbers.indexOf(node.getBegin().get().line);
		Class<? extends Node> nodeType = node.getClass();
		if (indexOf >= 0 && !reportedNodes.containsKey(linenumbers.get(indexOf))) {
			if (nodeType.equals(ExpressionStmt.class)) {
				reportedNodes.put(linenumbers.get(indexOf), ((ExpressionStmt) node).getExpression());
			} else if (nodeType.equals(SimpleName.class)) {
				ExpressionStmt ancestorOfType = node.getAncestorOfType(ExpressionStmt.class);
				reportedNodes.put(linenumbers.get(indexOf), ancestorOfType.getExpression());
			} else if (nodeType.equals(IfStmt.class)) {
				reportedNodes.put(linenumbers.get(indexOf), ((IfStmt) node).getCondition());
			}
		}
		if (nodeType.equals(MethodDeclaration.class)
				&& ((MethodDeclaration) node).getNameAsString().equals(bugreport.getMethodName())) {
			warningMethod = (MethodDeclaration) node;
		} else if (toRemoveTypes.contains(nodeType)) {
			nodesToRemove.add(node);
		} else if (nodeType.equals(ClassOrInterfaceType.class) || nodeType.equals(FieldAccessExpr.class)) {
			for (Node child : node.getChildNodes())
				nodesToRemove.add(child);
		} else if (warningMethod != null && warningMethod.containsWithin(node) && node.getBegin().isPresent()
				&& node.getBegin().get().line > linenumbers.get(linenumbers.size() - 1)) {
			// removing the things come after the warning
			nodesToRemove.add(node);
		} else if (nodeType.equals(TryStmt.class)) {
			TryStmt ts = (TryStmt) node;
			if (ts.getFinallyBlock().isPresent()) {
				nodesToRemove.add(ts.getFinallyBlock().get());
			}
		} else if (nodeType.equals(StringLiteralExpr.class)) {
			StringLiteralExpr sle = (StringLiteralExpr) node;
			if (sle.getValue().length() > 0) {
				sle.setValue("STRING");
			}
		}
		if (node.hasComment()) {
			node.getComment().setContent("");
			nodesToRemove.add(node.getComment());
		}
	}

	public List<MethodCallExpr> findMethodCallsInLines(Node node, Integer lineNumber) {
		Queue<Node> queue = new LinkedList<>();
		queue.offer(node);
		while (queue.size() > 0) {
			Node head = queue.peek();
			for (Node child : head.getChildNodes()) {
				queue.offer(child);
			}
			Node n = queue.poll();
			if (n.getBegin().isPresent()
					&& n.getBegin().get().line == lineNumber) { return n.getNodesByType(MethodCallExpr.class);

			}
		}
		return null;
	}

	public VariableDeclarationExpr getDeclaration(Node node, String name) {
		Queue<Node> queue = new LinkedList<>();
		queue.offer(node);
		while (queue.size() > 0) {
			Node head = queue.peek();
			for (Node child : head.getChildNodes()) {
				queue.offer(child);
			}
			Node n = queue.poll();
			if (n.getClass().equals(VariableDeclarationExpr.class)) {
				VariableDeclarationExpr vde = (VariableDeclarationExpr) n;
				List<VariableDeclaratorId> ids = vde.getNodesByType(VariableDeclaratorId.class);
				for (VariableDeclaratorId id : ids) {
					if (id.getNameAsString().equals(name)) { return (VariableDeclarationExpr) n; }
				}
			}
		}
		return null;

	}

	public MethodDeclaration getMethodDeclaration(Node node, String name) {
		Queue<Node> queue = new LinkedList<>();
		queue.offer(node);
		while (queue.size() > 0) {
			Node head = queue.peek();
			for (Node child : head.getChildNodes()) {
				queue.offer(child);
			}
			Node n = queue.poll();
			if (n.getClass().equals(MethodDeclaration.class)
					&& ((MethodDeclaration) n).getNameAsString().equals(name)) { return (MethodDeclaration) n.clone(); }
		}
		return null;
	}

	public String getImportString(Node node, String name) {
		Queue<Node> queue = new LinkedList<>();
		queue.offer(node);
		while (queue.size() > 0) {
			Node head = queue.peek();
			for (Node child : head.getChildNodes()) {
				queue.offer(child);
			}
			Node n = queue.poll();
			if (n.getClass().toString().contains("ImportDeclaration")
					&& n.toString().contains("." + name)) { return ((ImportDeclaration) n)
							.getNodesByType(ClassOrInterfaceType.class).get(0).getNameAsString(); }
		}
		return null;

	}

	@SuppressWarnings("deprecation")
	public MethodDeclaration findMethodBody(CompilationUnit cu, MethodCallExpr call) {
		CompilationUnit cuLocal = cu;
		if (call.getScope() != null) {
			VariableDeclarationExpr declaration = getDeclaration(cu, call.getScope().toString());

			// String srcFile = "../owasp-1.1/src/main/java/";
			String srcFile = "../Juliet4J/src/";
			List<ObjectCreationExpr> newStmts;
			if (declaration != null) {
				newStmts = declaration.getNodesByType(ObjectCreationExpr.class);
				if (!newStmts.isEmpty()) {
					String typeStr = newStmts.get(0).getNodesByType(ClassOrInterfaceType.class).get(0).toString();
					String importString = getImportString(cu, typeStr);
					srcFile += ((typeStr.contains(".") || importString == null) ? typeStr.replace('.', '/')
							: importString.replace('.', '/')) + ".java";
				} else {
					srcFile += declaration.getElementType().toString().replace('.', '/') + ".java";
				}
			} else {
				newStmts = cu.getNodesByType(ObjectCreationExpr.class);
				if (!newStmts.isEmpty()) {
					String typeStr = newStmts.get(0).getNodesByType(ClassOrInterfaceType.class).get(0).toString();
					String importString = getImportString(cu, typeStr);
					if (importString == null) { return null; }
					srcFile += (typeStr.contains(".") ? typeStr.replace('.', '/') : importString.replace('.', '/'))
							+ ".java";
				} else {
					return null;
				}
			}
			try {
				cuLocal = JavaParser
						.parse(String.join("\n", Files.readAllLines(Paths.get(srcFile), StandardCharsets.UTF_8)));
			} catch (IOException e) {
				// System.err.println(srcFile + " is not declared in this
				// project");
				return null;
			}
		}
		return getMethodDeclaration(cuLocal, call.getNameAsString());
	}

	public boolean isCalledByWM(String calle) {
		for (MethodCallExpr call : warningMethod.getNodesByType(MethodCallExpr.class)) {
			if (call.getNameAsString().contains(calle)) return true;
		}
		return false;
	}

	public boolean isUnusedVariable(Node node, String vdi, int declarationLine) {
		return !node.getNodesByType(SimpleName.class).stream()
				.anyMatch(c -> c.getIdentifier().equals(vdi) && c.getBegin().get().line > declarationLine);
	}

	public boolean isUsedByWM(String name, Class<SimpleName> clazz) {
		for (SimpleName n : warningMethod.getNodesByType(clazz)) {
			if (n.getIdentifier().equals(name)) return true;
		}
		return false;
	}

	public VariableDeclarationExpr createParamTaint(int line, Expression expression) {
		BinaryExpr binaryExpr = expression.getNodesByType(BinaryExpr.class).get(0);
		VariableDeclarationExpr newVde = new VariableDeclarationExpr(new ClassOrInterfaceType("String"),
				new VariableDeclarator("paramTaint", (BinaryExpr) binaryExpr.clone()));
		int col = expression.getBegin().get().column;
		newVde.setRange(new Range(new Position(line, col), new Position(line, col + newVde.toString().length())));
		addNode(expression, binaryExpr, newVde);
		return newVde;
	}

	private void addNode(Expression expression, BinaryExpr binaryExpr, VariableDeclarationExpr newVde) {
		BlockStmt parentBlock = expression.getAncestorOfType(BlockStmt.class);
		NodeList<Statement> statements = parentBlock.getStatements();
		for (int i = 0; i < statements.size(); i++) {
			Statement stmt = statements.get(i);
			if (stmt.getBegin().get().line == expression.getBegin().get().line) {
				parentBlock.addStatement(i, newVde);
				newVde.setParentNode(parentBlock);
				parentBlock.setAsParentNodeOf(newVde);
				binaryExpr.remove();
				break;
			}
		}
	}

	public void visitDepthFirstDebug(Node node) {
		int depth = depth(node);
		String intend = "|";
		for (int i = 1; i < depth; i++)
			intend += "-";
		if (depth < 60) {
			System.out.println(intend + "Kids:" + node.getChildNodes().size() + " , line" + node.getBegin().get().line
					+ ", " + node.getClass() + ": " + node.toString().replaceAll("\n", " "));
		}
		for (Node child : node.getChildNodes())
			visitDepthFirstDebug(child);
	}

	private int depth(Node node) {
		int result = 1;
		Optional<Node> tmp = node.getParentNode();
		while (tmp.isPresent()) {
			tmp = tmp.get().getParentNode();
			result++;
		}
		return result;
	}
}