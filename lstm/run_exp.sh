#!/bin/bash

# Usage

# ./run_exp.sh  splitID

# For OWASP
python2.7 lstm9876.py data/raw/owasp-slice-train-$1.txt data/raw/owasp-dict.txt 8 50 4 > log_owasp-slice-raw-$1-seed9876.txt 2>&1 &
python2.7 lstm9876.py data/abstract/owasp-slice-train-$1.txt data/abstract/owasp-dict.txt 8 50 4 > log_owasp-slice-abstract-$1-seed9876.txt 2>&1 &
python2.7 lstm9876.py data/abstractpp/owasp-slice-train-$1.txt data/abstractpp/owasp-dict.txt 8 50 4 > log_owasp-slice-abstractpp-$1-seed9876.txt 2>&1
python2.7 lstm9876.py data/extraction/owasp-slice-train-$1.txt data/extraction/owasp-dict.txt 8 50 4 > log_owasp-slice-extraction-$1-seed9876.txt 2>&1


# For RW-RAND
#python2.7 lstm.py data/raw/rw-slice-rand-train-$1.txt data/raw/rw-dict.txt 8 50 4 > log_rw-slice-raw-rand-$1.txt 2>&1
#python2.7 lstm.py data/abstract/rw-slice-rand-train-$1.txt data/abstract/rw-dict.txt 8 50 4 > log_rw-slice-abstract-rand-$1.txt 2>&1
#python2.7 lstm.py data/abstractpp/rw-slice-rand-train-$1.txt data/abstractpp/rw-dict.txt 8 50 4 > log_rw-slice-abstractpp-rand-$1.txt 2>&1
#python2.7 lstm.py data/extraction/rw-slice-rand-train-$1.txt data/extraction/rw-dict.txt 8 50 4 > log_rw-slice-extraction-rand-$1.txt 2>&1

# For RW-PW
#python2.7 lstm.py data/raw/rw-slice-pw-train-$1.txt data/raw/rw-dict.txt 8 50 4 > log_rw-slice-raw-pw-$1.txt 2>&1
#python2.7 lstm.py data/abstract/rw-slice-pw-train-$1.txt data/abstract/rw-dict.txt 8 50 4 > log_rw-slice-abstract-pw-$1.txt 2>&1
#python2.7 lstm.py data/abstractpp/rw-slice-pw-train-$1.txt data/abstractpp/rw-dict.txt 8 50 4 > log_rw-slice-abstractpp-pw-$1.txt 2>&1
#python2.7 lstm.py data/extraction/rw-slice-pw-train-$1.txt data/extraction/rw-dict.txt 8 50 4 > log_rw-slice-extraction-pw-$1.txt 2>&1