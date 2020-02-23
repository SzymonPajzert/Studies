#include <ctype.h>
#include <stddef.h>
#include <malloc.h>

#include "parse.h"

char peek(const char **expr) {
    while(isspace(**expr)) (*expr)++;
    return **expr;
}

char get(const char **expr) {
    char result = *((*expr)++);
    while(isspace(**expr)) (*expr)++;
    return result;
}

/** Parse number
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @return Parsed number.
 */
int* parse_number(const char ** expr) {
    if(peek(expr) != '\0') {
        int result = get(expr) - '0';
        while (peek(expr) >= '0' && peek(expr) <= '9')
        {
            result = 10*result + get(expr) - '0';
        }
        int* result_ptr = malloc(sizeof(int));
        *result_ptr = result;
        return result_ptr;
    } else {
        return NULL;
    }

}

/** Parse variable
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @return Parsed variable number
 */
int* parse_variable(const char ** expr) {
    if(peek(expr) != '\0') {
        get(expr); // x
        get(expr); // [
        int* var_num = parse_number(expr);
        get(expr); // ]
        return var_num;
    } else {
        return NULL;
    }
}

