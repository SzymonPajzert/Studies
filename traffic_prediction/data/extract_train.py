#!/usr/bin/python

from sys import argv

script, input_file, train_file, test_file = argv

with open(input_file) as inpt, open(train_file, 'w') as train, open(test_file, 'w') as test:
    header = inpt.readline()
    train.write(header)
    test.write(header)
    
    for line_num, line in enumerate(inpt):
        if line_num % 10 != 0:
            train.write(line)
        else:
            test.write(line)
