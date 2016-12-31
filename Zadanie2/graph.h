#ifndef GRAPH_H
#define GRAPH_H

#include <cstddef>
#include <vector>
#include <algorithm>

#include "set.h"
#include "logger.h"
#include "map.h"
#include "model.h"

class model::graph {
public:
    using node_t = size_t;

private:
    using map_type = map_t<node_t , std::vector<node_t> >;
    using set_type = set_t<node_t>;

    map_type create_map(std::istream &input) {
        logger::print_open("model::graph::create_map started");
        map_type map;
        size_t a, b;
        while (input >> a >> b) {
            logger::print("model::graph::create_map read input");
            all_node_ids.insert(a);
            all_node_ids.insert(b);
            if (map.find(a) == map.end()) {
                map[a] = {b};
            } else {
                map[a].push_back(b);
            }
        }
        logger::print_end("model::graph::create_map finished");
        return map;
    }

    std::vector<node_t> get_ids() const {
        std::vector<node_t> result;
        for (auto &&kv : descendants) {
            result.push_back(kv.first);
        }
        sort(result.begin(), result.end());
        return result;
    }

    std::map<node_t, size_t> get_representation() const {
        std::map<node_t, size_t> result;
        for(size_t i=0; i<node_ids.size(); i++) {
            result[node_ids[i]] = i;
        }
        return result;
    }

    set_type all_node_ids;

public:

    // TODO create lazy maps with default values.
    /// Instantiates map to contain keys as keys in this graph with values set to @param value
    template<typename V>
    void instantiate_map(map_t<node_t, V> & map, V value) const {
        map.clear();
        for(auto key : all_node_ids) {
            map[key] = value;
        }
    };

    graph(std::istream &input) :
            descendants(create_map(input)),
            node_ids(get_ids()),
            represent(get_representation())
    {}

    size_t node_number() const {
        return node_ids.size();
    }

    const map_type descendants;
    const std::vector<node_t> node_ids;
    const map_t<node_t, size_t> represent;
};

#endif //GRAPH_H
