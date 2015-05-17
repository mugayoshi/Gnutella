import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;



public class RecvThread extends Thread{
	Socket connectionSocket;
	String searchWord;
	ArrayList<byte[]> copyPong, copyQuery, copyQueryHit;
	String[] requests;
	boolean connectFlag,pingFlag, pongFlag, duplicateFlag, queryFlag, queryHitFlag;
	boolean httpFlag;
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
