import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;

public class ServerThread0 implements Runnable{
	private Socket client = null;
	private boolean f = true;
	private ServerSocket server;
	 
	public ServerThread0(ServerSocket server){
		this.server = server;
	}
	public void run(){
		while(f){
			try {
				client = server.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Connection built successfully!");
			new Thread(new ServerThread(client)).start();
		}
	}
}
//	Socket client = null;
//	boolean f = true;
//	while (f) {
//		client = server.accept(); 
//		System.out.println("Connection built successfully！");
//        new Thread(new ServerThread(client)).start();
//	}

