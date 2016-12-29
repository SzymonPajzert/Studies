#ifndef GRAPH_H
#define GRAPH_H

#include <cstddef>
#include <vector>

#include "map.h"
#include "model.h"

class model::graph {
private:
    using map_type = map_t<size_t, std::vector<size_t> >;

    static map_type create_map(std::istream &input) {
        map_type result;
        size_t a, b;
        while (input >> a >> b) {
            if (result.find(a) == result.end()) {
                result[a] = {b};
            } else {
                result[a].push_back(b);
            }
        }
        return result;
    }

    static std::vector<size_t> get_ids(const map_type & map) {
        std::vector<size_t> result;
        for (auto &&kv : map) {
            result.push_back(kv.first);
        }
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
    const std::vector<size_t> node_ids;
};

#endif //GRAPH_H
