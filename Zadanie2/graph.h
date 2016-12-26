#ifndef GRAPH_H
#define GRAPH_H

#include <cstddef>
#include <vector>
#include <map>

namespace model {
    template<typename external_t>
    class graph;
}

template<typename external_t>
class model::graph {
public:
    using internal_t = size_t;

    graph(std::istream &input) : next_id(0) {
        external_t a, b;
        while (input >> a >> b) {
            internal_t a = imp(a);
            internal_t b = imp(b);
            add_edge(a, b);
        }
    }

    std::vector<internal_t> neighbours(internal_t node) const {
        return descendants.at(node);
    }

    external_t exp(internal_t node) const {
        return representation.at(node);
    }

    size_t node_number() const {
        return next_id;
    }

private:
    internal_t imp(external_t input) {
        size_t result;

        auto pair = representation.find(input);
        if (pair == representation.end()) {
            result = next_id++;
            representation.emplace(input, result);
        } else {
            result = pair->second;
        }

        return result;
    }

    internal_t next_id;
    std::map<size_t, std::vector<size_t> > descendants;
    std::map<size_t, size_t> representation;

    void add_edge(size_t a, size_t b) {
        if (descendants.find(a) == descendants.end()) {
            descendants[a] = {b};
        } else {
            descendants[a].push_back(b);
        }
    }
};

#endif //GRAPH_H
