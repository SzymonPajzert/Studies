#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parse.h"

#define BUFFOR_SIZE 500000

struct Request* createRequest(void) {
    struct Request* result;
    result = (struct Request*) malloc(sizeof(struct Request));
    return result;
}

void deleteRequest(struct Request* request) {
    free(request -> str1);
    free(request -> str2);
    free(request);
    return;
}

void cleanRequest(struct Request *storage) {
    storage->command = NO_LINE_AVAILABLE;
    free(storage->str1);
    free(storage->str2);
    return;
}

void printRequest(struct Request *req) {
    printf(
        "req=%d\nstr1=%s\nstr2=%s\nnum=%d\n\n",
        req->command,
        req->str1,
        req->str2,
        req->num
    );
    return;
}

char* strallocopy(char* value) {
    char* newadress = (char*) malloc(sizeof(char)*(strlen(value)+1));
    strcpy(newadress, value);
    return newadress;
}

char** split(char* begin, int words) {
    char** result = (char**) malloc(sizeof(char*) * words);
    int i;

    for (i=0; i<words-1 && begin; i++) {
        result[i] = begin;
        begin = strchr(begin, ' ');
        if (begin) {
            (*begin) = '\0';
            begin++;
        }
    }
    result[i] = begin;

    return result;
}

int parseLine(struct Request *storage) {
    char *begin, *parametersPtr;
    size_t size;
    int charNum;
    char** parameters;

    cleanRequest(storage);
    begin = NULL;
    size = 0;

    /* Delimeter is counted, if takes care about no-line case */
    if (  charNum = getline(&begin, &size, stdin), charNum <= 1 ) { return 0; }
    /* delete delimeter char */
    begin[charNum-1] = '\0';

    parametersPtr = strchr(begin, ' ');
    *parametersPtr = '\0';
    parametersPtr++;

    if (strcmp("NEW_DISEASE_ENTER_DESCRIPTION", begin) == 0) {
        storage -> command = NEW_DISEASE_ENTER_DESCRIPTION;

        parameters = split(parametersPtr, 2);
        storage -> str1 = strallocopy(parameters[0]);
        storage -> str2 = strallocopy(parameters[1]);
        return 1;
    }
    if (strcmp("NEW_DISEASE_COPY_DESCRIPTION", begin) == 0) {
        storage -> command = NEW_DISEASE_COPY_DESCRIPTION;

        parameters = split(parametersPtr, 2);
        storage -> str1 = strallocopy(parameters[0]);
        storage -> str2 = strallocopy(parameters[1]);
        return 1;
    }
    if (strcmp("CHANGE_DESCRIPTION", begin) == 0) {
        storage -> command = CHANGE_DESCRIPTION;

        parameters = split(parametersPtr, 3);
        storage -> str1 = strallocopy(parameters[0]);
        storage -> num = atoi(parameters[1]);
        storage -> str2 = strallocopy(parameters[2]);
        return 1;
    }
    if (strcmp("PRINT_DESCRIPTION", begin) == 0) {
        storage -> command = PRINT_DESCRIPTION;

        parameters = split(parametersPtr, 2);
        storage -> str1 = strallocopy(parameters[0]);
        storage -> num = atoi(parameters[1]);
        return 1;
    }
    if (strcmp("DELETE_PATIENT_DATA", begin) == 0) {
        storage -> command = DELETE_PATIENT_DATA;

        parameters = split(parametersPtr, 1);
        storage -> str1 = strallocopy(parameters[0]);
        return 1;
    }
    return -1;
}
