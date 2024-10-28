# Blockchain Peer-to-Peer Node Network

## Project Overview

This project demonstrates a peer-to-peer blockchain network built using Java, which enables multiple nodes to communicate, exchange data, and maintain a synchronized, distributed ledger of blocks. Each node operates autonomously, handling its own blockchain, validating blocks, and sharing updates with its peers in real-time.

The primary goal of this project is to simulate the decentralized nature of blockchain technology, demonstrating how nodes can interact, maintain consensus, and propagate data in a distributed system. It focuses on core concepts such as network communication, concurrency, and data integrity—essential components in any distributed ledger system.

## Features

- Distributed Block Propagation: Each node can add new blocks to its blockchain and propagate them to its connected peers. Nodes validate incoming blocks and update their chain accordingly.
- Peer-to-Peer Communication: Nodes establish direct connections with each other via sockets. They exchange data (blocks and blockchain) using ObjectInputStream and ObjectOutputStream.
- Concurrency Management: Connections and data exchange are handled by individual threads, ensuring smooth, real-time interactions between nodes.
- Fault Tolerance: Nodes handle unexpected disconnections or communication errors gracefully, removing stale connections and maintaining a robust network.
- Blockchain Sharing: Nodes can request the entire blockchain from a peer and synchronize their chain with the most up-to-date version.

## Architecture

The project is structured around three main classes that manage node connections and blockchain functionality:

- `BCNode`: The core class representing a blockchain node. Each node maintains its own chain, validates blocks, and manages peer connections.
- `ConnectionHandler`: This class listens for new node connections on a server socket. When a peer connects, the handler sets up communication streams and manages block requests. It runs in its own thread, allowing the node to accept multiple peers concurrently.
- `ReadHandler`: A dedicated thread for each peer connection. It listens for incoming blocks and processes them, ensuring the node updates its blockchain and propagates new blocks to its peers.

## Technology Stack

- Java: The project is fully implemented in Java, using core features such as:
  - Sockets for network communication.
  - Threads for handling concurrent connections and managing multiple node interactions.
  - Serialization (ObjectInputStream / ObjectOutputStream) for transmitting data between nodes.
  
- Blockchain Principles: The project adheres to core blockchain principles:
  - Decentralization: Each node independently maintains its chain.
  - Consensus: Nodes update their chain based on valid, shared blocks from peers.
  - Data Integrity: Blocks are validated before being added to the chain.

## How It Works

1. **Node Initialization**: Each node starts up with its own blockchain and listens for incoming peer connections.
2. **Peer Connection**: Nodes connect to each other using a simple client-server model via ServerSocket and Socket. 
3. **Block Propagation**: When a node receives a new block, it validates it, adds it to its chain, and forwards the block to its peers.
4. **Blockchain Synchronization**: Peers can request the full blockchain from each other, ensuring their chain is synchronized.

## Project Setup and Usage

### Requirements
- Java 8+ installed.

### Running the Project

1. Clone this repository:
git clone https://github.com/yourusername/blockchain-p2p-network.git cd blockchain-p2p-network

2. Compile the project:
javac blockchain/*.java

3. Start a node:
java blockchain.BCNode

4. To simulate multiple nodes, open additional terminal instances and run the above command for each node.

5. **Peer Connections**:
- Each node listens on a specified port and accepts incoming connections from other nodes.
- To connect nodes, you can manually set up their IP addresses and ports in the configuration.

6. **Interaction**:
- Nodes will automatically exchange blocks when new ones are added, ensuring consensus across the network.
- They also respond to requests for the entire blockchain if a peer requests synchronization.

## Project Highlights

### Why This Project Stands Out:

- **Real-Time Networked Communication**: This project is a testament to my ability to implement low-level network communication using Java sockets. The peer-to-peer architecture showcases my understanding of decentralized systems.

- **Concurrency and Multithreading**: Handling multiple node connections in parallel is achieved through multithreaded design, demonstrating my experience in concurrent programming.

- **Blockchain Expertise**: I’ve implemented a simple blockchain system, incorporating crucial concepts like block validation and propagation, which are key to ensuring data integrity in a distributed ledger.

- **Error Handling and Robustness**: The network is built to handle disconnections and errors gracefully, ensuring smooth recovery and continued operation—an essential trait for any real-world distributed system.

## Future Enhancements

Some potential improvements for further iterations:
- **Block Validation Rules**: Enhancing block validation with more complex cryptographic hashing and consensus algorithms.
- **Security**: Implementing SSL for encrypted communication between nodes.
- **Proof of Work**: Adding a simple consensus algorithm like Proof of Work to demonstrate mining functionality.

## Contact

If you're interested in learning more about this project or discussing blockchain, peer-to-peer networking, or distributed systems, feel free to reach out!

- **Email**: kragen.wild@du.edu
- **LinkedIn**: https://www.linkedin.com/in/kragen-wild-b66221252
- **GitHub**: https://github.com/kragen-w
