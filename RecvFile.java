import java.io.*;
import java.net.*;
import java.util.*;

public class RecvFile extends RecvThread {
	boolean keepAllive;
	String fileName;
	int fileLen;
	final String FILE_ROOT = "/Users/yoshikawamuga/Documents/Gnutella/";
	public RecvFile(Socket s, String filename)throws SocketException{
		super(s);
		this.fileName = filename;
		this.keepAllive  = false;
		try{
			this.connectionSocket.setSoTimeout(50000);
			this.connectionSocket.setKeepAlive(true);
			
		}catch(SocketException e){
			System.out.println(e);
		}
		
	}
	
	public void run(){
		int i = 0;
		String serverPort = String.valueOf(MySingleton.getServerPortNum());
		String filePath = FILE_ROOT + serverPort + "/" +this.fileName;
		File file = new File(filePath);
		
		try{
			System.out.println("Recv File Thread");
			System.out.println("file path: " + filePath);
			FileOutputStream outstream = new FileOutputStream(file);
			byte[] buf = new byte[100];
			int writtenSize = 0;
			//data starts from line 5
			int s = 0, s_prev = 0;
			i = 0;
			DataInputStream reader = new DataInputStream(this.connectionSocket.getInputStream());
			System.out.println(reader.available());
			
			while((s = reader.read()) != -1){
				if(s_prev == '\n' && s == '\r' ){
					buf[i++] = (byte)s;
					buf[i] = (byte)reader.read();
					break;
				}else{
					buf[i++] = (byte)s;
					s_prev = s;
				}
				
			}//end of while loop
	//		System.out.println("i = " + i);
			i = 0;
			this.fileLen = getFileLen(buf);
			while((s = reader.read(buf, 0, 100)) != -1){
				byte[] bytes = new byte[s];
				for(int j = 0; j < s; j++){
					bytes[j] = buf[j];
				}
				outstream.write(bytes);
				writtenSize += s;
				//System.out.println(i + " written size: " + writtenSize);
				double percent = (double)(writtenSize) / (double)(this.fileLen) * 100;
				if(percent > 0 && (int)percent % 20 == 0 && i % 10 == 0){
					System.out.println("written size: " + writtenSize);
					System.out.println("Done " + percent + "%");
				}
				
				if(writtenSize >= this.fileLen){// <- Size !!
					//System.out.println("this.fileLen: " + this.fileLen);
					System.out.println("Break Written Size >= File Size !!");
					break;
				}
				i++;
			}
			//outstream.write(buf);
			outstream.close();
			
			MySingleton.deleteSocket(this.connectionSocket);
			System.out.println("Recv File thread done");
		}catch(IOException e){
			System.out.println(e);
		}
		
	}
	int getFileLen(byte[] b){
		int s = 100;
		String str = new String();
		ArrayList<String> lines = new ArrayList<String>();
		for(int i = 0; i < s; i++){
			char c = (char)b[i];
			
			if(c == '\n'){
				lines.add(str);
				str = new String();
			}else{
				Character C = new Character(c);
				str += C.toString();
			}
		}
		for(int i = 0; i < lines.size(); i++){
			String l =  lines.get(i);
			System.out.println(i + " " + l);
			if(l.contains("length")){
				String[] p =l.split(":");
				int p1len = p[1].length();
				Integer ret = new Integer(p[1].substring(1, p1len - 1));
				return ret;
			}
			
		}
		return 0;
		
	}
	
}
