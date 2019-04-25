#!/usr/bin/env python

import cPickle, re, os, string, random, io
from os.path import isfile, join
import ParseUtil as parser

truePattern = re.compile(".*truepositive.txt")
stringPattern = re.compile(r'(#\([^);]+\))') 

def processSliceFiles(data_name, data_dir, save_dir, testsets, abstract_list, rand_split):
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
			try:
				nid, kind, operation, ntype, value, edges = line.strip().split('::')
				tokens=parser.parseSDGNodeValue(ntype+' '+value, abstract_list)
				parser.add2Vocabulary(tokens, vocab)
				sample += kind.strip() + ' ' + operation.strip()+ ' ' + tokens + ' :: '
			except ValueError as ex:
				print file_name, line

		sample += ('truepositive' if truePattern.match(file_name) else 'falsepositive')+'\n'
		save_sample(instance, save_files, sample, testsets)

	return vocab, None

def processCFGFiles(data_name, data_dir, save_dir, testsets, abstract_list, rand_split=True):
	vocab={}
	save_files = [[open(save_dir+data_name+'-train-'+str(x)+'.txt','w'), 
					open(save_dir+data_name+'-test-'+str(x)+'.txt','w')]
					for x in xrange(1,6)]
	files = [join(data_dir, f) for f in os.listdir(data_dir) if isfile(join(data_dir, f))]
	for filePath in files:
		filename = filePath.split('/')[-1]
		if filename == '.DS_Store':
			continue
		instance = filename if rand_split else filename.split('.',1)[0]
		with open(filePath) as file: lines = file.readlines()
		sample=''
		content_started=False
		for line in lines:
			if line.strip() == 'Instructions:':
				content_started=True
				continue
			if not content_started or line.startswith('BB') or line.strip()=='':
				continue
			line = re.sub(r'\s\s+',' ', line.strip())
			value = line if not line[0].isdigit() else line.strip().split(' ', 1)[1]
			sample += parser.parseCFGNodeValue(value, abstract_list) + ' :: '

		parser.add2Vocabulary(sample, vocab)
		sample += ('truepositive' if truePattern.match(filename) else 'falsepositive')+'\n'
		save_sample(instance, save_files, sample, testsets)
	return vocab, None

def save_sample(instance, save_files, sample, testsets):
	for x in xrange(0,5):
		indx = 1 if instance in testsets[x] else 0
		save_files[x][indx].write(sample)
