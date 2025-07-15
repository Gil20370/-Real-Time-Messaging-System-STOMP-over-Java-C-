# Real-Time Messaging System – STOMP over Java & C++

## Overview

This project implements a real-time client-server messaging system for emergency services using the **STOMP protocol**. The server was developed in **Java**, supporting both **Thread-Per-Client** and **Reactor** architectures, while the client was written in **C++** and used two threads for interactive operation. The system allows users to subscribe to emergency channels (e.g., police, fire), send messages, and receive live updates.

The project was developed as part of a **System Programming** course and awarded a grade of **90**.

## Key Features

- **STOMP Protocol Support**: Full implementation of connection, subscription, message exchange, and error handling.
- **Dual Architectures**: Server supports both Thread-Per-Client and Reactor modes.
- **Cross-Language Communication**: Java server and C++ client integrated via network sockets.
- **Multi-threaded Clients**: Separate threads for user input and server communication.
- **Structured Messaging**: Clients interact with specific channels using defined message formats.

## Architecture

- **Java Server**: Handles STOMP protocol parsing, subscriptions, and concurrent user management.
- **C++ Client**: Uses a producer-consumer model to interact with the server in real time.
- **Channel Management**: Tracks logged-in clients and channel memberships.
- **Data Synchronization**: Ensures message consistency and safe concurrent access.

## Technical Highlights

- Languages: **Java** (server), **C++** (client)
- STOMP protocol parsing and validation
- Use of Java NIO and thread pools for scalability
- Socket programming for real-time communication
- Custom data structures for user and channel tracking

## Skills Demonstrated

- Network programming 
- Cross-language integration
- Protocol implementation 
- Designing concurrent systems with shared state
- Thread synchronization and socket lifecycle management

## Course Information

- **Course**: System Programming
- **Institution**: Ben-Gurion University of the Negev
- **Final Grade**: 90