#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <fcntl.h>

#include "err.h"

#define BUFFER_SIZE 1000

int main(int argc, char *argv[]) {
  int sock;
  struct addrinfo addr_hints;
  struct addrinfo *addr_result;

  int i, flags, sflags;
  char buffer[BUFFER_SIZE];
  ssize_t snd_len, rcv_len;
  struct sockaddr_in my_address;
  struct sockaddr_in srvr_address;
  socklen_t rcva_len;

  if (argc < 4) {
    fatal("Usage: %s host port file portion_size...\n", argv[0]);
  }

  // 'converting' host/port in string to struct addrinfo
  (void) memset(&addr_hints, 0, sizeof(struct addrinfo));
  addr_hints.ai_family = AF_INET; // IPv4
  addr_hints.ai_socktype = SOCK_DGRAM;
  addr_hints.ai_protocol = IPPROTO_UDP;
  addr_hints.ai_flags = 0;
  addr_hints.ai_addrlen = 0;
  addr_hints.ai_addr = NULL;
  addr_hints.ai_canonname = NULL;
  addr_hints.ai_next = NULL;
  if (getaddrinfo(argv[1], NULL, &addr_hints, &addr_result) != 0) {
    syserr("getaddrinfo");
  }

  my_address.sin_family = AF_INET; // IPv4
  my_address.sin_addr.s_addr =
      ((struct sockaddr_in*) (addr_result->ai_addr))->sin_addr.s_addr; // address IP
  my_address.sin_port = htons((uint16_t) atoi(argv[2])); // port from the command line

  freeaddrinfo(addr_result);

  sock = socket(PF_INET, SOCK_DGRAM, 0);
  if (sock < 0)
    syserr("socket");

  const size_t portion_size = atoi(argv[4]);
  if (portion_size >= BUFFER_SIZE) {
      (void) fprintf(stderr, "ignoring too long portion_size parameter %d\n");
      return 1;
  }

  const int filedesc = open(argv[3], O_RDONLY);
  if(filedesc < 0) {
	  (void) fprintf(stderr, "cannot open file %s", argv[3]);
	  return 1;
  }

  // size_t offset = 0;
  size_t read_size;
  do {
	  read_size = read(filedesc, buffer, portion_size);
	  if(read_size > 0) {
		  // offset += read_size;
		  (void) printf("sending to socket: %s\n", buffer);
		  sflags = 0;
		  rcva_len = (socklen_t) sizeof(my_address);
		  snd_len = sendto(sock, buffer, read_size, sflags,
						   (struct sockaddr *) &my_address, rcva_len);
		  if (snd_len != (ssize_t) read_size) {
			  syserr("partial / failed write");
		  }

		  (void) memset(buffer, 0, sizeof(buffer));
		  flags = 0;
		  const size_t buflen = (size_t) sizeof(buffer) - 1;
		  rcva_len = (socklen_t) sizeof(srvr_address);
		  rcv_len = recvfrom(sock, buffer, buflen, flags,
							 (struct sockaddr *) &srvr_address, &rcva_len);
  
		  if (rcv_len < 0) {
			  syserr("read");
		  }
		  (void) printf("read from socket: %zd bytes: %s\n", rcv_len, buffer);
	  }
	  
  } while(read_size > 0);

  if (close(sock) == -1) { //very rare errors can occur here, but then
	  syserr("close"); //it's healthy to do the check
  }

  return 0;
}
