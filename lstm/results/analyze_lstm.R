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

#seed,prep,strat,split,emb_dim,batch_size,depth,trainacc,testacc,tp,tn,fp,fn,recall,precision,epoch,epochtime,besttime,trainingtime
dir <- "/Users/ukoc/mangrove/ml/lstm/results";
all_data <- read.table(paste(dir,"/stat_all_last_rows.txt",sep=""), header=TRUE, sep=",");
summ_data <- do.call(data.frame, 
                     aggregate(cbind(epoch,trainingtime,recall,precision,testacc) ~ strat,
                               data = all_data,
                               function(x) c(median=median(x), siqr=SIQR(x)))) #, mean = mean(x), sd = sd(x)

print(xtable(summ_data, include.rownames=FALSE))