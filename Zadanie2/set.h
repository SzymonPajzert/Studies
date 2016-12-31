#ifndef SET_H
#define SET_H

#include <set>
#include <unordered_set>

#ifdef USE_UNORDERED_SET
    template<typename Value>
    using set_t = std::unordered_set<Value>;
#else
    template<typename Value>
    using set_t = std::set<Value>;
#endif

#endif //SET_H
