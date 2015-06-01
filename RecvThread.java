import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;

public class RecvThread extends Thread{
	Socket connectionSocket;
	String searchWord;
	ArrayList<byte[]> copyPong, copyQuery, copyQueryHit;
	String[] requests;
	boolean connectFlag,pingFlag, pongFlag, duplicateFlag, queryFlag, queryHitFlag;
	boolean httpFlag;
	
	
	byte[] messageID;
	byte TTL, HOP;
	byte[] payLoadLength;
	String commandType;

	byte[] port, ipaddr, numFiles, sizeFiles;
	byte[] minSpeed, searchCriteria;
	
	byte numHits;
	byte[] speed;
	
	byte pingHeader = (byte)0x00;
	byte pongHeader = (byte)0x01;
	byte queryHeader = (byte)0x80;
	byte queryHitHeader = (byte)0x81;
	
	boolean relayPongFlag, relayQueryHitFlag;
	byte[] zeroID;
	String httpRequestStr;
	boolean httpRequestFlag;
	int type;
	
	public RecvThread(Socket socket){
		this.connectionSocket = socket;
		//set flag 1 -> flag up
		int index = MySingleton.getSocketIndex(this.connectionSocket);
		MySingleton.flagUp(index);

		this.copyPong = new ArrayList<byte[]>();
		this.copyQuery = new ArrayList<byte[]>();
		this.copyQueryHit = new ArrayList<byte[]>();
		
		requests = new String[2];
		clearFlags();
	}
	
	public RecvThread(Socket socket, int type){
		//super(s);
		this.connectionSocket = socket;
		//set flag 1 -> flag up
		int index = MySingleton.getSocketIndex(this.connectionSocket);
		MySingleton.flagUp(index);

		this.copyPong = new ArrayList<byte[]>();
		this.copyQuery = new ArrayList<byte[]>();
		this.copyQueryHit = new ArrayList<byte[]>();
		
		requests = new String[2];
		clearFlags();
		
		this.messageID = new byte[16];
		this.payLoadLength = new byte[4];
		this.commandType = new String();
		this.port = new byte[2];
		this.ipaddr = new byte[4];
		this.numFiles = new byte[4];
		this.sizeFiles = new byte[4];
		
		this.minSpeed = new byte[2];
		this.speed = new byte[4];
		this.relayPongFlag = false;
		this.relayQueryHitFlag = false;
		
		zeroID = new byte[16];
		for(int i = 0; i < 16; i++){
			zeroID[i] = (byte)0;
		}
		this.httpRequestStr = new String();
		this.httpRequestFlag = false;
		this.type = type;
	}
	synchronized public void run(){

		/*byte[] buffer = new byte[1024];
		byte[] header = new byte[50];*/
		int size = 0;
		int i = 0;
		try {
			DataInputStream reader = new DataInputStream(this.connectionSocket.getInputStream());
			
			if(this.type == 0){
				SendThread send = new SendThread("OK", this.connectionSocket);
				send.start();
			}
			
			while (true) {
				byte[] buffer = new byte[1024];
				byte[] header = new byte[23];//this size must be 23 !!
				while ((size = reader.read(header, 0, header.length)) != -1) {
					System.out.println("Recv Thread is Reading Header");
					i++;
					analyzeHeader(header);
					break;
				}//end of first while

				if(this.connectFlag || this.httpFlag){
					clearNouveau();
					continue;
				}
				if (this.pingFlag) {
					sendBackPongRelayPing();
					clearNouveau();
					continue;
				}else{
					while ((size = reader.read(buffer, 0, getInt(this.payLoadLength))) != -1) {
						analyzePayload(buffer);
						if(size >= getInt(this.payLoadLength))
							break;//break must be needed !!
					}
					if (this.pongFlag) {
						relayPong();
						clearNouveau();
						continue;
					}
					if (this.queryFlag) {
						sendBackQueryHitRelayQuery();
						clearNouveau();
						continue;
					}
					if (this.queryHitFlag) {
						relayQueryHit();
						clearNouveau();
						continue;
						//break;
					}
				}
				//break;
				continue;

			}//end of while
			
			//System.out.println("Recevoir Nouveau done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	synchronized public void sendBackPongRelayPing(){
		int peerNum = MySingleton.getSizeSocketList();
		
		ArrayList<byte[]> al = new ArrayList<byte[]>();
		al.add(this.payLoadLength);
		MessageInfo info = new MessageInfo(al, this.messageID, this.TTL, this.HOP);
		if(MySingleton.checkRecvPing(this.messageID)){
			MySingleton.addRecvPing(info);
		}else{
			System.out.println("This PING has come before !!");
		//	clearNouveau();
			return ;
		}
		//MySingleton.addRecvPing(info);

		if (this.TTL > 1) {
			this.TTL--;
			this.HOP++;
			//int count = 0;
			for (int j = 0; j < peerNum; j++) {
				Socket s = MySingleton.getSocket(j);
				if (s.equals(this.connectionSocket) == false) {
					System.out.println("i try to send a PING");
					SendThread send = new SendThread("PING", s,
							this.TTL, this.HOP, this.messageID);
					send.start();//<- relay messages
					//count++;
				}

			}
			System.out.println("BEFORE SEND PONG in receive thread");
			SendThread sendPong = new SendThread("PONG",
					this.connectionSocket, this.messageID);
			sendPong.start();
		} else if (this.TTL == 1) {
			SendThread sendPong = new SendThread("PONG",
					this.connectionSocket, this.messageID);
			sendPong.start();
		}
		//clearNouveau();
		//continue;
	}
	synchronized public void relayPong(){
		int peerNum = MySingleton.getSizeSocketList();
		if (this.TTL > 1) {
			this.TTL--;
			this.HOP++;
			if (this.relayPongFlag) {
				for (int j = 0; j < peerNum; j++) {
					Socket s = MySingleton.getSocket(j);
					if (s.equals(this.connectionSocket) == false) {
						SendThread send = new SendThread(
								"PONG", s, this.TTL, this.HOP,
								this.messageID, this.copyPong);
						send.start();
					}
				}
			}
			this.copyPong.clear();
		}

	//	clearNouveau();
	}
	synchronized public void sendBackQueryHitRelayQuery(){
//		System.out.println("**** Send Back Query Hit Relay Query ****");

		MessageInfo queryInfo = new MessageInfo(this.copyQuery, this.messageID, this.TTL, this.HOP, this.searchWord);
		if(MySingleton.checkRecvQuery(this.messageID)){
			MySingleton.addRecvQuery(queryInfo);
			
		}else{
			System.out.println("This Query has come before !!!");
			return ;
		}
		int peerNum = MySingleton.getSizeSocketList();
		if (this.TTL > 1) {
			this.TTL--;
			this.HOP++;

			//int count = 0;
			for (int j = 0; j < peerNum; j++) {
				Socket s = MySingleton.getSocket(j);
				if (s.equals(this.connectionSocket) == false) {
					SendQueryThread sendQuery = new SendQueryThread(
							s, this.TTL, this.HOP,
							this.messageID, this.copyQuery);
					sendQuery.start();//<- relay messages
					//		count++;
				}
			}

			/*InfoNouveau info = new InfoNouveau(this.messageID,
					this.TTL, this.HOP, this.searchWord);*/
			//		MySingleton.addSentQuery(info);
			//System.out.println("----- Before Send Query Hit -----");
			System.out.println("This search word: " + this.searchWord);
			SendQueryHitThread sendQHit = new SendQueryHitThread(
					this.connectionSocket, this.searchWord,
					this.messageID);
			sendQHit.start();
			
			//System.out.println("----- After Send Query Hit -----");

		} else if (this.TTL == 1) {
			SendQueryHitThread sendQHit = new SendQueryHitThread(
					this.connectionSocket, this.searchWord,
					this.messageID);
			sendQHit.start();
		}
	//	clearNouveau();
//		System.out.println("**** End of Send Back Query Hit Relay Query ****");

	}
	synchronized public void relayQueryHit(){
		int peerNum = MySingleton.getSizeSocketList();
		if (this.TTL > 1) {
			this.TTL--;
			this.HOP++;
			if (this.relayQueryHitFlag) {
				for (int j = 0; j < peerNum; j++) {
					Socket s = MySingleton.getSocket(j);
					if (s.equals(this.connectionSocket) == false) {
						SendQueryHitThread send = new SendQueryHitThread(
								s, this.TTL, this.HOP,
								this.messageID,
								this.copyQueryHit, this.numHits);
						send.start();
					}
				}
			}
			this.copyQueryHit.clear();
		}
	//	clearNouveau();
	}
	synchronized public void clearNouveau(){
		this.connectFlag = false;
		this.pingFlag= false;
		this.pongFlag= false;
		this.queryFlag= false;
		this.queryHitFlag= false;
		this.duplicateFlag= false;
		this.httpFlag = false;
		
		this.searchWord = null;
		this.relayPongFlag = false;
		this.relayQueryHitFlag = false;
		this.httpRequestFlag = false;
		this.httpRequestStr = null;
		
		//System.out.println("CLEAR NOUVEAU done");
		/*
		this.TTL = 0;
		this.HOP = 0;*/
	}
	synchronized public void decideRelay(){
		if(this.pongFlag){
			if(MySingleton.checkSentPingID(this.messageID) == false){
				this.relayPongFlag = false;
				System.out.println("Relay Pong Flag false");
				//return false;
			}else{
				this.relayPongFlag = true;
				System.out.println("Relay Pong Flag true");

			}
		}else if(this.queryHitFlag){
			if(MySingleton.checkSentQueryID(this.messageID) == false){
				this.relayQueryHitFlag = false;
				System.out.println("Relay Query Hit Flag false");

			}else{
				this.relayQueryHitFlag = true;
				System.out.println("Relay Query Hit Flag true");

			}
		}
	}

	synchronized public void analyzePayload(byte[] buf){
		//sub 23 from buf suffix
		System.out.println("**** Analyze Payload ****");

		if(this.pongFlag){
			this.port[0] = buf[0];
			this.port[1] = buf[1];
			for(int i = 0; i < 4; i++){
				this.ipaddr[i] = buf[2 + i];
				this.numFiles[i] = buf[6 + i];
				this.sizeFiles[i] = buf[10 + i];
			}
			
			showPongInfo();
			this.copyPong.clear();
			this.copyPong.add(this.payLoadLength);
			this.copyPong.add(this.port);
			this.copyPong.add(this.ipaddr);
			this.copyPong.add(this.numFiles);
			this.copyPong.add(this.sizeFiles);
			
			MessageInfo pongInfo = new MessageInfo(this.copyPong, this.messageID, this.TTL, this.HOP);
			if(MySingleton.checkRecvPong(pongInfo)){
				MySingleton.addRecvPong(pongInfo);
			}else{
				System.out.println("J'ai deja ce Pong!");
			}
			//MySingleton.addRecvPong(pongInfo);
			
		}
		if(this.queryFlag){
			//showFlags();

			this.minSpeed[0] = buf[0];
			this.minSpeed[1] = buf[1];
			showQueryInfo();
			//this.searchCriteria = new byte[getInt(this.payLoadLength) - 2];
			this.searchCriteria = new byte[getInt(this.payLoadLength) - 3];
		//	System.out.println("criteria length: " + this.searchCriteria.length);
			for(int i = 0; i < this.searchCriteria.length; i++){
				this.searchCriteria[i] = buf[2 + i];
				//System.out.println(this.searchCriteria[i] + " ");
			}
			this.searchWord = new String(this.searchCriteria);
			System.out.println("SEARCH WORD = "  + this.searchWord);
			
			byte[] pll = Arrays.copyOf(this.payLoadLength, this.payLoadLength.length);
			byte[] msp = Arrays.copyOf(this.minSpeed, this.minSpeed.length);
			byte[] sc = Arrays.copyOf(this.searchCriteria, this.searchCriteria.length);
			this.copyQuery.clear();
			this.copyQuery.add(pll);
			this.copyQuery.add(msp);
			this.copyQuery.add(sc);
	//		System.out.println("----After this.copy.add()----");
			/*InfoNouveau queryInfo = new InfoNouveau(this.copyQuery, this.messageID, this.TTL, this.HOP, this.searchWord);
			if(MySingleton.checkRecvQuery(this.messageID)){
				MySingleton.addRecvQuery(queryInfo);
			}*/
			//MySingleton.addRecvQuery(queryInfo);
//			System.out.println("----Add Recv Query Done in analyze payload method----");
			
		
		}
		if(this.queryHitFlag){
		//	System.out.println("----in if(this.QueryHitFlag)----");
			//showFlags();
			this.numHits = buf[0];
			this.port[0] = buf[1];
			this.port[1] = buf[2];
			for(int i = 0; i < 4; i++){
				this.ipaddr[i] = buf[3 + i];
				this.speed[i] = buf[7 + i];
			}
			int resultsetFieldLength = getInt(this.payLoadLength) - 16 - 11;
			byte[] resultsetField = new byte[resultsetFieldLength];
			for(int i = 0; i < resultsetFieldLength; i++){
				resultsetField[i] = buf[11 + i];
			}
			byte[] serventIdentifier = new byte[16];
			for(int i = 0; i < 16; i++){
				serventIdentifier[i] = buf[11 + resultsetFieldLength + i];
			}
			
			byte[] pll = Arrays.copyOf(this.payLoadLength, this.payLoadLength.length);

			byte[] p = Arrays.copyOf(this.port, this.port.length);
			byte[] ipad = Arrays.copyOf(this.ipaddr, this.ipaddr.length);
			byte[] sp = Arrays.copyOf(this.speed, this.speed.length);
			
			this.copyQueryHit.clear();
			this.copyQueryHit.add(pll);//0
			this.copyQueryHit.add(p);//1
			this.copyQueryHit.add(ipad);//2
			this.copyQueryHit.add(sp);//3
			this.copyQueryHit.add(resultsetField);//4
			this.copyQueryHit.add(serventIdentifier);//5
			showQueryHitInfo();
			analyzeResultset(resultsetField);
			
			MessageInfo queryHitInfo = new MessageInfo(this.copyQueryHit, this.messageID, this.TTL, this.HOP, this.numHits);
			if(MySingleton.checkRecvQueryHit(queryHitInfo)){
				MySingleton.addRecvQueryHit(queryHitInfo);
			}else{
				System.out.println("J'ai deja ce Query Hit.");
			}
			//MySingleton.addRecvQueryHit(queryHitInfo);
			
		}
		System.out.println("**** End of Analyze Payload ****");

		
	}
	synchronized public void analyzeHeader(byte[] buf){
		for(int i = 0; i < 16; i++){
			this.messageID[i] = buf[i];
			
		}
		if(Arrays.equals(zeroID, this.messageID)){
			System.out.println("THIS Message ID is ALL ZERO !!");
			return ;
		}
		System.out.println("**** Analyze Header ****");
		showMessageId(this.messageID);
		if(buf[16] == this.pingHeader){
			this.pingFlag = true;
			System.out.println("RECEIVE: C'est PING");
			//showMessageId(this.messageID );
		}else if(buf[16] == this.pongHeader){
			this.pongFlag = true;
			System.out.println("RECEIVE: Dies ist PONG");
			//
			decideRelay();

		}else if(buf[16] == this.queryHeader){
			this.queryFlag = true;
			System.out.println("RECEIVE: Este es QUERY");

		}else if(buf[16] == this.queryHitHeader){
			this.queryHitFlag = true;
			System.out.println("RECEIVE: Questo e Query Hit");
			//
			decideRelay();

		}else{
			System.out.println("else in header check: "  + buf[16]);
			return ;
		}

		this.TTL = buf[17];
		this.HOP = buf[18];

		for(int i = 0; i < 4; i ++){
			this.payLoadLength[i] = buf[19 + i];
		}
		System.out.println("TTL: " + this.TTL + " HOP: " + this.HOP);
		System.out.println("Payload Length: " + getInt(this.payLoadLength));
		
		System.out.println("**** End of Analyze Header ****");


	}
	synchronized public void showQueryHitInfo(){
		showMessageId(this.messageID);
		System.out.println("TTL: " + this.TTL + " HOP: " + this.HOP);
		System.out.println("Payload Length = " + getInt(this.payLoadLength));
		System.out.println("Number of Hits: " + this.numHits);
		showPortNumber(this.port);
		showIPAddressLittleEndian(this.ipaddr);
		System.out.print("\nSpeed(KB/s) : ");
		showFourBytes(this.speed);
		byte[] sv = this.copyQueryHit.get(5);
		System.out.println("Servent Identifier");
		for(int i = 0; i < 16; i++){
			System.out.print((char)sv[i]);
		}
		System.out.println("");
		
	}
	public void analyzeResultset(byte[] rsets){
		byte[] index = new byte[4];
		byte[] size = new byte[4];
		byte[] name = new byte[rsets.length - 8];
		int k = 0;
		for(int j = 0 ;j < rsets.length;){
			k = 0;
			if(j + 4 > rsets.length || j + 8 > rsets.length){
				break;
			}
			for(int i = 0; i < 4; i++){
				index[i] = rsets[i + j];
				size[i] = rsets[i + 4 + j];
			}
			for(int i = 0; ;i++){
				name[i] = rsets[i + 8 + j];
				
				if (i > 0) {
					if (name[i] == (byte) 0x00 && name[i - 1] == (byte) 0x00) {
						break;
					}
				}
				k++;
			}
			byte[] fileName = new byte[k];
			for(int l = 0; l < k; l++){
				fileName[l] = name[l];
			}
			String nameStr = new String(fileName);
			ResultSet rset = new ResultSet(nameStr);
			rset.index = Arrays.copyOf(index, index.length);
			rset.size = Arrays.copyOf(size, size.length);
			System.out.print("File Index: ");
			showFourBytes(rset.index);
			System.out.print("File Size(KB): ");
			showFourBytes(rset.size);
			System.out.println("File Name : " + nameStr);
			
			j += 8 + fileName.length + 1;//
			continue;
			
		}
	}
	synchronized public void showQueryInfo(){
		showMessageId(this.messageID);
		System.out.println("TTL: " + this.TTL + " HOP: " + this.HOP);
		System.out.println("Min Speed; " + getShort(this.minSpeed));
		System.out.println("Payload Length: " + getInt(this.payLoadLength));
	}
	synchronized public void showPongInfo(){
		showMessageId(this.messageID );
		System.out.println("TTL: " + this.TTL + " HOP: " + this.HOP);
		showPortNumber(this.port);
		showIPAddressLittleEndian(this.ipaddr);
		System.out.print("\nNumber of Files Shared: ");
		showFourBytes(this.numFiles);
		System.out.print("Size of Files Shared: ");
		showFourBytes(this.sizeFiles);
	}
	synchronized public void showFlags(){
		System.out.println("Connect Flag " + this.connectFlag);
		System.out.println("HTTP Flag " + this.httpFlag);
		System.out.println("PingFlag: " + this.pingFlag);
		System.out.println("PongFlag: " + this.pongFlag);
		System.out.println("QueryFlag: " + this.queryFlag);
		System.out.println("Query Hit Flag: " + queryHitFlag);
	}
	synchronized public void showMessageId(byte[] id){
		System.out.println("----SHOW Message ID----");
		for(int i = 0; i < 16; i++){
			System.out.print((char)id[i]);
		}
		System.out.println(" ");
	}
	public short getShort(byte[] p){
		short f = p[0];
		/*if(f > 0){//
			f &= (short)0x00FF;
		}*/
		f = (short) (f << 8);
		short s = p[1];
		/*if(s > 0){//
			s &= (short)0x00FF;
		}*/
		return (short) (f + s);
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
	public void showIPAddressLittleEndian(byte[] ip){
		System.out.print("IP Address: ");
		for(int i = 3; i >= 0; i--){
			int in = ip[i];
			if(in < 0){
				in &= (int)0x000000FF;
			}
			System.out.print(in + " ");
			//	System.out.print(message2[i + 2] + " ");
		}
	}

	public void showFourBytes(byte[] b){
		/*for(int i = 0; i < 4; i++){
			System.out.println(b[i]);
		}*/
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
		System.out.println(sum);
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
	
	public String[] analyzeRequest(String req){
		String[] reqPart = req.split("/");
		String[] info = new String[2];
		info[0] = reqPart[2];//file index
		info[1] = reqPart[3];//file name
		return info;
	}


	public static int returnFileNameLength(String input){
		String nullStr = "0000" + "0000";
		String[] tb = new String[input.length()/ 8];
		for(int i = 0; i < tb.length; i++){
			tb[i] = input.substring(i * 8, i * 8 + 8);//see sub string of 2bytes of binary 
			
		}
		for(int i = 0; i < tb.length;i++){
			if(tb[i].equals(nullStr) && tb[i + 1].equals(nullStr)){
				return i;
			}
		}
		return -1;
		
	}

	public void clearFlags(){
		this.connectFlag = false;
		this.pingFlag= false;
		this.pongFlag= false;
		this.queryFlag= false;
		this.queryHitFlag= false;
		this.duplicateFlag= false;
		this.httpFlag = false;
	}
	
	public String showPongDescriptorInfo(int i){
		switch(i){
		case 0:
			return "UUID\n";
		case 1:
			return "PayloadDescriptor\n";
		case 2:
			return "TTL\n";
		case 3:
			return "HOPS\n";
		case 4:
			return "Payload Length\n";
		case 5:
			return "PORT\n";
		case 6:
			return "IP Address\n";
		case 7:
			return "Number of File Shared\n";
		case 8:
			return "Number of KB Shared\n";
		default:
			return "else\n";
		}
	}
	
	public String showQueryDescriptorInfo(int i){
		switch(i){
		case 0:
			return "UUID\n";
		case 1:
			return "PayloadDescriptor\n";
		case 2:
			return "TTL\n";
		case 3:
			return "HOPS\n";
		case 4:
			return "Payload Length\n";
		case 5:
			return "Minimum Speed\n";
		case 6:
			return "Search Chriteria\n";
		default:
			return "else\n";
		}
	}
	public String showQueryHitDescriptorInfo(int i){
		switch(i){
		case 0:
			return "UUID\n";
		case 1:
			return "PayloadDescriptor\n";
		case 2:
			return "TTL\n";
		case 3:
			return "HOPS\n";
		case 4:
			return "Payload Length\n";
		case 5:
			return "Number of Hits\n";
		case 6:
			return "Port\n";
		case 7:
			return "IPAddress\n";
		case 8:
			return "Speed\n";
		case 9:
			return "Result Set\n";
		case 10:
			return "Servent Identifer\n";
		default:
			return "else\n";		
			
		}
	}
	
}
