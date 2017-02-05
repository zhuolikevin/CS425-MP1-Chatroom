import java.net.ServerSocket;
import java.net.Socket;

public class Server1 {
	public static void main(String[] args) throws Exception{
		ServerSocket server = new ServerSocket(2002);
//		Socket client = null;
//		boolean f = true;
		new Thread(new ServerThread0(server)).start();
//		while (f) {
//			client = server.accept(); 
//			System.out.println("Connection built successfully！");
//	        new Thread(new ServerThread(client)).start();
//		}
		server.close();
	}

}
