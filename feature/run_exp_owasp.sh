#!/bin/bash

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-1.arff -T data/owasp-test-1.arff -d models/owasp-J48-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-2.arff -T data/owasp-test-2.arff -d models/owasp-J48-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-3.arff -T data/owasp-test-3.arff -d models/owasp-J48-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-4.arff -T data/owasp-test-4.arff -d models/owasp-J48-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-5.arff -T data/owasp-test-5.arff -d models/owasp-J48-5-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-1.arff -T data/owasp-test-1.arff -d models/owasp-J48-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-2.arff -T data/owasp-test-2.arff -d models/owasp-J48-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-3.arff -T data/owasp-test-3.arff -d models/owasp-J48-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-4.arff -T data/owasp-test-4.arff -d models/owasp-J48-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/owasp-train-5.arff -T data/owasp-test-5.arff -d models/owasp-J48-5-seed-$1.dat