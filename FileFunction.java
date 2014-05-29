import java.io.*;
import java.io.ObjectOutputStream.PutField;
import java.util.ArrayList;
public class FileFunction {
	final String FILE_ROOT = "/Users/yoshikawamuga/Documents/Gnutella/";
	public int getNumberOfFiles(String port){
		String dirRoot = FILE_ROOT + port;
	//	System.out.println("DIR ROOT = " + dirRoot);
		File dir = new File(dirRoot);
		File[] files = dir.listFiles();
		Long numFile = new Long(files.length);
		return numFile.intValue();
		
	}
	
	public int getSizeOfFileShared(String port){
		String dirRoot = FILE_ROOT + port;
//		System.out.println("DIR ROOT = " + dirRoot);

		File dir = new File(dirRoot);
		File[] files = dir.listFiles();
		Long size = new Long(0);
		for(int i=0; i<files.length; i++){
		    size += files[i].length() / 1000;
		}
		return size.intValue();
	}
	public int getNumhit(String word, String port){
		
		//System.out.println("---- Get Num Hit ----");
		int numHit = 0;
		File dir = new File(FILE_ROOT + port);
		File[] files = dir.listFiles();
		for(int  i = 0; i < files.length; i++){
			//System.out.println("file name: " + files[i].getName());
			//System.out.println("word: " + word);
			//System.out.println("index " + files[i].getName().indexOf(word));
			if(files[i].getName().contains(word)){
				numHit++;
			}
		}
	//	System.out.println("File Path: " + FILE_ROOT + port);
		//System.out.println("Number of Hits: " + numHit);
	//	System.out.println("---- End of Get Num Hit ----");

		return numHit;
	}
	
	public ArrayList<ResultSet> getResultSet(String word, String port){
		ResultSet resultset;
		ArrayList<ResultSet> rset = new ArrayList<ResultSet>();
		File dir = new File(FILE_ROOT + port);
		File[] files = dir.listFiles();
		
		int j = 0;
		for(int  i = 0; i < files.length; i++){
			if(files[i].getName().contains(word)){

				resultset = new ResultSet(files[i].getName()); 
				resultset.index = resultset.putInt(i);
				resultset.size = resultset.putInt((int) (files[i].length() / 1000));
				rset.add(resultset);
				j++;
			}
		}
		if(j == 0){
			return rset;
		}
	
		return rset;
	}
	
}
