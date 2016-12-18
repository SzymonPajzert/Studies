#ifndef GRAPH_H
#define GRAPH_H

#include <map>

template<typename input_type>
class Graph {
public:
    using node_id = size_t;

    Graph(std::istream & input, int threadNumber) : next_id(0) {
        input_type a_input, b_input;
        while(input >> a_input >> b_input) { // TODO make it work xd
            auto a = represent(a_input);
            auto b = represent(b_input);
            add_edge(a, b);
        }
    }

    node_id node_number() const {
        return next_id;
    }

    input_type node_representation(node_id node) const {
        return representation.at(node);
    }

    std::vector<node_id> neighbours(node_id node) const {
        return graph.at(node);
    }

private:
    node_id next_id;
    std::map<node_id, std::vector<node_id> > graph;
    std::map<node_id, input_type> representation;

    size_t represent(input_type a) {
        auto node = representation.find(a);
        size_t result;

        if(node == representation.end()) {
            result = next_id;
            representation.emplace(a, next_id);
            next_id++;
        } else {
            result = node->second;
        }

        return result;
    }

    void add_edge(node_id a, node_id b) {
        //TODO
    }
};


#endif //GRAPH_H
