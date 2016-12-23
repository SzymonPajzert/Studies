/**@file
 * Implementation of game utilities, such as turn counting and result returning.
 */

#include <stdlib.h>
#include <stdio.h>
#include "player.h"
#include "map.h"
#include "error.h"
#include "game.h"


/**
 * Internal representation of the game, contains current player, max turn
 * number and turn count.
 */
typedef struct {
    int initialized;
    /**< -1 before, 0 during, 1 after initialization*/
    int cur_player_num;
    /**< Number of current player (0 or 1) */
    int turn_count;
    /**< Current turn number (counted from 1) */
    int max_turn_num;
    /**< Maximum number of possible turns. */
    Player players[2];  /**< Array of players taking part in the game. */
} Game;

/**
 * Singleton of the game.
 */
Game *game_instance;

void start_game() {
    game_instance = malloc(sizeof(Game));
    game_instance->initialized = 0;
    game_instance->cur_player_num = 0;
    game_instance->turn_count = 1;
    game_instance->max_turn_num = -1;

    for (int i = 0; i < 2; i++) {
        game_instance->players[i].has_king = 0;
        game_instance->players[i].number = i + 1;
    }
}

void init_game(int n, int p) {
    if (n > 0) {
        if (game_instance->initialized++ < 1) {
            if (game_instance->max_turn_num == -1) {
                game_instance->max_turn_num = n+1;
                return;
            }

            if (game_instance->max_turn_num != n+1) {
                input_error();
            }

            game_instance->cur_player_num=p;
            if (p==2) game_instance->turn_count--;
        }
    } else {
        input_error();
    }

}

void end_game() {
    int result=41;
    switch (get_game_result()) {
        case WIN:
            result = 0;
            break;

        case DEFEAT:
            result=2;
            break;

        case DRAW:
            result=1;
            break;

        default:
            input_error();
            break;
    }
    deallocate_map();
    free(game_instance);
    exit(result);
}

void end_turn() {
    if (!is_initialized()) input_error();
    game_instance->turn_count++;
    printf("END_TURN\n");
    fflush(stdout);
}

game_result get_game_result() {
    if (game_instance->initialized > 0) {
        int first_has_king, second_has_king;
        first_has_king = game_instance->players->has_king;
        second_has_king = (game_instance->players + 1)->has_king;

        if (first_has_king) {
            if (second_has_king) {
                if (game_instance->turn_count == game_instance->max_turn_num) {
                    return DRAW;
                } else {
                    return PLAYED;
                }
            } else {
                if (get_cur_player()->number == 1) return DEFEAT;
                else return WIN;
            }
        } else {
            if (second_has_king) {
                if (get_cur_player()->number == 2) return DEFEAT;
                else return WIN;
            } else {
                return DRAW;
            }
        }
    } else {
        return PLAYED;
    }
}

Player *get_cur_player() {
    return game_instance->players + (game_instance->cur_player_num);
}

Player *get_n_player(int n) {
    if (0 < n && n < 3) {
        return game_instance->players + (n - 1);
    } else {
        input_error();
        return NULL;
    }
}

int get_turn_number() {
    return game_instance->turn_count;
}

int is_initialized() {
    return game_instance->initialized > 0;
}