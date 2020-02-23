#ifndef PARSE_H
#define PARSE_H

char peek(const char **expr);

char get(const char **expr);

/** Parse number
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @return Parsed number.
 */
int* parse_number(const char ** expr);

/** Parse variable
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @return Parsed variable number
 */
int* parse_variable(const char ** expr);

#endif //PARSE_H
