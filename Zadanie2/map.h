#ifndef MAP_H
#define MAP_H

/**
 * For testing purposes it abstracts type of maps used in implementation
 */

#ifdef USE_UNORDERED
#   include <unordered_map>
    template<typename Key, typename Value>
    using map_t = std::unordered_map<Key, Value>;
#else
#   include <map>
    template<typename Key, typename Value>
    using map_t = std::map<Key, Value>;
#endif

#endif //MAP_H
