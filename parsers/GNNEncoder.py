#!/usr/bin/env python

import cPickle, re, os, string, random, io
from os.path import isfile, join
import ParseUtil as parser
import numpy as np
import json
from collections import OrderedDict

import pickle as pkl
import networkx as nx
import scipy.sparse as sp
from scipy.sparse.linalg.eigen.arpack import eigsh


truePattern = re.compile(".*truepositive.txt")
#truePattern = re.compile(".*_tp_.*.txt")
stringPattern = re.compile(r'(#\([^);]+\))') 
dep_types = {'JM':0, 'HE': 1, 'PS': 2, 'CF': 3, 'NF': 4, 'JF': 5, 'RF': 6, 'UN': 7, 'CD': 8, 'CE': 9, 'CC': 10, 'JD': 11, 'NTSCD': 12, 'SD': 13, 'DD': 14, 'DH': 15, 'DA': 16, 'DL': 17, 'VD': 18, 'RD': 19, 'SU': 20, 'SH': 21, 'SF': 22, 'CL': 23, 'PI': 24, 'PO': 25, 'ID': 26, 'IW': 27, 'RY': 28, 'FORK': 29, 'FORK_IN': 30, 'FORK_OUT': 31, 'JOIN': 32, 'JOIN_OUT': 33, 'CONFLICT_DATA': 34, 'CONFLICT_ORDER': 35, 'FD': 36, 'FI': 37}

def createVector4Value(value, emb_map, emb_dim):
	tokens=value.split()
	t_encoding = np.zeros(emb_dim)
	for t in tokens:
		if emb_map.has_key(t):
			t_encoding = np.add(t_encoding, emb_map[t])
		else:
			t_encoding = np.add(t_encoding, emb_map['UNK'])
	return t_encoding / len(tokens)

def add2VocabularyEncodings(nid, kind, operation, value, vocab, emb_map, emb_dim, edges):
	key = nid + ' ' + kind + ' ' + operation+ ' ' + value + ' ' + ','.join([str(x) for x in edges])
	t_encoding = createVector4Value(value, emb_map, emb_dim)
	encoding = np.concatenate(([float(nid)], emb_map[kind], emb_map[operation], t_encoding, edges), axis=0)
	if vocab.has_key(key):
		assert(np.array_equal(vocab[key],encoding))
	else:
		vocab[key] = encoding
	return key

def getEdges(edges_str):
	slot_DD, slot_CD, slot_CF = [[0, 0], [0, 0], [0, 0]]
	indx_DD, indx_CD, indx_CF = [0, 0, 0]
	if edges_str != '':
		edges = edges_str.split(':')
		pairs =[ e.split(',') for e in edges]
		for edgetype, target in pairs:
			if indx_DD < 2 and edgetype == 'DD':
				slot_DD[indx_DD] = int(target)
				indx_DD = indx_DD + 1
			elif indx_CD < 2 and edgetype == 'CD':
				slot_CD[indx_CD] = int(target)
				indx_CD = indx_CD + 1
			elif indx_CF < 2 and edgetype == 'CF':
				slot_CF[indx_CF] = int(target)
				indx_CF = indx_CF + 1
	return np.concatenate((slot_DD, slot_CD, slot_CF), axis = 0)

def processSliceFiles(data_name, data_dir, save_dir, testsets, abstract_list, emb_map, emb_dim, rand_split):
	vocab={}
	save_files = [[open(save_dir+data_name+'-train-'+str(x)+'.txt','w'), 
					open(save_dir+data_name+'-test-'+str(x)+'.txt','w')]
					for x in xrange(1,6)]
	files = [join(data_dir, f) for f in os.listdir(data_dir) if isfile(join(data_dir, f))]
	for filePath in files:
		file_name = filePath.split('/')[-1]
		if file_name == '.DS_Store':
			continue
		instance = file_name if rand_split else file_name.split('.',1)[0]

		with open(filePath) as file: content = file.read()
		content = re.sub(r'#\((\d*)\)', ' \g<1> ', content)
		content = re.sub(r'#\((.)\)', ' \g<1> ', content)
		content = re.sub(r'#\(null\)', ' null ', content)
		content = re.sub(r'compile\(#\(.*\)\)', ' compile ( PATTERN ) ', content)

		all_matches=re.findall(stringPattern, content) #replace each unique string with STRING #
		#print file_name, set(all_matches)
		counter = 0
		for s in set(all_matches):
			content = content.replace(s, 'STRING ' + str(counter)+' ')
			counter = counter + 1
		
		lines= content.split('\n')
		sample = ''
		for line in lines:
			if line.strip() == '' or re.match(r'\[([0-9]+, )*[0-9]+\]', line):
				continue
			nid, kind, operation, ntype, value, edges = [ s.strip() for s in line.strip().split('::')]
			tokens=parser.parseSDGNodeValue(ntype+' '+value, abstract_list)
			edges = getEdges(edges)
			key=add2VocabularyEncodings(nid, kind, operation, tokens, vocab, emb_map, emb_dim, edges)
			sample += key + ' :: '

		sample += ('truepositive' if truePattern.match(file_name) else 'falsepositive')+'\n'
		save_sample(instance, save_files, sample, testsets)

	return vocab, None

def processSliceFilesJson(data_name, data_dir, save_dir, testsets, abstract_list, emb_map, emb_dim, rand_split):
	vocab={}
	word_id = 1
	# with open('dict.txt') as f: content = f.read().splitlines()
	# for l in content:
	# 	k,v = l.split(' ')
	# 	vocab[k] = int(v)	
	kinds_map=dict()
	operations_map=dict()
	types_map=dict()
	train_set = [[]]
	train_set.append([])
	train_set.append([])
	train_set.append([])
	train_set.append([])
	train_set.append([])
	test_set = [[]]
	test_set.append([])
	test_set.append([])
	test_set.append([])
	test_set.append([])
	test_set.append([])
	KOT, KOTI, Embed = [True, False, False]
	max_node_len = 0
	type_counter = operation_counter = kind_counter = max_nid = 1
	files = [join(data_dir, f) for f in os.listdir(data_dir) if isfile(join(data_dir, f))]
	for filePath in files:
		file_name = filePath.split('/')[-1]
		if file_name == '.DS_Store':
			continue
		instance = file_name if rand_split else file_name.split('.',1)[0]

		with open(filePath) as file: content = file.read()
		content = re.sub(r'#\((\d*)\)', ' \g<1> ', content)
		content = re.sub(r'#\((.)\)', ' \g<1> ', content)
		content = re.sub(r'#\(null\)', ' null ', content)
		content = re.sub(r'compile\(#\(.*\)\)', ' compile ( PATTERN ) ', content)

		all_matches=re.findall(stringPattern, content) #replace each unique string with STRING #
		print file_name
		counter = 0
		for s in set(all_matches):
			content = content.replace(s, 'STRING ' + str(counter)+' ')
			counter = counter + 1
		
		lines= content.split('\n')
		sample = ''
		node_features = []
		nids={}
		graph={}
		nid_counter = 0
		for line in lines:
			if line.strip() == '' or re.match(r'\[([0-9]+, )*[0-9]+\]', line):
				continue
			nid, kind, operation, ntype, value, edges = [ s.strip() for s in line.strip().split('::')]

			nid=int(nid)
			nids[nid] = nid_counter
			nid_counter += 1

			if not kinds_map.has_key(kind):
				kinds_map[kind] = kind_counter
				kind_counter +=1

			if not operations_map.has_key(operation):
				operations_map[operation] = operation_counter
				operation_counter +=1

			if not types_map.has_key(ntype):
				types_map[ntype] = type_counter
				type_counter +=1

			tokens = parser.parseSDGNodeValue(ntype+' '+value, abstract_list)#
			tokensArr = tokens.split(' ')
			node_len=len(tokensArr)
			if node_len > max_node_len and 'PHI' not in tokens:
				max_node_len = node_len
				print(max_node_len, '  ::  ', tokens)
			
			graph[nid]=[]
			if edges != '':
				edge_pairs =[ e.split(',') for e in edges.split(':')]
				for dep_type, target_id in edge_pairs:
					graph[nid].append([dep_types[dep_type], int(target_id)])
			
			node_rep = []
			if Embed:
				t_encoding = createVector4Value(tokens, emb_map, emb_dim)
				node_rep = np.concatenate((emb_map[kind], emb_map[operation], t_encoding), axis=0)
				node_rep = node_rep.tolist()
			elif KOT:
				node_rep = [kinds_map[kind], operations_map[operation], types_map[ntype]]
			elif KOTI:
				token = extractToken(operation, value)
				if not vocab.has_key(token):
					vocab[token] = word_id
					word_id += 1
				node_rep = [kinds_map[kind], operations_map[operation],types_map[ntype],vocab[token]]
			
			# node_features.append(node_rep)

		graph = adjust_ids(nids, graph)
		# datapoint=OrderedDict()
		# datapoint['targets']=[[0,1] if truePattern.match(file_name) else [1,0]]
		# datapoint['graph']=graph
		# datapoint['node_features']=node_features
		
		# dataset = []
		# dataset.append(datapoint)
		# with open(save_dir+'singles/owasp/'+file_name.replace('.txt','.json'), 'w') as train_setfile: 
		# 	json.dump(dataset, train_setfile)

		# for x in xrange(0,5):
		# 	if instance in testsets[x]:
		# 		test_set[x].append(datapoint)
		# 	else:
		# 		train_set[x].append(datapoint)
		
		if max_nid< len(nids):
			max_nid = len(nids)

	print(max_node_len)
	# for x in xrange(1,6):
	# 	with open(save_dir+data_name+'-train-'+str(x)+'.json', 'w') as train_setfile: 
	# 		json.dump(train_set[x-1], train_setfile)
	# 	with open(save_dir+data_name+'-test-'+str(x)+'.json', 'w') as test_setfile: 
	# 		json.dump(test_set[x-1], test_setfile)

def adjust_ids(nids, graph):
	n_graph=[]
	for key, value in graph.iteritems():
		for dep_type, target_id in value:
			n_graph.append([nids[key], dep_type, nids[target_id]])

	n_graph = sorted(n_graph, key=lambda x:x[0])
	return n_graph

def save_sample(instance, save_files, sample, testsets):
	for x in xrange(0,5):
		indx = 1 if instance in testsets[x] else 0
		save_files[x][indx].write(sample)


def extractToken(operation, value):
	token = ''
	if operation == 'empty':
		token = value
	elif operation == 'intconst':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'floatconst':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'charconst':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'stringconst':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'functionconst':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'shortcut':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'question':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'binary':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'unary':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'derefer':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'refer':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'array':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'select':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'reference':
		token = 'p0' if 'p0' in value else 'aggregate'
	elif operation == 'declaration':
		token = 'aggregate' if '[]' in value else 'object'
	elif operation == 'modify':
		token = 'p0' if 'p0' in value else 'var'
	elif operation == 'modassign':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'assign':
		token = 'PHI' if 'PHI' in value else 'assign' 
	elif operation == 'IF':
		token = '0' if '#(0)' in value else 'null'
	elif operation == 'loop': 
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'jump':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'compound':
		token = 'return' if 'return' in value else 'goto'
	elif operation == 'call':
		end = value.find('(')
		start =  (value[0:end].rfind('.') + 1) if '.' in value[0:end] else (value[0:end].rfind(' ') + 1)
		token = value[start:end]
	elif operation == 'entry' or operation == 'exit':
		end = value.find('(') - 1
		start =  value[0:end].rfind('.') + 1
		token = value[start:end]
	elif operation == 'form':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'act':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'monitor':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	elif operation == 'summary':
		start = value.find('') + 1
		end =  value.find('') - 1
		token = value[start:end]
	return token