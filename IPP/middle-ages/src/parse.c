/**@file
 * Implementation of parser.
 */

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "error.h"
#include "parse.h"

char buffer[100];

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

command_type extract_command_type() {
    command_type result = INIT;

    char *first_space;
    first_space = strchr(buffer, ' ');
    if (first_space) {
        *first_space = '\0';
    }

    int i;
    const char *keys[] = {"INIT", "MOVE", "PRODUCE_KNIGHT", "PRODUCE_PEASANT",
                          "END_TURN"};
    const command_type values[] = {INIT, MOVE, PRODUCE_KNIGHT, PRODUCE_PEASANT,
                                   END_TURN};
    for (i = 0; i < 5; ++i) {
        if (strcmp(buffer, keys[i]) == 0) {
            result = values[i];
        }
    }

    // If no value was matched against key.
    if (result == INIT && strcmp(buffer, "INIT")) input_error();

    if (first_space) {
        *first_space = ' ';
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
    if (size < 0) {
        input_error();
        return;
    } else {
        char *cur_ptr, *next_space;
        int converted_int;

        int i;
        cur_ptr = buffer;
        for (i = 0; i < size; i++) {
            if (cur_ptr) {
                next_space = strchr(cur_ptr, ' ');
            } else {
                input_error();
                return;
            }

            if (!next_space) {
                input_error();
                return;
            }
            cur_ptr = next_space + 1;
            converted_int = atoi(cur_ptr);

            // Failed conversion, wrong input
            if (converted_int == 0 && *cur_ptr != '0') {
                input_error();
                return;
            }
            arr[i] = converted_int;
        }

        //if there is something else, wrong input
        if (strchr(cur_ptr, ' ')) {
            input_error();
        }
    }
}

command *parse_command() {
    command *result = malloc(sizeof(command));

    char *delimiter;

    int correct_input =
            fgets(buffer, 100, stdin) &&                   /* input read     */
            buffer[0] != '\n' &&                           /* not empty line */
            (delimiter = strchr(buffer, '\n'), delimiter); /* full line read */

    if (correct_input) {
        // Delete delimiter
        *delimiter = '\0';
        result->name = extract_command_type();

        int arg_num;
        arg_num = get_command_arg_len(result->name);
        extract_int_args(result->data, arg_num);
        return result;
    } else {
        input_error();
        return NULL;
    }
}