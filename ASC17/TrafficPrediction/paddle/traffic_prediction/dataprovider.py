from paddle.trainer.PyDataProvider2 import *
import sys
import numpy as np

from consts import *

def initHook(settings, file_list, **kwargs):
    # Modify a little bit default initHook
    
    del kwargs  #unused 

    settings.pool_size = sys.maxint
    #Use a time seires of the same time in past as feature.
    #Dense_vector's expression form is [float,float,...,float]
    settings.input_types = {
        'data': integer_value_sequence(TERM_NUM),
        'label': integer_value(LABEL_VALUE_NUM)}

def sequences(size, array):
    filtered = filter(lambda x: x > -1, array)

    res = list()
    for i in xrange(len(filtered) - size + 1):
        res.append(filtered[i:i+size])

    return res

def get_samples(line):
    _speeds = map(int, line.rstrip('\r\n').split(",")[1:])
    speeds  = [j - 1 for j in map(int, _speeds)]
    
    # Scanning and generating samples
    for i in xrange(TERM_NUM):
        for s in sequences(TERM_NUM+1, speeds[i::DATES_IN_DAY]): 
            yield s
    
@provider(
    init_hook=initHook, cache=CacheType.CACHE_PASS_IN_MEM, should_shuffle=True)
def process(settings, file_name):
    with open(file_name) as f:
        #abandon fields name
        f.readline()
        for line in f:
            for sample in get_samples(line):
                yield {'data': sample[:-1], 'label': sample[-1]}
            

def predict_initHook(settings, file_list, **kwargs):
    settings.pool_size = sys.maxint
    settings.input_types = {
        'data': integer_value_sequence(TERM_NUM)} 



@provider(init_hook=predict_initHook, should_shuffle=False)
def process_predict(settings, file_name):
    with open(file_name) as f:
        #abandon fields name
        f.readline()
        for line in f: 
            _speeds = map(int, line.rstrip('\r\n').split(",")[1:])
            speeds  = [j - 1 for j in _speeds]

            # Scanning and generating samples
            for time in range(8*12+1, 10*12+1):
                # Get data corresponding to the given date
                s = filter(lambda x: x!=-1, speeds[time::DATES_IN_DAY]) 

                # Get last TERM_NUM elts
                res = s[-TERM_NUM:]
                yield  {'data': res}
            
            
