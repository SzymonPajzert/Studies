#ifndef BARRIER_H
#define BARRIER_H

#include <condition_variable>
#include <thread>

class Barrier {
    unsigned int count;
    std::condition_variable barrier_breached;
    std::mutex protection;

public:
    const unsigned int resistance;

    Barrier(unsigned int resistance): count(0), resistance(resistance) {};

    void reach() {
        std::unique_lock<std::mutex> lock(protection);
        count++;
        while(count < resistance) {
            barrier_breached.wait(lock, [this]{return this->count >= this->resistance; });
        }
        barrier_breached.notify_all();
    }
};


#endif //BARRIER_H
