package blockchain;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

public class ReadHandler implements Runnable {
    public BCNode node;                    // Reference to the node running this ReadHandler
    public ObjectInputStream in;           // Input stream to read data from the socket
    public ObjectOutputStream out;         // Output stream to send data to the socket
    public int connectionNumber;           // Keeps track of the connection number (unique for each connection)
    public Socket socket;                  // The socket connection with the other node

    // Constructor to initialize the ReadHandler with the node, input/output streams, socket, and connection number
    public ReadHandler(BCNode node, ObjectInputStream in, ObjectOutputStream out, Socket s, int numberOfConnections) {
        this.node = node;
        this.in = in;
        this.out = out;
        this.connectionNumber = numberOfConnections;
        this.socket = s;
    }

    @Override
    public void run() {
        // The run() method contains the main logic that gets executed when the thread is started
        while (true) {
            try {
                // Attempt to read an object from the input stream
                Object obj = this.in.readObject();

                // If the object read is a Block, handle it
                if (obj instanceof Block) {
                    Block b = (Block) obj;

                    // Add the block to the node's blockchain if it's valid
                    if (this.node.addExistingBlock(b)) {
                        // Broadcast the block to other connected nodes if successfully added
                        this.node.sendBlock(b);
                    }
                }

            } catch (SocketException | EOFException e) {
                // If there's a socket error (like the connection closing), remove the node
                this.node.removeNode(this.socket, this.in, this.out, this.connectionNumber);
                break;  // Exit the loop and end the thread

            } catch (ClassNotFoundException | IOException e) {
                // Handle any issues with reading the object
                e.printStackTrace();

            } catch (NoSuchAlgorithmException e) {
                // Handle issues related to cryptographic algorithms
                e.printStackTrace();
            }
        }
    }
}
