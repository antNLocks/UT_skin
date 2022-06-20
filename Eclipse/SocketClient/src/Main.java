import java.io.DataOutputStream;
import java.net.Socket;

public class Main {

	public static void main(String[] args) {
		try{   
			Socket s=new Socket((String) null, 51470);  
			DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
			
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);
			dout.write(0x01);

			
			dout.write(0xFF);
			
			Thread.sleep(3000);

			
			dout.write(0x01);
			dout.write(0x02);
			dout.write(0x03);
			dout.write(0x04);
			
			dout.write(0xFF);
			dout.write(0xFF);

			dout.write(0x02);
			dout.write(0x03);
			dout.write(0x04);
			dout.write(0x00);
			
			dout.write(0xFF);

			
			dout.write(0x01);
			dout.write(0x02);
			dout.write(0x03);
			dout.write(0x04);
			
			dout.write(0xFF);
			
			
			dout.flush();  
			dout.close();  
			s.close();  
		}catch(Exception e){e.printStackTrace();}  
	}

}
