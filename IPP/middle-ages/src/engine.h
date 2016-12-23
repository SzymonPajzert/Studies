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
 * Initializes a game with size of a board, number of rounds and positions of kings.
 */
void init(int n, int k, int p, int x1, int y1, int x2, int y2);

/**
 * Makes a move.
 * @param[in] x1 Column number before a move.
 * @param[in] y1 Row number before a move.
 * @param[in] x2 Column number after a move.
 * @param[in] y2 Row number before a move.
 * @return 0.
 */
void move(int x1, int y1, int x2, int y2);

/**
 * Produces knight if peasant haven't moved for 2 turns.
 * @param[in] x1 Column number of peasant.
 * @param[in] y1 Row number of peasant.
 * @param[in] x2 Column number of to-be-created knight.
 * @param[in] y2 Row number of to-be-created knight.
 * @return 0.
 */
void produce_knight(int x1, int y1, int x2, int y2);

/**
 * Produces peasant if peasant haven't moved for 2 turns.
 * @param[in] x1 Column number of peasant.
 * @param[in] y1 Row number of peasant.
 * @param[in] x2 Column number of to-be-created peasant.
 * @param[in] y2 Row number of to-be-created peasant.
 * @return 0.
 */
void produce_peasant(int x1, int y1, int x2, int y2);


/**
 * End turn command, increasing turn counter and changing current player.
 * @return 0.
 */
void end_turn();

/**
 * End state the game - print result and free memory.
 */
void end_game();

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
 * Check whether game is still playable - turn count isn't exceeded and both
 * players have kings.
 * @return game result or PLAYED if it hasn't ended.
 */
game_result get_game_result();

#endif /* MIDDLE_AGES_ENGINE_H */
