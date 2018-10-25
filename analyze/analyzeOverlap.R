#source(paste(dir,"/analyzeOverlap.R",sep=""))

library(car)
library(plotrix)
library(ggplot2)
library(doBy)
library(plyr)
library(Hmisc)
library(pgirmess)
library(xtable)
library(fmsb)

dir <- "/Users/ukoc/mangrove/ml";
all_data <- read.table(paste(dir,"/cartesian4All.txt",sep=""), header=TRUE, sep=",");
uniq_FB_rand_arr <- c(0,0,0,0,0);
uniq_FP_pw_arr <- c(0,0,0,0,0);
uniq_BoW_rand_arr <- c(0,0,0,0,0);
uniq_BoW_pw_arr <- c(0,0,0,0,0);
uniq_LSTM_rand_arr <- c(0,0,0,0,0);
uniq_LSTM_pw_arr <- c(0,0,0,0,0);
uniq_GGNN_rand_arr <- c(0,0,0,0,0);
uniq_GGNN_pw_arr <- c(0,0,0,0,0);
inter_FB_BOW_rand_arr <- c(0,0,0,0,0);
inter_FP_BOW_pw_arr <- c(0,0,0,0,0);
inter_FB_LSTM_rand_arr <- c(0,0,0,0,0);
inter_FP_LSTM_pw_arr <- c(0,0,0,0,0);
inter_FB_GGNN_rand_arr <- c(0,0,0,0,0);
inter_FP_GGNN_pw_arr <- c(0,0,0,0,0);
inter_BOW_LSTM_rand_arr <- c(0,0,0,0,0);
inter_BOW_LSTM_pw_arr <- c(0,0,0,0,0);
inter_BOW_GGNN_rand_arr <- c(0,0,0,0,0);
inter_BOW_GGNN_pw_arr <- c(0,0,0,0,0);
inter_LSTM_GGNN_rand_arr <- c(0,0,0,0,0);
inter_LSTM_GGNN_pw_arr <- c(0,0,0,0,0);
inter_FB_BOW_LSTM_rand_arr <- c(0,0,0,0,0);
inter_FP_BOW_LSTM_pw_arr <- c(0,0,0,0,0);
inter_FB_LSTM_GGNN_rand_arr <- c(0,0,0,0,0);
inter_FP_LSTM_GGNN_pw_arr <- c(0,0,0,0,0);
inter_FB_BOW_GGNN_rand_arr <- c(0,0,0,0,0);
inter_FP_BOW_GGNN_pw_arr <- c(0,0,0,0,0);
inter_BOW_LSTM_GGNN_rand_arr <- c(0,0,0,0,0);
inter_BOW_LSTM_GGNN_pw_arr <- c(0,0,0,0,0);
inter_all_rand_arr <- c(0,0,0,0,0);
inter_all_pw_arr <- c(0,0,0,0,0);
none_rand_arr <- c(0,0,0,0,0);
none_pw_arr <- c(0,0,0,0,0);

pw_benchs = array(c(c('Biojava','Freecs', 'Jackrabbit',''), c('Susi','','','',''), c('Apollo', 'Giraph', 'JPF', 'Mybatis'), c('Jetty',  'H2', 'UPM',''), c('Hsqldb', 'Okhttp3', 'JodaTime','')), dim = c(4,5));
rand_testfiles_P = "/Users/ukoc/mangrove/data/testsets/rw-test-fn-XYZ.txt";


for (split_id in c(1)) {#,2,3,4,5
  rand_testfiles <- read.table(gsub("XYZ", split_id, rand_testfiles_P), header=TRUE, sep=",");
  rand_models <- c("dpname", paste("rw.fb.rand.",split_id,sep=""), paste("rw.BoW.Freq.rand.",split_id,sep=""), paste("rw.LSTM.rand.",split_id,sep=""), paste("rw.GGNN.rand.",split_id,sep=""));
  rand_ts <- subset(all_data, dpname %in% rand_testfiles$dpname)[rand_models];
  names(rand_ts)[2] <-'fb_rand'
  names(rand_ts)[3] <-'bow_rand'
  names(rand_ts)[4] <-'lstm_rand'
  names(rand_ts)[5] <-'ggnn_rand'

  pw_models <- c("bench","dpname",paste("rw.fb.pw.",split_id,sep=""), paste("rw.BoW.Freq.pw.",split_id,sep=""), paste("rw.LSTM.pw.",split_id,sep=""), paste("rw.GGNN.pw.",split_id,sep=""));
  pw_ts <- subset(all_data, bench %in% pw_benchs[,split_id])[pw_models];
  names(pw_ts)[3] <-'fb_pw'
  names(pw_ts)[4] <-'bow_pw'
  names(pw_ts)[5] <-'lstm_pw'
  names(pw_ts)[6] <-'ggnn_pw'

  uniq_FB_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 0),]
  uniq_FP_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 0 ),]
  uniq_BoW_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 0),]
  uniq_BoW_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 0 ),]
  uniq_LSTM_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 0),]
  uniq_LSTM_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 0 ),]
  uniq_GGNN_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 1),]
  uniq_GGNN_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 1 ),]
  inter_FB_BOW_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 0),]
  inter_FP_BOW_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 0 ),]
  inter_FB_LSTM_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 0),]
  inter_FP_LSTM_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 0 ),]
  inter_FB_GGNN_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 1),]
  inter_FP_GGNN_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 1 ),]
  inter_BOW_LSTM_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 0),]
  inter_BOW_LSTM_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 0 ),]
  inter_BOW_GGNN_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 1),]
  inter_BOW_GGNN_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 1 ),]
  inter_LSTM_GGNN_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 1),]
  inter_LSTM_GGNN_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 1 ),]
  inter_FB_BOW_LSTM_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 0),]
  inter_FP_BOW_LSTM_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 0 ),]
  inter_FB_LSTM_GGNN_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 1),]
  inter_FP_LSTM_GGNN_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 1 ),]
  inter_FB_BOW_GGNN_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 1),]
  inter_FP_BOW_GGNN_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 1 ),]
  inter_BOW_LSTM_GGNN_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 1),]
  inter_BOW_LSTM_GGNN_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 1 ),]
  inter_all_rand <- rand_ts[which(rand_ts$fb_rand == 1 & rand_ts$bow_rand == 1 & rand_ts$lstm_rand == 1 & rand_ts$ggnn_rand == 1),]
  inter_all_pw <- pw_ts[which(pw_ts$fb_pw == 1 & pw_ts$bow_pw == 1 & pw_ts$lstm_pw == 1 & pw_ts$ggnn_pw == 1 ),]
  none_rand <- rand_ts[which(rand_ts$fb_rand == 0 & rand_ts$bow_rand == 0 & rand_ts$lstm_rand == 0 & rand_ts$ggnn_rand == 0),]
  none_pw <- pw_ts[which(pw_ts$fb_pw == 0 & pw_ts$bow_pw == 0 & pw_ts$lstm_pw == 0 & pw_ts$ggnn_pw == 0),]
  
  
  uniq_FB_rand_arr[split_id] <- nrow(uniq_FB_rand);
  uniq_FP_pw_arr[split_id] <- nrow(uniq_FP_pw);
  uniq_BoW_rand_arr[split_id] <- nrow(uniq_BoW_rand);
  uniq_BoW_pw_arr[split_id] <- nrow(uniq_BoW_pw);
  uniq_LSTM_rand_arr[split_id] <- nrow(uniq_LSTM_rand);
  uniq_LSTM_pw_arr[split_id] <- nrow(uniq_LSTM_pw);
  uniq_GGNN_rand_arr[split_id] <- nrow(uniq_GGNN_rand);
  uniq_GGNN_pw_arr[split_id] <- nrow(uniq_GGNN_pw);
  inter_FB_BOW_rand_arr[split_id] <- nrow(inter_FB_BOW_rand);
  inter_FP_BOW_pw_arr[split_id] <- nrow(inter_FP_BOW_pw);
  inter_FB_LSTM_rand_arr[split_id] <- nrow(inter_FB_LSTM_rand);
  inter_FP_LSTM_pw_arr[split_id] <- nrow(inter_FP_LSTM_pw);
  inter_FB_GGNN_rand_arr[split_id] <- nrow(inter_FB_GGNN_rand);
  inter_FP_GGNN_pw_arr[split_id] <- nrow(inter_FP_GGNN_pw);
  inter_BOW_LSTM_rand_arr[split_id] <- nrow(inter_BOW_LSTM_rand);
  inter_BOW_LSTM_pw_arr[split_id] <- nrow(inter_BOW_LSTM_pw);
  inter_BOW_GGNN_rand_arr[split_id] <- nrow(inter_BOW_GGNN_rand);
  inter_BOW_GGNN_pw_arr[split_id] <- nrow(inter_BOW_GGNN_pw);
  inter_LSTM_GGNN_rand_arr[split_id] <- nrow(inter_LSTM_GGNN_rand);
  inter_LSTM_GGNN_pw_arr[split_id] <- nrow(inter_LSTM_GGNN_pw);
  inter_FB_BOW_LSTM_rand_arr[split_id] <- nrow(inter_FB_BOW_LSTM_rand);
  inter_FP_BOW_LSTM_pw_arr[split_id] <- nrow(inter_FP_BOW_LSTM_pw);
  inter_FB_LSTM_GGNN_rand_arr[split_id] <- nrow(inter_FB_LSTM_GGNN_rand);
  inter_FP_LSTM_GGNN_pw_arr[split_id] <- nrow(inter_FP_LSTM_GGNN_pw);
  inter_FB_BOW_GGNN_rand_arr[split_id] <- nrow(inter_FB_BOW_GGNN_rand);
  inter_FP_BOW_GGNN_pw_arr[split_id] <- nrow(inter_FP_BOW_GGNN_pw);
  inter_BOW_LSTM_GGNN_rand_arr[split_id] <- nrow(inter_BOW_LSTM_GGNN_rand);
  inter_BOW_LSTM_GGNN_pw_arr[split_id] <- nrow(inter_BOW_LSTM_GGNN_pw);
  inter_all_rand_arr[split_id] <- nrow(inter_all_rand);
  inter_all_pw_arr[split_id] <- nrow(inter_all_pw);
  none_rand_arr[split_id] <- nrow(none_rand);
  none_pw_arr[split_id] <- nrow(none_pw);
  
}

# cat(sprintf("FB	%s : %s\n",mean(uniq_FB_rand_arr), mean(uniq_FP_pw_arr)))
# cat(sprintf("BoW	%s : %s\n",mean(uniq_BoW_rand_arr), mean(uniq_BoW_pw_arr)))
# cat(sprintf("LSTM	%s : %s\n",mean(uniq_LSTM_rand_arr), mean(uniq_LSTM_pw_arr)))
# cat(sprintf("GGNN	%s : %s\n",mean(uniq_GGNN_rand_arr), mean(uniq_GGNN_pw_arr)))
# cat(sprintf("FB_BOW	%s : %s\n",mean(inter_FB_BOW_rand_arr), mean(inter_FP_BOW_pw_arr)))
# cat(sprintf("FB_LSTM	%s : %s\n",mean(inter_FB_LSTM_rand_arr), mean(inter_FP_LSTM_pw_arr)))
# cat(sprintf("FB_GGNN	%s : %s\n",mean(inter_FB_GGNN_rand_arr), mean(inter_FP_GGNN_pw_arr)))
# cat(sprintf("BOW_LSTM	%s : %s\n",mean(inter_BOW_LSTM_rand_arr), mean(inter_BOW_LSTM_pw_arr)))
# cat(sprintf("BOW_GGNN	%s : %s\n",mean(inter_BOW_GGNN_rand_arr), mean(inter_BOW_GGNN_pw_arr)))
# cat(sprintf("LSTM_GGNN	%s : %s\n",mean(inter_LSTM_GGNN_rand_arr), mean(inter_LSTM_GGNN_pw_arr)))
# cat(sprintf("FB_BOW_LSTM	%s : %s\n",mean(inter_FB_BOW_LSTM_rand_arr), mean(inter_FP_BOW_LSTM_pw_arr)))
# cat(sprintf("FB_LSTM_GGNN	%s : %s\n",mean(inter_FB_LSTM_GGNN_rand_arr), mean(inter_FP_LSTM_GGNN_pw_arr)))
# cat(sprintf("FB_BOW_GGNN	%s : %s\n",mean(inter_FB_BOW_GGNN_rand_arr), mean(inter_FP_BOW_GGNN_pw_arr)))
# cat(sprintf("BOW_LSTM_GGNN	%s : %s\n",mean(inter_BOW_LSTM_GGNN_rand_arr), mean(inter_BOW_LSTM_GGNN_pw_arr)))
# cat(sprintf("all	%s : %s\n",mean(inter_all_rand_arr), mean(inter_all_pw_arr)))
# cat(sprintf("none	%s : %s\n",mean(none_rand_arr), mean(none_pw_arr)))