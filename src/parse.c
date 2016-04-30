#include <stdlib.h>
#include <stdio.h>
#include <string.h>

typedef enum {
    INIT, MOVE, PRODUCE_KNIGHT, PRODUCE_PEASANT, END_TURN
} command_type;

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

typedef struct def_command {
    command_type name;
    int data[7];
} command;

void input_error() {
    perror("input error\n");
    exit(42);
}

command_type extract_command_type(char *buffer) {
    command_type result = INIT;

    char *first_space;
    first_space = strchr(buffer, ' ');
    *first_space = '\0';

    int i;
    const char *keys[] = {"INIT", "MOVE", "PRODUCE_KNIGHT", "PRODUCE_PEASANT", "ENT_TURN"};
    const command_type values[] = {INIT, MOVE, PRODUCE_KNIGHT, PRODUCE_PEASANT, END_TURN};
    for (i = 0; i < 4; ++i) {
        if (strcmp(buffer, keys[i])) {
            result = values[i];
        }
    }

    // If no value was matched against key.
    if (result == INIT && !strcmp(buffer, "INIT")) input_error();

    *first_space = ' ';
    return result;
}

/**
 * Extract list of integer arguments to tab. If length of array is different than expected size, exception is thrown.
 * @param[in] buffer Given string.
 * @param[in] arr Array in which result will be stored.
 * @param[in] size Expected number of integers in output.
 */
void extract_int_args(char *buffer, int *arr, int size) {
    char *cur_ptr, *next_space;
    int converted_int;
    next_space = strchr(buffer, ' ');
    cur_ptr = next_space + 1;

    int i;
    for (i = 0; i < size; i++) {
        // Look for space if it isn't last token
        if (i < size - 1) {
            next_space = strchr(cur_ptr, ' ');
            if (next_space == NULL) input_error();
            else *next_space = '\0';
        }

        converted_int = atoi(cur_ptr);

        // Failed conversion, wrong input
        if (converted_int == 0 && *cur_ptr != '0') input_error();
        arr[i] = converted_int;

        if (next_space == NULL) input_error();
        else *next_space = ' ';
        cur_ptr = next_space + 1;
    }
}

command *parse_command() {
    command *result = malloc(sizeof(command));

    char *buffer = NULL;
    size_t size = 0;
    getline(&buffer, &size, stdin);
    // Delete delimiter
    buffer[size - 1] = '\0';

    result->name = extract_command_type(buffer);
    int arg_num;
    arg_num = get_command_arg_len(result->name);
    extract_int_args(buffer, result->data, arg_num);

    free(buffer);
    return result;
}