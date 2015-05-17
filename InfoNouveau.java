package Gnutella;

import java.util.*;
public class InfoNouveau {
	ArrayList<byte[]> list;
	byte[] ID;
	byte TTL, HOP;
	byte numHits;
	String searchCriteria;
	ArrayList<ResultSet> resultSets;
	public InfoNouveau(byte[] idIn, byte ttl, byte hop){//for Ping
		this.ID = Arrays.copyOf(idIn, idIn.length);
		this.TTL = ttl;
		this.HOP = hop;
	}
	public InfoNouveau(byte[] idIn, byte ttl, byte hop, String searchWord){//for query
		this.ID = Arrays.copyOf(idIn, idIn.length);
		this.TTL = ttl;
		this.HOP = hop;
		this.searchCriteria = searchWord;
	}
	public InfoNouveau(ArrayList<byte[]> in ,byte[] Idin, byte ttl, byte hop, String searchWord){
		this.list = new ArrayList<byte[]>();
		//this.list = in;
		for(int i = 0; i < in.size(); i++){
			byte[] b = Arrays.copyOf(in.get(i), in.get(i).length );
			this.list.add(b);
		}
		this.ID = Arrays.copyOf(Idin, Idin.length);
		this.TTL = ttl;
		this.HOP = hop;
		this.searchCriteria = searchWord;
	}
	public InfoNouveau(ArrayList<byte[]> in, byte[] idIn, byte ttl, byte hop){//for pong
		this.list = new ArrayList<byte[]>();
		//this.list = in;
		for(int i = 0; i < in.size(); i++){
			byte[] b = Arrays.copyOf(in.get(i), in.get(i).length );
			this.list.add(b);
		}
		this.ID = Arrays.copyOf(idIn, idIn.length);
		this.TTL = ttl;
		this.HOP = hop;
		/*PONG
		 * this.copyPong.add(this.payLoadLength);//0
			this.copyPong.add(this.port);//1
			this.copyPong.add(this.ipaddr);//2
			this.copyPong.add(this.numFiles);//3
			this.copyPong.add(this.sizeFiles);//4
		 */
	}
	public InfoNouveau(ArrayList<byte[]> in, byte[] idIn, byte ttl, byte hop, byte numHits){//for query hit
		this.list = new ArrayList<byte[]>();
		//this.list = in;
		for(int i = 0; i < in.size(); i++){
			byte[] b = Arrays.copyOf(in.get(i), in.get(i).length );
			this.list.add(b);
		}
		this.ID = new byte[16];
		this.ID = Arrays.copyOf(idIn, idIn.length);
		this.TTL = ttl;
		this.HOP = hop;
		
		this.numHits = numHits;
/*		QUERY HIT
		this.copyQueryHit.add(this.payLoadLength);//0
		this.copyQueryHit.add(this.port);//1
		this.copyQueryHit.add(this.ipaddr);//2
		this.copyQueryHit.add(this.speed);//3
		this.copyQueryHit.add(resultsetField);//4
		this.copyQueryHit.add(serventIdentifier);//5
*/
		
		this.resultSets = new ArrayList<ResultSet>();
		analyzeResultset(this.list.get(4));
	}
	public void showPingInfo(){
		System.out.println("Message ID");
		for(int i = 0; i < 16; i++){
			System.out.print((char)this.ID[i]);
		}
		System.out.println("");
		System.out.println("TTL: " + this.TTL + " HOP: " + this.HOP);
	}
	public void showPongInfo(){
		System.out.println("Message ID");
		for(int i = 0; i < 16; i++){
			System.out.print((char)this.ID[i]);
		}
		System.out.println("");
		System.out.println("TTL: " + this.TTL + " HOP: " + this.HOP);
		//this list includes payloadlen, port, ipaddress, num files, num KB shared

		showPortNumber(this.list.get(1));
		showIPAddressLittleEndian(this.list.get(2));
		System.out.print("\nNumber of Files Shared: ");
		showFourBytes(this.list.get(3));
		System.out.print("Size of Files Shared: ");
		showFourBytes(this.list.get(4));
	}
	public void showQueryInfo(){
		showPingInfo();
		System.out.println("Search Criteria: " + this.searchCriteria);
	}
	public void showQueryHitInfo(){
		System.out.println("Message ID");
		for(int i = 0; i < 16; i++){
			System.out.print((char)this.ID[i]);
		}
		System.out.println("");
		System.out.println("TTL: " + this.TTL + " HOP: " + this.HOP);
		System.out.println("Number of Hits: " + this.numHits);
		showPortNumber(this.list.get(1));
		System.out.print("IP Address ");
		//showFourBytes(this.list.get(2));
		showIPAddressLittleEndian(this.list.get(2));
		System.out.print("Speed (KB/s)");
		showFourBytes(this.list.get(3));
		showResultSets();
		System.out.println("Servent Identifier");
		for(int i = 0; i < 16; i++){
			System.out.print((char)this.list.get(5)[i]);
		}
		System.out.println("");
		
	}
	public void showResultSets(){
		for(int i = 0; i < this.resultSets.size(); i++){
			ResultSet rset = this.resultSets.get(i);
			System.out.print("File Index: ");
			showFourBytes(rset.index);
			System.out.print("File Size(KB): ");
			showFourBytes(rset.size);
			System.out.println("File Name: " + new String(rset.name));
		}
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
			this.resultSets.add(rset);
		
			j += 8 + fileName.length + 1;//
			continue;
			
		}
	}

	public void showFourBytes(byte[] b){
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
	public int getPortNumber(){
		byte[] p = this.list.get(1);
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
	/*public void showIPAddress(byte[] ip){
		System.out.print("IP Address: ");
		for(int i = 0; i < 4; i++){
			int in = ip[i];
			if(in < 0){
				in &= (int)0x000000FF;
			}
			System.out.print(in + " ");
		//	System.out.print(message2[i + 2] + " ");
		}
	}*/
	public String makeIPAddressStr(){
		byte[] ip = this.list.get(2);
		String ipaddr = new String();
		for(int i = 3; i >= 0; i--){
			int in = ip[i];
			if(in < 0){
				in &= (int)0x000000FF;
			}
			if(i > 0){
				ipaddr += Integer.toString(in) + ".";
			}else{
				ipaddr += Integer.toString(in);
			}
			
		//	System.out.print(message2[i + 2] + " ");
		}
		return ipaddr;
	}
	
}
