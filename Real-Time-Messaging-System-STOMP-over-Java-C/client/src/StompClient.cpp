#include <iostream>
#include <thread>
#include <mutex>
#include <string>
#include "ConnectionHandler.h"
#include "event.h"
#include <fstream>

std::mutex mtx;                  // A mutex for synchronizing console output.
bool running = true;             // A flag to indicate whether the program is running.
bool isConnected = false;        // Tracks whether the connection is established.
ConnectionHandler* connectionHandlerPtr = nullptr; // Pointer to a dynamically created ConnectionHandler.
std::map<std::string, std::pair<int, int>> subscriptions; // Map to store channel -> (subscriptionId, receiptId).
std::map<std::string,std::vector<Event>> emergencymap;
std::string localHost;
std::string username;
std::string password;
std::vector<Event> sumerevent;
#include <iomanip>
std::string trim(const std::string& str) {
    auto start = str.begin();
    while (start != str.end() && std::isspace(*start)) {
        start++;
    }

    auto end = str.end();
    do {
        end--;
    } while (std::distance(start, end) > 0 && std::isspace(*end));

    return std::string(start, end + 1);
}
// Thread function for handling communication with the server
std::string epoch_to_date(int epoch_time) {
    // Convert epoch_time to time_t
    std::time_t time = static_cast<std::time_t>(epoch_time);

    // Convert time_t to tm struct for local time
    std::tm* timeinfo = std::localtime(&time);

    // Format the date and time as "DD/MM/YYYY_HH:MM"
    std::ostringstream oss;
    oss << std::put_time(timeinfo, "%d/%m/%Y_%H:%M");

    return oss.str();
}
void communicationThread() {
    std::map<int, std::string> myMap; // Map to track subscriptions and unsubscriptions by receipt ID

    while (running) {
        if (!isConnected || !connectionHandlerPtr) {
            std::this_thread::sleep_for(std::chrono::milliseconds(100)); // Wait until connected
            continue;
        }

        std::string serverMessage;
        if (connectionHandlerPtr->getFrameAscii(serverMessage, '\0')) {

            std::lock_guard<std::mutex> lock(mtx);

            if (serverMessage.find("CONNECTED") == 0) {
                std::cout << "CONNECTION HAS BEEN SUCCESSFUL" << std::endl;
            } 
            else if(serverMessage.find("ERROR") == 0){
               std::cout << serverMessage << std::endl;
            }
            else if(serverMessage.find("MESSAGE") == 0){
                
                Event myEvent=Event(serverMessage);   
                sumerevent.push_back(myEvent);
                std::cout << "Received: " << myEvent.toString() << std::endl;
            }
            else if (serverMessage.find("RECEIPT") == 0) {
                size_t receiptPos = serverMessage.find("receipt-id:");
                if (receiptPos != std::string::npos) {
                    std::string receiptIdStr = serverMessage.substr(receiptPos + 11); // Skip "receipt-id:"
                    receiptIdStr = receiptIdStr.substr(0, receiptIdStr.find('\n'));  // Trim after newline
                    int receiptId = std::stoi(receiptIdStr);

                    std::cout << "Received receipt-id: " << receiptId << std::endl;

                    if (receiptId >= 0) {
                        // Handle positive receipt-id
                        if (myMap.find(receiptId) == myMap.end()) {
                            // First time seeing this receipt-id: Add to map and print subscription
                            myMap[receiptId] = "active";
                            std::cout << "Subscription confirmed for receipt-id: " << receiptId << std::endl;
                        } else {
                            // Second time seeing this receipt-id: Remove from map and print unsubscription
                            myMap.erase(receiptId);
                            std::cout << "Unsubscription confirmed for receipt-id: " << receiptId << std::endl;
                        }
                    } else if (receiptId < 0) {
                        // Handle logout with negative receipt-id
                        std::cout << "Logout confirmed. Closing connection..." << std::endl;
                        connectionHandlerPtr->close();
                         delete connectionHandlerPtr;
                        connectionHandlerPtr = nullptr;
                        isConnected = false;
                            
                        
                    }
                } 
                else {
                    std::cerr << "Malformed RECEIPT frame: receipt-id not found." << std::endl;
                }
            }
        } else {
            std::cerr << "Disconnected from server." << std::endl;
            isConnected=false;
        }
    }
}


// Dynamically build a STOMP frame for the given command and arguments.
std::string buildFrame(const std::string& command, const std::vector<std::string>& args) {
    static int subscriptionId = -1; // Static counter for unique subscription IDs.
    static int receiptId = -1;
    if (command == "login" && args.size() == 3) {
        localHost=args[0];
        username=args[1];
        password=args[2];
    return "CONNECT\naccept-version:1.2\nhost:stomp.cs.bgu.ac.il" 
           "\nlogin:" + args[1] + 
           "\npasscode:" + args[2] + "\n\n\0";
    }
    else if (command == "join" ) {
        const std::string& channel = args[0];
        subscriptionId++; // Increment the unique ID for the subscription.
        receiptId++;
        subscriptions[channel] = {subscriptionId, receiptId};
        return "SUBSCRIBE\ndestination:" + channel + "\nreceipt:" + std::to_string(receiptId)+
               "\nid:" + std::to_string(subscriptionId) +"\n\n\0";

    } else if (command == "exit" && args.size() == 1) {
        const std::string& channel = args[0];
        if (subscriptions.find(channel) != subscriptions.end()) {
            int id = subscriptions[channel].first;
            int receipt = subscriptions[channel].second;

            if (receipt != -1) {
                return "UNSUBSCRIBE\nid:" + std::to_string(id) +
                       "\nreceipt:" + std::to_string(receipt) + "\n\0";
            }
        }
        return "";
    } 

    else if (command == "report" && args.size() == 1) {
        const std::string& filePath = args[0];
        // Parse events and channel name using parseEventsFile
        names_and_events parsedData;
        try {
            parsedData = parseEventsFile(filePath); // Parse the file
        } catch (const std::exception& e) {
            std::cerr << "Error parsing file: " << e.what() << std::endl;
            return "";
        }

        const std::string& channelName = parsedData.channel_name;
        std::cout<<"channelName="+channelName<<std:: endl;
        const std::vector<Event>& events = parsedData.events;
        std::ostringstream allFrames; // For debugging/logging
        for (const auto& event : events) {
            std::ostringstream frame;
            // Build the SEND frame
            frame << "SEND\n";
            frame << "destination:/" << channelName ;
            frame << "\n\nuser: " << username;
            frame << "\n"; // Separator between headers and body
            frame << event.toString(); // Assume Event has a toString method to format the body
            frame << "\n\0";

            // Append the frame to the log
            allFrames << frame.str();

            // Send each frame immediately
            if (!connectionHandlerPtr->sendFrameAscii(frame.str(), '\0')) {
                std::cerr << "Failed to send event to channel: " << channelName << std::endl;
            } else {
                std::cout << "Event sent to channel: " << channelName << std::endl;
            }
        }

        return allFrames.str(); // Return all frames for debugging/logging
    }
  
    
    else if (command == "logout") {
        static int receiptId = 0;
        receiptId--;
        
        return "DISCONNECT\nreceipt:" + std::to_string(receiptId) + "\n\n\0";

    }
    return ""; // Return an empty string for invalid or unrecognized commands.
}
void handleReportCommand(const std::vector<std::string>& args);

// Thread function for reading user input and sending it to the server.
void inputThread() {
    while (running) {
        std::string input;
        std::getline(std::cin, input); // Read user input.

        // Parse the command and its arguments.
        std::istringstream iss(input);
        std::string command;
        iss >> command;

        std::vector<std::string> args;
        std::string arg;
        while (iss >> arg) {
            args.push_back(arg);
        }
       if (command == "summary") {
        
            if (args.size() < 3) {
                std::cerr <<"Invalid arguments for summary. Expected:summary {channel_name} {user} {file}" << std::endl;
                return;
            }

            std::string summaryChannelName = args[0];
            std::string summaryUsername = args[1];
            std::string outputPath = args[2];
            int sumOfTotalReports = 0;
            int sumOfTotalTrue = 0;
            int sumOgTotalArrive = 0;

            std::ofstream outFile(outputPath);
            if (!outFile) {
                std::cerr << "Error: Could not open file " <<outputPath << " for writing." << std::endl;
                return;
            }

            outFile << "Channel: " << summaryChannelName << "\n";
            outFile << "Stats:\n";

            for (const auto& e : sumerevent) {
                std::cerr << "jimy " << e.toString() << std::endl;
                       // std::cout << "Checking event for user: " <<trim(e.getEventOwnerUser()) <<"against " << trim(summaryUsername) <<std::endl;
                if (trim(e.getEventOwnerUser()) == trim(summaryUsername)) {
                    //std::cout << "inside" << std::endl;
                    sumOfTotalReports++;
                    for (const auto& [key, value] :e.get_general_information()) {
                        if (trim(key)=="active" && trim(value)=="true")
                            sumOfTotalTrue++;
                        if (trim(key)=="forces_arrival_at_scene" &&trim(value)=="true")
                            sumOgTotalArrive++;
                    }
                }
            }

            outFile << "Total Reports: " << sumOfTotalReports << "\n";
            outFile << "Total Active (true): " << sumOfTotalTrue << "\n";
            outFile << "Forces arrival at scene: " << sumOgTotalArrive << "\n";
            sumOfTotalReports=0;
            for (const auto& e : sumerevent) {
                       // std::cout << "Checking event for user: " <<trim(e.getEventOwnerUser())<<" against " << trim(summaryUsername)<<std::endl;
                if (trim(e.getEventOwnerUser()) == trim(summaryUsername)) {
                    //std::cout << "inside" << std::endl;
                    sumOfTotalReports++;
                    for (const auto& [key, value] :e.get_general_information()) {
                        if (trim(key)=="active" && trim(value)=="true")
                            sumOfTotalTrue++;
                        if (trim(key)=="forces_arrival_at_scene" && trim(value)=="true")
                            sumOgTotalArrive++;
                    }

                    outFile << "Report_" << sumOfTotalReports << ":\n";
                    outFile << "  city: " << e.get_city() << "\n";
                    outFile << "  date time: " <<epoch_to_date(e.get_date_time()) << "\n";
                    outFile << "  event name: " << e.get_name() << "\n";

                    std::string description = e.get_description().substr(0, 27);
                    if (e.get_description().size() > 27) description += "...";
                    outFile << "  summary: " << description << "\n";
                }
            }

            outFile.flush();
            outFile.close();
            std::cout << "Summary written to " << outputPath << std::endl;
        }

        else if(command == "login") {
            if (args.size() != 3) {
                // If the user doesn't provide exactly 3 arguments, display the error.
                std::cerr << "Invalid host:port format. Expected <host>:<port>." << std::endl;
                continue;
            }

            // Split host:port into separate components.
            std::string hostPort = args[0];
            size_t colonPos = hostPort.find(':');
            if (colonPos == std::string::npos) {
                // If the host:port format is invalid, display the error.
                std::cerr << "Invalid host:port format. Expected <host>:<port>." << std::endl;
                continue;
            }

            // Extract host and port components.
            std::string host = hostPort.substr(0, colonPos);         // Extract host (before ':').
            std::string portStr = hostPort.substr(colonPos + 1);     // Extract port (after ':').

            short port = 0;
            try {
                port = std::stoi(portStr); // Convert port to integer.
            } catch (const std::invalid_argument& e) {
                std::cerr << "Invalid port value: " << portStr << std::endl;
                continue;
            }
            // Dynamically create the ConnectionHandler and connect.
            if (!connectionHandlerPtr) {
                connectionHandlerPtr = new ConnectionHandler(host, port);
                running=true;
                if (!connectionHandlerPtr->connect()) {
                    std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
                    delete connectionHandlerPtr;
                    connectionHandlerPtr = nullptr;
                    continue;
                }

                isConnected = true; // Mark the connection as established.
                std::cout << "Connected to " << host << ":" << port << std::endl;
            }
        }
        if (command == "report") 
            handleReportCommand(args);
        else
        if (isConnected && connectionHandlerPtr) {
            std::string frame = buildFrame(command, args);

            if (!frame.empty()) {
                if (!connectionHandlerPtr->sendFrameAscii(frame,'\0')) { // Send the frame to the server.
                    std::cerr << "Error sending frame to server." << std::endl;
                    isConnected = false;
                     delete connectionHandlerPtr;
                    connectionHandlerPtr = nullptr;

                }
                else {
                    std::cout << "frame sent: " << frame << std::endl;
                }
            }
            else 
            if (command!="summary")
            {
                std::cerr << "Invalid command or arguments. Frame could not be built." << std::endl;
            }
        }
    }
}
int main() {
    // Create threads for communication and input handling.
    std::thread commThread(communicationThread); // Thread for handling server messages.
    std::thread inputThreadInstance(inputThread); // Thread for handling user input.

    // Wait for both threads to complete.
    commThread.join();
    inputThreadInstance.join();

    if (connectionHandlerPtr) {
        connectionHandlerPtr->close();
        
        delete connectionHandlerPtr;
    }

    return 0; // Exit the program successfully.
}
void handleReportCommand(const std::vector<std::string>& args) {
    if (args.size() != 1) {
        std::cerr << "Invalid arguments for report. Expected: report {file_path}" << std::endl;
        return;
    }

    std::string filePath = args[0];

    // Parse events from the file
    names_and_events parsedData;
    try {
        parsedData = parseEventsFile(filePath); // Parse the file
    } catch (const std::exception& e) {
        std::cerr << "Error parsing file: " << e.what() << std::endl;
        return;
    }

    const std::string& channelName = parsedData.channel_name;
    const std::vector<Event>& events = parsedData.events;

    // Iterate through each event and send it as a separate message
    for (const auto& event : events) {
        // Build the SEND frame for the event
        std::ostringstream frame;
        frame << "SEND\n";
        frame << "destination:/" << channelName << "\n";
        frame << "\nuser: " << username << "\n"; // Sender's information (if needed)
        frame << event.toString(); // Event content
        frame << "\n\0"; // End of frame

        // Send the frame
        if (!connectionHandlerPtr->sendFrameAscii(frame.str(), '\0')) {
            std::cerr << "Failed to send event to channel: " << channelName << std::endl;
        } else {
            std::cout << "Event sent to channel: " << channelName << std::endl;
        }
    }
}

