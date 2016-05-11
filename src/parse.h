/** @file
   Interface of parser.

*/

#ifndef MIDDLE_AGES_PARSE_H
#define MIDDLE_AGES_PARSE_H

typedef enum {
    INIT, MOVE, PRODUCE_KNIGHT, PRODUCE_PEASANT, END_TURN
} command_type;

typedef struct def_command {
    command_type name;
    int data[7];
} command;


/**
 * Reads a command.
 * Returns command with data points using 'command' structure.
 */
command* parse_command();

#endif /* MIDDLE_AGES_PARSE_H */
