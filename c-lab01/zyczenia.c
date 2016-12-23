#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <string.h>

const int n = 8;

char message[] = "Hohoho, wszystkiego najlepszego kujonku\n";
const size_t message_size = sizeof(message);

int main() {
    int proc_number = 0;
    int pid;
    int created_child = 0;
    char buffer[message_size];
  
    int pipe_dsc[2];
    int child_input, parent_output;
  
    while(proc_number < n-1 && !created_child) {
	if(pipe(pipe_dsc) == -1) exit(1);
    
	switch (pid = fork()) {
	    
	case -1: exit(1);

	case 0:
	    proc_number++;
	    //printf("Created child %d %d\n", proc_number, created_child);
	    parent_output = pipe_dsc[0];
	    close(pipe_dsc[1]);
	    break;
      
	default:
	    created_child = 1;
	    child_input = pipe_dsc[1];
	    close(pipe_dsc[0]);
	    break;
	}
    }

    if(proc_number == n-1) {
	// Jeśli wpiszę tutaj 0 to wszystko działa
	// Jeśli jednak zmienię je na 1 to dodają mi się d
	// czyli znaki \000 zamieniają się na coś drukowalnego 
	child_input = dup(1);
    }
  
    int size;
    while(read(parent_output, buffer, sizeof(buffer)) > 0 && proc_number >= 0) {
	write(child_input, buffer, sizeof(buffer));
    }
    close(parent_output);
    write(child_input, message, sizeof(message));
    close(child_input);
    if(proc_number < n-1) wait(0);
  

  
}
