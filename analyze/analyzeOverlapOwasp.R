#source(paste(dir,"/analyzeOverlapOwasp.R",sep=""))

library(car)
library(plotrix)
library(ggplot2)
library(doBy)
library(plyr)
library(Hmisc)
library(pgirmess)
library(xtable)
library(fmsb)

dir <- "/Users/ukoc/mangrove/ml/analyze";
all_data <- read.table(paste(dir,"/cartesianOwasp.txt",sep=""), header=TRUE, sep=",");
testfiles_P = "/Users/ukoc/mangrove/data/testsets/owasp-test-fn-XYZ.txt";
uniq_FB_arr <- c(0,0,0,0,0)
uniq_BoW_arr <- c(0,0,0,0,0)
uniq_LSTM_arr <- c(0,0,0,0,0)
uniq_GGNN_arr <- c(0,0,0,0,0)
inter_FB_BOW_arr <- c(0,0,0,0,0)
inter_FB_LSTM_arr <- c(0,0,0,0,0)
inter_FB_GGNN_arr <- c(0,0,0,0,0)
inter_BOW_LSTM_arr <- c(0,0,0,0,0)
inter_BOW_GGNN_arr <- c(0,0,0,0,0)
inter_LSTM_GGNN_arr <- c(0,0,0,0,0)
inter_FB_BOW_LSTM_arr <- c(0,0,0,0,0)
inter_FB_LSTM_GGNN_arr <- c(0,0,0,0,0)
inter_FB_BOW_GGNN_arr <- c(0,0,0,0,0)
inter_BOW_LSTM_GGNN_arr <- c(0,0,0,0,0)
inter_all_arr <- c(0,0,0,0,0)
none_arr <- c(0,0,0,0,0)

for (split_id in c(1,2,3)) {
  testfiles <- read.table(gsub("XYZ", split_id, testfiles_P), header=TRUE, sep=",");
  models <- c("dpname", paste("fb_",split_id,sep=""), paste("bow_",split_id,sep=""), paste("lstm_",split_id,sep=""), paste("ggnn_",split_id,sep=""));
  ts <- subset(all_data, dpname %in% testfiles$dpname)[ models];
  names( ts)[2] <-'fb'
  names( ts)[3] <-'bow'
  names( ts)[4] <-'lstm'
  names( ts)[5] <-'ggnn'
  
  
  uniq_FB <- ts[which( ts$fb == 1 & ts$bow == 0 & ts$lstm == 0 & ts$ggnn == 0),]
  uniq_BoW <- ts[which( ts$fb == 0 & ts$bow == 1 & ts$lstm == 0 & ts$ggnn == 0),]
  uniq_LSTM <- ts[which( ts$fb == 0 & ts$bow == 0 & ts$lstm == 1 & ts$ggnn == 0),]
  uniq_GGNN <- ts[which( ts$fb == 0 & ts$bow == 0 & ts$lstm == 0 & ts$ggnn == 1),]
  inter_FB_BOW <- ts[which( ts$fb == 1 & ts$bow == 1 & ts$lstm == 0 & ts$ggnn == 0),]
  inter_FB_LSTM <- ts[which( ts$fb == 1 & ts$bow == 0 & ts$lstm == 1 & ts$ggnn == 0),]
  inter_FB_GGNN <- ts[which( ts$fb == 1 & ts$bow == 0 & ts$lstm == 0 & ts$ggnn == 1),]
  inter_BOW_LSTM <- ts[which( ts$fb == 0 & ts$bow == 1 & ts$lstm == 1 & ts$ggnn == 0),]
  inter_BOW_GGNN <- ts[which( ts$fb == 0 & ts$bow == 1 & ts$lstm == 0 & ts$ggnn == 1),]
  inter_LSTM_GGNN <- ts[which( ts$fb == 0 & ts$bow == 0 & ts$lstm == 1 & ts$ggnn == 1),]
  inter_FB_BOW_LSTM <- ts[which( ts$fb == 1 & ts$bow == 1 & ts$lstm == 1 & ts$ggnn == 0),]
  inter_FB_LSTM_GGNN <- ts[which( ts$fb == 1 & ts$bow == 0 & ts$lstm == 1 & ts$ggnn == 1),]
  inter_FB_BOW_GGNN <- ts[which( ts$fb == 1 & ts$bow == 1 & ts$lstm == 0 & ts$ggnn == 1),]
  inter_BOW_LSTM_GGNN <- ts[which( ts$fb == 0 & ts$bow == 1 & ts$lstm == 1 & ts$ggnn == 1),]
  inter_all <- ts[which( ts$fb == 1 & ts$bow == 1 & ts$lstm == 1 & ts$ggnn == 1),]
  none <- ts[which( ts$fb == 0 & ts$bow == 0 & ts$lstm == 0 & ts$ggnn == 0),]
  
  
  uniq_FB_arr[split_id] <- nrow(uniq_FB );
  uniq_BoW_arr[split_id] <- nrow(uniq_BoW );
  uniq_LSTM_arr[split_id] <- nrow(uniq_LSTM );
  uniq_GGNN_arr[split_id] <- nrow(uniq_GGNN );
  inter_FB_BOW_arr[split_id] <- nrow(inter_FB_BOW );
  inter_FB_LSTM_arr[split_id] <- nrow(inter_FB_LSTM );
  inter_FB_GGNN_arr[split_id] <- nrow(inter_FB_GGNN );
  inter_BOW_LSTM_arr[split_id] <- nrow(inter_BOW_LSTM );
  inter_BOW_GGNN_arr[split_id] <- nrow(inter_BOW_GGNN );
  inter_LSTM_GGNN_arr[split_id] <- nrow(inter_LSTM_GGNN );
  inter_FB_BOW_LSTM_arr[split_id] <- nrow(inter_FB_BOW_LSTM );
  inter_FB_LSTM_GGNN_arr[split_id] <- nrow(inter_FB_LSTM_GGNN );
  inter_FB_BOW_GGNN_arr[split_id] <- nrow(inter_FB_BOW_GGNN );
  inter_BOW_LSTM_GGNN_arr[split_id] <- nrow(inter_BOW_LSTM_GGNN );
  inter_all_arr[split_id] <- nrow(inter_all );
  none_arr[split_id] <- nrow(none );
  
}

cat(sprintf("FB	 %s\n",mean(uniq_FB_arr)))
cat(sprintf("BoW	 %s\n",mean(uniq_BoW_arr)))
cat(sprintf("LSTM	 %s\n",mean(uniq_LSTM_arr)))
cat(sprintf("GGNN	 %s\n",mean(uniq_GGNN_arr)))
cat(sprintf("FB_BOW	 %s\n",mean(inter_FB_BOW_arr)))
cat(sprintf("FB_LSTM	 %s\n",mean(inter_FB_LSTM_arr)))
cat(sprintf("FB_GGNN	 %s\n",mean(inter_FB_GGNN_arr)))
cat(sprintf("BOW_LSTM	 %s\n",mean(inter_BOW_LSTM_arr)))
cat(sprintf("BOW_GGNN	 %s\n",mean(inter_BOW_GGNN_arr)))
cat(sprintf("LSTM_GGNN	 %s\n",mean(inter_LSTM_GGNN_arr)))
cat(sprintf("FB_BOW_LSTM	 %s\n",mean(inter_FB_BOW_LSTM_arr)))
cat(sprintf("FB_LSTM_GGNN	 %s\n",mean(inter_FB_LSTM_GGNN_arr)))
cat(sprintf("FB_BOW_GGNN	 %s\n",mean(inter_FB_BOW_GGNN_arr)))
cat(sprintf("BOW_LSTM_GGNN	 %s\n",mean(inter_BOW_LSTM_GGNN_arr)))
cat(sprintf("all	 %s\n",mean(inter_all_arr)))
cat(sprintf("none	 %s\n",mean(none_arr)))