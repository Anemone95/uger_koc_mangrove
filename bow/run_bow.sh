#!/bin/bash

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-1-Occ.arff -T data/owasp-slice-test-1-Occ.arff -d models/owasp-J48-model-Occ-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-2-Occ.arff -T data/owasp-slice-test-2-Occ.arff -d models/owasp-J48-model-Occ-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-3-Occ.arff -T data/owasp-slice-test-3-Occ.arff -d models/owasp-J48-model-Occ-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-4-Occ.arff -T data/owasp-slice-test-4-Occ.arff -d models/owasp-J48-model-Occ-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-5-Occ.arff -T data/owasp-slice-test-5-Occ.arff -d models/owasp-J48-model-Occ-5-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-1-Freq.arff -T data/owasp-slice-test-1-Freq.arff -d models/owasp-J48-model-Freq-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-2-Freq.arff -T data/owasp-slice-test-2-Freq.arff -d models/owasp-J48-model-Freq-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-3-Freq.arff -T data/owasp-slice-test-3-Freq.arff -d models/owasp-J48-model-Freq-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-4-Freq.arff -T data/owasp-slice-test-4-Freq.arff -d models/owasp-J48-model-Freq-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-slice-train-5-Freq.arff -T data/owasp-slice-test-5-Freq.arff -d models/owasp-J48-model-Freq-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-1-Occ.arff -T data/rw-slice-rand-test-1-Occ.arff -d models/rw-rand-J48-model-Occ-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-2-Occ.arff -T data/rw-slice-rand-test-2-Occ.arff -d models/rw-rand-J48-model-Occ-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-3-Occ.arff -T data/rw-slice-rand-test-3-Occ.arff -d models/rw-rand-J48-model-Occ-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-4-Occ.arff -T data/rw-slice-rand-test-4-Occ.arff -d models/rw-rand-J48-model-Occ-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-5-Occ.arff -T data/rw-slice-rand-test-5-Occ.arff -d models/rw-rand-J48-model-Occ-5-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-1-Freq.arff -T data/rw-slice-rand-test-1-Freq.arff -d models/rw-rand-J48-model-Freq-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-2-Freq.arff -T data/rw-slice-rand-test-2-Freq.arff -d models/rw-rand-J48-model-Freq-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-3-Freq.arff -T data/rw-slice-rand-test-3-Freq.arff -d models/rw-rand-J48-model-Freq-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-4-Freq.arff -T data/rw-slice-rand-test-4-Freq.arff -d models/rw-rand-J48-model-Freq-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-rand-train-5-Freq.arff -T data/rw-slice-rand-test-5-Freq.arff -d models/rw-rand-J48-model-Freq-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-1-Occ.arff -T data/rw-slice-pw-test-1-Occ.arff -d models/rw-pw-J48-model-Occ-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-2-Occ.arff -T data/rw-slice-pw-test-2-Occ.arff -d models/rw-pw-J48-model-Occ-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-3-Occ.arff -T data/rw-slice-pw-test-3-Occ.arff -d models/rw-pw-J48-model-Occ-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-4-Occ.arff -T data/rw-slice-pw-test-4-Occ.arff -d models/rw-pw-J48-model-Occ-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-5-Occ.arff -T data/rw-slice-pw-test-5-Occ.arff -d models/rw-pw-J48-model-Occ-5-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-1-Freq.arff -T data/rw-slice-pw-test-1-Freq.arff -d models/rw-pw-J48-model-Freq-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-2-Freq.arff -T data/rw-slice-pw-test-2-Freq.arff -d models/rw-pw-J48-model-Freq-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-3-Freq.arff -T data/rw-slice-pw-test-3-Freq.arff -d models/rw-pw-J48-model-Freq-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-4-Freq.arff -T data/rw-slice-pw-test-4-Freq.arff -d models/rw-pw-J48-model-Freq-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-slice-pw-train-5-Freq.arff -T data/rw-slice-pw-test-5-Freq.arff -d models/rw-pw-J48-model-Freq-5-seed-$1.dat


# ./run_Occ.sh 1234
# ./run_Occ.sh 3252
# ./run_Occ.sh 5827
# ./run_Occ.sh 7421
# ./run_Occ.sh 9876
