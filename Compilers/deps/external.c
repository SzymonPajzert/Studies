#include <string.h>
#include <stdlib.h>
#include <stdio.h>

char* string_concat(char* a, char* b) {
	size_t a_len = strlen(a);
	size_t b_len = strlen(b);

	char* result = (char*) malloc(a_len + b_len + 1);
	strcpy(result, a);
	strcpy(result + a_len, b);
	result[a_len + b_len] = '\0';

	return result;
}

char* readString() {
	char *line = NULL;  /* forces getline to allocate with malloc */
	size_t len = 0;     /* ignored when line = NULL */
	ssize_t read;

	while((read = getline(&line, &len, stdin)) <= 1) {
		len = 0;
	};
	
	line[read - 1] = 0;

	return line;
}

/*
int main() {
	char* a = readString();
	char* b = readString();
	printf("%s", string_concat(a, b));
}
*/
