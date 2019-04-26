package edu.umd.cs.mangrove.smutator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.umd.cs.mangrove.util.SCAWarning;

public class MutateOwasp {
	public static final Charset utf8 = StandardCharsets.UTF_8;
	public static HashMap<String, Integer> warningLines = new HashMap<String, Integer>();
	public static HashMap<String, String> warningLabels = new HashMap<String, String>();

	public static void readWarningLabels(String warningsFile) throws IOException {
		List<String> warnings = Files.readAllLines(Paths.get(warningsFile), utf8);
		for (String warning : warnings) {
			String[] pair = warning.split(",");
			warningLabels.put(pair[0], pair[1].equals("TRUE") ? "truepositive" : "falsepositive");
		}
	}

	private static void mergeStats() {
		String file1 = "/Users/ukoc/mangrove/phase2/lstm/data/java/CWE89OwaspCategories.txt";
		String file2 = "/Users/ukoc/mangrove/phase2/lstm/data/java/CWE89OwaspStats.java";
		String fileout = "/Users/ukoc/mangrove/phase2/lstm/data/java/CWE89OwaspStatsAll.txt";

		try {
			readWarningLabels(file1);
			List<String> warnings = Files.readAllLines(Paths.get(file2), utf8);
			PrintWriter writer = new PrintWriter(fileout, "UTF-8");
			for (String warning : warnings) {
				String[] pair = warning.split(",");
				if (warningLabels.containsKey(pair[0])) {
					System.out.println(pair[0] + ": " + warningLabels.get(pair[0]));
					writer.println(warning + "," + warningLabels.get(pair[0]));
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<SCAWarning> initWarnings(String warningFile, String owaspDir) throws IOException {
		ArrayList<SCAWarning> scaWarnings = new ArrayList<SCAWarning>();
		List<String> warnings = Files.readAllLines(Paths.get(warningFile), utf8);
		for (String warning : warnings) {
			String[] pair = warning.split(",");
			scaWarnings
					.add(new SCAWarning(Integer.parseInt(pair[1]), owaspDir + pair[0] + ".java", "SQLi", pair[2], pair[3]));
			warningLabels.put(pair[0], pair[1]);
		}
		return scaWarnings;
	}

	public static void createMutants() {
		String warningStats = "/Users/ukoc/mangroveL/sa-lstm/data/java/CWE89OwaspAll.txt";
		String owaspDir = "/Users/ukoc/workspace/owasp-1.1/src/main/java/org/owasp/benchmark/testcode/";
		try {
			ArrayList<SCAWarning> scaWarnings = initWarnings(warningStats, owaspDir);
			// int counter = 0;
			for (SCAWarning scaWarning : scaWarnings) {
				Mutator mutator = new Mutator("/Users/ukoc/workspace/Mutants/src/main/java/org/owasp/benchmark/testcode/",
						6);
				mutator.mutateFunction(scaWarning.getFileName(), scaWarning.getLineNumber(), scaWarning.getLabel(),
						scaWarning.getDescription());
				// if (counter++ > 100) {
				// break;
				// }
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		mergeStats();
		// createMutants();
	}

}