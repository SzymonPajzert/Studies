#ifndef GRAPH_H
#define GRAPH_H

#include <cstddef>
#include <vector>

#include "logger.h"
#include "map.h"
#include "model.h"

class model::graph {
public:
    using node_t = size_t;

private:
    using map_type = map_t<node_t , std::vector<node_t> >;

    static map_type create_map(std::istream &input) {
        logger::print_open("model::graph::create_map started");
        map_type result;
        size_t a, b;
        while (input >> a >> b) {
            logger::print("model::graph::create_map read input");
            if (result.find(a) == result.end()) {
                result[a] = {b};
            } else {
                result[a].push_back(b);
            }
        }
        logger::print_end("model::graph::create_map finished");
        return result;
    }

    static std::vector<node_t> get_ids(const map_type & map) {
        std::vector<node_t> result;
        for (auto &&kv : map) {
            result.push_back(kv.first);
        }
        return result;
    }

public:

    /// Instantiates map to contain keys as keys in this graph with values set to @param value
    template<typename K, typename V>
    void instantiate_map(map_t<K, V> & map, V value) const {
        map.clear();
        for(auto key : node_ids) {
            map[key] = value;
        }
    };

    graph(std::istream &input) :
            descendants(create_map(input)),
            node_ids(get_ids(descendants))
    {}

    size_t node_number() const {
        return node_ids.size();
    }

    const map_type descendants;
    const std::vector<node_t> node_ids;
};

#endif //GRAPH_H
