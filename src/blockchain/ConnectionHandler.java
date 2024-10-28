package blockchain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler implements Runnable {
    public BCNode node;                         // Reference to the node using this connection handler
    public ServerSocket ss;                     // Server socket for listening to incoming connections
    
    private List<Socket> nodeSockets;           // List of sockets for each connected node
    private List<ObjectInputStream> nodeInputStreams;  // List of input streams for each connected node
    private List<ObjectOutputStream> nodeOutputStreams; // List of output streams for each connected node
    private int numberOfConnections;            // Keeps track of the number of active connections

    // Constructor to initialize the ConnectionHandler with node, server socket, and list of sockets and streams
    public ConnectionHandler(BCNode node, ServerSocket ss, List<Socket> nodeSockets, List<ObjectInputStream> nodeInputStreams, List<ObjectOutputStream> nodeOutputStreams, int numberOfConnections) {
        this.node = node;
        this.ss = ss;
        this.nodeSockets = nodeSockets;
        this.nodeInputStreams = nodeInputStreams;
        this.nodeOutputStreams = nodeOutputStreams;
        this.numberOfConnections = numberOfConnections;
    }

    @Override
    public void run() {
        // This method will run in a loop to continuously accept new connections
        while (true) {
            try {
                // Accept a new connection from a peer node
                Socket s = ss.accept();
                
                // Create input and output streams to handle communication with the connected node
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                
                // Add the socket and its streams to the respective lists for tracking all connections
                this.nodeSockets.add(s);
                this.nodeInputStreams.add(in);
                this.nodeOutputStreams.add(out);
                
                // Read a string input from the connected node to determine what action to take
                String input = (String) in.readObject();
                
                // If the node requests the blockchain, send the chain to it
                if (input.equals("Chain Requested")) {
                    out.writeObject(this.node.chain);
                    out.reset();  // Reset the output stream to ensure the object is written fresh
                }

                // Create a new ReadHandler to handle incoming data from this node
                ReadHandler rh = new ReadHandler(this.node, in, out, s, this.numberOfConnections);
                
                // Start a new thread for handling the incoming data from this connection
                Thread t = new Thread(rh);
                t.start();
                
                // Increment the number of connections
                this.numberOfConnections++;
                
            } catch (IOException e) {
                // Catch and handle IO-related exceptions
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // Catch and handle issues with reading an object
                e.printStackTrace();
            }
        }
    }
}
