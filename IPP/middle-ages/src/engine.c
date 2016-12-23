/**@file
 * Implementation of game engine.
 */

#include "error.h"
#include "player.h"
#include "pawn.h"
#include "game.h"
#include "map.h"
#include "engine.h"

/** Max function */
#define max(x, y) (((x)<(y)) ? (y) : (x))

/** Abs function */
#define abs(x) ((x)<0 ? -(x) : (x) )

void init(int n, int k, int p, int x1, int y1, int x2, int y2) {
    // Check max metric
    if (max(abs(x1 - x2), abs(y1 - y2)) < 8) {
        input_error();
    }

    init_map(n);
    init_game(k, (p + 1) % 2 + 1);

    Player *first_player = get_n_player(1);
    Player *second_player = get_n_player(2);

    insert_pawn(x1, y1, create(KING, first_player));
    first_player->has_king=1;
    insert_pawn(x1 + 1, y1, create(PEASANT, first_player));
    insert_pawn(x1 + 2, y1, create(KNIGHT, first_player));
    insert_pawn(x1 + 3, y1, create(KNIGHT, first_player));

    insert_pawn(x2, y2, create(KING, second_player));
    second_player->has_king=1;
    insert_pawn(x2 + 1, y2, create(PEASANT, second_player));
    insert_pawn(x2 + 2, y2, create(KNIGHT, second_player));
    insert_pawn(x2 + 3, y2, create(KNIGHT, second_player));

    if(p == 1) {
        end_turn();
    }
}

/**
 * Checks whether pointer points to existing pawn owned by current player.
 */
void assert_proper_existence(Pawn *pawn) {
    if (pawn == NULL || pawn->owner != get_cur_player() || !rested_pawn(pawn)) {
        input_error();
    }
}

void move(int x1, int y1, int x2, int y2) {
    if (!is_initialized() || abs(x1 - x2) > 1 || abs(y1 - y2) > 1) {
        input_error();
    }

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
                die(remove_pawn(x1, y1));
                break;

            case SECOND:
                die(remove_pawn(x2, y2));
                move_pawn(x1, y1, x2, y2);
                break;

            case BOTH:
                die(remove_pawn(x1, y1));
                die(remove_pawn(x2, y2));
                break;

            case IMPOSSIBLE:
                input_error();
                break;
        }
    }

}

/**
 * Universal produce function, generating pawns.
 */
void produce(int x1, int y1, int x2, int y2, pawn_type type) {
    if (!is_initialized() || abs(x1 - x2) > 1 || abs(y1 - y2) > 1) {
        input_error();
    }

    Pawn *peasant = get_pawn(x1, y1);
    assert_proper_existence(peasant);

    if (rested_peasant(peasant) && is_free(x2, y2)) {
        Pawn *produced = create(type, get_cur_player());
        insert_pawn(x2, y2, produced);
    } else {
        input_error();
    }
}

void produce_knight(int x1, int y1, int x2, int y2) {
    produce(x1, y1, x2, y2, KNIGHT);
}

void produce_peasant(int x1, int y1, int x2, int y2) {
    produce(x1, y1, x2, y2, PEASANT);
}