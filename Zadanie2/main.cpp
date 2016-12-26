#include <iostream>
#include <fstream>

#include "graph.h"
#include "calculate.h"
#include "brandes.h"

int main() {
    int thread_number;
    std::string inputFileName, outputFileName;
    std::cin >> thread_number >> inputFileName >> outputFileName;

    auto main = brandes(thread_number, inputFileName, outputFileName);
    main.run();
    main.save();

    return 0;
}
