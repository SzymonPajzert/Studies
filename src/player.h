/**@file
 * Interface and implementation of player structure.
 */

#ifndef MIDDLE_AGES_PLAYER_H
#define MIDDLE_AGES_PLAYER_H

/**
 * Structure of player.
 */
typedef struct {
    int has_king;
    /**< 1 if player has an alive king. */
    int number;     /**< Number of the player. */
} Player;

#endif //MIDDLE_AGES_PLAYER_H
