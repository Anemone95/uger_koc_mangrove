## Direectories 

- hef,bow,lstm, and ggnn directories contain the machine learning scripts, datasets, and dictinories.
`run_exp.sh` scripts under these directories show the usage of that ML code. And the data directories contain sample input files to the ML script

- parsers directory contain the scripts that process the raw slice files to create the ML input files.

- data directory contain the raw program slices, FindSEcBugs reports and labels (classification) files.

-analyze diretory contain the R scripts used to create the tables in te paper.

## Computing the Program Slices
To create the raw slice files from the target program, it is necessary to obtain Joana and WALA libraries.
Next, obtain the sources of the target files from their repositories (use the versions specified in the paper).

## Creating the FindSecBugs Reports
We will use the Eclipse IDE. Firstly install the SputBugs plugin to the ecplise.
Obtain the FindSecBugs 1.4.6 and compile it to get the jar file.
Then in Eclipse preferences, open SoptBugs configuration and add FindSecBugs jar file under the 'Plugins and misc. Settings' tab.
Lastly, only selected following detectors under the 'Detector Configuration' tab.