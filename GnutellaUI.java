import java.io.*;
import java.net.*;
public class GnutellaUI extends Thread{
	String command;
	
	//final byte NUM_HOP = 2;
	byte NUM_HOP;
	public GnutellaUI(){
		NUM_HOP = MySingleton.getNumHop();
	}
	public void run(){
		while(true){
			System.out.print("$Gnutella: ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			//		ServerSocket serverSocket = null;
			try{
				String inputCommand = br.readLine();
				String[] argument = inputCommand.split(" ");
				this.command = argument[0];
				if(this.command.toUpperCase().equals("QUIT")){
					break;
				}
				System.out.println("Command: " + this.command);
				if(inputCommand.toUpperCase().equals("")){
					continue;
				}
				this.distinguishInput(argument);
				
			}catch(IOException e){
				System.out.println("\nIOException occurred in UI");
				System.exit(1);
			}

		}
		System.exit(0);
	}
	
	public void distinguishInput(String[] arguments){
		if(this.command.toUpperCase().equals("PING")){
			if(arguments.length != 1){
				System.out.println("Format de la this.commande est Errone !!");
				System.out.println("le format est 'PING' ");
				return;
			}
			DescriptorHeader ping = new DescriptorHeader(NUM_HOP, (byte)0);
			byte[] pingId = ping.messageID;
			
			MessageInfo info = new MessageInfo(pingId, NUM_HOP, (byte)0);
			MySingleton.addSentPing(info);
			
			
			int size = MySingleton.getSizeSocketList();
			for(int i = 0; i < size; i ++){
				Socket s = MySingleton.getSocket(i);
				SendThread sendPing = new SendThread("PING", s, this.NUM_HOP, (byte)0, pingId);
				sendPing.start();
			}
			return;
		}else if(this.command.toUpperCase().equals("QUERY")){
			if(arguments.length != 2){
				System.out.println("Format de la this.commande est Errone !!");
				System.out.println("le format est 'QUERY <Search Criteria>' ");
				return;
			}
			String searchWord = arguments[1];
			Query q = new Query(this.NUM_HOP, (byte)0);
			byte[] queryId = q.messageID;
			
			MessageInfo info = new MessageInfo(queryId, NUM_HOP, (byte)0, searchWord);
			MySingleton.addSentQuery(info);
			
			int size = MySingleton.getSizeSocketList();
			for(int i = 0; i < size; i ++){
				Socket s = MySingleton.getSocket(i);
				SendThread sendQuery = new SendQueryThread(s, searchWord, queryId);
				sendQuery.start();
			}
			return;
		}else if(this.command.toUpperCase().equals("CONNECT") | this.command.equals("CON")){

			if(arguments.length != 3){
				System.out.println("Format de la this.commande est Errone !!");
				System.out.println("le format est 'CONNECT <IP Address> <Port No.>' ");
				return;
			}
			String ip = arguments[1];					
			int portNum = Integer.parseInt(arguments[2]);
			this.connectOtherSever(ip, portNum);
			return;

		}else if(this.command.toUpperCase().equals("SHOW") | this.command.toUpperCase().equals("HIST")
				| this.command.toUpperCase().equals("INFO")){
			if(arguments.length != 1){
				System.out.println("Format de la this.commande est Errone !!");
				System.out.println("le format set 'SHOW' or 'HIST' ");
				return;
			}
			MySingleton.showInfo();
			return;
		}else if (this.command.toUpperCase().contains("DOWNLOAD")){
			if(arguments.length != 1){
				System.out.println("Format de la this.commande est Errone !!");
				System.out.println("le format est 'DOWNLOAD' ");
				return;
			}
			this.downloadFile();
			
		}else if(this.command.toUpperCase().contains("SOC")){
			System.out.println("----- Existing Sockets Begin -----");
			MySingleton.showSocket();
			System.out.println("----- Existing Sockets End -----");
			return;
		}else{
			for(String s: arguments){
				System.out.print(s + " ");
			}
			System.out.println("is wrong !");
			return;
		}
	}
	public void connectOtherSever(String ip, int portNum){
		try{
			Socket destSocket = new Socket(ip, portNum);
			MySingleton.addSocket(destSocket);
			
			BufferedReader inputstream = new BufferedReader(new InputStreamReader(destSocket.getInputStream()));
			OutputStream outstream = destSocket.getOutputStream();

			outstream.write("GNUTELLA CONNECT/0.4\n\n".getBytes());
			outstream.flush();

			String line;
			while((line = inputstream.readLine()) != null){
				if(line.contains("GNUTELLA")){
					System.out.println("UI: " + line);
					RecvThread recevoir = new RecvThread(destSocket, 1);
					recevoir.start();
					System.out.println("UI: Recevoir Fil Commence ");
					//
					break;
				}
			}
			if(line == null) {
				System.out.println("Log: line is null");
			}
			
			return;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("UnknownHostException " + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOException " + e);
			//e.printStackTrace();
		}
	}
	public void downloadFile(){
		MySingleton.showResultSets();
		try{
			System.out.print("SELECT an Index of Result Sets: ");
			BufferedReader input =
		            new BufferedReader (new InputStreamReader (System.in));
		    String str = input.readLine( );
		    int index = Integer.valueOf(str);
		    int rSetSize = MySingleton.getResultSetSize();
		    if(rSetSize <= index){
		    	System.out.println("le Index est Errone !!");
		    	return;
		    }
		    ResultSet rSet = MySingleton.getResultSet(index);
		    MessageInfo info = MySingleton.getInfo(rSet);
		    if(info == null){
		    	System.out.println("Not Found info nouveau");
		    	return;
		    }
		    String ip = info.makeIPAddressStr();				
		    int portNum = info.getPortNumber();
			System.out.println("** Port de Destination = " + portNum + " ***");
			Socket destSocket = new Socket(ip, portNum);

			if(MySingleton.checkSocket(destSocket) >= 0){
				System.out.println("J'ai le meme Socket");
				return;
			}else{
				MySingleton.addSocket(destSocket);
				OutputStream outstream = destSocket.getOutputStream();
			//	BufferedReader inputstream = new BufferedReader(new InputStreamReader(destSocket.getInputStream()));

			    String message = "GET/get/" + rSet.get4Bytes(rSet.index) + "/" + rSet.getNameStr() + "/ HTTP/1.0\r\n" +
						"Coonection: Keep-Alive\r\n" +
						"Range:bytes=0-\r\n" + "\r\n";//need to consider range at the end
				System.out.println("message: " + message);
				outstream.write(message.getBytes());
				outstream.flush();

				System.out.println("**** Le HTTP  Message de connexion est envoye ****");
				RecvFile recv = new RecvFile(destSocket, rSet.getNameStr());
				recv.start();
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("\nUnknownHostException " + e + "in UI in while loop");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("\nIOException: " + e + " in UI in while loop");
			//e.printStackTrace();
			return;
		}
		
	}

	
}
