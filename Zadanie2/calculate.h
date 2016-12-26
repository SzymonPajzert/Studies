#ifndef CALCULATE_H
#define CALCULATE_H

#include <stack>
#include <vector>
#include <atomic>
#include <list>
#include <queue>

#include "graph.h"

void calculate(const brandes::graph<size_t> & graph, size_t s, std::vector<std::atomic<int> > & BC) {
    using node_id = brandes::graph<size_t>::internal_t;

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

#endif //CALCULATE_H
