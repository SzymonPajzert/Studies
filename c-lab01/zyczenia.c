const int n = 10;

char[] message = "Hohoho, wszystkiego najlepszego kujonku";
const size_t message_size = sizeof(message);

int main() {
  int proc_number = 0;
  int pid;
  bool created_child = false;
  char buffer[message_size];
  
  int pipe_dsc[2];
  int child_input, parent_output;
  
  while(proc_number < n && !created_child) {
    if(pipe(pipe_dcs) == -1) syserr("Error in pipe\n");
    
    switch (pid = fork()) {
    case -1:
      syserr("Error in fork\n");

    case 0:
      proc_number++;
      parent_output = pipe_dsc[0];
      close(pipe_dsc[1]);
      
    default:
      created_child = true;
      child_input = pipe_dsc[1];
      close(pipe_dsc[0]);
    }
  }

  if(proc_number < n-1) {
    while(read(parent_output, buffer, message_size)) {
      write(child_input, buffer, sizeof(buffer));
    }
    close(parent_output);
    write(child_input, message, message_size);
    close(child_input);
  }

  if(proc_number == n-1) {
    
  }
}
