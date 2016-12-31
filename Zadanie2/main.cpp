#include <iostream>
#include <fstream>

#include "brandes.h"

int main(int argc, char* args[]) {

    int thread_number = std::stoi(args[1]);
    std::string input_file_name = args[2];
    std::string output_file_name = args[3];

    auto main = brandes(thread_number, input_file_name, output_file_name);
    main.run();
    main.save();

    return 0;
}
