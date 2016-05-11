#include <stdlib.h>
#include <stdio.h>
#include "../src/parse.h"

int main() {
    command *new_command;
    while (1) {
        new_command = parse_command();

        printf("%d ", new_command->name);
        for (int i = 0; i < 7; i++) {
            printf("%d ", new_command->data[i]);
        };

        printf("\n");

        free(new_command);
    }
}

