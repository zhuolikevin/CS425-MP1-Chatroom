import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionListener implements Runnable {
	private Socket connectedClient = null;
	private ServerSocket server;
	 
	public ConnectionListener(ServerSocket server) { this.server = server; }

	public void run() {
		while (true) {
			try {
				connectedClient = server.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[NOTICE] Connected with " + connectedClient.getRemoteSocketAddress());
			new Thread(new MessageReceiver(connectedClient)).start();
		}
	}
}
