package edu.umd.cs.mangrove.smutator;

import java.util.EnumSet;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;

public class CuPrinter {

	public static final VoidType VOID_TYPE = new VoidType();

	public static void main(String[] args) throws Exception {
		// creates an input stream for the file to be parsed
		// FileInputStream in = new FileInputStream("src/test/java/com/mutator/SMutator/SpoonTests.java");
		// creates the compilation unit
		CompilationUnit cu = createCU();
		List<Node> childNodes = cu.getChildNodes();
		for (Node node : childNodes) {
			System.out.println("Node: " + node.getNodeLists() + " start :" + node.getBegin().toString());
		}
		// parse the file
		// CompilationUnit cu = JavaParser.parse(in);
		// prints the resulting compilation unit to default system output
		List<NodeList<?>> nodeLists = cu.getNodeLists();
		for (NodeList<?> nodeList : nodeLists) {
			for (Node node : nodeList) {
				// System.out.println(node.toString());
			}
		}
	}

	/**
	 * creates the compilation unit
	 */
	private static CompilationUnit createCU() {
		CompilationUnit cu = new CompilationUnit();
		// set the package
		cu.setPackage(new PackageDeclaration(Name.parse("java.parser.test")));

		// or a shortcut
		cu.setPackageName("com.mutator.SMutator");

		// create the type declaration
		ClassOrInterfaceDeclaration type = cu.addClass("GeneratedClass");

		// create a method
		EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
		MethodDeclaration method = new MethodDeclaration(modifiers, VOID_TYPE, "main");
		modifiers.add(Modifier.STATIC);
		method.setModifiers(modifiers);
		type.addMember(method);

		// or a shortcut
		MethodDeclaration main2 = type.addMethod("main2", Modifier.PUBLIC, Modifier.STATIC);

		// add a parameter to the method
		Parameter param = new Parameter(new ClassOrInterfaceType("String"), "args");
		param.setVarArgs(true);
		method.addParameter(param);

		// or a shortcut
		main2.addAndGetParameter(String.class, "args").setVarArgs(true);

		// add a body to the method
		BlockStmt block = new BlockStmt();
		method.setBody(block);

		// add a statement do the method body
		NameExpr clazz = new NameExpr("System");
		FieldAccessExpr field = new FieldAccessExpr(clazz, "out");
		MethodCallExpr call = new MethodCallExpr(field, "println");
		call.addArgument(new StringLiteralExpr("Hello World!"));
		block.addStatement(call);

		return cu;
	}
}