#ifndef LOGGER_H
#define LOGGER_H

#include <iostream>
#include <mutex>

class logger {
public:
    static void print(std::string message) {
        guard lock(mutex);
        _print(message);
    }

    static void print_open(std::string message) {
        guard lock(mutex);
        _print(message);
        spaces.append(" ");
    }

    static void print_end(std::string message) {
        guard lock(mutex);
        spaces.erase(spaces.end() - 1);
        _print(message);
    }

private:
    using guard = std::lock_guard<std::mutex>;

    static void _print(std::string message) {
        std::cout << spaces << message << std::endl << std::flush;
    }

    static std::mutex mutex;
    static std::string spaces;
};

#endif //LOGGER_H
