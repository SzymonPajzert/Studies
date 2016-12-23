/**@file
 * Implementation of map structure and associated functionalities.
 */

#include <stdio.h>
#include "error.h"
#include "map.h"

/**
 * Basic element of the map, implemented as deque element.
 *
 */
typedef struct Tile {
    int number;
    /**< number of the tile */
    struct Tile *prev_tile;
    /**< pointer to the previous tile or NULL */
    struct Tile *next_tile;
    /**< pointer to the next tile or NULL */
    Pawn *occupying_pawn;       /**< pointer to the occupying pawn */
} Tile;

/**
 * Get tile which number is the biggest smaller than n and after begin.
 * @return Found tile or closest before.
 */
Tile *get_tile(Tile *begin, int n) {
    Tile *result = begin;
    while (result->next_tile && result->next_tile->number <= n) {
        result = result->next_tile;
    }

    return result;
}

/**
 * Insert to tile at nth position. Exits with error if tile exists.
 * @return Pointer to newly inserted tile.
 */
Tile *insert_tile(Tile **begin, int n, Pawn *inserted_pawn) {
    Tile *found = get_tile(*begin, n);
    if (found->number == n) {
        input_error();
        return NULL;
    } else {
        Tile *new_tile = malloc(sizeof(Tile));
        new_tile->number = n;
        new_tile->occupying_pawn = inserted_pawn;

        if (found->number < n) {
            new_tile->next_tile = found->next_tile;
            new_tile->prev_tile = found;
            found->next_tile = new_tile;
            if (new_tile->next_tile) {
                (new_tile->next_tile)->prev_tile = new_tile;
            }
        } else {
            new_tile->next_tile = found;
            new_tile->prev_tile = found->prev_tile;
            if (found->prev_tile) {
                found->prev_tile->next_tile = new_tile;
            }
            found->prev_tile = new_tile;
            *begin = new_tile;
        }
        return new_tile;
    }
}

/**
 * Remove the tile and set pointer to the next or previous tile.
 */
Pawn *remove_tile(Tile **removed_tile) {
    Tile *prev, *next;
    Pawn *result;

    result = (*removed_tile)->occupying_pawn;
    prev = (*removed_tile)->prev_tile;
    next = (*removed_tile)->next_tile;
    free(*removed_tile);
    *removed_tile = NULL;

    if (next != NULL) {
        next->prev_tile = prev;
        (*removed_tile) = next;
    }

    if (prev != NULL) {
        prev->next_tile = next;
        (*removed_tile) = prev;
    }

    return result;
}

/**
 * Tier 2 element of the map, implemented as deque element.
 */
typedef struct Column {
    int number;
    /**< number of the column */
    struct Column *prev_column;
    /**< pointer to previous column or NULL */
    struct Column *next_column;
    /**< pointer to next column or NULL */
    Tile *first_tile;               /**< pointer to first tile in column */
} Column;

/**
 * Get column by number.
 */
Column *get_column(Column *begin, int n) {
    Column *result = begin;
    while (result->next_column && result->next_column->number <= n) {
        result = result->next_column;
    }

    return result;
}

/**
 * Insert column at nth position with one tile. Exits with error if tile exists.
 * @return Pointer to newly inserted/updated tile.
 */
Column *insert_column(Column *begin, int n, Tile *inserted_tile) {
    Column *found = get_column(begin, n);
    if (found->number == n) {
        input_error();
        return NULL;
    } else {
        Column *new_column = malloc(sizeof(Tile));
        new_column->number = n;
        new_column->first_tile = inserted_tile;
        new_column->next_column = found->next_column;
        new_column->prev_column = found;

        found->next_column = new_column;
        if (new_column->next_column) {
            new_column->next_column->prev_column = new_column;
        }
        return new_column;
    }
}

/**
 * Remove column at nth position. It is assumed that there are no tiles in
 * column, otherwise error is thrown.
 */
void remove_colum(Column **removed_column) {
    Column *prev, *next;

    if ((*removed_column)->first_tile != NULL) {
        input_error();
    }

    prev = (*removed_column)->prev_column;
    next = (*removed_column)->next_column;
    free(*removed_column);

    if (next != NULL) {
        next->prev_column = prev;
        (*removed_column) = next;
    }

    if (prev != NULL) {
        prev->next_column = next;
        (*removed_column) = prev;
    }
}

/**
 * Map size variable used by validate_input function - maximum possible
 * column or tile number.
 */
int map_size = -1;
/**
 * Instance of the map, containing after initialization column with number 0
 * - to ensure that map isn't empty and column won't be used.
 */
Column *map;

/**
 * Set map size and allocate memory for the map.
 */
void init_map(int n) {
    if (n > 0) {
        if (map_size == -1) {
            map_size = n;
            map = malloc(sizeof(Column));
            map->number = 0;
            return;
        }

        if (map_size != n) {
            input_error();
        }
    } else {
        input_error();
    }
}

void validate_input(int x, int y) {
    if (x > map_size || x < 0 || y > map_size || y < 0) {
        input_error();
    }
}

Tile *get_tile_from_map(int x, int y) {
    Column *found_column = get_column(map, x);
    if (found_column->number != x) {
        return NULL;
    }

    Tile *found_tile = get_tile(found_column->first_tile, y);
    if (found_tile->number != y) {
        return NULL;
    }

    return found_tile;
}

Pawn *get_pawn(int x, int y) {
    validate_input(x, y);
    Tile *found = get_tile_from_map(x, y);
    if (found != NULL) {
        return found->occupying_pawn;
    } else {
        return NULL;
    }
}

Pawn *remove_pawn(int x, int y) {
    validate_input(x, y);
    Tile *found = get_tile_from_map(x, y);
    if (found != NULL) {
        Column *result_column;
        Pawn *result;

        result_column = get_column(map, x);
        if (result_column->first_tile == found) {
            result = remove_tile(&(result_column->first_tile));
        } else {
            result = remove_tile(&found);
        }

        if (result_column->first_tile == NULL) {
            remove_colum(&(result_column));
        }

        return result;
    } else {
        return NULL;
    }
}

void insert_pawn(int x, int y, Pawn *pawn) {
    validate_input(x, y);
    Column *column;
    Tile *tile;

    column = get_column(map, x);
    if (column->number != x) {
        tile = malloc(sizeof(Tile));
        tile->number = y;
        tile->prev_tile = NULL;
        tile->next_tile = NULL;
        tile->occupying_pawn = pawn;
        insert_column(column, x, tile);
    } else {
        insert_tile(&(column->first_tile), y, pawn);
    }
}

void move_pawn(int x1, int y1, int x2, int y2) {
    validate_input(x1, y1);
    validate_input(x2, y2);
    Pawn *moved = remove_pawn(x1, y1);

    if (rested_pawn(moved)) {
        update_rest(moved);
        insert_pawn(x2, y2, moved);
    } else {
        input_error();
    }

}

int is_free(int x, int y) {
    validate_input(x, y);
    return (get_pawn(x, y) == NULL);
}

void deallocate_map() {
    Column *column = map, *next_column;
    Tile *tile, *next_tile;

    while (column) {
        tile = column->first_tile;
        while (tile) {
            next_tile = tile->next_tile;
            free(tile->occupying_pawn);
            free(tile);
            tile = next_tile;
        }
        next_column = column->next_column;
        free(column);
        column = next_column;
    }
}