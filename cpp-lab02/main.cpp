#include <iostream>
#include <queue>
#include <chrono>

#include "Barrier.h"

int main() {
    using ui = unsigned int;
    using cui = const ui;

    cui resistance = 3;
    cui thread_num = 5;

    Barrier barrier{resistance};

    std::cout << "Created barrier with resistance = " << barrier.resistance << std::endl;

    std::queue<std::thread> thread_queue;
    for(ui i=0; i<thread_num; i++) {
        thread_queue.push(std::thread{[&barrier, i] {
            std::cout << "Started thread number(" << i << ")" << std::endl;
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
            barrier.reach();
            std::cout << "Barrier breached in thread number(" << i << ")" << std::endl;
        }});
    }

    while(!thread_queue.empty()) {
        thread_queue.front().join();
        thread_queue.pop();
    }

    return 0;
}