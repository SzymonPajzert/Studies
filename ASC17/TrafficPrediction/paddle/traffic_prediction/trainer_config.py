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
from paddle.trainer_config_helpers import *
from consts import *
################################### DATA Configuration #############################################
is_predict = get_config_arg('is_predict', bool, False)
trn = './data/train.list' if not is_predict else None
tst = './data/test.list' if not is_predict else './data/pred.list'
process = 'process' if not is_predict else 'process_predict'
define_py_data_sources2(
    train_list=trn, test_list=tst, module="dataprovider", obj=process)
################################### Parameter Configuaration #######################################
batch_size = 128 if not is_predict else 1
settings(
    batch_size=batch_size,
    learning_rate=1e-3,
    learning_method=AdamOptimizer(),
    regularization=L2Regularization(8e-4))
################################### Algorithm Configuration ########################################
data = data_layer(name='data', size=TERM_NUM)
emb = embedding_layer(input=data, size=2*TERM_NUM)

lstm = simple_lstm(
    input=emb,
    size=2*TERM_NUM,
    lstm_cell_attr=ExtraAttr(drop_rate=0.25))

lstm_max = pooling_layer(input=lstm, pooling_type=MaxPooling())

output = fc_layer(input=lstm_max, size=4, act=SoftmaxActivation())

if is_predict:
    maxid = maxid_layer(output)
    outputs(maxid)
else:
    label = data_layer(name="label", size=4)
    cls = classification_cost(input=output, label=label)
    outputs(cls)
