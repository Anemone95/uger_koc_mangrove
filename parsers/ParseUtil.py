#!/usr/bin/env python

import cPickle, re, os, string, random
from os.path import isfile, join

methodPrefixes = ['null','STRING','this','is','to','get','set','add','Listener','equal','basic','binary','bit','block', 'android',
		'check','class','close','command','column','convert','create','new','file','current','contain','database', 'receive', 'click',
		'date','decode','encode','default','define','delete','directory','domain','download','dynamic','enable', 'local', 'public',
		'disable','execute','FIELD','field','find','first','focus','generate','generic','has','include','init', 'secure',
		'initialize','input','insert','write','read','window','warning','index','verify','user','url','update','type', 'send',
		'text','test','target','source','table','stream','status','sql','space','socket','show','server','select','search', 'simple',
		'scroll','schema','scan','save','safe','run','row','resolve','request','response','remove','register','read','random', 'commit',
		'quote','query','put','provider','property','process','print','prepare','perform','pattern','path','parse','parameter', 'xml',
		'other','ordinal','open','old','object','next','new','method','menu','max','min','log','load','list','link','line','last', 'do']
#y=['okhttp3','freecs','biojava','owasp','juliet','ibatis','h2','hsqldb','jackrabbit','apollo','giraph','upm','joda']

prefixesPattern ='|'.join(methodPrefixes);

arbitraryThings=['javax.servlet.http.','java.util.','java.lang.','\.\.\.','_','\.', '#', ';', ',', '->','&#\d+', '\d+:']
arbitraryThingsPattern ='|'.join(arbitraryThings);

tokenazables=['\(','\)','\[','\]','{','}','\$','\'', ':', 'XML']
tokenazablesPattern='|'.join(tokenazables);

replaceMap={ '@': ' @ ', ' -0 ':' 0 ','= =':' == ','! =':' != ', ' &':' & ', '& &':'&&', 'a-z':' a-z ', '\d':' \d ', '?':' ? ', '\s':' \s ', '`':' ` '}

def parseSDGNodeValue(node_value, to_abstract_list):
	abstractionPattern='|'.join(to_abstract_list);
	
	value = re.sub(r'(v[0-9]+).([a-zA-Z]+)', '\g<1> \g<2>', node_value)# v17.getNextException
	value = re.sub(r' \.([a-zA-Z]+)', ' \g<1> ', value)# .getNextException
	value = re.sub(r'L([a-zA-Z0-9]+(/[a-zA-Z0-9])+)', '\g<1>', value)# Lorg/h2/message/DbException to org/h2/message/DbException
	value = value.replace('/', '.')# org/h2/message/DbException to org.h2.message.DbException
	value = re.sub(r'<([a-zA-Z0-9]+)>?', ' \g<1> ', value)
	value = re.sub(r'('+arbitraryThingsPattern+')', ' ', value)
	value = re.sub(r'('+tokenazablesPattern+')', ' \g<1> ', value)
	
	#abraction
	value = re.sub(r'('+abstractionPattern+')', ' ARBITRARY ', value) #abracted things

	#extraction
	value = re.sub(r'v(\d+)','variable \g<1> ', value) # v1 -> variable 1
	value = re.sub(r'( |^)('+prefixesPattern+')', ' \g<2> ', value)	
	value = re.sub(r'([A-Z]+[a-z0-9]*)', ' \g<1> ', value) # StringBuilder -> String Builder
	value = re.sub(r'([A-Z]+)([A-Z]+[a-z0-9]+)', '\g<1> \g<2>', value) # PDFConfigReader -> PDF Config Reader
	value = re.sub(r'( [A-Za-z]+)(\d+)\s', ' \g<1> \g<2> ', value) #file1 -> file 1 Binary180 -> Binary 180

	#number abstraction
	value = re.sub(r'( |^)-?([0-9]+)[eE]-([0-9])($| )', ' NSMALL ', value) # numbers in scientific notation e.g. 0.12e-43
	value = re.sub(r'( |^)\d\d\d+($| )', ' N3P ', value) # 3+ digit numbers
	value = re.sub(r'( |^)-\d\d\d+($| )', ' NN3P ', value) # 3+ digit negative numbers
	
	value = re.sub(r'key[a-zA-Z0-9\-]+', ' key ', value) # remove key56988 Owasp specific pattern
	
	value = re.sub(r'([\w\']+)>',' \g<1> ', value)
	value = re.sub(r'(:@?|\w+)<',' \g<1> ', value)
	value = re.sub(r'(\w+):', ' \g<1> ', value)
	value = re.sub(r'(\w+)-\s', ' \g<1> ', value)
	value = re.sub(r'([a-zA-Z]+)-', ' \g<1> ', value)

	for key,val in replaceMap.iteritems():
		value=value.replace(key,val)
		value = re.sub(r'\s\s+', ' ', value)

	return value.rstrip()

def parseCFGNodeValue(node_value, to_abstract_list):
	abstractionPattern='|'.join(to_abstract_list);
	
	value = re.sub(r'\(line.*\)', '', node_value) #Remove (line ...)
	#value = re.sub(r'^\d+', '', value) #Remove numbers in the begining
	#value = re.sub(r'@\d*', ' ', value) #Remove @... tokens
	
	value = re.sub(r'L([a-zA-Z0-9]+(/[a-zA-Z0-9])+)', '\g<1>', value)# Lorg/h2/message/DbException to org/h2/message/DbException
	value = value.replace('/', '.')# org/h2/message/DbException to org.h2.message.DbException
	value = value.replace('III', 'I I I')# org/h2/message/DbException to org.h2.message.DbException
	value = value.replace('invoke', 'invoke ')# org/h2/message/DbException to org.h2.message.DbException
	value = re.sub(r'v(\d+)',' variable \g<1> ', value)
	value = re.sub(r'('+abstractionPattern+')', ' ARBITRARY ', value) #abracted things
	value = re.sub(r'( |^)('+prefixesPattern+')', ' \g<2> ', value)
	value = re.sub(r'('+arbitraryThingsPattern+')', ' ', value)
	value = re.sub(r'('+tokenazablesPattern+')', ' \g<1> ', value)

	value = re.sub(r'@', ' @ ', value) #Make room for @
	value = re.sub(r',\s+|,$', ' ', value) #Remove , at the end of a token
	value = re.sub(r'<|>', ' ', value) #Remove < and >
	value = re.sub('Application(,\s)*','', value) #remove Application part
	value = re.sub(r'\{(.*?)\}', '{ \g<1> }', value) #Make room for { and } tokens
	#value = re.sub(r'(\d)+=\[(.*?)\]', 'v\g<1> = \g<2> ', value) #Make room form stuff inside {...}
	value = re.sub(r'\s+,\s+|\s+,$', ' ', value) #Remove , in the middle of nothing
	
	value = re.sub(r'([A-Z]+[a-z0-9]*)', ' \g<1> ', value) # StringBuilder -> String Builder
	value = re.sub(r'([A-Z]+)([A-Z]+[a-z0-9]+)', ' \g<1> \g<2> ', value) # PDFConfigReader -> PDF Config Reader
	value = re.sub(r'( [A-Za-z]+)(\d+)\s', ' \g<1> \g<2> ', value) #file1 -> file 1 Binary180 -> Binary 180

	value = re.sub(r'( |^)-?([0-9]+)[eE]-([0-9])($| )', ' NSMALL ', value) # numbers in scientific notation e.g. 0.12e-43
	value = re.sub(r'( |^)\d\d\d+($| )', ' N3P ', value) # 3+ digit numbers
	value = re.sub(r'( |^)-\d\d\d+($| )', ' NN3P ', value) # 3+ digit negative numbers
	
	value = re.sub(r':|;', ' ', value) #Remove : ; ( )
	value = re.sub(r'#', ' # ', value) #I don't know what # means
	value = re.sub(r'=', ' = ', value) #Make room for = operation
	value = re.sub(r']', ' ] ', value) #Make room for = operation

	value = re.sub(r'(\w+?)\[(\w+?)\]', '\g<1> [ \g<2> ]', value) #fix v6[v25]

	value = re.sub(r'\[(\w+?)', '\g<1>', value) #Remove random [ in the code
	value = re.sub(r',', ' ', value)

	for key,val in replaceMap.iteritems():
		value=value.replace(key,val)
	
	value = re.sub(r'\s\s+', ' ', value) # remove multi spaces
	return value.rstrip().strip()

def add2Vocabulary(sample, vocab):
	tokens=sample.split()
	for t in tokens:
		vocab[t] = 1 if not vocab.has_key(t) else (vocab[t]+1)

def printVocab(vocab, counts=None):
	wordID = 3
	if counts==None:
		for key, value in vocab.iteritems():
			print(key +' '+ str(wordID))
			wordID = wordID + 1
	else:
		for key, value in vocab.iteritems():
			if counts[value]>5:
				print(key +' '+ str(value))
