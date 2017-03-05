from paddle.trainer_config_helpers import *
from consts import * 

father = dict()
child = dict()

with open('data/graph.csv') as graph_file:
    graph_file.readline()
    for line in graph_file:
        source, destination = map(int, line.split(','))
        father[source] = father.get(source, []) + [destination]
        child[destination] = child.get(destination, []) + [source]

################################### DATA Configuration #############################################
is_predict = get_config_arg('is_predict', bool, False)
trn = './data/train.list' if not is_predict else None
tst = './data/test.list' if not is_predict else './data/pred.list'
process = 'process' if not is_predict else 'process_predict'

define_py_data_sources2(
    train_list=trn,
    test_list=tst,
    module="graph_dataprovider",
    obj=process,
    args={"father_graph": father, "child_graph": child})

################################### Parameter Configuaration #######################################

batch_size = 128 if not is_predict else 1
settings(
    batch_size=batch_size,
    learning_rate=1e-3,
    learning_method=RMSPropOptimizer())

################################### Algorithm Configuration ########################################

time = data_layer(name='time', size=1)

# long term inputs from the same date previous days
long_prev = data_layer(name='long_prev', size=LONG_TERM_NUM)

# short term inputs from the previous dates the same day
short_prev = data_layer(name='short_prev', size=SHORT_TERM_NUM)

child_prev = data_layer(name='child_prev', size=NEIGH_PREV)

father_prev = data_layer(name='father_prev', size=NEIGH_PREV)

neighbour_input = concat_layer(name='neighbour', input=[father_prev, child_prev])
neighbour_emb_layer = fc_layer(input=neighbour_input, size=NEIGHBOUR_EMB_SIZE, act=LinearActivation())
neighbour_output = fc_layer(input=neighbour_emb_layer, size=2, act=LinearActivation())

neural_input = concat_layer(name='neural_input', input=[neighbour_output, long_prev, short_prev, time])
link_vec = fc_layer(input=neural_input, size=EMB_SIZE)
score = fc_layer(input=neural_input, size=4, act=SoftmaxActivation())

if is_predict:
    maxid = maxid_layer(score)
    outputs(maxid)
else:
    # Multi-task training.
    label = data_layer(name='label', size=LABEL_VALUE_NUM)
    cls = classification_cost(
        input=score, name="cost", label=label)
    outputs(cls)
