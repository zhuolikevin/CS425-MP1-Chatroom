import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageReceiver implements Runnable {
	private Socket client = null;

	public MessageReceiver(Socket client){
		this.client = client;
	}

	public void run() {
		try {
      // Read messages which are sent by other nodes
			BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
			boolean flag = true;
			while (flag) {
				String str = buf.readLine();
				if ("bye".equals(str)) {
					flag = false;
				} else {
					System.out.println(str);
				}
			}
			client.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
