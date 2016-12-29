#include "brandes.h"

#include <algorithm>

brandes::vertex_calculation::vertex_calculation(brandes &upper, size_t s) :
        graph(upper.graph),
        BC(upper.BC),
        node_number(upper.graph.node_ids.size()),
        s(s) {
    sigma[s] = 1;
    d[s] = 0;
    queue.push(s);

    graph.instantiate_map(P, std::list<size_t>());
    graph.instantiate_map(sigma, 0);
    graph.instantiate_map(d, -1);
    graph.instantiate_map(delta, 0);
}

void
brandes::vertex_calculation::empty_queue() {
    while (!queue.empty()) {
        auto v = queue.front();
        queue.pop();

        stack.push(v);
        for(auto w : graph.descendants.at(v)) {
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
}


void
brandes::vertex_calculation::empty_stack() {
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

void
brandes::vertex_calculation::run() {
    empty_queue();
    empty_stack();
}


model::graph
brandes::read_graph(std::string input_file_name) {
    std::fstream inputFile;
    inputFile.open(input_file_name, std::ios_base::in);
    const auto graph = model::graph(inputFile);
    inputFile.close();
    return graph;
}

brandes::brandes(int thread_number, const std::string &input_file_name, const std::string &output_file_name) :
        thread_number(thread_number),
        output_file_name(output_file_name),
        graph(read_graph(input_file_name)) {
    std::cout << "Brandes instance created successfully for";
    for(auto v : graph.node_ids) {
        BC[v] = 0;
    }
    std::cout << "BC in brandes instance has been instantiated";
}

void
brandes::run() {
    for(const auto & v : graph.node_ids) {
        vertex_calculation(*this, v).run();
    }
}

void brandes::save() {
    // Save result
    std::fstream output_file;
    output_file.open(output_file_name, std::ios_base::out);

    auto node_ids = graph.node_ids;
    sort(node_ids.begin(), node_ids.end());

    for(auto v : node_ids) {
        output_file << v << " " << BC[v] << std::endl;
    }
    output_file.close();
}
