#!/usr/bin/env python

import re
import os, io
from os.path import isfile, join
import sys
sys.path.insert(0, '../../scripts')

import ParseUtil as parser

tokens = {}
token_count = {}
next_token = 1
def get_token(word):
	global next_token
	if word in tokens:
		token_count[tokens[word]] += 1
		return tokens[word]
	else:
		tokens[word] = next_token
		next_token += 1
		token_count[tokens[word]] = 1
		return tokens[word]

nodeIDs = {}
next_node = 0
def get_node_ID(node):
	global next_node
	if node in nodeIDs:
		return nodeIDs[node]
	else:
		nodeIDs[node] = next_node
		next_node += 1
		return nodeIDs[node]

def processSliceFiles(data_name, data_dir, save_dir, testsets, abstract_list, rand_split):
	print('data_name:'+data_name+' data_dir:'+data_dir+' save_dir:'+save_dir+' rand_split:'+str(rand_split))
	save_files = [[open(save_dir+data_name+'-train-'+str(x)+'.txt','w'), 
					open(save_dir+data_name+'-test-'+str(x)+'.txt','w')]
					for x in xrange(1,6)]
	file_counter = 0
	graphs = []
	files = [join(data_dir, f) for f in os.listdir(data_dir) if isfile(join(data_dir, f))]
	for filename in files:
		if '.DS_Store' in filename:
			continue
		file_counter += 1
		#print(filename, file_counter)

		inp = io.open(filename, 'r', encoding='utf-8')

		out_edges = {}
		node_tokens = {}
		
		nodeIDs = {}
		next_node = 0
		for line in inp:
			if re.match('\[([0-9]+, )*[0-9]+\]', line):
				targets = [int(i) for i in line.strip()[1:-1].split(', ')]
			elif re.match('\d+ ::.*', line): #edge description line
				source, destination_list = line.strip().split('::')
				destination_list = destination_list.replace('<', '')
				destination_list = destination_list.split('>')[:-1] #:-1 to remove the last empty string
				def fix(dest):
					dest = dest.strip()
					dest = dest.split(',')
					if len(dest)>2:
						dest = [dest[0], int(dest[2])]
					else:
						dest = [dest[0], int(dest[1])]
					return dest
				destination_list = [fix(i) for i in destination_list]
				out_edges[int(source)] = destination_list
			elif line.strip() != '':
				node, value = line.strip().split(':', 1)
				temp_tokens = parser.parseSDGNodeValue(value, abstract_list)

				temp_tokens = temp_tokens.split(' ')
				temp_tokens = [i for i in temp_tokens if i!='']
				temp_tokens = [get_token(i) for i in temp_tokens]
				node_tokens[node.strip()] = temp_tokens

		#NEED TO ADD EDGE TYPES
		in_edges = {}
		# print([key for key in sorted(out_edges.keys())])
		for key in sorted(out_edges.keys()):
			get_node_ID(key)
		for key in out_edges.keys():
			in_edges.setdefault(get_node_ID(key), [])
			for dest in out_edges[key]:
				in_edges.setdefault(get_node_ID(dest[-1]), []).append(get_node_ID(key))
		for key in in_edges.keys():
			if in_edges[key] == []:
				in_edges[key] = [-1]

		node_tokens_old = node_tokens
		node_tokens = {}
		for key in in_edges.keys():
			node_tokens.setdefault(key, [-1])
		for key in node_tokens_old.keys():
			node_tokens[get_node_ID(int(key))] = node_tokens_old[key]
		for key in node_tokens.keys():
			if len(node_tokens[key]) == 0:
				node_tokens[key] = [-1]

		targets = [nodeIDs[i] for i in targets if i in nodeIDs]
		graphs.append((in_edges, node_tokens, targets, filename.split('/')[-1]))

	for graph in graphs:
		filename = graph[3]
		instance = filename if rand_split else filename.split('.',1)[0]
		in_edges = graph[0]
		in_edges_str = ' '.join([','.join(map(str, list(set(in_edges[key])) )) for key in sorted(in_edges)])
		
		node_tokens = graph[1]
		for key in node_tokens:
			node_tokens[key] = [i for i in node_tokens[key] if i == -1 or token_count[i]>5]
			if len(node_tokens[key]) == 0 :
				node_tokens[key] = [-1]
		
		tokens_str = ' '.join([','.join(map(str, node_tokens[key])) for key in sorted(node_tokens)])
		
		targets = graph[2]
		targets_str = ','.join(map(str, targets))

		dp_line=in_edges_str+'\t'+tokens_str+'\t'+targets_str+'\t'+str(int('truepositive' in filename.lower()))
		save_sample(instance, save_files, dp_line, testsets)
	return tokens, token_count


def processCFGFiles(data_name, data_dir, save_dir, testsets, abstract_list, rand_split):
	save_files = [[open(save_dir+data_name+'-train-'+str(x)+'.txt','w'), 
					open(save_dir+data_name+'-test-'+str(x)+'.txt','w')]
					for x in xrange(1,6)]
	file_counter = 0
	graphs = []
	files = [join(data_dir, f) for f in os.listdir(data_dir) if isfile(join(data_dir, f))]
	for filename in files:
		if '.DS_Store' in filename:
			continue
		file_counter += 1
		print(filename, file_counter)
		
		inp = io.open(filename, 'r', encoding='utf-8')
		
		for line in inp:
			if re.match('\[([0-9]+, )*[0-9]+\]', line):
				targets = [int(i) for i in line.strip()[1:-1].split(', ')]
			if line.strip() == 'CFG:':
				break

		out_edges = {}
		current = -1
		for line in inp:
			if line.strip() == 'Instructions:':
				break
			if line.startswith('BB'):
				line = line.strip().replace('BB','')
				line = re.sub(r'\[.*\]', '', line)
				current = int(line)
				out_edges.setdefault(current, [])
			else:
				line = line.strip().replace('-> BB','')
				out_edges[current].append(int(line))
				out_edges.setdefault(int(line), [])

		in_edges = {}
		for key in out_edges.keys():
			in_edges.setdefault(key, [])
			for dest in out_edges[key]:
				in_edges.setdefault(dest, []).append(key)

		for key in in_edges.keys():
			if in_edges[key] == []:
				in_edges[key] = [-1]

		node_tokens = {}
		for key in in_edges.keys():
			node_tokens.setdefault(key, [])

		current = -1
		count = 0
		for line in inp:
			if line.startswith('BB'):
				line = line.strip().replace('BB','')
				line = re.sub(r'\[.*\]|\(.*\)', '', line)
				line = re.sub(r'<.*>', '', line)
				current = int(line)
			elif line.strip() != '':
				line = re.sub(r'\s\s+',' ', line.strip())
				value = line if not line[0].isdigit() else line.strip().split(' ', 1)[1]
				value = parser.parseCFGNodeValue(value, abstract_list)
				node_tokens[current] += [get_token(i) for i in re.split(r'\s+', value)]

		for key in node_tokens.keys():
			if node_tokens[key] == []:
				node_tokens[key] = [-1]
		
		in_edges_str = ' '.join([','.join(map(str, list(set(in_edges[key])) )) for key in sorted(in_edges)])
		tokens_str =' '.join([','.join(map(str, node_tokens[key])) for key in sorted(node_tokens)])
		targets_str = '1'#.join(map(str, targets))
		
		indx = 0
		filename=filename.split('/')[-1]
		instance = filename if rand_split else filename.split('.',1)[0]
		dp_line=in_edges_str+'\t'+tokens_str+'\t'+targets_str+'\t'+str(int('truepositive' in filename.lower()))
		save_sample(instance, save_files, dp_line, testsets)
	return tokens, token_count

def save_sample(instance, save_files, sample, testsets):
	for x in xrange(0,5):
		indx = 1 if instance in testsets[x] else 0
		save_files[x][indx].write(sample+'\n')