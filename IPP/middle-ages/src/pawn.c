/**@file
 * Implementation of pawn structure - representing king, knight and peasant.
 */

#include <stdlib.h>
#include "player.h"
#include "game.h"
#include "pawn.h"

#define PEASANT_TURN_REST 2

Pawn *create(pawn_type type, Player *owner) {
    Pawn *created = malloc(sizeof(Pawn));
    created->owner = owner;
    created->type = type;
    // Set turn number to make move possible
    created->last_moved = get_turn_number() - 1;
    return created;
}


void die(Pawn *killed) {
    if (killed->type == KING) {
        killed->owner->has_king = 0;
    }

    free(killed);
}

/**
 * Compare types of pawns, given the enum is written in order of increasing strength.
 * @return -1 if first is weaker and rest likewise.
 */
int cmp_pawn_type(pawn_type first, pawn_type second) {
    if (first < second) return -1;
    if (first > second) return 1;
    return 0;
}

void update_rest(Pawn *pawn) {
    pawn->last_moved = get_turn_number();
}

int rested_peasant(Pawn *peasant) {
    return (get_turn_number() - peasant->last_moved) > PEASANT_TURN_REST;
}

int rested_pawn(Pawn *peasant) {
    return (get_turn_number() - peasant->last_moved > 0);
}

fight_result fight(Pawn *first, Pawn *second) {
    if (first->owner == second->owner) {
        return IMPOSSIBLE;
    }

    switch (cmp_pawn_type(first->type, second->type)) {
        case -1:
            return FIRST;
        case 0:
            return BOTH;
        case 1:
            return SECOND;
        default:
            return IMPOSSIBLE;
    }
}


/**
 * Return char of the given pawn as to be represented on the map.
 */
char to_char(Pawn *pawn) {
    char result = 0;

    switch (pawn->type) {
        case KING:
            result = 'K';
            break;

        case PEASANT:
            result = 'C';
            break;

        case KNIGHT:
            result = 'R';
            break;
    }

    if (pawn->owner->number == 2) {
        result += 32;
    }

    return result;
}