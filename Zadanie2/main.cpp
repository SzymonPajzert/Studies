#include <iostream>
#include <fstream>
#include <stack>
#include <vector>
#include <atomic>
#include <list>
#include <queue>
#include <memory>
#include <future>

#include "Graph.h"

using namespace std;

void calculate(const Graph<int> & graph, const Graph<int>::node_id s, std::vector<std::atomic<int> > & BC) {
    using node_id = Graph<int>::node_id;

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

int main() {
    int thread_number;
    std::string inputFileName, outputFileName;
    std::cin >> thread_number >> inputFileName >> outputFileName;

    std::fstream inputFile;
    inputFile.open(inputFileName, ios_base::in);
    const auto graph = Graph<int>(inputFile, thread_number);
    inputFile.close();

    std::vector<std::atomic<int> > BC(graph.node_number());
    for(Graph<int>::node_id v = 0; v < graph.node_number(); v++) {
        BC[v] = 0;
    }

    for(Graph<int>::node_id v = 0; v < graph.node_number(); v++) {
        calculate(graph, v, BC);
    }

    std::fstream outputFile;
    outputFile.open(inputFileName, ios_base::out);
    for(Graph<int>::node_id v = 0; v < graph.node_number(); v++) {
        calculate(graph, v, BC);
    }
    outputFile.close();

    return 0;
}