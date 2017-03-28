#!/bin/bash

./train.sh
./predict.sh 24
./extract.py data/speeds.csv data/dump results.csv

rm data/dump
