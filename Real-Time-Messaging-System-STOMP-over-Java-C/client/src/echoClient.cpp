#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include <thread>
#include <mutex>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
//argv[0]-program name
/*argv[1]-host
argv[2]-port
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1]; //make host a strung
    short port = atoi(argv[2]);//change port to int using atoi
    
    ConnectionHandler connectionHandler(host, port); //build connectionHandler
    if (!connectionHandler.connect()) {//try to connect 
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;//cant connect throws error
        return 1;
    }
	
	//From here we will see the rest of the ehco client implementation:
    while (1) {//infinite loop
        const short bufsize = 1024;
        char buf[bufsize];// build an array of char
        std::cin.getline(buf, bufsize); //insert the user commands to the buf
		std::string line(buf); // Converts the char array buf into a std::string called line.
		int len=line.length(); // get line length
        if (!connectionHandler.sendLine(line)) {//trying to send the server the command
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
		// connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
        std::cout << "Sent " << len+1 << " bytes to server" << std::endl;

 
        // We can use one of three options to read data from the server:
        // 1. Read a fixed number of characters
        // 2. Read a line (up to the newline character using the getline() buffered reader
        // 3. Read up to the null character
        std::string answer;
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        if (!connectionHandler.getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        
		len=answer.length();
		// A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
		// we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
        answer.resize(len-1);
        std::cout << "Reply: " << answer << " " << len << " bytes " << std::endl << std::endl;
        if (answer == "bye") {
            std::cout << "Exiting...\n" << std::endl;
            break;
        }
    }
    return 0;
}
