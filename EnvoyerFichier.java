import java.net.*;
import java.io.*;
public class EnvoyerFichier extends SendThread{
	String[] info;
	final String FILE_ROOT = "/Users/yoshikawamuga/Documents/Gnutella/";
	public EnvoyerFichier(Socket s, String[] info){
		super("HTTP", s);
		this.info = new String[2];
		this.info = info;
	}
	public void run(){
		String serverPort = String.valueOf(MySingleton.getServerPortNum());
		
		System.out.println("this.info[1] "  + this.info[1]);
		
		String filepath = FILE_ROOT + serverPort + "/"
				+ this.info[1];
		System.out.println("file path: " + filepath);
		
		try {
			File file = new File(filepath);
				if(file.exists()){
					
					FileInputStream in = new FileInputStream(file);
					int len = (int)file.length();
					System.out.println("Send File Thread; file name = " + file.getName());
					String initialMessage = "HTTP 200 OK\r\n" + 
							"Server: Gnutella\r\n" +
							"Content-type:application/binary \r\n" +
							"Content-length: " + len + "\r\n" +
							"\r\n";
					OutputStream outstream = this.connectionSocket.getOutputStream();
					outstream.write(initialMessage.getBytes());
					System.out.println("http Response is sent");
					byte buf[] = new byte[1024];
					int s = 0;
					int writtenSize = 0;
					while((s = in.read(buf)) != -1){
						outstream.write(buf);
						writtenSize += s;
						double percent = (double)(writtenSize) / (double)(len) * 100;
						if((int)percent % 20 == 0)
							//System.out.println("Written Size: " + (int)percent);
							System.out.println("Writing " + (float)percent +  "% DONE");
					}
					outstream.flush();
					in.close();
					System.out.println("initial message\n" + initialMessage);
				//	System.out.println("response message's length: " + initialMessage.length());
					System.out.println("Data has been sent");
				}else{
					System.out.println("file doesn't exist !");
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
