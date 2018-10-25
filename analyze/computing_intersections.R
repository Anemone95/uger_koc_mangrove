intrsct_rand_4 = rand_testset['rw.fb.rand.J48.1'] + rand_testset['rw.rand.J48.Freq.1.BoW'] + rand_testset['rw.extraction.LSTM.rand.1'] + rand_testset['rw.rand.GGNN.KOT.1'];
intrsct_rand_4 = table(intrsct_rand_4 == 4)["TRUE"]

intrsct_rand_NO_FB_3 = rand_testset['rw.rand.J48.Freq.1.BoW'] + rand_testset['rw.extraction.LSTM.rand.1'] + rand_testset['rw.rand.GGNN.KOT.1'];
intrsct_rand_NO_FB_3 = table(intrsct_rand_NO_FB_3 == 3)["TRUE"] - intrsct_rand_4

intrsct_rand_NO_BOW_3 = rand_testset['rw.fb.rand.J48.1'] + rand_testset['rw.extraction.LSTM.rand.1'] + rand_testset['rw.rand.GGNN.KOT.1'];
intrsct_rand_NO_BOW_3 = table(intrsct_rand_NO_BOW_3 == 3)["TRUE"] - intrsct_rand_4

intrsct_rand_NO_LSTM_3 = rand_testset['rw.fb.rand.J48.1'] + rand_testset['rw.rand.J48.Freq.1.BoW'] + rand_testset['rw.rand.GGNN.KOT.1'];
intrsct_rand_NO_LSTM_3 = table(intrsct_rand_NO_LSTM_3 == 3)["TRUE"] - intrsct_rand_4

intrsct_rand_NO_GGNN_3 = rand_testset['rw.fb.rand.J48.1'] + rand_testset['rw.rand.J48.Freq.1.BoW'] + rand_testset['rw.extraction.LSTM.rand.1']
intrsct_rand_NO_GGNN_3 = table(intrsct_rand_NO_GGNN_3 == 3)["TRUE"] - intrsct_rand_4

intrsct_FB_BoW_rand  = rand_testset['rw.fb.rand.J48.1'] + rand_testset['rw.rand.J48.Freq.1.BoW'];
intrsct_FB_BoW_rand = table(intrsct_FB_BoW_rand == 2)["TRUE"] - intrsct_rand_4 - intrsct_rand_NO_LSTM_3 - intrsct_rand_NO_GGNN_3

intrsct_FB_LSTM_rand = rand_testset['rw.fb.rand.J48.1'] + rand_testset['rw.extraction.LSTM.rand.1'];
intrsct_FB_LSTM_rand = table(intrsct_FB_LSTM_rand == 2)["TRUE"] - intrsct_rand_4 - intrsct_rand_NO_BOW_3 - intrsct_rand_NO_GGNN_3

intrsct_FB_GGNN_rand = rand_testset['rw.fb.rand.J48.1'] + rand_testset['rw.rand.GGNN.KOT.1'];
intrsct_FB_GGNN_rand = table(intrsct_FB_GGNN_rand == 2)["TRUE"] - intrsct_rand_4 - intrsct_rand_NO_LSTM_3 - intrsct_rand_NO_BOW_3

intrsct_BoW_LSTM_rand = rand_testset['rw.rand.J48.Freq.1.BoW'] + rand_testset['rw.extraction.LSTM.rand.1'];
intrsct_BoW_LSTM_rand = table(intrsct_BoW_LSTM_rand == 2)["TRUE"] - intrsct_rand_4 - intrsct_rand_NO_FB_3 - intrsct_rand_NO_GGNN_3

intrsct_BoW_GGNN_rand = rand_testset['rw.rand.J48.Freq.1.BoW'] + rand_testset['rw.rand.GGNN.KOT.1'];
intrsct_BoW_GGNN_rand = table(intrsct_BoW_GGNN_rand == 2)["TRUE"] - intrsct_rand_4 - intrsct_rand_NO_FB_3 - intrsct_rand_NO_LSTM_3

intrsct_LSTM_GGNN_rand = rand_testset['rw.extraction.LSTM.rand.1'] + rand_testset['rw.rand.GGNN.KOT.1'];
intrsct_LSTM_GGNN_rand = table(intrsct_LSTM_GGNN_rand == 2)["TRUE"] - intrsct_rand_4 - intrsct_rand_NO_BOW_3 - intrsct_rand_NO_FB_3



intrsct_pw_4   = pw_testset['rw.fb.pw.J48.1'] + pw_testset['rw.pw.J48.Freq.1.BoW'] + pw_testset['rw.extraction.LSTM.pw.1'] + pw_testset['rw.pw.GGNN.KOT.1'];
intrsct_pw_4 = table(intrsct_pw_4 == 4)["TRUE"]

intrsct_pw_NO_FB_3   = pw_testset['rw.pw.J48.Freq.1.BoW'] + pw_testset['rw.extraction.LSTM.pw.1'] + pw_testset['rw.pw.GGNN.KOT.1'];
intrsct_pw_NO_FB_3 = table(intrsct_pw_NO_FB_3 == 3)["TRUE"] - intrsct_pw_4
intrsct_pw_NO_BOW_3   = pw_testset['rw.fb.pw.J48.1']  + pw_testset['rw.extraction.LSTM.pw.1'] + pw_testset['rw.pw.GGNN.KOT.1'];
intrsct_pw_NO_BOW_3 = table(intrsct_pw_NO_BOW_3 == 3)["TRUE"] - intrsct_pw_4
intrsct_pw_NO_LSTM_3   = pw_testset['rw.fb.pw.J48.1'] + pw_testset['rw.pw.J48.Freq.1.BoW'] +pw_testset['rw.pw.GGNN.KOT.1'];
intrsct_pw_NO_LSTM_3 = table(intrsct_pw_NO_LSTM_3 == 3)["TRUE"] - intrsct_pw_4
intrsct_pw_NO_GGNN_3   = pw_testset['rw.fb.pw.J48.1'] + pw_testset['rw.pw.J48.Freq.1.BoW'] + pw_testset['rw.extraction.LSTM.pw.1']
intrsct_pw_NO_GGNN_3 = table(intrsct_pw_NO_GGNN_3 == 3)["TRUE"] - intrsct_pw_4

intrsct_FB_BoW_pw = pw_testset['rw.fb.pw.J48.1'] + pw_testset['rw.pw.J48.Freq.1.BoW'];
intrsct_FB_BoW_pw = table(intrsct_FB_BoW_pw == 2)["TRUE"] - intrsct_pw_4 - intrsct_pw_NO_LSTM_3 - intrsct_pw_NO_GGNN_3
intrsct_FB_LSTM_pw = pw_testset['rw.fb.pw.J48.1'] + pw_testset['rw.extraction.LSTM.pw.1'];
intrsct_FB_LSTM_pw = table(intrsct_FB_LSTM_pw == 2)["TRUE"] - intrsct_pw_4 - intrsct_pw_NO_BOW_3 - intrsct_pw_NO_GGNN_3
intrsct_FB_GGNN_pw = pw_testset['rw.fb.pw.J48.1'] + pw_testset['rw.pw.GGNN.KOT.1'];
intrsct_FB_GGNN_pw = table(intrsct_FB_GGNN_pw == 2)["TRUE"] - intrsct_pw_4 - intrsct_pw_NO_BOW_3 - intrsct_pw_NO_LSTM_3
intrsct_Bow_LSTM_pw   = pw_testset['rw.pw.J48.Freq.1.BoW'] + pw_testset['rw.extraction.LSTM.pw.1'];
intrsct_Bow_LSTM_pw = table(intrsct_Bow_LSTM_pw == 2)["TRUE"] - intrsct_pw_4 - intrsct_pw_NO_FB_3 - intrsct_pw_NO_GGNN_3
intrsct_BoW_GGNN_pw   = pw_testset['rw.pw.J48.Freq.1.BoW'] + pw_testset['rw.pw.GGNN.KOT.1'];
intrsct_BoW_GGNN_pw = table(intrsct_BoW_GGNN_pw == 2)["TRUE"] - intrsct_pw_4 - intrsct_pw_NO_FB_3 - intrsct_pw_NO_LSTM_3
intrsct_LSTM_GGNN_pw = pw_testset['rw.extraction.LSTM.pw.1'] + pw_testset['rw.pw.GGNN.KOT.1'];
intrsct_LSTM_GGNN_pw = table(intrsct_LSTM_GGNN_pw == 2)["TRUE"] - intrsct_pw_4 - intrsct_pw_NO_FB_3 - intrsct_pw_NO_BOW_3



# the other way

uniq_FB_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==0 & rand_testset$rw.extraction.LSTM.rand.1 ==0 & rand_testset$rw.rand.GGNN.KOT.1 ==0),]
uniq_FP_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==0 & pw_testset$rw.extraction.LSTM.pw.1 ==0 & pw_testset$rw.pw.GGNN.KOT.1==0 ),]
uniq_BoW_rand <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==0 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==0 & rand_testset$rw.rand.GGNN.KOT.1 ==0),]
uniq_BoW_pw   <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==0 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==0 & pw_testset$rw.pw.GGNN.KOT.1==0 ),]
uniq_LSTM_rand<- rand_testset[which(rand_testset$rw.fb.rand.J48.1==0 & rand_testset$rw.rand.J48.Freq.1.BoW ==0 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==0),]
uniq_LSTM_pw  <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==0 & pw_testset$rw.pw.J48.Freq.1.BoW ==0 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==0 ),]
uniq_GGNN_rand<- rand_testset[which(rand_testset$rw.fb.rand.J48.1==0 & rand_testset$rw.rand.J48.Freq.1.BoW ==0 & rand_testset$rw.extraction.LSTM.rand.1 ==0 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
uniq_GGNN_pw  <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==0 & pw_testset$rw.pw.J48.Freq.1.BoW ==0 & pw_testset$rw.extraction.LSTM.pw.1 ==0 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]
inter_FB_BOW_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==0 & rand_testset$rw.rand.GGNN.KOT.1 ==0),]
inter_FP_BOW_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==0 & pw_testset$rw.pw.GGNN.KOT.1==0 ),]
inter_FB_LSTM_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==0 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==0),]
inter_FP_LSTM_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==0 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==0 ),]
inter_FB_GGNN_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==0 & rand_testset$rw.extraction.LSTM.rand.1 ==0 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
inter_FP_GGNN_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==0 & pw_testset$rw.extraction.LSTM.pw.1 ==0 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]
inter_BOW_LSTM_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==0 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==0),]
inter_BOW_LSTM_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==0 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==0 ),]
inter_BOW_GGNN_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==0 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==0 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
inter_BOW_GGNN_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==0 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==0 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]
inter_LSTM_GGNN_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==0 & rand_testset$rw.rand.J48.Freq.1.BoW ==0 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
inter_LSTM_GGNN_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==0 & pw_testset$rw.pw.J48.Freq.1.BoW ==0 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]
inter_FB_BOW_LSTM_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==0),]
inter_FP_BOW_LSTM_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==0 ),]
inter_FB_LSTM_GGNN_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==0 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
inter_FP_LSTM_GGNN_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==0 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]
inter_FB_BOW_GGNN_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==0 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
inter_FP_BOW_GGNN_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==0 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]
inter_BOW_LSTM_GGNN_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==0 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
inter_BOW_LSTM_GGNN_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==0 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]
inter_all_rand  <- rand_testset[which(rand_testset$rw.fb.rand.J48.1==1 & rand_testset$rw.rand.J48.Freq.1.BoW ==1 & rand_testset$rw.extraction.LSTM.rand.1 ==1 & rand_testset$rw.rand.GGNN.KOT.1 ==1),]
inter_all_pw    <- pw_testset[which(pw_testset$rw.fb.pw.J48.1 ==1 & pw_testset$rw.pw.J48.Freq.1.BoW ==1 & pw_testset$rw.extraction.LSTM.pw.1 ==1 & pw_testset$rw.pw.GGNN.KOT.1==1 ),]

nrow(uniq_FB_rand)
nrow(uniq_FP_pw)
nrow(uniq_BoW_rand)
nrow(uniq_BoW_pw)
nrow(uniq_LSTM_rand)
nrow(uniq_LSTM_pw)
nrow(uniq_GGNN_rand)
nrow(uniq_GGNN_pw)
nrow(inter_FB_BOW_rand)
nrow(inter_FP_BOW_pw)
nrow(inter_FB_LSTM_rand)
nrow(inter_FP_LSTM_pw)
nrow(inter_FB_GGNN_rand)
nrow(inter_FP_GGNN_pw)
nrow(inter_BOW_LSTM_rand)
nrow(inter_BOW_LSTM_pw)
nrow(inter_BOW_GGNN_rand)
nrow(inter_BOW_GGNN_pw)
nrow(inter_LSTM_GGNN_rand)
nrow(inter_LSTM_GGNN_pw)
nrow(inter_FB_BOW_LSTM_rand)
nrow(inter_FP_BOW_LSTM_pw)
nrow(inter_FB_LSTM_GGNN_rand)
nrow(inter_FP_LSTM_GGNN_pw)
nrow(inter_FB_BOW_GGNN_rand)
nrow(inter_FP_BOW_GGNN_pw)
nrow(inter_BOW_LSTM_GGNN_rand)
nrow(inter_BOW_LSTM_GGNN_pw)
nrow(inter_all_rand)
nrow(inter_all_pw)

uniq_FB_rand[1]
uniq_FP_pw[2]
uniq_BoW_rand[1]
uniq_BoW_pw[2]
uniq_LSTM_rand[1]
uniq_LSTM_pw[2]
uniq_GGNN_rand[1]
uniq_GGNN_pw[2]
inter_FB_BOW_rand[1]
inter_FP_BOW_pw[2]
inter_FB_LSTM_rand[1]
inter_FP_LSTM_pw[2]
inter_FB_GGNN_rand[1]
inter_FP_GGNN_pw[2]
inter_BOW_LSTM_rand[1]
inter_BOW_LSTM_pw[2]
inter_BOW_GGNN_rand[1]
inter_BOW_GGNN_pw[2]
inter_LSTM_GGNN_rand[1]
inter_LSTM_GGNN_pw[2]
inter_FB_BOW_LSTM_rand[1]
inter_FP_BOW_LSTM_pw[2]
inter_FB_LSTM_GGNN_rand[1]
inter_FP_LSTM_GGNN_pw[2]
inter_FB_BOW_GGNN_rand[1]
inter_FP_BOW_GGNN_pw[2]
inter_BOW_LSTM_GGNN_rand[1]
inter_BOW_LSTM_GGNN_pw[2]
inter_all_rand[1]
inter_all_pw[2]
none_rand[1]
none_pw[2]