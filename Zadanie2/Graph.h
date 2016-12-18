#ifndef GRAPH_H
#define GRAPH_H

class Graph {

public:

    Graph(std::istream input, int threadNumber);

    using node_id = size_t;
    node_id nodeNumber() const;
    int nodeRepresentation(node_id node) const;

    std::vector<node_id> neighbours(node_id node) const;
};


#endif //GRAPH_H
