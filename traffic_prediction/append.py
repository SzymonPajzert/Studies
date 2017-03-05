from __future__ import print_function
from sys import argv

filename, result_name, train_input_name, train_output_name = argv

with open(result_name) as result, open(train_input_name) as train_input, open(train_output_name, 'w') as train_output:
    header = train_input.readline().rstrip(';\n\r').split(',')
    old_id = int(header[len(header)-1])
    new_id = old_id+5 if old_id % 100 != 60 else old_id+40     
    header.append(str(new_id))
    print(','.join(header), file=train_output)
    
    for result, line in zip(result, train_input):
        print(line.rstrip(';\n\r') + ',' + result.rstrip(';\n\r'), file=train_output)
