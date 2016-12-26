#ifndef BRANDES_H
#define BRANDES_H

#include <iostream>
#include <fstream>

#include "graph.h"

class brandes {
public:
    brandes(int thread_number, const std::string &input_file_name, const std::string &output_file_name) :
            thread_number(thread_number),
            output_file_name(output_file_name),
            graph(read_graph(input_file_name)),
            BC(graph.node_number()) {
        for(size_t i = 0; i < BC.size(); i++) {
            BC[i] = 0;
        }
    }

    void calculate(const size_t s) {
        using node_id = model::graph<size_t>::internal_t;

        const size_t nodeNumber = graph.node_number();

        std::stack<node_id> stack;
        std::vector<std::list<node_id > > P(nodeNumber, std::list<node_id>());
        std::vector<int> sigma(nodeNumber, 0);
        std::vector<int> d(nodeNumber, -1);
        std::vector<int> delta(nodeNumber, 0);

        sigma[s] = 1;
        d[s] = 0;

        std::queue<node_id> queue; // FIFO
        queue.push(s);

        while (!queue.empty()) {
            auto v = queue.front();
            queue.pop();

            stack.push(v);
            for(auto w : graph.neighbours(v)) {
                if(d[w] < 0) {
                    queue.push(w);
                    d[w] = d[v] + 1;
                }
                if (d[w] == d[v] + 1) {
                    sigma[w] += sigma[v];
                    P[w].push_back(v);
                }
            }
        }

        while (!stack.empty()) {
            auto w = stack.top();
            stack.pop();

            for(auto v : P[w]) {
                delta[v] += (sigma[v] / sigma[w]) * (1 + delta[w]);
            }

            if (w != s) {
                BC[w] += delta[w];
            }
        }
    }

    void run() {
        for(size_t v = 0; v < graph.node_number(); v++) {
            calculate(v);
        }
    }

    void save() {
        // Save result
        std::fstream output_file;
        output_file.open(output_file_name, std::ios_base::out);
        for(size_t v = 0; v < graph.node_number(); v++) {
            output_file << graph.exp(v) << " " << BC[v] << std::endl;
        }
        output_file.close();
    }


private:
    int thread_number;
    std::string output_file_name;
    const model::graph<size_t> graph;
    std::vector<std::atomic<int> > BC;

    model::graph<size_t> read_graph(std::string input_file_name) {
        std::fstream inputFile;
        inputFile.open(input_file_name, std::ios_base::in);
        const auto graph = model::graph<size_t>(inputFile);
        inputFile.close();
        return graph;
    }

};


#endif //BRANDES_H
