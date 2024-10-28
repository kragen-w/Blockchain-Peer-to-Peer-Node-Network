package blockchain;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.Serializable;

public class Block implements Serializable {
	public String data;           // This is the data stored inside the block (could be anything like a transaction, message, etc.)
	public long timestamp;        // A timestamp of when the block was created
	public int nonce;             // The nonce is a number that gets changed during mining to find a valid hash
	public String previousHash;   // The hash of the previous block in the blockchain (links blocks together)
	public String hash;           // The hash of this block (calculated using its data, timestamp, nonce, and previous hash)
	private static final long serialVersionUID = 1L;  // A unique ID to ensure this class is compatible with serialization
	
	// Constructor to create a new block with given data
	public Block(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		this.data = data;  // Store the block's data
		this.timestamp = new Date().getTime();  // Get the current time in milliseconds and store it as the block's timestamp
		this.nonce = 0;  // Initialize the nonce to 0
		this.hash = this.calculateHash();  // Calculate the hash for this block using the data, timestamp, nonce, and previous hash
	}
	
	// Special constructor for the "Genesis Block" (the first block in the chain)
	public Block() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.data = "Genesis Block";  // Set the data to a default string ("Genesis Block")
        this.hash = this.calculateHash();  // Calculate the hash for this genesis block
        this.previousHash = "0";  // Since it's the first block, there is no previous block, so we set previousHash to "0"
        this.timestamp = new Date().getTime();  // Get the current timestamp
        this.nonce = 0;  // Initialize the nonce to 0
    }
	
	// Method to calculate the hash of this block
	// The hash is a unique string created by combining the block's data, timestamp, nonce, and previous hash
	public String calculateHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		// Combine the block's fields into one long string
		String combined = data + Long.toString(timestamp) + Integer.toString(nonce) + previousHash;
		
		// Use the SHA-256 hashing algorithm to generate a hash for this string
		MessageDigest myDigest = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = myDigest.digest(combined.getBytes("UTF-8"));  // Convert the combined string to a byte array and hash it
		
		// Convert the byte array into a readable hexadecimal string
		StringBuffer buffer = new StringBuffer();
		for (byte b : hashBytes) {
			buffer.append(String.format("%02x", b));  // For each byte, format it as a 2-digit hexadecimal number
		}
		
		// Return the final hash string
		String hashStr = buffer.toString();
		return hashStr;
	}
	
	// Getter method to return the hash of this block
	public String getHash() {
		return hash;
	}	
	
	// Getter method to return the hash of the previous block (linking the blockchain)
	public String getPreviousHash() {
		return previousHash;
	}
	
	// Getter method to return the current value of the nonce
	public int getNonce() {
		return nonce;
	}
	
	// A simple method to return a string representation of this block's details
	// This is useful for debugging or printing the blockchain
	public String toString() {
		return "Timestamp: " + timestamp + "\n" + 
		       "Nonce: " + nonce + "\n" + 
		       "Data: " + data + "\n" + 
		       "Hash: " + hash + "\n" + 
		       "Previous Hash: " + previousHash + '\n';
	}

}
