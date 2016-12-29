#include "brandes.h"

brandes::vertex_calculation::vertex_calculation(brandes &upper, size_t s) :
        upper(upper),
        s(s),
        node_number(upper.graph.node_number()),
        P(node_number, std::list<node_id>()),
        sigma(node_number, 0),
        d(node_number, -1),
        delta(node_number, 0) {
    sigma[s] = 1;
    d[s] = 0;
    queue.push(s);
}

void
brandes::vertex_calculation::empty_queue() {
    while (!queue.empty()) {
        auto v = queue.front();
        queue.pop();

        stack.push(v);
        for(auto w : upper.graph.neighbours(v)) {
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
            upper.BC[w] += delta[w];
        }
    }
}

void
brandes::vertex_calculation::run() {
    empty_queue();
    empty_stack();
}


model::graph<size_t>
brandes::read_graph(std::string input_file_name) {
    std::fstream inputFile;
    inputFile.open(input_file_name, std::ios_base::in);
    const auto graph = model::graph<size_t>(inputFile);
    inputFile.close();
    return graph;
}

brandes::brandes(int thread_number, const std::string &input_file_name, const std::string &output_file_name) :
        thread_number(thread_number),
        output_file_name(output_file_name),
        graph(read_graph(input_file_name)),
        BC(graph.node_number()) {
    std::cout << "Brandes instance created succesfully for";
    for(size_t i = 0; i < BC.size(); i++) {
        BC[i] = 0;
    }
}

void
brandes::run() {
    for(size_t v = 0; v < graph.node_number(); v++) {
        vertex_calculation(*this, v).run();
    }
}

void brandes::save() {
    // Save result
    std::fstream output_file;
    output_file.open(output_file_name, std::ios_base::out);
    for(size_t v = 0; v < graph.node_number(); v++) {
        output_file << graph.exp(v) << " " << BC[v] << std::endl;
    }
    output_file.close();
}
