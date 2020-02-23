CXX = g++
CXXFLAGS = -Wall -std=c++14 -g3 -O3

all: brandes

brandes: brandes.o main.o logger.o
	$(CC) -o $@ $^ -lstdc++ -lpthread

clean:
	rm *.o

