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
      boolean keepConnection = true;
      while (keepConnection) {
        String str = buf.readLine();
        if (Node.TERMINATION_MSG.equals(str) || str == null) {
          keepConnection = false;
          System.out.print("\r[NOTICE] Lost connection with " + client.getRemoteSocketAddress() + "\n>> ");
        } else {
          System.out.print("\r["+ client.getRemoteSocketAddress() + "] " + str + "\n>> ");
        }
      }
      client.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
