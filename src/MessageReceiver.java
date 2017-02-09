import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import mputil.NodeNotifHandler;

public class MessageReceiver implements Runnable {
  private Socket client = null;
  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public MessageReceiver(Socket client){ this.client = client; }

  /**
   * Read messages which are sent by other nodes
   */
  public void run() {
    try {
      BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
      boolean keepConnection = true;
      while (keepConnection) {
        String str = buf.readLine();
        String remoteAddress = client.getRemoteSocketAddress().toString().substring(1);
        if (Node.TERMINATION_MSG.equals(str) || str == null) {
          keepConnection = false;
          notifHandler.printNoticeMsg("Lost connection with " + remoteAddress);
        } else {
          System.out.print(notifHandler.wrapNotif("[" + remoteAddress + "] " + str));
        }
      }
      client.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
