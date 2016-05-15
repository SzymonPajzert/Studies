/**@file
 * Implementation of error library.
 */

#include <stdio.h>
#include <stdlib.h>
#include "map.h"
#include "error.h"

void input_error() {
    fprintf(stderr, "input error\n");
    deallocate_map();
    exit(42);
}