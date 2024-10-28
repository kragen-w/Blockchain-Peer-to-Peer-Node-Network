package blockchain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class BCNode {
	public ArrayList<Block> chain;  // The blockchain itself, an arraylist to hold all the blocks
	public int myPort;              // The port number this node will listen on
	public List<Integer> remotePorts;  // A list of ports that other nodes are listening on
	public ServerSocket serversocket;  // The server socket that allows this node to accept connections
	
	// Lists to manage communication with other nodes
	public List<Socket> nodeSockets = new ArrayList<>();  // Sockets for each node we're connected to
    public List<ObjectInputStream> nodeInputStreams = new ArrayList<>();  // Input streams for each connected node
    public List<ObjectOutputStream> nodeOutputStreams = new ArrayList<>();  // Output streams for each connected node

	// Constructor for creating a BCNode, which sets up networking and initializes the blockchain
	public BCNode(int myPort, List<Integer> remotePorts) throws NoSuchAlgorithmException, UnknownHostException, IOException, ClassNotFoundException {
		this.myPort = myPort;  // Assign the port number for this node
		this.remotePorts = remotePorts;  // Assign the ports of other nodes
		this.chain = null;  // Initialize the blockchain as null for now
		int numberOfConnections = 0;  // We'll use this to keep track of the number of other nodes we've connected to
		
		// If there are no remote ports (this node is the first), create a new blockchain with the genesis block
		if (remotePorts.size() == 0) {
			this.chain = new ArrayList<Block>();  // Create a new blockchain (an empty arraylist of blocks)
			this.chain.add(new Block());  // Add the first block (genesis block)
		} else {
			// If there are remote ports, we need to connect to those nodes
			for (int port : remotePorts) {
				System.out.println("Port being added is " + port);  // Just print the port we're connecting to for debugging
				Socket socket = new Socket("localhost", port);  // Create a socket to connect to the node at the specified port
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());  // Set up an output stream to send data
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());  // Set up an input stream to receive data
				
				// Add the socket, input stream, and output stream to our lists
				this.nodeSockets.add(socket);
				this.nodeInputStreams.add(in);
				this.nodeOutputStreams.add(out);

				// If we don't have a blockchain yet, request it from the first node we connect to
				if (this.chain == null) {
					out.writeObject("Chain Requested");  // Ask the remote node for its blockchain
					out.reset();  // Reset the stream to ensure the message is sent
					this.chain = (ArrayList<Block>) in.readObject();  // Read the blockchain from the remote node
				} else {
					out.writeObject("No Chain Request");  // If we already have a blockchain, no need to request it
					out.reset();
				}

				// Start a new thread to handle incoming messages from this connection
				ReadHandler ch = new ReadHandler(this, in, out, socket, numberOfConnections);
				Thread t = new Thread(ch);
				t.start();  // Start the thread
				numberOfConnections++;  // Keep track of how many nodes we're connected to
            }			
		}
		
		// Start a server socket to accept incoming connections from other nodes
		try {
			ServerSocket ss = new ServerSocket(myPort);  // Create a server socket that listens on our port
			// Start a new thread to handle incoming connections
			Thread th = new Thread(new ConnectionHandler(this, ss, this.nodeSockets, this.nodeInputStreams, this.nodeOutputStreams, numberOfConnections));
			th.start();
		} catch (IOException e) {
			e.printStackTrace();  // If something goes wrong with the server socket, print an error
		}
	}
	
	// Method to send a block to all connected nodes
	public synchronized void sendBlock(Block b) {
		for (ObjectOutputStream out : this.nodeOutputStreams) {  // For each node we're connected to...
			try {
				out.writeObject(b);  // Send the block
				out.reset();  // Reset the stream so the block gets sent properly
			} catch (IOException e) {
				e.printStackTrace();  // If something goes wrong with sending the block, print an error
			}
		}
		System.out.println("Block sent to all nodes");  // Print a message saying the block was sent
	}
	
	// Method to add a block that already exists in the chain (maybe received from another node)
	public synchronized boolean addExistingBlock(Block b) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String prevhash = this.chain.get(this.chain.size()-1).hash;  // Get the hash of the last block in the chain
		this.chain.add(b);  // Add the new block to the chain
        boolean valid = true;        
        valid = valid && this.isValid(5);  // Check if the chain is still valid with this new block (5 is the difficulty level)
		
		// If the chain becomes invalid after adding this block, remove it
		if (!valid) {
			this.chain.remove(b);
		}
		// If the hash of the previous block matches the last block in the chain, remove the block (duplicate?)
		if (prevhash.equals(this.chain.get(this.chain.size()-1).hash)) {
			this.chain.remove(b);
			valid = false;
		}
		return valid;  // Return whether the block was successfully added
    }
	
	// Method to mine and add a new block to the chain
	public void addBlock(Block b) throws NoSuchAlgorithmException, IOException {
		// Set the previous hash of this block to the hash of the last block in the chain
		b.previousHash = chain.get(chain.size() - 1).hash;
		// Calculate the hash of this block
		b.hash = b.calculateHash();
		int N = 5;  // Set the difficulty level (how many leading zeros the hash should have)
		String prefixZeros = new String(new char[N]).replace('\0', '0');  // Create a string of N zeros
		
		// Keep changing the nonce and recalculating the hash until it has N leading zeros
		while (!b.hash.substring(0, N).equals(prefixZeros)) {
			b.nonce++;  // Increment the nonce
			b.hash = b.calculateHash();  // Recalculate the hash with the new nonce
		}
		
		// Add the block to the chain
		chain.add(b);
		
		// Check if the chain is still valid after adding this block
		if (this.isValid(N)) {
			this.sendBlock(b);  // If valid, send the block to all other nodes
		} else {
			chain.remove(b);  // If not valid, remove the block from the chain
		}
	}
	
	// Method to check if the blockchain is valid (i.e., all hashes are correct and the chain is linked properly)
	public boolean isValid(int N) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		for (int i = 1; i < chain.size(); i++) {
			
			Block currentBlock = chain.get(i);  // Get the current block
			Block previousBlock = chain.get(i - 1);  // Get the previous block
			
			String prefixZeros = new String(new char[N]).replace('\0', '0');  // Create a string of N zeros
			
			// Check if the current block's hash has the correct number of leading zeros
			if (!currentBlock.hash.substring(0, N).equals(prefixZeros)) {
				return false;  // If not, the chain is invalid
			}
			
			// Check if the current block's hash matches its calculated hash
			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				return false;  // If not, the chain is invalid
			}
			
			// Check if the current block's previous hash matches the hash of the previous block
			if (!currentBlock.previousHash.equals(previousBlock.hash)) {
				return false;  // If not, the chain is invalid
			}
		}
		return true;  // If everything checks out, the chain is valid
	}
	
	// Method to display the entire blockchain as a string
	public String toString() {
		String result = "";
		for (int i = 0; i < chain.size(); i++) {
			result += "Block " + i + ":  " + chain.get(i).toString() + "\n";  // Print each block
		}
		return result;
	}
	
	// Method to remove a node from the network (disconnect and close all streams)
	public synchronized void removeNode(Socket s, ObjectInputStream in, ObjectOutputStream out, int connectionNum) {
		if (this.nodeInputStreams.contains(in)) {
			this.nodeInputStreams.remove(in);  // Remove the input stream if it exists
		} else {
			System.out.println("Input not found");  // If not found, print an error
		}
		if (this.nodeOutputStreams.contains(out)) {
			this.nodeOutputStreams.remove(out);  // Remove the output stream if it exists
		} else {
			System.out.println("Output not found");  // If not found, print an error
		}
		if (this.nodeSockets.contains(s)) {
			this.nodeSockets.remove(s);  // Remove the socket if it exists
		} else {
			System.out.println("Socket not found");  // If not found, print an error
		}
		System.out.println("Node removed!");  // Print a message confirming the node was removed
	}


	
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnknownHostException, IOException, ClassNotFoundException {
        Scanner keyScan = new Scanner(System.in);
        
        // Grab my port number on which to start this node
        System.out.print("Enter port to start (on current IP): ");
        int myPort = keyScan.nextInt();
        
        // Need to get what other Nodes to connect to
        System.out.print("Enter remote ports (current IP is assumed): ");
        keyScan.nextLine(); // skip the NL at the end of the previous scan int
        String line = keyScan.nextLine();
        List<Integer> remotePorts = new ArrayList<Integer>();
        if (line != "") {
            String[] splitLine = line.split(" ");
            for (int i=0; i<splitLine.length; i++) {
                remotePorts.add(Integer.parseInt(splitLine[i]));
            }
        }
        // Create the Node
        BCNode n = new BCNode(myPort, remotePorts);
        
        String ip = "";
        try {
             ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Node started on " + ip + ": " + myPort);
        
        // Node command line interface
        while(true) {
            System.out.println("\nNODE on port: " + myPort);
            System.out.println("1. Display Node's blockchain");
            System.out.println("2. Create/mine new Block");
            System.out.println("3. Kill Node");
            System.out.print("Enter option: ");
            int in = keyScan.nextInt();
            
            if (in == 1) {
                System.out.println(n);
                
            } else if (in == 2) {
                // Grab the information to put in the block
                System.out.print("Enter information for new Block: ");
                String blockInfo = keyScan.next();
                Block b = new Block(blockInfo);
                n.addBlock(b);
                
            } else if (in == 3) {
                // Take down the whole virtual machine (and all the threads)
                //   for this Node.  If we just let main end, it would leave
                //   up the Threads the node created.
                keyScan.close();
                System.out.println("Node killed");
                System.exit(0);
            }
        }
        
    }
}


