/**@file
 * Interface of game utilities, such as turn counting and result returning.
 */

#ifndef MIDDLE_AGES_GAME_H
#define MIDDLE_AGES_GAME_H

#include "player.h"

/**
 * Initializes a game. Needed before first INIT.
 */
void start_game();

/**
 * Set game max turn number and player number.
 */
void init_game(int n, int p);

// Added include guard, because type is used in two files.
#ifndef MIDDLE_AGES_GAME_RESULT_ENUM
#define MIDDLE_AGES_GAME_RESULT_ENUM
/**
 * Enum type containing all possible game results.
 */
typedef enum {
    WIN, DEFEAT, DRAW, PLAYED
} game_result;
#endif /* MIDDLE_AGES_GAME_RESULT_ENUM */

/**
 * End state the game - print result and free memory.
 */
void end_game();

/**
 * End turn command, returns game result.
 */
void end_turn();

/**
 * Check whether game is still playable - turn count isn't exceeded and both
 * players have kings.
 * @return game result or PLAYED if it hasn't ended.
 */
game_result get_game_result();

/**
 * Return pointer to currently playing player.
 */
Player *get_cur_player();

/**
 * If argument is 1 or 2 return pointer to the given player.
 */
Player *get_n_player(int n);

/**
 * Return turn count.
 */
int get_turn_number();

/**
 * Return 1 if game is initialized and other than INIT commands are accepted.
 */
int is_initialized();

#endif //MIDDLE_AGES_GAME_H
