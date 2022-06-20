import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Main {

	
	
	
	public static void main(String[] args) {
		byte b = -6;
		
		System.out.println(b);
		
		Socket s;
		try {
			s = new Socket((String) null, 51470);
			OutputStream os = s.getOutputStream();
			
			os.write(0xFF);
			os.write(b);
			os.write(0xFF);
			
			
			os.write(0xFF);
			os.write(167);
			os.write(0xFF);
			
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}

}
