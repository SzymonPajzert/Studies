# CC=gcc
# CFLAG=
# DEPS = structure.h parse.h
# OBJ = structure.o parse.o hospital.o

# all: $(OBJ)
#	$(CC) $(CFLAG) -o hospital $(OBJ)

# %.o: %.c
#	$(CC) $(CFLAG) -c $^ -o $@

#debug: CFLAG= -g --pedantic
#debug: all

CC=gcc

all:
	$(CC) -c parse.c
	$(CC) -c structure.c
	$(CC) -c hospital.c
	$(CC) -o hospital structure.o parse.o hospital.o

testing: CC=gcc -g --pedantic
testing:
	$(CC) -c parse.c
	$(CC) -c test.c
	$(CC) -o test parse.o test.o

debug: CC=gcc -g --pedantic
debug:
	$(CC) -c parse.c
	$(CC) -c structure.c
	$(CC) -c hospital.c
	$(CC) -o hospital structure.o parse.o hospital.o

clean:
	rm *.o hospital
