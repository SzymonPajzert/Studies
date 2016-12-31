#ifndef MAP_H
#define MAP_H

/**
 * For testing purposes it abstracts type of maps used in implementation
 */

#include <unordered_map>
#include <map>

#ifdef USE_UNORDERED_MAP
    template<typename Key, typename Value>
    using map_t = std::unordered_map<Key, Value>;
#else
    template<typename Key, typename Value>
    using map_t = std::map<Key, Value>;
#endif

#endif //MAP_H
