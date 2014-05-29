import java.net.*;
import java.util.*;

import org.omg.CORBA.INTF_REPOS;

public class MySingleton {
	private ArrayList<Socket> socketList;

	private ArrayList<Integer> recvThreadFlag; //if recv thread (its socket list index is i) is active, recvflag[i] get 1
//	private ArrayList<byte[]> messageID;
	
	private ArrayList<InfoNouveau> sentPingsList;
	private ArrayList<InfoNouveau> sentQuerysList;
	
	private  ArrayList<InfoNouveau> recvPongsList;
	private  ArrayList<InfoNouveau> recvPingsList;
	private  ArrayList<InfoNouveau> recvQuerysList;
	private  ArrayList<InfoNouveau> recvQueryHitsList;
	
	private  ArrayList<ResultSet> resultSets;
	private  int serverPortNumber;
	private final byte NUM_HOP = 7;
	private byte[] zeroID;
	private static MySingleton obj = new MySingleton();
	
	private MySingleton(){
		socketList = new ArrayList<Socket>();
		recvThreadFlag = new ArrayList<Integer>();
		recvPongsList = new ArrayList<InfoNouveau>();
		recvPingsList = new ArrayList<InfoNouveau>();
		recvQuerysList = new ArrayList<InfoNouveau>();
		recvQueryHitsList= new ArrayList<InfoNouveau>();

		sentPingsList = new ArrayList<InfoNouveau>();
		sentQuerysList = new ArrayList<InfoNouveau>();
		
		resultSets = new ArrayList<ResultSet>();
		zeroID = new byte[16];
		for(int i = 0; i < 16; i++){
			zeroID[i] = (byte)0;
		}
	}
	public static MySingleton getInstance(){
		return obj;
	}
	public static byte getNumHop(){
		return obj.NUM_HOP;
	}
	public static void registServerPortNum(int port){
		obj.serverPortNumber = port;
	}
	public static int getServerPortNum(){
		return obj.serverPortNumber;
	}
	
	/*public static void addServentIdentifier(String s){
		obj.serventIdentifier.add(s);
	}*/
	synchronized public static void addRecvPing(InfoNouveau info){
		byte[] id = Arrays.copyOf(info.ID, 16);
		//byte[] zero = new byte[16];
		
		if(Arrays.equals(obj.zeroID, id)){
			System.out.println("ID is Incorrect because it's ALL ZERO");
			System.out.println("This ID isn't added to MySingleton RecvPing");
		}else{
			obj.recvPingsList.add(info);
		}
		
	}
	synchronized public static void addSentPing(InfoNouveau info){
		obj.sentPingsList.add(info);
	}
	synchronized public static void addRecvPong(InfoNouveau info){
		obj.recvPongsList.add(info);
	}
	synchronized public static void addSentQuery(InfoNouveau info){
		obj.sentQuerysList.add(info);
	}
	synchronized public static void addRecvQuery(InfoNouveau info){
		obj.recvQuerysList.add(info);
	}
	synchronized public static void addRecvQueryHit(InfoNouveau l){
		obj.recvQueryHitsList.add(l);
		for(int i = 0; i < l.resultSets.size(); i++){
			ResultSet rset = l.resultSets.get(i);
			obj.resultSets.add(rset);
		}
	}
	
	public static void showSocket(){
		System.out.println("Show Socket Method");
		for(int i = 0; i < obj.socketList.size(); i++){
			Socket s = obj.socketList.get(i);
			Integer flag = obj.recvThreadFlag.get(i);
			System.out.println(i + ": " + s.toString() + " :flag " + flag);
		
		}
	}
	
	public static void showResultSets(){
		System.out.println("---- Show Result Set Method ----");
		for(int i = 0; i < obj.resultSets.size(); i++){
			System.out.println("Result Set Index: " + i);
			ResultSet rSet = obj.resultSets.get(i);
			InfoNouveau info = MySingleton.getInfo(rSet);
			byte[] ipaddr = info.list.get(2);
			//System.out.println("IP Address: ");
			info.showIPAddressLittleEndian(ipaddr);
			System.out.println("PORT Number: " + info.getPortNumber());
			rSet.showMyself();
		}
		System.out.println("---- End Show Result Set Method ----");

	}
	public static ResultSet getResultSet(int index){
		return obj.resultSets.get(index);
		
	}
	public static InfoNouveau getInfo(ResultSet rSet){
		for(int i = 0; i < obj.recvQueryHitsList.size(); i++){
			InfoNouveau info = obj.recvQueryHitsList.get(i);
			for(int j = 0; j < info.resultSets.size(); j++){
				if(info.resultSets.get(j).equals(rSet)){
					return info;
				}
			}
			
		}
		return null;
	}
	public static int getResultsetIndex(ResultSet s){
		int index = obj.resultSets.indexOf(s);
		return index;
	}
	
	synchronized public static void showInfo(){
		System.out.println("----Show Information Method----");
		
		if(obj.sentPingsList.size() == 0){
			System.out.println("**** NO SENT PING **** ");
		}else{
			System.out.println("**** SENT PING ****");
			for(int i = 0; i < obj.sentPingsList.size(); i++){
				InfoNouveau info = obj.sentPingsList.get(i);
				info.showPingInfo();
			}
		}
		if(obj.sentQuerysList.size() == 0){
			System.out.println("**** NO SENT QUERY **** ");
		}else{
			System.out.println("**** SENT QUERY ****");
			for(int i = 0; i < obj.sentQuerysList.size(); i++){
				InfoNouveau info = obj.sentQuerysList.get(i);
				info.showQueryInfo();
			}	
		}
		
		if(obj.recvPingsList.size() == 0){
			System.out.println("**** NO RECV PING **** ");
		}else{
			System.out.println("**** RECV PING ****");
			for(int i = 0; i < obj.recvPingsList.size(); i++){
				System.out.println("i = " + i);
				InfoNouveau info = obj.recvPingsList.get(i);
				info.showPingInfo();
			}	
		}
		
		if(obj.recvPongsList.size() == 0){
			System.out.println("**** NO RECV PONG **** ");

		}else{
			System.out.println("**** RECV PONG ****");
			for(int i = 0; i < obj.recvPongsList.size(); i++){
				System.out.println("i = " + i);
				InfoNouveau info = obj.recvPongsList.get(i);
				info.showPongInfo();
			}	
		}
		
		
		if(obj.recvQuerysList.size() == 0){
			System.out.println("**** NO RECV QUERY **** ");
		}else{
			System.out.println("**** RECV QUERY ****");
			for(int i = 0; i < obj.recvQuerysList.size(); i++){
				System.out.println("i = " + i);
				obj.recvQuerysList.get(i).showQueryInfo();
				
			}	
		}
		
		if(obj.recvQueryHitsList.size() == 0){
			System.out.println("**** NO RECV QUERY HITS ****");
		}else{
			System.out.println("**** RECV QUERY HITS *****");
			for(int i = 0; i < obj.recvQueryHitsList.size(); i++){
				System.out.println("i = " + i);
				obj.recvQueryHitsList.get(i).showQueryHitInfo();
				
			}	
		}
		System.out.println("----End Show Information Method----");

	}
	public static boolean addSocket(Socket s){
		if(obj.socketList.size() >= 1000){
			System.out.println("can't regist socket");
			return false;
		}else{
			//obj.socket[obj.numSocket] = s;
			obj.socketList.add(s);
			Integer i = new Integer(0);
			obj.recvThreadFlag.add(i);//add 0 in obj flag ArrayList
			return true;
		}
	}
	public static Socket getSocket(int i){
		return obj.socketList.get(i);
	}
	public static int getSocketIndex(Socket s){
		int index = obj.socketList.indexOf(s);
		return index;
	}
	public static int getSizeSocketList(){
		return obj.socketList.size();
	}
	public static boolean deleteSocket(Socket s){
		if(obj.socketList.size() <= 0){
			System.out.println("Number of Socket is less than 0");
			return false;
		}else{
			int destIndex = obj.socketList.indexOf(s);
			if(destIndex < 0){
				System.out.println("obj socket doesn't exist");
				return false;
			}else{
				System.out.println("\nRemove Socket: \n" + obj.socketList.get(destIndex).toString());
				obj.socketList.remove(destIndex);
				obj.recvThreadFlag.remove(destIndex);
				
				return true;
			}
			
		}
	}
	public static void flagUp(int index){
		Integer i = new Integer(1);
		obj.recvThreadFlag.add(index, i);
	}
	public static boolean flagCheck(Socket s){
		int destIndex = obj.socketList.indexOf(s);
		if(obj.recvThreadFlag.get(destIndex).equals(1)){
			return true;
		}else{
			return false;
		}
	}
	
	public static int checkSocket(Socket s){
		for(int i = 0; i < obj.socketList.size(); i++){
			if(s.equals(obj.socketList.get(i))){
				return i;
			}
		}
		return -1;
	}
	
	//checkRecvPingID methods returns false 
	//if MySinglton HAS recvPing's ID in recvPing
	synchronized public static boolean checkRecvPing(byte[] recvPingId){
		for(int i = 0; i < obj.recvPingsList.size(); i++){
			if(Arrays.equals(recvPingId, obj.recvPingsList.get(i).ID)){
				return false;
			}
		}
		return true;
	}
	//checkRecvQueryID methods returns false 
		//if MySinglton HAS recvQuery's ID in recvQuerysList
		synchronized public static boolean checkRecvQuery(byte[] recvQueryId){
			for(int i = 0; i < obj.recvQuerysList.size(); i++){
				if(Arrays.equals(recvQueryId, obj.recvQuerysList.get(i).ID)){
					return false;
				}
			}
			return true;
		}
	//checkSentPingID methods returns false 
	//if MySinglton HAS recvPong's ID in recvPing
	synchronized public static boolean checkSentPingID(byte[]recvPongId){
		for(int i = 0; i < obj.sentPingsList.size(); i++){
			if(Arrays.equals(recvPongId, obj.sentPingsList.get(i).ID)){
				return false;
			}
		}
		return true;
	}
	//checkSentQueryID methods returns false 
	//if MySinglton HAS recvQuery's ID in recvQuery
	synchronized public static boolean checkSentQueryID(byte[]recvId){
		for(int i = 0; i < obj.sentQuerysList.size(); i++){
			if(Arrays.equals(recvId, obj.sentQuerysList.get(i).ID)){
				return false;
			}
		}
		return true;
	}
	synchronized public static  boolean checkRecvPong(InfoNouveau info){
		//this method returns false if my singleton includes info in recv pong
		/*PONG
		 * this.copyPong.add(this.payLoadLength);//0
			this.copyPong.add(this.port);//1
			this.copyPong.add(this.ipaddr);//2
			this.copyPong.add(this.numFiles);//3
			this.copyPong.add(this.sizeFiles);//4
		 */
		byte[] ipAddr = new byte[4];
		byte[] port = new byte[2];
		byte[] numFiles = new byte[4];
		byte[] sizeFiles = new byte[4];
		for(int i = 0; i < obj.recvPongsList.size(); i++){
			port =  Arrays.copyOf(obj.recvPongsList.get(i).list.get(1), 2);
			ipAddr = Arrays.copyOf(obj.recvPongsList.get(i).list.get(2), 4);
			numFiles = Arrays.copyOf(obj.recvPongsList.get(i).list.get(3), 4);
			sizeFiles = Arrays.copyOf(obj.recvPongsList.get(i).list.get(4), 4);
			
			if(Arrays.equals(port, info.list.get(1)) && 
					Arrays.equals(ipAddr, info.list.get(2)) &&
					Arrays.equals(numFiles, info.list.get(3)) &&
					Arrays.equals(sizeFiles, info.list.get(4))){
				return false;
			}
		}
		return true;
		
	}
	synchronized public static  boolean checkRecvQueryHit(InfoNouveau info){
		//this method returns false if my singleton includes info in recv pong
		/*		QUERY HIT
		this.copyQueryHit.add(this.payLoadLength);//0
		this.copyQueryHit.add(this.port);//1
		this.copyQueryHit.add(this.ipaddr);//2
		this.copyQueryHit.add(this.speed);//3
		this.copyQueryHit.add(resultsetField);//4
		this.copyQueryHit.add(serventIdentifier);//5
*/
		byte[] ipAddr = new byte[4];
		byte[] port = new byte[2];
		byte[] sv = new byte[16];
		for(int i = 0; i < obj.recvQueryHitsList.size(); i++){
				port =  Arrays.copyOf(obj.recvQueryHitsList.get(i).list.get(1), 2);
				ipAddr = Arrays.copyOf(obj.recvQueryHitsList.get(i).list.get(2), 4);
				sv = Arrays.copyOf(obj.recvQueryHitsList.get(i).list.get(5), 16);
			if(Arrays.equals(port, info.list.get(1)) && 
					Arrays.equals(ipAddr, info.list.get(2)) &&
					Arrays.equals(sv, info.list.get(5))){
				return false;
			}
		}
		return true;
		
	}

}


