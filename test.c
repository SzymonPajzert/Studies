#include <stdio.h>
#include <stdlib.h>
#include "parse.h"

int main (int argc, char **argv) {
    struct Request *storage;
    storage = (struct Request*) malloc(sizeof(struct Request));
    printRequest(storage);

    while(storage -> command != NO_LINE_AVAILABLE) {
        printf("%d\n", parseLine(storage));
        printRequest(storage);
    }

    deleteRequest(storage);
    return 0;
}
