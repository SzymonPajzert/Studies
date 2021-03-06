#!/bin/bash
# Copyright (c) 2016 PaddlePaddle Authors, Inc. All Rights Reserved
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
set -e

cfg=${MODIFIER}trainer_config.py
model="output/pass-00005"

paddle train \
  --config=$cfg \
  --save_dir=./output2 \
  --num_passes=1 \
  --job=test \
  --log_period=1000 \
  --use_gpu=false \
  --init_model_path=$model \ 
  2>&1 | tee 'test.log'
