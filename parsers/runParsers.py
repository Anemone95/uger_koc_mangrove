#!/usr/bin/env python

import cPickle, re, os, string, random, io, sys
from os.path import isfile, join

import ParseUtil as parserUtil
import Parser4GNN as parser4GNN
import Parser4LSTM as parser4LSTM
import Parser4LSTMStar as parser4LSTMStar
import Parser4LSTMEncoding as parser4LSTMEncoding

project_dir='/Users/ukoc/Documents/workspace/mangrove/mangrove/'

owasp_abstract_list = ['BenchmarkTest\d+', 'testcode', 'moresafe', 'safe', 'Safe']
rw_abstract_list = ['okhttp3', 'freecs', 'biojava', 'owasp', 'juliet', 'ibatis', 'h2', 'jackrabbit', 'apollo', 'giraph', 'upm', 'joda','nbio', 'AFPChain', 'jetty', 'Astral', #'PLCommands', 'GJChronology',
	'ALKALI', 'ALKALINE', 'CIF', 'CTXSYS', 'DDLThrough','Polymer','RCSBDescription', 'PDB','zzz', 'zz', 'yy', 'aa1', '_17od', 'aaa', 'tz', 'AIML', 'NPETest', 'hsqldb', 'Susi', #'GZIPInput',
	'susi', 'cms', 'DAO', 'ai',  'BUREAUCRAT', 'eclipse', 'apache', 'mmcif', 'ctrip', 'Pdbx','ESParser','JPF', 'jpf', 'FFmul', 'chem', 'nasa', 'nio','Hsql', 'F\d+',
	'Saccharide14and14linking', 'Saccharide14and16linking']

and_abstract_list = ['DUMMY']

testsets_pw = [['Biojava','Freecs', 'Jackrabbit'], # 58+10+7=75
				['Susi'], # 93
				['Apollo', 'Giraph', 'JPF', 'Mybatis' ], # 10+9+42+18=79
				['Jetty',  'H2', 'UPM'], # 16+47+15=78
				['Hsqldb', 'Okhttp3', 'JodaTime']] # 58+12+5=75

owasp_cfg_data_dir = project_dir+'data/controlflowgraphs/owasp/'
owasp_slice_data_dir = project_dir+'data/slices/owasp/'

rw_cfg_data_dir = project_dir+'data/controlflowgraphs/rw/'
rw_slice_data_dir = project_dir+'data/slices/rw/'
and_slice_data_dir = project_dir+'data/slices/android/'

ts_slice_data_dir = project_dir+'data/slices/testsuite/'

#gnn_save_dir = project_dir+'datasets/gnn/'
gnn_save_dir = project_dir+'ggnn/data/android/'
lstm_save_dir = project_dir+'ml/lstm/data/'

def parseOwaspSlices(parser, save_dir):
	filePattern=project_dir+'data/testsets/owasp-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	data_name = 'owasp-slice'
	vocab = parser.processSliceFiles(data_name, owasp_slice_data_dir, save_dir, testsets, owasp_abstract_list, True)
	#parserUtil.printVocab(vocab)

def parseOwaspCFG(parser, save_dir):
	filePattern=project_dir+'data/testsets/owasp-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	data_name = 'owasp-cfg'
	vocab = parser.processCFGFiles(data_name, owasp_cfg_data_dir, save_dir, testsets, owasp_abstract_list, True)
	parserUtil.printVocab(vocab)

def parseRWCFG(parser, save_dir):
	filePattern=project_dir+'datasets/rw-cfg-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	data_name = 'rw-cfg-rand'
	vocab, counts = parser.processCFGFiles(data_name, rw_cfg_data_dir, save_dir, testsets, rw_abstract_list, True)
	parserUtil.printVocab(vocab, counts=counts)

def parseRWCFGPW(parser, save_dir):
	data_name = 'rw-cfg-pw'
	vocab, counts = parser.processCFGFiles(data_name, rw_cfg_data_dir, save_dir, testsets_pw, rw_abstract_list, False)
	parserUtil.printVocab(vocab, counts=counts)


def parseRWSlices(parser, save_dir):
	filePattern=project_dir+'data/testsets/rw-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	data_name = 'rw-slice-rand'
	vocab, counts = parser.processSliceFiles(data_name, rw_slice_data_dir, save_dir, testsets, rw_abstract_list, True)

def parseAndroidSlices(parser, save_dir):
	filePattern=project_dir+'datasets/and-test-fn-XYZ.txt'
	testsets=[]
	for x in xrange(1,6):
		with open(filePattern.replace('XYZ',str(x)),'r') as file: testsets.append(file.read().splitlines())
	data_name = 'and-slice'
	vocab, counts = parser.processSliceFiles(data_name, and_slice_data_dir, save_dir, testsets, and_abstract_list, True)
	parserUtil.printVocab(vocab)

def parseRWSlicesPW(parser, save_dir):
	data_name = 'rw-slice-pw'
	vocab, counts = parser.processSliceFiles(data_name, rw_slice_data_dir, save_dir, testsets_pw, rw_abstract_list, False)
	
def parseTSSlicesPWLSTMStar(parser, save_dir):
	testsets=[]
	with open(project_dir+'datasets/TestsuiteTestsetFileNames.txt','r') as file: 
		testsets.extend(file.read().splitlines())
	data_name = 'ts-slice'
	vocab, counts = parser.processSliceFiles(data_name, ts_slice_data_dir, save_dir, testsets, rw_abstract_list, True)
	parserUtil.printVocab(vocab)

def parseRWSlicesPWEncoding(parser, save_dir):
	data_name = 'rw-slice-pw'
	vocab, counts = parser.processSliceFiles(data_name, rw_slice_data_dir, save_dir, testsets_pw, rw_abstract_list, False)
	parserUtil.printVocab(vocab)
	
parser_map = {'gnn':parser4GNN, 'lstm':parser4LSTM, 'lstmStar':parser4LSTMStar, 'lstmEncoding':parser4LSTMEncoding}
if __name__ == '__main__':
	data_name = sys.argv[1]
	parser_name = sys.argv[2]
	save_dir = lstm_save_dir if 'lstm' in parser_name else gnn_save_dir
	running_parser = parser_map[parser_name]
	
	if data_name=="owasp-cfg":
		parseOwaspCFG(running_parser, save_dir)
	elif data_name=="owasp-slice":
		parseOwaspSlices(running_parser, save_dir)
	elif data_name=="rw-cfg-rand":
		parseRWCFG(running_parser, save_dir)
	elif data_name=="rw-cfg-pw":
		parseRWCFGPW(running_parser, save_dir)
	elif data_name=="rw-slice-rand":
		parseRWSlices(running_parser, save_dir)
	elif data_name=="rw-slice-pw":
		parseRWSlicesPW(running_parser, save_dir)
	elif data_name=="and-slice-rand":
		parseAndroidSlices(running_parser, save_dir)
	# elif data_name=="ts-slice-pw-lstmStar":
	# 	parseTSSlicesPWLSTMStar(running_parser, save_dir)
	# elif data_name=="rw-slice-pw-encoding":
	# 	parseRWSlicesPWEncoding(running_parser, save_dir)