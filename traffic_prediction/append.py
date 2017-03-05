from __future__ import print_function
from sys import argv

filename, result_name, train_input_name, train_output_name = argv

with open(result_name) as result, open(train_input_name) as train_input, open(train_output_name, 'w') as train_output:
    header = train_input.readline().rstrip(';\n\r').split(',')
    header.append(str(int(header[len(header)-1])+5))
    print(','.join(header), file=train_output)
    
    for result, line in zip(result, train_input):
        print(line.rstrip(';\n\r') + ',' + result.rstrip(';\n\r'), file=train_output)
