package edu.umd.cs.mangrove.cte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.observing.AstObserver;
import com.github.javaparser.ast.observing.AstObserverAdapter;
import com.github.javaparser.ast.observing.ObservableProperty;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class JP_ASTTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *           name of the test case
	 */
	public JP_ASTTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(JP_ASTTest.class);
	}

	public void testApp() throws IOException {
		List<String> statements = Files.readAllLines(
				Paths.get("/Users/ukoc/owasp-1.1/src/main/java/org/owasp/benchmark/testcode/BenchmarkTest00321.java"),
				StandardCharsets.UTF_8);
		String code = String.join("\n", statements);
		CompilationUnit cu = JavaParser.parse(code);
		final List<String> changes = new ArrayList<String>();
		AstObserver observer = new AstObserverAdapter() {
			@Override
			public void propertyChange(Node observedNode, ObservableProperty property, Object oldValue, Object newValue) {

				changes.add(String.format("%s.%s changed from %s to %s", observedNode.getClass().getSimpleName(),
						property.name().toLowerCase(), oldValue, newValue));
			}
		};
		cu.registerForSubtree(observer);

		assertEquals(Arrays.asList(), changes);

		cu.getClassByName("BenchmarkTest00321").setName("MyCoolClass");

		cu.getClassByName("MyCoolClass").getMethodsByName("doPost").get(0).getParamByName("request").setName("myParam");

		//System.out.println(cu.toString());
	}
}
