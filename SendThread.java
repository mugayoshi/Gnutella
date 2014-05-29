import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;


public class SendThread extends Thread{
	String comm;
	Socket connectionSocket;

//	final byte MySingleton.getNumHop() = 2;
	ArrayList<byte[]> copy;
	byte TTL, HOP;
	byte pingheader = (byte)0x00;
	byte pongheader = (byte)0x01;
	
	byte[] messageID;

	boolean relayFlag;
	public SendThread(String comm, Socket socket){
		this.comm = comm;
		this.connectionSocket = socket;
		this.TTL = (byte)MySingleton.getNumHop();
		this.HOP = 0;
		this.relayFlag = false;
		messageID = new byte[16];

	}
	public SendThread(String comm, Socket socket, byte[] id){
		this.comm = comm;
		this.connectionSocket = socket;
		this.TTL = (byte)MySingleton.getNumHop();
		this.HOP = (byte)0;
		this.relayFlag = false;
		this.messageID = new byte[16];
		this.messageID = id;

	}

	public SendThread(String comm, Socket socket, byte ttl, byte hop, byte[] id){
		this.comm = comm;
		this.connectionSocket = socket;
		this.TTL = (byte)ttl;
		this.HOP = (byte)hop;
		this.messageID = Arrays.copyOf(id, 16);
		this.relayFlag = true;//maybe in this case this flag might be false !!
//		this.relayFlag = false;

	}
	public SendThread(String comm, Socket socket, byte ttl, byte hop, byte[] id, ArrayList<byte[]> input){
		this.comm = comm;
		this.connectionSocket = socket;
		this.TTL = (byte)ttl;
		this.HOP = (byte)hop;
		this.messageID = id;
		this.relayFlag = true;
		this.copy = new ArrayList<byte[]>();//
		for(int i = 0; i < input.size(); i++){
			byte[] c = input.get(i);
			byte[] b = new byte[c.length];
			for(int j = 0; j < c.length;j ++){
				b[j] = c[j];
			}
			this.copy.add(b);
		}
		
	}
	
 	public void run(){
		//make packets

		try{
			System.out.println("**** Send Thread Starts ****");
			OutputStream outstream = this.connectionSocket.getOutputStream();
			if(this.comm.equals("OK")){
				outstream.write("GNUTELLA OK\n\n".getBytes());
				outstream.flush();
				System.out.println("**** Gnutella OK is Sent ****");
			}else if(this.comm.equals("PING")){
				sendPing(this.TTL, this.HOP, outstream);
				outstream.flush();
				System.out.println("***** PING is Sent *****");
			}else if(this.comm.equals("PONG")){
				sendPong(this.TTL, this.HOP, outstream);
				outstream.flush();
				//System.out.println("Pong is sent");
				System.out.println("***** PONG is Sent *****");

			}else{
				String message = this.comm + "\n";
				outstream.write(message.getBytes());//‚Æ‚è‚ ‚¦‚¸
				outstream.flush();
				System.out.println("log: "+ this.comm + " is sent.\nSend thread is done\n");
			}
			//
			/*recvThread recv = new recvThread(this.connectionSocket);
			recv.start();*/
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	synchronized public void sendPing(byte ttl, byte hops, OutputStream out){
		try{
			DescriptorHeader ping = new DescriptorHeader(ttl, hops);
			byte[] ID = ping.messageID;
			
			if(this.relayFlag){
				ID = this.messageID;
				//MySingleton.addRelayIDbytes(ID);
				//MySingleton.addRelayMessage("PING");
			}else{
				//MySingleton.addSentIDbytes(ID);
				//MySingleton.addSentMessage("PING");
			}
			int messageLength  = ID.length + 1 + 1 + 1 + 4;//ID + TTL + HOP + pay load length (4bytes)
			byte[] message = new byte[messageLength];
			for(int i = 0; i < 16; i++){
				message[i] = ID[i];
			}
			message[16] = this.pingheader;
			message[17] = this.TTL;
			message[18] = this.HOP;
			for(int i = 0; i < 4; i++){
				message[19 + i] = (byte)0;
			}
			
			//out.write(message.getBytes());
			out.write(message);
			out.flush();
			showMessageId(this.messageID, 0);
		}catch(IOException e){
			System.out.println(e);
		}
	}
	synchronized public void sendPong(byte ttl, byte hops, OutputStream out){
		try{
			Pong pong = new Pong(ttl, hops);
			//byte[] ID = this.messageID;
			byte[] ipaddr = new byte[4];
			byte[] payloadlen = new byte[4];
			byte[] servPort = new byte[2];
			
			byte[] numFileShared = new byte[4];
			byte[] sizeFileShared = new byte[4];
	
			int serverPortNum = MySingleton.getServerPortNum();
			short portShort = (short)serverPortNum;
			/*MySingleton.addSentIDbytes(this.messageID);
			MySingleton.addSentMessage("PONG");*/
			if(this.relayFlag){
				/*MySingleton.addRelayIDbytes(ID);
				MySingleton.addRelayMessage("PONG");*/
				payloadlen = this.copy.get(0);//
				servPort = this.copy.get(1);
				ipaddr = this.copy.get(2);
				numFileShared = this.copy.get(3);
				sizeFileShared= this.copy.get(4);
			}else{
				this.connectionSocket.getInetAddress();
				/*MySingleton.addSentIDbytes(this.messageID);
				MySingleton.addSentMessage("PONG");*/
				ipaddr = pong.returnIPAddressBinaryStr(InetAddress.getLocalHost().getHostAddress());
				FileFunction f = new FileFunction();
				servPort = putShort(portShort);
				
				Integer p = new Integer(serverPortNum);
				int numFiles = f.getNumberOfFiles(p.toString());
				numFileShared = putInt(numFiles);
				
				int sizeShared = f.getSizeOfFileShared(p.toString());
				sizeFileShared = putInt(sizeShared);
				int payLoadLength = 2 + ipaddr.length + numFileShared.length + sizeFileShared.length;
				payloadlen = putInt(payLoadLength);
			}

		/*	//port, ip address, num of files , file size*/
			byte[] message1 = new byte[23];
			byte[] message2 = new byte[14];

			for(int i = 0; i < 16; i++){
				message1[i] = this.messageID[i];
			}
			message1[16] = this.pongheader;
			message1[17] = this.TTL;
			message1[18] = this.HOP;
			for(int i = 0; i < 4; i++){
				message1[19 + i] = payloadlen[i];
			}
			for(int i = 0; i < 2; i++){
				message2[i] = servPort[i];
			}
			for(int i = 0; i < 4; i++){
				message2[i + 2] = ipaddr[i];
				message2[i + 6] = numFileShared[i];
				message2[i + 10] = sizeFileShared[i];

			}
			out.write(message1);
			out.write(message2);
			out.flush();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	public byte[] putInt(int n){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(n);
		return bb.array();
		
	}
	public byte[] putShort(short n){
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort(n);
		return bb.array();
		
	}
	synchronized public void showMessageId(byte[] id, int type){
		if(type == 0){
			System.out.println("SEND PING");
		}else if(type == 1){
			System.out.println("SEND PONG");
		}else if(type == 2){
			System.out.println("SEND QUERY");
		}
		for(int i = 0; i < 16; i++){
			System.out.print(id[i] + " ");
		}
		System.out.println(" ");
	}
	public void showCopy(ArrayList<byte[]> copy){
		System.out.println("show copy");

		for(int i = 0; i < copy.size(); i++){
			byte[] b = copy.get(i);
			System.out.print("i = " + i + " ");
			for(int j = 0; j < b.length; j++){
				System.out.print(b[j] + " ");
			}
			System.out.println(" ");
		}
	}
	public void showPortNumber(byte[] p){
		int f = p[0];
		if(f > 0){
			f &= (int)0x000000FF;
		}
		f = f << 8;
		int s = p[1];
		if(s > 0){
			s &= (int)0x000000FF;
		}
		int sum = f + s;
		System.out.println("Port Number: " + sum);
	}
}

