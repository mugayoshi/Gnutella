package Gnutella;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Server extends Thread{
	ServerSocket serverSocket;
	Socket clientSocket;
	String comm;
	int port;
	public Server(int port){
		/*
		server is just waiting for other peers, this means server thread  only 'accept' and give socket to connection threads 
		*/
		this.port = port;
	}
	public void run(){
		//long id = getId();
		// System.out.print("\n" + id + " Server start port number svp: ");
		try {
			InetAddress ip = InetAddress.getLocalHost();
			//serverSocket = new ServerSocket(this.port);
			this.serverSocket = new ServerSocket();
			this.serverSocket.bind(new InetSocketAddress(ip, this.port));
			
			System.out.println("Server: ip: " + this.serverSocket.getInetAddress().getHostAddress()
					+ " port " + this.serverSocket.getLocalPort());
			
			while(true){
				this.clientSocket = serverSocket.accept();//wait until client ask for server

				System.out.println("Server accepts the host\nIP: "
						+ clientSocket.getInetAddress().getHostAddress() + " Port No. " + this.clientSocket.getPort());

				MySingleton.addSocket(this.clientSocket);
				
				BufferedReader br = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
				String in = new String();
				while((in = br.readLine()) != null){
					if(in.toUpperCase().contains("GNUTELLA")){

						RecevoirNouveau recv = new RecevoirNouveau(this.clientSocket, 0);
						recv.start();
						System.out.println("Server Thread: Recv Thread Starts");
						break;
					}else if(in.toUpperCase().contains("HTTP")){
						System.out.println("Server Thread: Recv File Starts");
						System.out.println(in);
						String[] req = analyzeRequest(in);
						EnvoyerFichier sendFile = new EnvoyerFichier(this.clientSocket, req);
						sendFile.start();
						break;
					}
				}
				continue;
				
				
			}//end of while(true)
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

	}
	public String[] analyzeRequest(String req){
		String[] reqPart = req.split("/");
		String[] info = new String[2];
		info[0] = reqPart[2];//file index
		info[1] = reqPart[3];//file name
		return info;
	}
}
