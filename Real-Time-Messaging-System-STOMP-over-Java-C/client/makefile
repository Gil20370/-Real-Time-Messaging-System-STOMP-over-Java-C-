# Compiler flags
CFLAGS := -c -Wall -Weffc++ -g -std=c++17 -Iinclude 
LDFLAGS := -lboost_system -lpthread

# Default target
all: bin/StompEMIClient

# Link object files to create the executable
bin/StompEMIClient: bin/ConnectionHandler.o bin/StompClient.o bin/event.o
	g++ -o bin/StompEMIClient bin/ConnectionHandler.o bin/StompClient.o bin/event.o $(LDFLAGS)

# Compile ConnectionHandler
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

# Compile event
bin/event.o: src/event.cpp
	g++ $(CFLAGS) -o bin/event.o src/event.cpp

# Compile StompClient
bin/StompClient.o: src/StompClient.cpp
	g++ $(CFLAGS) -o bin/StompClient.o src/StompClient.cpp

# Clean all compiled files
.PHONY: clean
clean:
	rm -f bin/*
