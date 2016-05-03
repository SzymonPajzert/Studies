#include <stdio.h>
#include "error.h"
#include "map.h"

typedef struct Tile {
    int number;
    struct Tile *prev_tile;
    struct Tile *next_tile;
    Pawn *occupying_pawn;
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
Tile *insert_tile(Tile *begin, int n, Pawn *inserted_pawn) {
    Tile *found = get_tile(begin, n);
    if (found->number == n) {
        perror("existing tile insertion");
        exit(40);
    } else {
        Tile *new_tile = malloc(sizeof(Tile));
        new_tile->number = n;
        new_tile->occupying_pawn = inserted_pawn;
        new_tile->next_tile = found->next_tile;
        new_tile->prev_tile = found;

        found->next_tile = new_tile;
        new_tile->next_tile->prev_tile = new_tile;

        return new_tile;
    }
}

Pawn *remove_tile(Tile **removed_tile) {
    Tile *prev, *next;
    Pawn *result;

    result = (*removed_tile)->occupying_pawn;
    prev = (*removed_tile)->prev_tile;
    next = (*removed_tile)->next_tile;
    free(*removed_tile);

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

typedef struct Column {
    int number;
    struct Column *prev_column;
    struct Column *next_column;
    Tile *first_tile;
} Column;

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
        map_error("same tile insertion");
    } else {
        Column *new_column = malloc(sizeof(Tile));
        new_column->number = n;
        new_column->first_tile = inserted_tile;
        new_column->next_column = found->next_column;
        new_column->prev_column = found;

        found->next_column = new_column;
        new_column->next_column->prev_column = new_column;
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
        map_error("non empty column deletion");
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

int map_size = -1;
Column *map;

void init_map(int n) {
    if (map_size == -1) {
        map_size = n;
        map = malloc(sizeof(Column));
        map->number = 0;
        return;
    }

    if (map_size != n) {
        map_error("double map initialization\n");
    }
}

int get_size() {
    return map_size;
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
    Tile *found = get_tile_from_map(x, y);
    if (found != NULL) {
        return found->occupying_pawn;
    } else {
        return NULL;
    }
}

Pawn *remove_pawn(int x, int y) {
    Tile *found = get_tile_from_map(x, y);
    if (found != NULL) {
        Column *result_column;
        Pawn *result = found->occupying_pawn;

        result_column = get_column(map, x);
        if (result_column->first_tile == found) {
            remove_tile(&(result_column->first_tile));
        } else {
            remove_tile(&found);
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
        tile = get_tile(column->first_tile, y);
        if (tile->number == y) {
            tile->occupying_pawn = pawn;
        } else {
            insert_tile(tile, y, pawn);
        }
    }
}

void move_pawn(int x1, int y1, int x2, int y2) {
    Pawn *moved = remove_pawn(x1, y1);
    insert_pawn(x2, y2, moved);
}

int is_free(int x, int y) {
    if (get_pawn(x, y) != NULL) return 1;
    else return 0;
}

void print_map() {
    int m = map_size < 10 ? map_size : 10;
    Pawn *grid[m + 1][m + 1];
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= m; j++) {
            grid[i][j] = NULL;
        }
    }

    Tile *tile;
    Column *column = map;
    /* Switching to the next column, first one is a place holder */
    column = column->next_column;

    while (column && column->number <= m) {
        tile = column->first_tile;
        while (tile && tile->number <= m) {
            grid[column->number][tile->number] = tile->occupying_pawn;
        }
    }

    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= m; j++) {
            if (grid[i][j] != NULL) {
                printf("%c", to_char(grid[i][j]));
            } else {
                printf(".");
            }
            printf("\n");
        }
    }
}