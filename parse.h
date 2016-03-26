#ifndef PARSE_H
#define PARSE_H

enum command_type {
    NEW_DISEASE_ENTER_DESCRIPTION,
    NEW_DISEASE_COPY_DESCRIPTION,
    CHANGE_DESCRIPTION,
    PRINT_DESCRIPTION,
    DELETE_PATIENT_DATA,
    NO_LINE_AVAILABLE
};

struct Request {
    enum command_type command;
    char* str1;
    char* str2;
    int num;
};

struct Request* createRequest();
void deleteRequest(struct Request* request);

void printRequest(struct Request *storage);

char* strallocopy(char* value);
char** split(char* string, int words);
int parseLine(struct Request *storage);

#endif /* PARSE_H */
