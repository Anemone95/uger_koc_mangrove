## An Empirical Assessment of Machine Learning Approaches for Triaging Reports of a Java Static Analysis Tool
###Ugur Koc, Shiyi Wei, Jeffrey Foster, Marine Carpuat and Adam   Porter

## Directories 

- **hef,bow,lstm**, and **ggnn** directories contain the machine learning scripts, datasets, and dictinories. `run_exp.sh` scripts under these directories show the usage of that ML code. And the data directories contain sample input files to the ML script

- **parsers** directory contain the scripts that process the raw slice files to create the ML input files.

- **data** directory contains the raw program slices, FindSecBugs reports, and labels (classification) files.

- **analyze** diretory contains the R scripts used to create the tables in te paper.
- **Slicer** diretory contains the java program that computes the backward slices (and some other useful artifacts) from programs source code.

## Computing the Program Slices
We used Joana and WALA libraries for computing the backward slice. You can obtain Joana from https://github.com/joana-team/joana. Follow the for compilation instructions.
Import the slicer project into the Eclipse IDE. 
Next, obtain the sources of the target files from their repositories (use the versions specified in the paper).
You will need to update some paths in `pom.xml` file and the some java files to point to the target programs and the Joana jars.
After all the paths are fixed, running `edu.umd.cs.mangrove.slicing.JoanaSlicingTest.java` as Java Application should produce the backward slice the example program from the paper (BenchmarkTest06570).


## Creating the FindSecBugs Reports
First, install the SputBugs plugin to Ecplise. Obtain FindSecBugs 1.4.6 source and compile it to get the jar file (using mvn).
In Eclipse preferences, open SpotBugs configuration, add FindSecBugs jar file under the 'Plugins and misc. Settings' tab and only select following detectors under the 'Detector Configuration' tab. This completes the confiugration of the SA tool.

- CommandInjectionDetector
- CrlfLogInjectionDetector
- CustomInjectionDetector
- LdapInjectionDetector
- ScriptInjectionDetector
- SqlInjectionDetector
- XPathInjectionDetector
- XssServletDetector
- XssJsp
- XssRequestWrapper

Note that all of these detectors are from the Find Security Bugs provider.

Next, import the target programs into Eclipse and you can run FindSecBugs through the right-click menu on any program.

## Contact

Please reach out to Ugur Koc at ukoc@cs.umd.edu for questions or futher assistance.