#ifndef BRANDES_H
#define BRANDES_H

#include <iostream>
#include <fstream>
#include <atomic>
#include <stack>
#include <queue>
#include <list>

#include "map.h"
#include "graph.h"

class brandes {
public:
    brandes(size_t thread_number, const std::string &input_file_name, const std::string &output_file_name);

    void run();
    void save();


private:
    size_t thread_number;
    std::string output_file_name;
    const model::graph graph;

    void calculate(size_t mod);

    using bc_t = std::vector<std::atomic<double> >;
    bc_t BC;

    model::graph read_graph(std::string input_file_name);

    class vertex_calculation {
    public:
        using node_t = model::graph::node_t;
        vertex_calculation(brandes &upper, node_t s);
        void run();

    private:
        const model::graph & graph;
        bc_t & BC;
        const size_t node_number;
        const node_t s;
        std::stack<node_t> stack;
        std::queue<node_t> queue;

        map_t<node_t, std::list<node_t > > P;
        map_t<node_t, double> delta;
        map_t<node_t, int> d, sigma;

        void empty_queue();
        void empty_stack();
    };

};


#endif //BRANDES_H
