package edu.umd.cs.mangrove.smutator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MutateCodePatterns {
	public static final Charset utf8 = StandardCharsets.UTF_8;

	public static void createMutants() {
		try {

			Mutator mutator = new Mutator(
					"/Users/ukoc/workspace-bench/CodePatternsTest/src/main/java/cs/umd/edu/mangrove/CodePatternsTest/", 400);
			mutator.mutateFunction("CodePattern.java", 29, "FP", "SQLi");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		createMutants();
	}

}