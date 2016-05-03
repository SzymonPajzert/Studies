/**@file
 * Interface of map structure and associated functionalities.
 */
#include <stdlib.h>
#include "pawn.h"

#ifndef MIDDLE_AGES_MAP_H
#define MIDDLE_AGES_MAP_H

/**
 * Create map of given maximal size.
 */
void init_map(int n);

/**
 * Get size of map.
 */
int get_size();

/**
 * Get pawn residing in the tile. If tile is empty, NULL is returned.
 */
Pawn *get_pawn(int x, int y);

/**
 * Free pawn at the tile. Returns pointer to pawn - it isn't destroyed. NULL
 * is returned if tile was free.
 */
Pawn *remove_pawn(int x, int y);

/**
 * Insert pawn to given tile. Pawn residing there is overridden.
 */
void insert_pawn(int x, int y, Pawn *pawn);

/**
 * Move pawn from given tile to another. No checking of possibility of move
 * is done. Pawn residing in x2, y2 is overridden.
 */
void move_pawn(int x1, int y1, int x2, int y2);

/**
 * Check whether tile is occupied by pawn.
 */
int is_free(int x, int y);

/**
 * Return top-left corner of map of size at most 10.
 */
void print_map();

#endif /* MIDDLE_AGES_MAP_H */