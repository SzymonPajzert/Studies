/**@file
 * Interface of game engine.
 */

#ifndef MIDDLE_AGES_ENGINE_H
#define MIDDLE_AGES_ENGINE_H

/**
 * Initializes a game. Needed before first INIT.
 */
void start_game();

/**
 * Frees memory. Needed after finishing game.
 */
void end_game();

/**
 * Initializes a game with size of a board, number of rounds and positions of kings.
 */
int init(int n, int k, int p, int x1, int y1, int x2, int y2);

/**
 * Makes a move.
 * @param[in] x1 Column number before a move.
 * @param[in] y1 Row number before a move.
 * @param[in] x2 Column number after a move.
 * @param[in] y2 Row number before a move.
 * @return 0.
 */
int move(int x1, int y1, int x2, int y2);

/**
 * Produces knight if peasant haven't moved for 2 turns.
 * @param[in] x1 Column number of peasant.
 * @param[in] y1 Row number of peasant.
 * @param[in] x2 Column number of to-be-created knight.
 * @param[in] y2 Row number of to-be-created knight.
 * @return 0.
 */
int produce_knight(int x1, int y1, int x2, int y2);

/**
 * Produces peasant if peasant haven't moved for 2 turns.
 * @param[in] x1 Column number of peasant.
 * @param[in] y1 Row number of peasant.
 * @param[in] x2 Column number of to-be-created peasant.
 * @param[in] y2 Row number of to-be-created peasant.
 * @return 0.
 */
int produce_peasant(int x1, int y1, int x2, int y2);


/**
 * End turn command, increasing turn counter and changing current player.
 * @return 0.
 */
int end_turn();

/**
 * Enum type containing all possible game results.
 */
typedef enum {
    FIRST_WON, SECOND_WON, DRAW, PLAYED,
} game_result;

/**
 * Check whether game is still playable - turn count isn't exceeded and both
 * players have kings.
 * @return game result or PLAYED if it hasn't ended.
 */
game_result get_game_result();

/**
 * Finishes game. Prints winner and frees memory.
 */
void end_game();

/**
 * Prints (into stdout) top-left corner of the board of size m x m where m = min(n,10).
 */
void print_topleft();

#endif /* MIDDLE_AGES_ENGINE_H */
