#include <stdlib.h>
#include "parse.h"
#include "engine.h"

int main() {

    start_game();

    command *new_command;
    while (get_game_result() == PLAYED) {
        new_command = parse_command();
        int *data = new_command->data;

        switch (new_command->name) {
            case INIT:
                init(data[0], data[1], data[2], data[3], data[4], data[5],
                     data[6]);
                break;

            case MOVE:
                move(data[0], data[1], data[2], data[3]);
                break;

            case PRODUCE_KNIGHT:
                produce_knight(data[0], data[1], data[2], data[3]);
                break;

            case PRODUCE_PEASANT:
                produce_peasant(data[0], data[1], data[2], data[3]);
                break;

            case END_TURN:
                end_turn();
                break;
        }
        free(new_command);
        print_topleft();
    }

    end_game();

    return 0;
}
