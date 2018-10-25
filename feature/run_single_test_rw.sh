#!/bin/bash

java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-rand-NaiveBayes-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-rand-NaiveBayes-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-rand-NaiveBayes-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-rand-NaiveBayes-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-rand-NaiveBayes-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-rand-Logistic-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-rand-Logistic-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-rand-Logistic-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-rand-Logistic-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-rand-Logistic-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-rand-MultilayerPerceptron-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-rand-MultilayerPerceptron-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-rand-MultilayerPerceptron-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-rand-MultilayerPerceptron-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-rand-MultilayerPerceptron-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-rand-SMO-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-rand-SMO-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-rand-SMO-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-rand-SMO-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-rand-SMO-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-rand-KStar-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-rand-KStar-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-rand-KStar-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-rand-KStar-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-rand-KStar-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-rand-OneR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-rand-OneR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-rand-OneR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-rand-OneR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-rand-OneR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-rand-ZeroR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-rand-ZeroR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-rand-ZeroR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-rand-ZeroR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-rand-ZeroR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-rand-J48-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-rand-J48-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-rand-J48-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-rand-J48-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-rand-J48-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-rand-RandomForest-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-rand-RandomForest-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-rand-RandomForest-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-rand-RandomForest-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-rand-RandomForest-model-5-seed-$1.dat


java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-pw-NaiveBayes-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-pw-NaiveBayes-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-pw-NaiveBayes-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-pw-NaiveBayes-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.bayes.NaiveBayes -T data/singles/$2 -l models/rw-pw-NaiveBayes-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-pw-Logistic-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-pw-Logistic-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-pw-Logistic-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-pw-Logistic-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.Logistic -T data/singles/$2 -l models/rw-pw-Logistic-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-pw-MultilayerPerceptron-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-pw-MultilayerPerceptron-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-pw-MultilayerPerceptron-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-pw-MultilayerPerceptron-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.MultilayerPerceptron -T data/singles/$2 -l models/rw-pw-MultilayerPerceptron-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-pw-SMO-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-pw-SMO-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-pw-SMO-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-pw-SMO-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.functions.SMO -T data/singles/$2 -l models/rw-pw-SMO-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-pw-KStar-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-pw-KStar-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-pw-KStar-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-pw-KStar-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.lazy.KStar -T data/singles/$2 -l models/rw-pw-KStar-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-pw-OneR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-pw-OneR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-pw-OneR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-pw-OneR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.OneR -T data/singles/$2 -l models/rw-pw-OneR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-pw-ZeroR-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-pw-ZeroR-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-pw-ZeroR-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-pw-ZeroR-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.rules.ZeroR -T data/singles/$2 -l models/rw-pw-ZeroR-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-pw-J48-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-pw-J48-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-pw-J48-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-pw-J48-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.J48 -T data/singles/$2 -l models/rw-pw-J48-model-5-seed-$1.dat

java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-pw-RandomForest-model-1-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-pw-RandomForest-model-2-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-pw-RandomForest-model-3-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-pw-RandomForest-model-4-seed-$1.dat
java -cp ~/weka/weka.jar weka.classifiers.trees.RandomForest -T data/singles/$2 -l models/rw-pw-RandomForest-model-5-seed-$1.dat