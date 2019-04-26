package edu.umd.cs.mangrove.slicing;

public class TestClassA {
	private static final String finalStr = "AAB";

	public static void main(String[] args) {
		if (finalStr.equals("AAA")) {
			foo(args[0]);
		} else {
			String dummStr = "BBB";
			bar(dummStr);
		}
	}

	private static void bar(String dummStr) {
		foo(dummStr);
	}

	private static void foo(String dummStr) {
		String testStr = "Hi" + dummStr;
		System.out.println(testStr);
	}
}
