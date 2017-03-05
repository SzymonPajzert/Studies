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

DIR="$( cd "$(dirname "$0")" ; pwd -P )"
cd $DIR
TAR_NAME=traffic_data.tar.gz

if [ ! -f $TAR_NAME ]; then
	#download the dataset
	echo "Downloading traffic data..."
	wget http://paddlepaddle.cdn.bcebos.com/demo/traffic/$TAR_NAME
	
	#extract package
	echo "Unzipping..."
	tar -zxvf $TAR_NAME
else
	echo "Package exists"
fi

python extract_train.py "speeds.csv" "train_input.data" "train_output.data"

echo "data/train_input.data" > train.list
echo "data/train_input.data" > test.list
echo "data/train_input.data" > pred.list

echo "Done."
