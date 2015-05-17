import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.*;
public class SendQueryThread extends SendThread{
	String searchWord;
	byte queryheader = (byte)0x80;
	byte[] minSpeed;
	public SendQueryThread(Socket s, String searchWord, byte[] id){
		super("QUERY", s, MySingleton.getNumHop(), (byte)0, id);
		this.searchWord = searchWord;
		this.minSpeed = putShort((short)1);
		this.relayFlag = false;
	}
	public SendQueryThread(Socket s, byte ttl, byte hop, byte[] id, ArrayList<byte[]> copy){
		super("QUERY", s, ttl, hop, id, copy);
		//copy indludes payloadlength, min speed, searchcriteria
		//byte[] plen = copy.get(0);
		this.minSpeed = copy.get(1);
		this.searchWord = new String(copy.get(2));
		this.relayFlag = true;
	}
	public void run(){
		try{
			System.out.println("**** QUERY Envoyer Fil Commence ****");
			OutputStream outstream = this.connectionSocket.getOutputStream();
			sendQueryMessage(outstream);
			System.out.println("**** QUERY Envoyer Fil Termine ****");
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	public void sendQueryMessage(OutputStream out){
		byte[] ID = this.messageID;
		byte[] payloadlen = new byte[4];
		//byte[] minSpeed = new byte[2];
		byte[] searchCriteria = this.searchWord.getBytes();
		int payloadLength = 2 + searchCriteria.length + 1;//add '+1'

		byte[] header = new byte[23];
		byte[] payload = new byte[payloadLength];// remove '+1'
		for(int i = 0; i < 16; i++){
			header[i] = this.messageID[i];//
		}

		header[16] = this.queryheader;
		header[17] = this.TTL;
		header[18] = this.HOP;
		
		payloadlen = putInt(payloadLength);
		for(int i = 0; i < 4; i++){
			header[i + 19] = payloadlen[i];
		}
		//minSpeed = putShort((short)1);

		payload[0] = this.minSpeed[0];
		payload[1] = this.minSpeed[1];
		for(int i = 0; i < searchCriteria.length; i++){
			payload[i + 2] = searchCriteria[i];
		}
		int payloadSize = payload.length;
		payload[payloadSize - 1] = (byte)0x00; //a null(0x00) terminates search criteria
		
		/*System.out.println("HEADER");
		for(int i = 0; i < header.length; i++){
			System.out.print(header[i] + " ");
		}
		System.out.println("\nPayload");
		for(int i = 0; i < payload.length; i++){
			System.out.print(payload[i] + " ");
		}
		System.out.println("");*/
	//	showMessageId(this.messageID, 2);
		try {
			out.write(header);
			out.write(payload);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	public int getInt(byte[] b){
		int[] in = new int[4];
		for(int i = 0; i < 4; i++){
			in [i] = b[i];
			if(in[i] < 0){
				in[i] &= (int)0x000000FF;
			}
		}
		in[0] = in[0] << 8 * 3;
		in[1] = in[1] << 8 * 2;
		in[2] = in[2] << 8;
		int sum = 0;
		for(int i = 0; i < 4; i++){
			sum += in[i];
		}
		return sum;
	}
	
	
}
