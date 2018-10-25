#source(paste(dir,"/analyze.R",sep=""))

library(car)
library(plotrix)
library(ggplot2)
library(doBy)
library(plyr)
library(Hmisc)
library(pgirmess)
library(xtable)
library(fmsb)
library(reshape2)

#seed,alg,strat,split,tprate,fprate,precision,recall,fscore,acc,time
dir <- "/Users/ukoc/mangrove/ml/feature/results";
all_data <- read.table(paste(dir,"/stats_rw_seeds_all.txt",sep=""), header=TRUE, sep=",");

top_four <- subset(all_data, (alg == "MultilayerPerceptron" | alg=="RandomForest" | alg=="J48" | alg=="KStar"))
summ_data <- do.call(data.frame, 
                     aggregate(cbind(time,recall,precision,acc) ~ alg+strat,
                               data = top_four,
                               function(x) c(median=median(x), siqr=SIQR(x)))) #, mean = mean(x), sd = sd(x)

print(xtable(summ_data, include.rownames=FALSE))