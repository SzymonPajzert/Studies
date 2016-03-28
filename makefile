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
CFLAG=-O2

all:
	$(CC) $(CFLAG) -c parse.c
	$(CC) $(CFLAG) -c structure.c
	$(CC) $(CFLAG) -c hospital.c
	$(CC) $(CFLAG) -o hospital structure.o parse.o hospital.o

debug: CC=gcc -g --pedantic
debug:
	$(CC) $(CFLAG) -c parse.c
	$(CC) $(CFLAG) -c structure.c
	$(CC) $(CFLAG) -c hospital.c
	$(CC) $(CFLAG) -o hospital.dbg structure.o parse.o hospital.o

clean:
	rm *.o hospital
