package edu.umd.cs.mangrove.smutator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.observing.AstObserver;
import com.github.javaparser.ast.observing.AstObserverAdapter;
import com.github.javaparser.ast.observing.ObservableProperty;
import com.github.javaparser.ast.type.PrimitiveType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	@SuppressWarnings("deprecation")
	public void testApp() {
		String code = "class A { int f; void foo(int p) { return 'z'; }}";
		CompilationUnit cu = JavaParser.parse(code);
		final List<String> changes = new ArrayList<String>();
		AstObserver observer = new AstObserverAdapter() {
			@Override
			public void propertyChange(Node observedNode, ObservableProperty property, Object oldValue,
					Object newValue) {
				changes.add(String.format("%s.%s changed from %s to %s", observedNode.getClass().getSimpleName(),
						property.name().toLowerCase(), oldValue, newValue));
			}
		};
		cu.registerForSubtree(observer);
		
		assertEquals(Arrays.asList(), changes);

        cu.getClassByName("A").setName("MyCoolClass");
        assertEquals(Arrays.asList("ClassOrInterfaceDeclaration.name changed from A to MyCoolClass"), changes);

        cu.getClassByName("MyCoolClass").getFieldByName("f").setElementType(new PrimitiveType(PrimitiveType.Primitive.BOOLEAN));
        assertEquals(Arrays.asList("ClassOrInterfaceDeclaration.name changed from A to MyCoolClass",
                "FieldDeclaration.element_type changed from int to boolean"), changes);

        cu.getClassByName("MyCoolClass").getMethodsByName("foo").get(0).getParamByName("p").setName("myParam");
        assertEquals(Arrays.asList("ClassOrInterfaceDeclaration.name changed from A to MyCoolClass",
                "FieldDeclaration.element_type changed from int to boolean",
                "VariableDeclaratorId.name changed from p to myParam"), changes);
	}
}
