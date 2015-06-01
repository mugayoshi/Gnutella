import java.util.Random;

public class DescriptorHeader{

	int NUM_HOPS = 2;
	
	byte[] messageID;
	byte payloadDescriptor;//0x00:ping, 0x01 pong, 0x40 Push, 0x80 query, 0x81 QueryHit
	byte ttlByte, hopByte;
	byte[] payloadlengthByte;
	
	public DescriptorHeader(byte ttl, byte hops){
		
		this.messageID = new byte[16];
		this.payloadlengthByte = new byte[4];
		this.ttlByte = (byte)ttl;
		this.hopByte = (byte)hops;
		Random r = new Random();
		for(int i = 0; i < 16; i++){
			int ran = r.nextInt(128);
			if((ran >= 65 && ran <= 90) || (ran >= 97 && ran <= 122)){
				this.messageID[i] = (byte)ran;
				continue;
			}else{
				ran = (byte)r.nextInt(65);
				this.messageID[i] = (byte)(ran + 65);
				if(this.messageID[i] > 90 && this.messageID[i] < 97 || this.messageID[i] > 122){
					i--;
					continue;
				}else if(this.messageID[i] > 127 || this.messageID[i] < 0){
					i--;
					continue;
				}
			}
			//this.messageID[i] = (byte)ran;
			
		}
	}
	public int getIntValFromByteArray(byte[] b){
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

class Pong extends DescriptorHeader{
	public Pong(byte ttl, byte hops) {
		super(ttl, hops);
		this.payloadDescriptor = (byte)0x01;
		// TODO Auto-generated constructor stub
	}
	
	byte[] portByte = new byte[2];// port;//2byte[0,1]
	byte[] ipaddrBytes = new byte[4];//IPAddress; //4bytes [2,5]
	byte[] numfileBytes = new byte[4];//numFileShared;//4bytes [6,9]
	byte[] numKBBytes = new byte[4];//numKBShared;//4bytes [10, 13]
	
	public byte[] returnIPAddressBinaryStr(String ipadr){
		
		String[] valStr = ipadr.split("\\.");//need to put ESCAPE sequence
		
		byte[] ipaddr = new byte[4];
		for(int i = valStr.length - 1, j = 0; i >= 0; i--, j++){// this IPAddress is Little Endian !!
			Integer val = Integer.valueOf(valStr[i]);
			//Integer v = new Integer(getInt(b))
			ipaddr[j] = val.byteValue();
		}
		System.out.println("");
		return ipaddr;
	}
	
}

class Query extends DescriptorHeader{
	
	//int minSpeed;//2bytes [0,1]
	int searhCriteria;//more than 1byte the maximum length is bounded by pay load length field of descriptor header
	
	byte[] minspeedByte = new byte[2];
	public Query(byte ttl,byte hops) {
		super(ttl, hops);
		this.payloadDescriptor = (byte)0x80;
		
		// TODO Auto-generated constructor stub
	}
}
class QueryHit extends DescriptorHeader{
	public QueryHit(byte ttl, byte hops) {
		super(ttl, hops);
		this.payloadDescriptor = (byte)0x81;
		// TODO Auto-generated constructor stub
	}
	int numHit;//1byte
	int port;//1byte
	int IPAddress;//
	
	byte numhitByte;
	byte[] portByte = new byte[2];
	byte[] ipaddrBytes = new byte[4];
	public byte[] returnIPAddressBinaryStr(String ipadr){

		String[] valStr = ipadr.split("\\.");//need to put ESCAPE sequence

		byte[] ipaddr = new byte[4];
		for(int i = valStr.length - 1, j = 0; i >= 0; i--, j++){// this IPAddress is Little Endian !!
			Integer val = Integer.valueOf(valStr[i]);
			//Integer v = new Integer(getInt(b))
			ipaddr[j] = val.byteValue();
		}
		System.out.println("");
		return ipaddr;
	}
	
}

class MessageID{
	long high, low;
	public MessageID(long h, long l){
		this.high = h;
		this.low = l;
	}
}
