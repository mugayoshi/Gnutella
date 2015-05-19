public class Gnutella {

	public static void main(String[] args) {
		//int destPortNumber = Integer.parseInt(args[0]);
		if(args.length  != 1){
			System.err.println("Command Format : java Gnutella <port no>");
			//this port num is the number which server is waiting for
			System.exit(1);
		}
		
		GnutellaUI userInterfaceThread = new GnutellaUI();
		userInterfaceThread.start();
		
		int serverPortNumber = Integer.parseInt(args[0]);
		MySingleton.registerPortNum(serverPortNumber);
		Server serv = new Server(serverPortNumber);
		serv.start();
	}

}

