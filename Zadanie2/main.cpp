#include <iostream>
#include <fstream>

#include "brandes.h"
#include "logger.h"

int main(int argc, char* args[]) {

    logger::print_open("Program started");
    int thread_number = std::stoi(args[1]);
    std::string input_file_name = args[2];
    std::string output_file_name = args[3];
    logger::print_end("Input read successfully");

    logger::print_open("Attempting to initialize brandes");
    auto main = brandes(thread_number, input_file_name, output_file_name);
    logger::print_end("Initialized brandes successfully");
    main.run();
    main.save();

    return 0;
}
