package Gnutella;

import java.io.*;
import java.net.*;

public class Gnutella {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//int destPortNumber = Integer.parseInt(args[0]);
		if(args.length  != 1){
			System.err.println("Command Format : java Gnutella <port no>");
			//this port no is the number which server is waiting for
			System.exit(1);
		}
		UI userInterface = new UI();
		userInterface.start();
		int serverPortNumber = Integer.parseInt(args[0]);
		MySingleton singleton = MySingleton.getInstance();
		singleton.registServerPortNum(serverPortNumber);
		
		Server serv = new Server(serverPortNumber);
		serv.start();
	}

}

