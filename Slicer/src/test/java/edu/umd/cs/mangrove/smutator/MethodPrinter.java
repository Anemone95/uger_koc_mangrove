package edu.umd.cs.mangrove.smutator;

import java.io.FileInputStream;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.ModifierVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

public class MethodPrinter {
	public static void main(String[] args) throws Exception {
		// creates an input stream for the file to be parsed
		FileInputStream in = new FileInputStream("src/test/java/com/mutator/SMutator/SpoonTests.java");

		// parse it
		CompilationUnit cu = JavaParser.parse(in);
		new MethodVisitor().visit(cu, null);
		System.out.println(cu.toString());
		// bfs(cu, 0);
	}

	static void bfs(Node n, int level) {
		List<Node> childNodes = n.getChildNodes();
		String indentation = "";
		for (int i = 0; i < level; i++)
			indentation += " ";
		if (childNodes.size() < 2) {
			System.out.println(indentation + n.toString(new PrettyPrinterConfiguration()));
		}
		for (Node node : childNodes)
			bfs(node, level + 1);
	}

	/**
	 * Simple visitor implementation for visiting MethodDeclaration nodes.
	 */
	private static class MethodVisitor extends ModifierVisitorAdapter<Void> {
		@Override
		public Node visit(MethodDeclaration n, Void args) {
			/*
			 * here you can access the attributes of the method. this method
			 * will be called for all methods in this CompilationUnit, including
			 * inner class methods
			 */
			// change the name of the method to upper case
			n.setName(n.getNameAsString().toUpperCase());

			// add a new parameter to the method
			n.addParameter("int", "value");
			System.out.println(n.toString());
			return (Node) super.visit(n, args);
		}

		@Override
		public Node visit(VariableDeclarator declarator, Void args) {
			if (declarator.getIdentifierAsString().equals("a")
					// the initializer is optional, first check if there is one
					&& declarator.getInitializer().isPresent()) {
				Expression expression = declarator.getInitializer().get();
				// We're looking for a literal integer.
				if (expression instanceof IntegerLiteralExpr) {
					// We found one. Is it literal integer 20?
					if (((IntegerLiteralExpr) expression).getValue().equals("20")) {
						// Returning null means "remove this VariableDeclarator"
						return null;
					}
				}
			}

			return declarator;
		}

		@Override
		public Node visit(ClassOrInterfaceDeclaration declarator, Void args) {
			BodyDeclaration<VariableDeclarator> decl = new BodyDeclaration<VariableDeclarator>() {

				public <A> void accept(VoidVisitor<A> v, A arg) {
				}

				public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
					return null;
				}
			};
			declarator.addMember(decl);
			return declarator;
		}
	}
}