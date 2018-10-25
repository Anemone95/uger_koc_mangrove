#!/bin/bash

java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-NaiveBayes-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-NaiveBayes-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-NaiveBayes-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-NaiveBayes-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-NaiveBayes-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-Logistic-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-Logistic-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-Logistic-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-Logistic-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-Logistic-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-MultilayerPerceptron-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-MultilayerPerceptron-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-MultilayerPerceptron-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-MultilayerPerceptron-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-MultilayerPerceptron-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-SMO-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-SMO-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-SMO-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-SMO-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-SMO-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-KStar-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-KStar-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-KStar-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-KStar-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-KStar-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-OneR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-OneR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-OneR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-OneR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-OneR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-ZeroR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-ZeroR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-ZeroR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-ZeroR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-ZeroR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-J48-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-J48-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-J48-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-J48-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-J48-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-rand-train-1.arff -T data/rw-rand-test-1.arff -d models/rw-rand-RandomForest-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-rand-train-2.arff -T data/rw-rand-test-2.arff -d models/rw-rand-RandomForest-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-rand-train-3.arff -T data/rw-rand-test-3.arff -d models/rw-rand-RandomForest-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-rand-train-4.arff -T data/rw-rand-test-4.arff -d models/rw-rand-RandomForest-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-rand-train-5.arff -T data/rw-rand-test-5.arff -d models/rw-rand-RandomForest-model-5-seed-$1.dat


java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-NaiveBayes-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-NaiveBayes-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-NaiveBayes-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-NaiveBayes-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-NaiveBayes-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-Logistic-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-Logistic-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-Logistic-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-Logistic-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4 -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-Logistic-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-MultilayerPerceptron-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-MultilayerPerceptron-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-MultilayerPerceptron-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-MultilayerPerceptron-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.3 -M 0.2 -N 500 -V 0 -S $1 -E 20 -H a -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-MultilayerPerceptron-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-SMO-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-SMO-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-SMO-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-SMO-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4" -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-SMO-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-KStar-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-KStar-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-KStar-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-KStar-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -B 20 -M a -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-KStar-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-OneR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-OneR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-OneR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-OneR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -B 6 -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-OneR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-ZeroR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-ZeroR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-ZeroR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-ZeroR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-ZeroR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-J48-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-J48-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-J48-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-J48-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-J48-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-pw-train-1.arff -T data/rw-pw-test-1.arff -d models/rw-pw-RandomForest-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-pw-train-2.arff -T data/rw-pw-test-2.arff -d models/rw-pw-RandomForest-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-pw-train-3.arff -T data/rw-pw-test-3.arff -d models/rw-pw-RandomForest-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-pw-train-4.arff -T data/rw-pw-test-4.arff -d models/rw-pw-RandomForest-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S $1 -s $1 -t data/rw-pw-train-5.arff -T data/rw-pw-test-5.arff -d models/rw-pw-RandomForest-model-5-seed-$1.dat