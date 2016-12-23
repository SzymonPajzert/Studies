#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>

const int n = 8;

char message[] = "Hohoho, wszystkiego najlepszego kujonku od procesu nr %d\n";
const size_t message_size = sizeof(message);

int main() {
  int proc_number = 0;
  int pid;
  int created_child = 0;
  char buffer[message_size];
  
  int pipe_dsc[2];
  int child_input, parent_output;
  
  while(proc_number < n && !created_child) {
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

  int size;
  if(proc_number < n-1) {
    while(read(parent_output, buffer, sizeof(buffer)) > 0 && proc_number >= 0) {
      write(child_input, buffer, sizeof(buffer));
    }
    close(parent_output);
    sprintf(buffer, message, proc_number);
    write(child_input, buffer, sizeof(buffer));
    close(child_input);
    wait(0);
  }

  if(proc_number == n-1) {
    while(read(parent_output, buffer, message_size)) {
      printf("%s", buffer);
    }
    close(parent_output);
    printf("%s", message);
  }
}
