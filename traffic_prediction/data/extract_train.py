#!/usr/bin/python

from sys import argv

script, input_file, train_file, test_input_file, test_output_file = argv

def split(array):
    input_data = array[0:-24]
    output_data = array[-24:]
    return input_data, output_data

def cut(array):
    return array[:-(22*12)]

def get(array):
    array2 = cut(array)
    return split(array2)

with open(input_file) as inpt, open(train_file, 'w') as train, open(test_input_file, 'w') as test_input, open(test_output_file, 'w') as test_output:
    header = inpt.readline()
    spl = header.split(',')[1:]
    input_data, output_data = get(spl)
    
    test_input.write('id,' + ','.join(input_data) + '\n')
    test_output.write('id,' + ','.join(output_data) + '\n')
    
    train.write(header)
    
    for line_num, line in enumerate(inpt):
        if line_num % 10 != 0:
            train.write(line)
        else:
            input_data, output_data = get(line.split(','))
            test_input.write(','.join(input_data)+'\n')
            test_output.write(','.join(output_data) + '\n')     
