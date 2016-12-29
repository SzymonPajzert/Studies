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
    brandes(int thread_number, const std::string &input_file_name, const std::string &output_file_name);

    void run();
    void save();


private:
    int thread_number;
    std::string output_file_name;
    const model::graph graph;
    map_t<size_t, std::atomic<int> > BC;

    model::graph read_graph(std::string input_file_name);

    class vertex_calculation {
    public:
        vertex_calculation(brandes &upper, size_t s);
        void run();

    private:
        const model::graph & graph;
        map_t<size_t, std::atomic<int> > & BC;
        const size_t node_number;
        const size_t s;
        std::stack<size_t> stack;
        std::queue<size_t> queue;

        map_t<size_t, std::list<size_t > > P;
        map_t<size_t, int> sigma, d, delta;

        void empty_queue();
        void empty_stack();
    };

};


#endif //BRANDES_H
