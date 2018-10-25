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

#seed,alg,epoch,strat,split,loss,acc,precision,recall,f1,speed,time
dir <- "/Users/ukoc/mangrove/ml/ggnn/results";
#all_data <- read.table(paste(dir,"/stats_seeds_last_rows.txt",sep=""), header=TRUE, sep=",");
#all_data <- read.table(paste(dir,"/stats_seeds_all.txt",sep=""), header=TRUE, sep=",");
all_data <- read.table(paste(dir,"/stats_owasp_seed_all_rows.txt",sep=""), header=TRUE, sep=",");
#all_data <- read.table(paste(dir,"/stats_owasp_acc.csv",sep=""), header=TRUE, sep=",");

max_data <- all_data %>% group_by(seed,alg,strat,split) %>% top_n(1, time)# %>% top_n(1, epoch)
write.csv(max_data, file = paste(dir,"/max_time_data.csv",sep=""))

summ_data <- do.call(data.frame,
                      aggregate(cbind(epoch,time) ~ alg+strat,
                                data = max_data,
                                function(x) c(median=median(x), siqr=SIQR(x)))) #, mean = mean(x), sd = sd(x)

print(xtable(summ_data, include.rownames=FALSE))