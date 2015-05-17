package Gnutella;

import java.io.*;
import java.net.*;
public class UI extends Thread{
	String command;
	
	//final byte NUM_HOP = 2;
	byte NUM_HOP;
	public UI(){
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
				if(this.command.toUpperCase().equals("PING")){
					if(argument.length != 1){
						System.out.println("Format de la Commande est Errone !!");
						System.out.println("le format est 'PING' ");
						continue;
					}
					DescriptorHeader ping = new DescriptorHeader(NUM_HOP, (byte)0);
					byte[] pingId = ping.messageID;
					
					InfoNouveau info = new InfoNouveau(pingId, NUM_HOP, (byte)0);
					MySingleton.addSentPing(info);
					
					
					int size = MySingleton.getSizeSocketList();
					for(int i = 0; i < size; i ++){
						Socket s = MySingleton.getSocket(i);
						SendThread sendPing = new SendThread("PING", s, this.NUM_HOP, (byte)0, pingId);
						sendPing.start();
					}
				}else if(this.command.toUpperCase().equals("QUERY")){
					if(argument.length != 2){
						System.out.println("Format de la Commande est Errone !!");
						System.out.println("le format est 'QUERY <Search Criteria>' ");
						continue;
					}
					String searchWord = argument[1];
					Query q = new Query(this.NUM_HOP, (byte)0);
					byte[] queryId = q.messageID;
					
					InfoNouveau info = new InfoNouveau(queryId, NUM_HOP, (byte)0, searchWord);
					MySingleton.addSentQuery(info);
					
					int size = MySingleton.getSizeSocketList();
					for(int i = 0; i < size; i ++){
						Socket s = MySingleton.getSocket(i);
						SendThread sendQuery = new SendQueryThread(s, searchWord, queryId);
						sendQuery.start();
					}
				}else if(this.command.toUpperCase().equals("CONNECT") || 
						this.command.toUpperCase().equals("CON")){//ex) connect localhost 8000
					
					if(argument.length != 3){
						System.out.println("Format de la Commande est Errone !!");
						System.out.println("le format est 'CONNECT <IP Address> <Port No.>' ");
						continue;
					}
					try{
						String ip = argument[1];					
						int port = Integer.parseInt(argument[2]);
						//System.out.println("Log: dest port :" + port);
						Socket destSocket = new Socket(ip, port);
						MySingleton.addSocket(destSocket);
						
						BufferedReader inputstream = new BufferedReader(new InputStreamReader(destSocket.getInputStream()));
						OutputStream outstream = destSocket.getOutputStream();

						outstream.write("GNUTELLA CONNECT/0.4\n\n".getBytes());
						outstream.flush();

					//	System.out.println("Log: Send Gnutella Connect Message");

						String line;
						while((line = inputstream.readLine()) != null){
							if(line.contains("GNUTELLA")){
								System.out.println("UI: " + line);
								RecevoirNouveau recevoir = new RecevoirNouveau(destSocket, 1);
								recevoir.start();
								System.out.println("UI: Recevoir Fil Commence ");
								//
								break;
							}
						}
						if(line == null) {
							System.out.println("Log: line is null");
						}
						

					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						System.out.println("UnknownHostException " + e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("IOException " + e);
						//e.printStackTrace();
					}

				}else if(inputCommand.toUpperCase().equals("")){
					continue;
				}else if(this.command.toUpperCase().equals("SHOW") || this.command.toUpperCase().equals("HIST")){
					//MySingleton.showSocket();
					if(argument.length != 1){
						System.out.println("Format de la Commande est Errone !!");
						System.out.println("le format set 'SHOW' or 'HIST' ");
						continue;
					}
					MySingleton.showInfo();
					
				}else if(inputCommand.toUpperCase().contains("INFO")){
					if(argument.length != 1){
						System.out.println("Format de la Commande est Errone !!");
						System.out.println("le format est 'INFO' ");
						continue;
					}
					MySingleton.showInfo();
					
				}else if(inputCommand.toUpperCase().contains("DOWNLOAD")){
					if(argument.length != 1){
						System.out.println("Format de la Commande est Errone !!");
						System.out.println("le format est 'DOWNLOAD' ");
						continue;
					}
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
					    	continue;
					    }
					    ResultSet rSet = MySingleton.getResultSet(index);
					    InfoNouveau info = MySingleton.getInfo(rSet);
					    if(info == null){
					    	System.out.println("Not Found info nouveau");
					    	continue;
					    }
					    String ip = info.makeIPAddressStr();				
					    int portNum = info.getPortNumber();
						System.out.println("** Port de Destination = " + portNum + " ***");
						Socket destSocket = new Socket(ip, portNum);

						if(MySingleton.checkSocket(destSocket) >= 0){
							System.out.println("J'ai le meme Socket");
							continue;
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
						continue;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("\nIOException: " + e + " in UI in while loop");
						//e.printStackTrace();
						continue;
					}
					
				}else if(inputCommand.toUpperCase().contains("SOC")){
					
					System.out.println("----- Existing Sockets Begin -----");
					MySingleton.showSocket();
					System.out.println("----- Existing Sockets End -----");
					continue;
					
				}else{
				
					System.out.println(inputCommand + " is wrong !");
					continue;
				}
				
			}catch(IOException e){
				System.out.println("\nIOException occurred in UI");
				System.exit(1);
			}

		}
		System.exit(0);
	}

	
}
