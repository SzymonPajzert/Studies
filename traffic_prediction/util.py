#!/usr/bin/python3

import sys
from math import sqrt

def pprint(d):
    print("{" + "\n".join("{}: {}".format(k, v) for k, v in d.items()) + "}")


def calculate_square(first_file_name, second_file_name):
    with open(first_file_name) as first_file, open(second_file_name) as second_file:
        first_file.readline()
        second_file.readline()

        temp_results = dict()
        results = dict()

        # add results from first file
        for line in first_file:
            identifier, *str_results = line.split(',')
            result = map(int, str_results)
            temp_results[int(identifier)] = result

        # delete results from second file
        for line in second_file:
            identifier, *str_results = line.split(',')
            result = map(int, str_results)
            prev_result = temp_results[int(identifier)]

            res = list()
            
            for (a, b) in zip(result, prev_result):
                res.append(abs(a - b))

            results[int(identifier)] = res
                    
        del temp_results

        # pprint(results)

        counter = 0
        squares = 0
        for line in results.values():
            print(line)
            counter += len(line)
            for value in line:
               squares += value * value

        del results

        return sqrt(squares / counter)

result = calculate_square(sys.argv[1], sys.argv[2])
print('Result is: ' + str(result))
