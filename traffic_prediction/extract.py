#!/usr/bin/python

from sys import argv

script, input_file, train_input_file, train_output_file = argv

def split(array):
    input_data = array[0:-24]
    output_data = array[-24:]
    return input_data, output_data

with open(input_file) as inpt, open(train_input_file, 'w') as train_input, open(train_output_file, 'w') as train_output:
    header = inpt.readline()
    spl = header.rstrip('\n\r').split(',')
    input_data, output_data = split(spl)
    
    train_input.write('id,' + ','.join(input_data) + '\n')
    train_output.write('id,' + ','.join(output_data) + '\n')
    
    for line in inpt:
        splitted = line.rstrip('\n\r').split(',')
        number = splitted[0]
        input_data, output_data = split(splitted[1:])
        train_input.write(str(number) + ',' + ','.join(input_data)+'\n')
        train_output.write(str(number) + ',' + ','.join(output_data) + '\n')     
