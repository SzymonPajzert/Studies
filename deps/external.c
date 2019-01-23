#include <string.h>

char* string_concat(char* a, char* b) {
	char* result = (char*) malloc(strlen(a) + strlen(b));
	strcpy(result, a);
	strcpy(result + strlen(a), b);
	return result;
}
/*
int main() {
	char a[5] = "haha\0";
	char b[5] = "hoho\0";
	printf("%s", string_concat(a, b));
}
*/
