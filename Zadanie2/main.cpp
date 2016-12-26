#include <iostream>
#include <fstream>

#include "graph.h"
#include "calculate.h"

int main() {
    int thread_number;
    std::string inputFileName, outputFileName;
    std::cin >> thread_number >> inputFileName >> outputFileName;

    std::fstream inputFile;
    inputFile.open(inputFileName, std::ios_base::in);
    const auto graph = brandes::graph<size_t>(inputFile, thread_number);
    inputFile.close();

    std::vector<std::atomic<int> > BC(graph.node_number());
    for(size_t v = 0; v < graph.node_number(); v++) {
        BC[v] = 0;
    }

    for(size_t v = 0; v < graph.node_number(); v++) {
        calculate(graph, v, BC);
    }

    // Save result
    std::fstream outputFile;
    outputFile.open(inputFileName, std::ios_base::out);
    for(size_t v = 0; v < graph.node_number(); v++) {
        outputFile << graph.exp(v) << " " << BC[v] << std::endl;
    }
    outputFile.close();

    return 0;
}
