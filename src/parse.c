#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "error.h"
#include "parse.h"

int get_command_arg_len(command_type command) {
    switch (command) {
        case INIT:
            return 7;
        case MOVE:
            return 4;
        case PRODUCE_KNIGHT:
            return 4;
        case PRODUCE_PEASANT:
            return 4;
        case END_TURN:
            return 0;
    }
    return 0;
}

/** TODO documentation
 *
 */
command_type extract_command_type(char *buffer) {
    command_type result = INIT;

    int i;
    const char *keys[] = {"INIT", "MOVE", "PRODUCE_KNIGHT", "PRODUCE_PEASANT",
                          "END_TURN"};
    const command_type values[] = {INIT, MOVE, PRODUCE_KNIGHT, PRODUCE_PEASANT, END_TURN};
    for (i = 0; i < 5; ++i) {
        if (strcmp(buffer, keys[i]) == 0) {
            result = values[i];
        }
    }

    // If no value was matched against key.
    if (result == INIT && strcmp(buffer, "INIT")) {
        input_error();
    }

    return result;
}

/**
 * Extract list of integer arguments to tab. If length of array is different than expected size, exception is thrown.
 * @param[in] buffer Given string.
 * @param[in] arr Array in which result will be stored.
 * @param[in] size Expected number of integers in output.
 */
void extract_int_args(int *arr, int size) {
    for (int i = 0; i < size; i++) {
        if (!scanf("%d", arr + i)) {
            input_error();
        }
    }
}

command *parse_command() {
    command *result = malloc(sizeof(command));

    char buffer[100];
    if (scanf("%90s", buffer) == 1) {
        result->name = extract_command_type(buffer);
        int arg_num;
        arg_num = get_command_arg_len(result->name);
        extract_int_args(result->data, arg_num);
    } else {
        input_error();
    }


    return result;
}