import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt
import sys, io, re, datetime, time
import random
from os import listdir
from os.path import isfile, join

SEED = 1234

def getBatchIndx(data_size, batch_size, shuffle=True):
    idx_list = np.arange(data_size, dtype="int32")
    if shuffle:
        np.random.shuffle(idx_list)

    batches = []
    minibatch_start = 0
    for i in range(data_size // batch_size):
        batches.append(idx_list[minibatch_start:minibatch_start + batch_size])
        minibatch_start += batch_size

    # if (minibatch_start != n):# Make a minibatch out of what is left
    #     batches.append(idx_list[minibatch_start:])
    return batches

def getBatch(batch_size, data, labels):
    indexes = random.sample(range(0, len(data)), batch_size)
    batch_data = [data[indx] for indx in indexes]
    batch_labels = [labels[indx] for indx in indexes]
    return batch_data, batch_labels

def loadVocab(vocab_file):
    print('Loading vocabulary from' + vocab_file+'...')
    vocab={}
    dictLines = open(vocab_file).readlines()
    for line in dictLines:
        keyValue=line.split()
        vocab[keyValue[0]] = int(keyValue[1])
    print('Done! Vocabulary lenght: '+ str(len(vocab)))
    return vocab

def loadEmbeddings(vocab=None, emb_dim=12, embeddings_file = None):
    embedding_map={}
    with open(embeddings_file) as file: lines = file.read().splitlines() 
    for line in lines:
        key, value = line.split('\t')
        #print('key:' + key+', value:'+ value)
        embedding_map[key] = np.array([np.float32(x) for x in value.split(' ')])

    embeddings = np.zeros([len(vocab)+3, emb_dim], dtype=np.float32)
    for key, value in vocab.items():
        embeddings[value] = embedding_map[key]
    return embeddings

def loadData(data_file, vocab):
    print('Loading data from '+data_file+'...')
    file_content = open(data_file, 'r')
    dataset = []
    labels = []
    max_seq_len = 0
    for prog in file_content:
        labels.append([1, 0] if 'truepositive' in prog else [0, 1])
        prog = re.sub(r'\s::\s\w+epositive', '', prog)
        sample = [vocab.get(token, 2) for token in prog.split()]
        #print(sample)
        if len(sample) > max_seq_len: 
            max_seq_len = len(sample)
        dataset.append(sample)
    print('Done! Data lenght:' + str(len(labels)))
    return dataset, labels, max_seq_len

def list2Matrix(data_list, max_seq_len):
    data_size = len(data_list)
    data_mat = np.zeros([data_size, max_seq_len])
    for i in range(0, data_size):
        data_mat[i][:len(data_list[i])] = data_list[i]
    return data_mat

def trainLstm(dim_proj=128, patience=30, max_epochs=5000, decay_c=0., batch_size=16,
              train_file=None, test_file=None, time_out_s=600, vocab_file=None, num_classes=2,
              embeddings_file='data/extraction/rw-embeddings.txt'):

    print("model options", locals().copy())

    # saveto = train_file.replace('/','-')
    vocab = loadVocab(vocab_file)
    embeddings = loadEmbeddings(vocab=vocab, embeddings_file=embeddings_file)
    train_data, train_labels, train_max_seq_len = loadData(train_file, vocab)
    test_data, test_labels, test_max_seq_len = loadData(test_file, vocab)
    #print(test_labels)
    #exit()
    if batch_size > len(test_labels):
        batch_size = len(test_labels)
    max_seq_len = train_max_seq_len if test_max_seq_len < train_max_seq_len else test_max_seq_len
    train_data = list2Matrix(train_data,  max_seq_len)
    test_data = list2Matrix(test_data,  max_seq_len)
    embedding_dim = dim_proj
    train_data_len = len(train_data)
    tf.reset_default_graph()
    tf.set_random_seed(SEED)

    labels = tf.placeholder(tf.float32, [batch_size, num_classes])
    input_data = tf.placeholder(tf.int32, [batch_size, max_seq_len])

    data = tf.Variable(tf.zeros([batch_size, max_seq_len, embedding_dim]),dtype=tf.float32)
    data = tf.nn.embedding_lookup(embeddings, input_data)

    lstmCell = tf.contrib.rnn.BasicLSTMCell(dim_proj)
    lstmCell = tf.contrib.rnn.DropoutWrapper(cell=lstmCell, output_keep_prob=0.9)
    value, _ = tf.nn.dynamic_rnn(lstmCell, data, dtype=tf.float32)

    weight = tf.Variable(tf.truncated_normal([dim_proj, num_classes]))
    bias = tf.Variable(tf.constant(0.1, shape=[num_classes]))
    value = tf.transpose(value, [1, 0, 2])
    last = tf.gather(value, int(value.get_shape()[0]) - 1)

    prediction = (tf.matmul(last, weight) + bias)
    correctPred = tf.equal(tf.argmax(prediction,1), tf.argmax(labels,1))

    TP, TN, FP, FN = [tf.reduce_sum(prediction*labels), tf.reduce_sum((1-prediction)*(1-labels)), 
                      tf.reduce_sum(prediction*(1-labels)), tf.reduce_sum((1-prediction)*labels)]

    precision = TP / (TP + FP)
    recall = TP / (TP + FN)
    f1 = 2 * precision * recall / (precision + recall)
    accuracy = tf.reduce_mean(tf.cast(correctPred, tf.float32))
                
    loss = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits_v2(logits=prediction, labels=labels))
    optimizer = tf.train.AdamOptimizer().minimize(loss)

    tf.summary.scalar('Loss', loss)
    tf.summary.scalar('Accuracy', accuracy)

    sess = tf.InteractiveSession()
    saver = tf.train.Saver()
    logdir = "tensorboard/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S") + "/"
    writer = tf.summary.FileWriter(logdir, sess.graph)

    sess.run(tf.global_variables_initializer())

    print('epoch\ttrain_file\tbatch_size\ttest_precision\ttest_recall\ttest_f1\ttest_acc\ttrain_acc\tbest_acc\tepoch_time\tbest_acc_time\ttotal_time')
    epoch = 0
    best_acc = 0.
    start_time = time.time()
    total_time = time.time() - start_time
    best_acc_time = total_time
    while epoch < max_epochs or time_out_s > total_time:
        epoch = epoch + 1
        batches_indx = getBatchIndx(train_data_len, batch_size, shuffle=True)
        for train_index in batches_indx:
            train_x = [train_data[t] for t in train_index]
            train_y = [train_labels[t] for t in train_index]
            sess.run(optimizer, {input_data: train_x, labels: train_y})
            break

        test_x, test_y = getBatch(batch_size, test_data, test_labels)
        test_accuracy = sess.run(accuracy, {input_data: test_x, labels: test_y})*100
        temp_time = time.time() - start_time
        epoch_time = temp_time - total_time
        total_time = temp_time
        if best_acc < test_accuracy:
            best_acc = test_accuracy
            best_acc_time = total_time
            save_path = saver.save(sess, "models/pretrained_lstm.ckpt", global_step=epoch)
            print("Saved to %s" % save_path)
            print('Best test accuracy so far: %.2f' % best_acc)

        print('%d\t%s\t%d\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f'%
          (epoch, train_file, batch_size, 0.00, 0.00, 0.00, test_accuracy, 0.00, best_acc, epoch_time, best_acc_time, total_time))
        sys.stdout.flush()
    writer.close()

if __name__ == '__main__':
    np.set_printoptions(threshold=10000000, precision=2, suppress=True)
    random.seed(SEED)
    np.random.seed(SEED)
    train_file, vocab_file, emb_dim, max_epochs, time_out_h = sys.argv[1:6]
    emb_dim, max_epochs,time_out_h= [int(emb_dim), int(max_epochs), int(time_out_h)]
    test_file = train_file.replace('train', 'test')
    trainLstm(dim_proj=emb_dim, max_epochs=max_epochs, batch_size=75, time_out_s=time_out_h*60.0*60.0,
        vocab_file=vocab_file, train_file = train_file, test_file = test_file)