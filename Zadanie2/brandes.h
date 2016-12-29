#ifndef BRANDES_H
#define BRANDES_H

#include "graph.h"

#include <iostream>
#include <fstream>
#include <atomic>
#include <stack>
#include <queue>
#include <list>

class brandes {
public:
    brandes(int thread_number, const std::string &input_file_name, const std::string &output_file_name);

    void run();
    void save();


private:
    int thread_number;
    std::string output_file_name;
    const model::graph<size_t> graph;
    std::vector<std::atomic<int> > BC;

    model::graph<size_t> read_graph(std::string input_file_name);

    class vertex_calculation {
    public:
        using node_id = model::graph<size_t>::internal_t;
        vertex_calculation(brandes &upper, size_t s);
        void run();

    private:
        const size_t node_number;
        const size_t s;
        brandes & upper;
        std::stack<node_id> stack;
        std::queue<node_id> queue; // FIFO
        std::vector<std::list<node_id > > P;
        std::vector<int> sigma, d, delta;

        void empty_queue();
        void empty_stack();
    };

};


#endif //BRANDES_H
