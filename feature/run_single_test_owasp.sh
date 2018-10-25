#!/bin/bash

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/owasp/$3 -l models/owasp-J48-$1-seed-$2.dat