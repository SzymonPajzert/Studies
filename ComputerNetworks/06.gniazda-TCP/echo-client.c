#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>


#include "err.h"

#define BUFFER_SIZE 2000

int main(int argc, char *argv[])
{
  int sock;
  struct addrinfo addr_hints;
  struct addrinfo *addr_result;

  int err;
  char buffer[BUFFER_SIZE];
  ssize_t rcv_len;

  if (argc < 4) {
    fatal("Usage: %s host port file portion_size...\n", argv[0]);
  }

  // 'converting' host/port in string to struct addrinfo
  memset(&addr_hints, 0, sizeof(struct addrinfo));
  addr_hints.ai_family = AF_INET; // IPv4
  addr_hints.ai_socktype = SOCK_STREAM;
  addr_hints.ai_protocol = IPPROTO_TCP;
  err = getaddrinfo(argv[1], argv[2], &addr_hints, &addr_result);
  if (err == EAI_SYSTEM) { // system error
    syserr("getaddrinfo: %s", gai_strerror(err));
  }
  else if (err != 0) { // other error (host not found, etc.)
    fatal("getaddrinfo: %s", gai_strerror(err));
  }

  // initialize socket according to getaddrinfo results
  sock = socket(addr_result->ai_family, addr_result->ai_socktype, addr_result->ai_protocol);
  if (sock < 0)
    syserr("socket");

  // connect socket to the server
  if (connect(sock, addr_result->ai_addr, addr_result->ai_addrlen) < 0)
    syserr("connect");

  freeaddrinfo(addr_result);

  const size_t portion_size = atoi(argv[4]);
  if (portion_size >= BUFFER_SIZE) {
      (void) fprintf(stderr, "ignoring too long portion_size parameter %lu\n", portion_size);
      return 1;
  }

  const int filedesc = open(argv[3], O_RDONLY);
  if(filedesc < 0) {
	  (void) fprintf(stderr, "cannot open file %s", argv[3]);
	  return 1;
  }

  size_t read_size;
  do {
	  read_size = read(filedesc, buffer, portion_size);
	  if(read_size > 1) {
		  printf("writing to socket: %s\n", buffer);
		  if (write(sock, buffer, read_size) != read_size) {
			  syserr("partial / failed write");
		  }

		  memset(buffer, 0, sizeof(buffer));
		  rcv_len = read(sock, buffer, sizeof(buffer) - 1);
		  if (rcv_len < 0) {
			  syserr("read");
		  }
		  printf("read from socket: %zd bytes: %s\n", rcv_len, buffer);
	  }
  } while (read_size > 1);

  (void) close(filedesc);

  (void) close(sock); // socket would be closed anyway when the program ends

  return 0;
}
