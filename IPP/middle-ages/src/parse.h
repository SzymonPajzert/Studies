/**@file
 * Interface of parser.
 */

#ifndef MIDDLE_AGES_PARSE_H
#define MIDDLE_AGES_PARSE_H


/**
 * Possible parse_command outputs types.
 */
typedef enum {
    INIT, MOVE, PRODUCE_KNIGHT, PRODUCE_PEASANT, END_TURN
} command_type;

/**
 * Structure returned by parse command.
 */
typedef struct {
    command_type name;
    /**< type of the returned command @see command_type */
    int data[7];        /**< array of numbers extracted from commands */
} command;


/**
 * Reads a command.
 * Returns command with data points using 'command' structure.
 */
command *parse_command();

#endif /* MIDDLE_AGES_PARSE_H */
