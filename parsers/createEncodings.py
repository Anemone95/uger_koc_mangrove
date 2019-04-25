#!/usr/bin/env python

import cPickle, re, os, string, random, io, sys
from os.path import isfile, join
import numpy as np

import GNNEncoder as encoder

project_dir='/Users/ukoc/Documents/workspace/mangrove/mangrove/'

owasp_abstract_list = ['BenchmarkTest\d+', 'testcode', 'moresafe', 'safe', 'Safe']
rw_abstract_list = ['okhttp3', 'freecs', 'biojava', 'owasp', 'juliet', 'ibatis', 'h2', 'jackrabbit', 'apollo', 'giraph', 
	'upm', 'joda','nbio', 'AFPChain', 'jetty', 'Astral', #'PLCommands', 'GJChronology',
	'ALKALI', 'ALKALINE', 'CIF', 'CTXSYS', 'DDLThrough','Polymer','RCSBDescription', 'PDB','zzz', 'zz', 'yy', 'aa1', 
	'_17od', 'aaa', 'tz', 'AIML', 'NPETest', 'hsqldb', 'Susi', #'GZIPInput',
	'susi', 'cms', 'DAO', 'ai',  'BUREAUCRAT', 'eclipse', 'apache', 'mmcif', 'ctrip', 'Pdbx','ESParser','JPF', 'jpf', 
	'FFmul', 'chem', 'nasa', 'nio','Hsql', 'F\d+']

and_abstract_list = []

rw_slice_data_dir = project_dir+'data/slices/rw/'
owasp_slice_data_dir = project_dir+'data/slices/owasp/'
and_slice_data_dir = project_dir+'data/slices/android/'

ggnn_save_dir = project_dir+'ml/ggnn/data/'

def parseRWSlicesPWEncoding(parser, save_dir, emb_map, emb_dim):
	testsets_pw = [['Biojava','Freecs', 'Jackrabbit'], # 58+10+7=75
				['Susi'], # 93
				['Apollo', 'Giraph', 'JPF', 'Mybatis' ], # 10+9+42+18=79
				['Jetty',  'H2', 'UPM'], # 16+47+15=78
				['Hsqldb', 'Okhttp3', 'JodaTime']] # 58+12+5=75
	data_name = 'rw-slice-pw'
	parser.processSliceFilesJson(data_name, rw_slice_data_dir, save_dir, testsets_pw, rw_abstract_list, emb_map, emb_dim, False)
	
def parseRWSlicesRandEncoding(parser, save_dir, emb_map, emb_dim):
	filePattern=project_dir+'datasets/datasets_pre_ase/rw-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	data_name = 'rw-slice-rand'
	parser.processSliceFilesJson(data_name, rw_slice_data_dir, save_dir, testsets, rw_abstract_list, emb_map, emb_dim, True)

def parseOwaspSlices(parser, save_dir, emb_map, emb_dim):
	filePattern=project_dir+'data/testsets/owasp-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	parser.processSliceFilesJson('owasp-slice', owasp_slice_data_dir, save_dir, testsets, owasp_abstract_list, emb_map, emb_dim, True)

def parseAndroidSlicesRandEncoding(parser, save_dir, emb_map, emb_dim):
	filePattern=project_dir+'data/testsets/and-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	data_name = 'and-slice-rand'
	parser.processSliceFilesJson(data_name, and_slice_data_dir, save_dir, testsets, and_abstract_list, emb_map, emb_dim, True)

def load_embeddings(embeddings_file, emb_map):
    with open(embeddings_file) as file: lines = file.readlines()
    for line in lines:
        key, value = line.split('\t')
        if not emb_map.has_key(key):
        	emb_map[key] = np.array([float(x) for x in value.split(' ')])
    return emb_map

def printVocab(vocab, counts=None):
	wordID = 3
	for key, value in vocab.iteritems():
		print(str(wordID) + '|||' + key +'|||' + (' '.join(str(x) for x in value)))
		wordID = wordID + 1

if __name__ == '__main__':
	embeddings_file = project_dir + 'lstm/data/extraction/owasp-embeddings.txt'
	#embeddings_file = project_dir + 'lstm/data/extraction/android-embeddings.txt'
	#embeddings_file = project_dir + 'lstm/data/extraction/rw-embeddings2.txt'

	emb_map ={}
	emb_dim = 8
	
	load_embeddings(embeddings_file, emb_map)
	#parseRWSlicesPWEncoding(encoder, ggnn_save_dir, emb_map, emb_dim)
	#parseRWSlicesRandEncoding(encoder, ggnn_save_dir, emb_map, emb_dim)
	#parseAndroidSlicesRandEncoding(encoder, ggnn_save_dir, emb_map, emb_dim)
	parseOwaspSlices(encoder, ggnn_save_dir, emb_map, emb_dim)