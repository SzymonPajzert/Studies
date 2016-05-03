/*@file
 * Set of common utilities shared in middle_ages game
 */

#ifndef MIDDLE_AGES_UTIL_H
#define MIDDLE_AGES_UTIL_H

#include <stdio.h>

#define input_error() \
    perror("input error\n");\
    exit(42);

#define map_error(MSG) \
    perror((MSG)); \
    exit(40);

#define game_state_error() \
    perror("game state error");

#endif //MIDDLE_AGES_UTIL_H
