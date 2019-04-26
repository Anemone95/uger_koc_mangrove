package edu.umd.cs.mangrove.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

public class TaintConfigUtil {

	public static HashSet<String> taintedSources = new HashSet<String>();

	public static void loadTaintConfig() throws IOException, ParserConfigurationException, SAXException {
		File[] reports = new File(DirectoryUtil.PROJECT_DIR + "src/main/resources/taint-config/").listFiles();
		for (File r : reports) {
			if (r.isFile() && r.getName().endsWith(".txt")) {
				List<String> readLines = FileUtils.readLines(r);
				for (String line : readLines) {
					String[] split = line.split(":");
					if (split.length >= 2 && split[1].contains("TAINTED")) {
						String substring = split[0].substring(0, split[0].indexOf("("));
						//System.out.println(substring);
						taintedSources.add(substring);
					}
				}
			}
		}
	}

}
