import java.nio.ByteBuffer;


public class ResultSet {

	byte[] index, size, name;
	String nameStr;
	public ResultSet(String filename){
		this.nameStr = filename;
		byte[] b = filename.getBytes();
		addDoubleNull(b);
	}
	public String getNameStr(){
		return new String(this.name);
	}
	public int get4Bytes(byte[] b){
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
	public void showMyself(){
		System.out.print("--File Index: ");
		showFourBytes(this.index);
		System.out.print("--File Size(KB): ");
		showFourBytes(this.size);
		System.out.println("--File Name: " + new String(this.name));
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
	public byte[] mergeMyself(){
		int indexSize = this.index.length;
		int sizeSize = this.size.length;
		int nameSize = this.name.length;
		byte[] mergeBytes = new byte[indexSize + sizeSize + nameSize];
		for(int i = 0; i < indexSize; i++){
			mergeBytes[i] = this.index[i];
		}
		for(int i = 0; i < sizeSize; i++){
			mergeBytes[i + indexSize] = this.size[i];
		}
		for(int i = 0; i < nameSize; i++){
			mergeBytes[i + indexSize + sizeSize] = this.name[i];
		}
		return mergeBytes;
		
	}
	public int getOwnSize(){//return this result set't total size(bytes)
		return this.index.length + this.size.length + this.name.length;
	}
	public void addDoubleNull(byte[] b){//add double null to file name field
		int bSize = b.length;
		this.name = new byte[bSize + 2];
		for(int i = 0; i < this.name.length; i++){
			if(i < bSize){
				this.name[i] = b[i];
			}else{
				this.name[i] = (byte)0x00;
			}
		}
		
	}
	public byte[] putInt(int n){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(n);
		return bb.array();
		
	}
	public byte[] putShort(short n){
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort(n);
		return bb.array();
		
	}
}
