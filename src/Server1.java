import java.net.ServerSocket;
import java.net.Socket;

public class Server1 {
	public static void main(String[] args) throws Exception{

		int portNumber = 11111;

		if (args.length == 1) {
			portNumber = Integer.parseInt(args[0]);
		} else {
			System.out.println("Running format: java Server1 [port]");
			System.exit(0);
		}

		ServerSocket server = new ServerSocket(portNumber);

		new Thread(new ServerThread0(server)).start();
	}
}
