/**@file
 * Interface of pawn structure - representing king, knight and peasant.
 */

#ifndef MIDDLE_AGES_PAWN_H
#define MIDDLE_AGES_PAWN_H

#include "player.h"

/**
 * Type of the pawn as stated in the game.
 */
typedef enum {
    PEASANT, KING, KNIGHT
} pawn_type;

/**
 * Pawn structure.
 */
typedef struct {
    pawn_type type;
    /**< Type of the pawn @see pawn_type*/
    Player *owner;
    /**< Pointer to the owner of the pawn @see Player */
    int last_moved;     /**< Number of turn when pawn was moved last */
} Pawn;

/**
 * Create pawn of given type and pointer to the owner.
 */
Pawn *create(pawn_type type, Player *owner);

/**
 * Free memory and change has_king property of the owner if king is killed
 */
void die(Pawn *killed);

/**
 * Used by move and create peasant functions, updates last move turn.
 */
void update_rest(Pawn *pawn);

/**
 * Return 1 only if pointer isn't NULL, points to peasant and peasant is
 * ready for new unit creation.
 */
int rested_peasant(Pawn *peasant);

/**
 * Return 1 only if pointer isn't NULL and points to unmoved unit.
 */
int rested_pawn(Pawn *peasant);

/**
 * Enum type returned by fight - indicates who died.
 * IMPOSSIBLE states that given fight is forbidden - i.e between pawns of the same player.
 */
typedef enum {
    FIRST, SECOND, BOTH, IMPOSSIBLE
} fight_result;

/**
 * Simulates fight between first and second pawn and returns result without
 * killing anyone.
 */
fight_result fight(Pawn *first, Pawn *second);

char to_char(Pawn *pawn);

#endif /* MIDDLE_AGES_PAWN_H */