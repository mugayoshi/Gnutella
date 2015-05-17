package Gnutella;

import java.io.*;
import java.net.*;
import java.util.*;

public class SendQueryHitThread extends SendThread{
	String searchWord;
	byte queryhitheader = (byte)0x81;

	byte[] speed;
	byte numHits;
	public SendQueryHitThread(Socket s, String word, byte[] id){
		super("QUERY HIT", s, MySingleton.getNumHop(), (byte)0, id);
		this.searchWord = word;
		this.speed = putInt(100);
		this.relayFlag = false;
	}
	public SendQueryHitThread(Socket s, byte ttl, byte hop, byte[] id, ArrayList<byte[]> copy, byte numHits){
		super("QUERY HIT", s, ttl, hop, id, copy);
		//copy includes payload length, num hits, ip addr, speed, result sets, servent identifier
		this.searchWord = new String(copy.get(2));
		this.speed = copy.get(3);
		this.relayFlag = true;
		this.numHits = numHits;
	}
	public void run(){
		try{
			System.out.println("**** QUERY HIT Envoyer Fil Commence  *****");
			OutputStream outstream = this.connectionSocket.getOutputStream();
			sendQueryHitMessage(outstream);

			outstream.flush();
			System.out.println("**** QUERY HIT Envoyer Fil Termine *****");
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	public void sendQueryHitMessage(OutputStream out){
		//byte[] ID = this.messageID;
		byte[] payloadlen = new byte[4];
	
		byte[] ipaddr = new byte[4];
		byte[] servPort = new byte[2];
		
		//byte[] message;
		int numHit;
		byte[] resultSetField, serventIdentifier;
		
		byte[] header = new byte[23];
		byte[] payload;
		
		if(this.relayFlag){
			payloadlen = Arrays.copyOf(this.copy.get(0), this.copy.get(0).length);
			numHit = this.numHits;
			servPort = Arrays.copyOf(this.copy.get(1), this.copy.get(1).length);
			ipaddr = Arrays.copyOf(this.copy.get(2), this.copy.get(2).length);

			int plenInt = getInt(payloadlen);
			payload = new byte[plenInt];
			resultSetField = Arrays.copyOf(this.copy.get(4), this.copy.get(4).length);
			serventIdentifier = Arrays.copyOf(this.copy.get(5), this.copy.get(5).length);
			
		}else{
			QueryHit qhit =  new QueryHit(this.TTL, this.HOP);
			
			int serverPortNum = MySingleton.getServerPortNum();
			//short portShort = (short)serverPortNum;
			try {
				ipaddr = qhit.returnIPAddressBinaryStr(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			servPort = putShort((short)serverPortNum);
			FileFunction f = new FileFunction();
			Integer p = new Integer(serverPortNum);
		//	System.out.println("search word: "  + this.searchWord);
			numHit = f.getNumhit(this.searchWord,  p.toString());
			
			ArrayList<ResultSet> rSets = f.getResultSet(this.searchWord, p.toString());
			if(rSets.size() == 0){

		//		byte[] zero = {(byte)0x00};
				System.out.println("------ NOT FOUND ------");
				return ;
			}else{
				int rSetFieldSize = 0;
				ArrayList<byte[]> listForMerge = new ArrayList<byte[]>();
				for(int i = 0; i < rSets.size(); i++){
					ResultSet rset = rSets.get(i);
					rSetFieldSize += rset.getOwnSize();
					listForMerge.add(rset.mergeMyself());
					
				}
				resultSetField = new byte[rSetFieldSize];
				int j = 0;
				for(int i = 0; i < listForMerge.size(); i++){
					byte[] rsetFieldEach = listForMerge.get(i);
					for(int k = 0; k < rsetFieldEach.length; k++){
						resultSetField[j + k] = rsetFieldEach[k];
					}
					j += listForMerge.get(i).length;
					
				}
			}
			int payloadLength = 11 + resultSetField.length + 16;
			//num hits(1byte) + port(2bytes) + ip address(4bytes) + speed(4bytes) = 11 bytes
			
			payloadlen = putInt(payloadLength);
			serventIdentifier = makeServentIdentifier(resultSetField.length, this.messageID);
		//	int totalSize = 23 + payloadLength;
			//message = new byte[totalSize];
			payload = new byte[payloadLength];
		}
		
		for(int i = 0; i < 16; i++){
			header[i] = this.messageID[i];
		}
		header[16] = this.queryhitheader;
		header[17] = this.TTL;
		header[18] = this.HOP;
		for(int i = 0; i < 4; i++){
			header[19 + i] = payloadlen[i];
		}
		payload[0] = (byte)numHit;
		payload[1] = servPort[0];
		payload[2] = servPort[1];
		for(int i = 0; i < 4; i++){
			payload[3 + i] = ipaddr[i];
			payload[7 + i] = this.speed[i];
		}
		
		//showMessageId(header);
		
		
		//System.out.println("Result Set Field");
		for(int i = 0; i < resultSetField.length; i++){
			payload[11 + i] = resultSetField[i];
			//System.out.print(resultSetField[i] + " ");
		}
		//System.out.println("");
	//	System.out.println("\nServent Identifier");
		for(int i = 0; i < 16; i++){
			payload[11 + resultSetField.length + i] = serventIdentifier[i];
			//System.out.print(payload[11 + resultSetField.length + i] + " ");
		}
		//System.out.println("");
		
		//return message;
		
		try {
			out.write(header);
			out.write(payload);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	synchronized public void showMessageId(byte[] id){
		System.out.println("----SHOW Message ID in Send Query Hit Thread----");
		for(int i = 0; i < 16; i++){
			System.out.print(id[i] + " ");
		}
		System.out.println(" ");
	}
	public byte[] makeServentIdentifier(int resultSetFieldLen, byte[] id){//toriaezu
		byte[] sv = new byte[16];
		//servent identifier is made by server port number, result set length and low bits of UUID
		int serverPort = MySingleton.getServerPortNum();
		byte[] portNum = new byte[4];
		portNum = putInt(serverPort);
		byte[] rsetFieldLen= putInt(resultSetFieldLen);
		
		for(int i = 0; i < 16; i++){
			if(i < 4){
				sv[i] = portNum[i];
			}else if(i >= 4 && i < 8){
				sv[i] = rsetFieldLen[i - 4];
			}else{//i >= 9 && i < 16
				sv[i] = id[i];
			}
		}
		/*for(int i = 0; i < 16; i++){
			System.out.print(sv[i] + " ");
		}
		System.out.println("");*/
		//return sv;
		Random r = new Random();
		for(int i = 0; i < 16; i++){
			if((sv[i] >= 65 && sv[i] <= 90) || (sv[i] >= 97 && sv[i] <= 122)){
				continue;
			}else{
				sv[i] = (byte)r.nextInt(65);
				sv[i] += 65;
				if(sv[i] > 90 && sv[i] < 97 || sv[i] > 122){
					i--;
					continue;
				}else if(sv[i] > 127 || sv[i] < 0){
					i--;
					continue;
				}
			}
		}
		return sv;
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
