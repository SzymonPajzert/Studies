#include <stdio.h>
#include <stdlib.h>
#include "error.h"
#include "player.h"
#include "pawn.h"
#include "game.h"
#include "map.h"
#include "engine.h"


void start_game() {
    _start_game();
}

void init(int n, int k, int p, int x1, int y1, int x2, int y2) {
    init_map(n);
    set_max_turn(k);
    init_game();

    Player *init_player = get_n_player(p);
    init_player->has_king = 1;

    Pawn *king, *knight1, *knight2, *peasant;
    king = malloc(sizeof(Pawn));
    knight1 = malloc(sizeof(Pawn));
    knight2 = malloc(sizeof(Pawn));
    peasant = malloc(sizeof(Pawn));

    int x[2], y[2];
    x[0] = x1;
    x[1] = x2;
    y[0] = y1;
    y[1] = y2;

    insert_pawn(x[p - 1], y[p - 1], king);
    insert_pawn(x[p - 1] + 1, y[p - 1], knight1);
    insert_pawn(x[p - 1] + 2, y[p - 1], knight2);
    insert_pawn(x[p - 1] + 3, y[p - 1], peasant);
}

/**
 * Checks whether pointer points to existing pawn owned by current player.
 */
void assert_proper_existence(Pawn *pawn) {
    if (pawn == NULL || pawn->owner != get_cur_player()) {
        /* @TODO move of invalid pawn */
        input_error();
    }
}

void move(int x1, int y1, int x2, int y2) {
    Pawn *active, *passive;
    active = get_pawn(x1, y1);
    assert_proper_existence(active);
    passive = get_pawn(x2, y2);

    if (passive == NULL) {
        move_pawn(x1, y1, x2, y2);
    } else {
        fight_result result = fight(active, passive);
        switch (result) {
            case FIRST:
                /* @TODO death of first */
                break;

            case SECOND:
                /* @TODO death of second */
                break;

            case BOTH:
                /* @TODO simultaneous death */
                break;

            case IMPOSSIBLE:
                /* @TODO impossible fight */
                break;
        }
    }

}

void produce(int x1, int y1, int x2, int y2, pawn_type type) {
    Pawn *peasant = get_pawn(x1, y1);
    assert_proper_existence(peasant);

    if (peasant_is_rested(peasant, get_turn_number()) && is_free(x2, y2)) {
        Pawn *produced = malloc(sizeof(Pawn));
        produced->owner = get_cur_player();
        produced->last_moved = get_turn_number() - 1;
        produced->type = type;
        insert_pawn(x2, y2, produced);
    } else {
        /* @TODO invalid input during production */
    }
}

void produce_knight(int x1, int y1, int x2, int y2) {
    produce(x1, y1, x2, y2, KNIGHT);
}

void produce_peasant(int x1, int y1, int x2, int y2) {
    produce(x1, y1, x2, y2, PEASANT);
}

void end_turn() {
    _end_turn();
}

void end_game() {
    _end_game();
}

void print_topleft() {
    print_map();
}