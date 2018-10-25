#!/bin/bash

#Usage:
# ./run_single_test.sh splitID dataPointFileName

#For RW-Rand
python2.7 lstm.py data/singles/$2 data/extraction/rw-dict.txt 8 50 4 test models/rw-extraction-slice-rand-$1-emb8.npz


#For RW-PW
python2.7 lstm.py data/singles/$2 data/extraction/rw-dict.txt 8 50 4 test models/rw-extraction-slice-pw-$1-emb8.npz

#For owasp dataset
#python2.7 lstm.py data/singles/owasp/$2 data/extraction/owasp-dict.txt 8 50 4 test models/data-prefix-owasp-slice-train-$1-emb8.npz