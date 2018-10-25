#!/bin/bash

#java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/owasp-J48-model-Occ-1-seed-$1.dat
#java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/owasp-J48-model-Occ-2-seed-$1.dat
#java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/owasp-J48-model-Occ-3-seed-$1.dat
#java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/owasp-J48-model-Occ-4-seed-$1.dat
#java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/owasp-J48-model-Occ-5-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/owasp/$2 -l models/owasp-J48-model-Freq-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/owasp/$2 -l models/owasp-J48-model-Freq-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/owasp/$2 -l models/owasp-J48-model-Freq-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/owasp/$2 -l models/owasp-J48-model-Freq-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/owasp/$2 -l models/owasp-J48-model-Freq-5-seed-$1.dat

# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Occ-1-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Occ-2-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Occ-3-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Occ-4-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Occ-5-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Freq-1-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Freq-2-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Freq-3-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Freq-4-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-rand-J48-model-Freq-5-seed-$1.dat

# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Occ-1-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Occ-2-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Occ-3-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Occ-4-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Occ-5-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Freq-1-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Freq-2-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Freq-3-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Freq-4-seed-$1.dat
# java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T singles/$2 -l models/rw-pw-J48-model-Freq-5-seed-$1.dat
