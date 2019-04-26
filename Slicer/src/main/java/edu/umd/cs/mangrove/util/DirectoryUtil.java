package edu.umd.cs.mangrove.util;

public class DirectoryUtil {
	public static final String HOME_DIR = System.getProperty("user.home");
	public static final String PROJECT_DIR = System.getProperty("user.dir");
	public static final String WORKSPACE_DIR = PROJECT_DIR.substring(0, PROJECT_DIR.lastIndexOf("/") - 1);
	public static final String BENCH_DIR = HOME_DIR + "/workspace-bench/";
	public static final String BENCHDIR_ANDROID = HOME_DIR + "/bench/";
}