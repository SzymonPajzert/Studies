from paddle.trainer.PyDataProvider2 import *
import sys
import numpy as np
from random import shuffle

from consts import *

def initHook(settings, father_graph, child_graph, **kwargs):
    # Modify a little bit default initHook
    
    del kwargs  #unused 

    settings.pool_size = sys.maxint
    settings.father = father_graph
    settings.child = child_graph

    settings.input_types = {
        'long_prev': dense_vector(LONG_TERM_NUM),
        'short_prev': dense_vector(SHORT_TERM_NUM),
        'child_prev': dense_vector(NEIGH_PREV),
        'father_prev': dense_vector(NEIGH_PREV),
        'time': dense_vector(1),
        'label': integer_value(LABEL_VALUE_NUM)}


def get_prev_days(speeds, number, identifier, day, time):
    if number > 0 and day >= 0:
        if speeds[identifier][day][time] == -1:
            return get_prev_days(speeds, number, identifier, day - 1, time)
        else:
            return get_prev_days(speeds, number-1, identifier, day - 1, time) + [speeds[identifier][day][time]]
    else:
        return []

def get_prev_times(speeds, number, identifier, day, time):
    if time < 0:
        _time = time + DATES_IN_DAY
        _day = day - 1
    else:
        _time = time
        _day = day
        
    if number > 0 and _day >= 0:
        if speeds[identifier][_day][_time] == -1:
            return get_prev_times(speeds, number, identifier, _day, _time-1)
        else:
            return get_prev_times(speeds, number-1, identifier, _day, _time-1) + [speeds[identifier][_day][_time]]
    else:
        return []    

def adjust_len(a):
    if len(a):
        shuffle(a)
        return a * (NEIGH_PREV // len(a)) + a[0:(NEIGH_PREV % len(a))]
    else:
        return []
    
@provider(
    init_hook=initHook, cache=CacheType.CACHE_PASS_IN_MEM, should_shuffle=True)
def process(settings, file_name):
    # three fold acess to speed:
    # link -> day -> time -> congestion    
    speeds = dict()
    times = []
    
    with open(file_name) as f:
        #Save fields names
        times = map(int, f.readline().split(',')[1:])
        for line in f:
            line_splits = map(int, line.rstrip('\r\n').split(","))
            identifier = line_splits[0]
            _speeds  = [j - 1 for j in line_splits[1:]]

            speeds[identifier] = dict()
            for i in xrange((len(_speeds) + DATES_IN_DAY - 1)//DATES_IN_DAY):
                speeds[identifier][i] = dict()
           
            for i in xrange(len(_speeds)):
                speeds[identifier][i//DATES_IN_DAY][i % DATES_IN_DAY] = _speeds[i]    

    for identifier in speeds:
        print("New identifier read")
        for day in speeds[identifier]:
            for time in speeds[identifier][day]:
                if day > 0 and speeds[identifier][day][time] != -1:
                    long_prev = get_prev_days(speeds, LONG_TERM_NUM, identifier, day-1, time)
                    short_prev = get_prev_times(speeds, SHORT_TERM_NUM, identifier, day, time-1)
                    if len(long_prev) == LONG_TERM_NUM and len(short_prev) == SHORT_TERM_NUM:
                        child = settings.child.get(identifier, [])
                        father = settings.father.get(identifier, [])

                        prev_time = time - 1
                        prev_day = day
                        if prev_time < 0:
                            prev_time = prev_time + DATES_IN_DAY
                            prev_day = day - 1
                        
                        child_prev = adjust_len(filter(lambda x: x != -1, map(lambda ide: speeds[ide][prev_day][prev_time], child)))
                        father_prev = adjust_len(filter(lambda x: x != -1, map(lambda ide: speeds[ide][prev_day][prev_time], father)))

			if child_prev and father_prev:
                            res = {
                                'time': [time],
                                'long_prev': long_prev,
                                'short_prev': short_prev,
                                'child_prev': child_prev if child_prev else father_prev,
                                'father_prev': father_prev if father_prev else chilf_prev,
                                'label': speeds[identifier][day][time]}

                            yield res
            

def predict_initHook(settings, file_list, **kwargs):
    settings.pool_size = sys.maxint
    settings.input_types = {
        'data': dense_vector(TERM_NUM)} 



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
                print(res)
                yield  {'data': res}
            
            
