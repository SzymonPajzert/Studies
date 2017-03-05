#!/bin/bash
set -e

cfg=graph_trainer_config.py
# pass choice 
model="output_new/pass-00009"

for i in `seq 1 $1`; do
	paddle train \
		   --config=$cfg \
		   --use_gpu=false \
		   --job=test \
		   --init_model_path=$model \
		   --config_args=is_predict=1 \
		   --predict_output_dir=.
	python ./append.py ./rank-00000 data/speeds.csv data/temp.data
        mv data/temp.data data/speeds.csv
done
