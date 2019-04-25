#!/usr/bin/env python

import re, os, string, random, io, numpy as np
from os.path import isfile, join

vocab={}

def loadDictionary(dictFile):
	with open(dictFile) as file: lines = file.read().split('\n')
	for line in lines:
		token, tid = line.split(' ')
		tid=int(tid)
		vocab[token] = tid

def createBoW(data_name, data_dir, save_dir, approach):
	vocab_len = len(vocab)
	# metadata='@RELATION '+data_name+'-code\n\n'+'\n'.join('@ATTRIBUTE '+str(x)+'t {0,1}' 
	# 	for x in range(0,vocab_len+3)) + '\n@ATTRIBUTE label {0b,1b}\n\n@DATA\n'
	feature_type = '{0,1}' if approach == 'Occ' else 'Numeric'
	metadata='@RELATION '+data_name+'-code\n\n'+'\n'.join('@ATTRIBUTE '+str(x)+'t ' + feature_type 
		for x in range(0,vocab_len+3)) + '\n@ATTRIBUTE label {0b,1b}\n\n@DATA\n'

	files = [join(data_dir, f) for f in os.listdir(data_dir) if isfile(join(data_dir, f))]
	for filePath in files:
		file_name = filePath.split('/')[-1]
		if file_name == '.DS_Store' or 'dict.txt' in file_name or '-embeddings' in file_name or file_name.startswith(data_name):
			continue

		with open(filePath) as file: lines = file.read().split('\n')
		feature_vecs = []
		for line in lines:
			feature_vec = np.zeros(vocab_len + 4, dtype=np.int32)
			if 'truepositive' in line:
				feature_vec[vocab_len + 3] = 1
			for t in line.split(' '):
				key=t.strip()
				if key == '' or 'epositive' in key: 
					continue
				if not vocab.has_key(key):
					feature_vec[1] = 1
				elif approach == 'Freq':
					feature_vec[vocab[key]] = 1 + feature_vec[vocab[key]]
				elif approach == 'Occ':
					feature_vec[vocab[key]] = 1

			feature_vecs.append(feature_vec)
			break

		arrffFilename = save_dir+file_name.replace('.txt','-'+approach+'.arff')
		np.savetxt(arrffFilename, feature_vecs, fmt='%i', delimiter=",")
		line_prepender(arrffFilename, metadata)		
			
		# arrffFilename = save_dir+file_name.replace('.txt', '-'+approach+'.arff')
		# np.savetxt(arrffFilename, feature_vecs, fmt='%i', delimiter=",")
		# line_prepender(arrffFilename, metadata)


def line_prepender(filename, line):
    with open(filename, 'r+') as f:
        content = f.read()
        f.seek(0, 0)
        f.write(line.rstrip('\r\n') + '\n' + content)

if __name__ == '__main__':
	dataset = 'Benchmark'
	loadDictionary('../ml/lstm/data/extraction/rw-dict.txt')
	#createBoW(dataset, '../ml/lstm/data/extraction/', '../ml/bow/', 'Occ')
	#createBoW(dataset, '../ml/lstm/data/extraction/', '../ml/bow/', 'Freq')
	createBoW(dataset, '../ml/lstm/data/singles/', '../ml/bow/singles/', 'Occ')
	createBoW(dataset, '../ml/lstm/data/singles/', '../ml/bow/singles/', 'Freq')
