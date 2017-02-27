import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Timer;

import mputil.NodeNotifHandler;

public class MessageReceiver implements Runnable {
  private Socket client;
  private Node thisNode;

  private Timer failureDetectorTimer = new Timer(true);
  private FailureDetector failureDetector;

  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public MessageReceiver(Socket client, Node thisNode){
    this.client = client;
    this.thisNode = thisNode;

    // For each message receiver, start a timer task for heartbeat
    this.failureDetector = new FailureDetector(client, thisNode);
    this.failureDetectorTimer.schedule(failureDetector, 5200);  // Setup delay for 5 seconds
  }

  /**
   * Read messages which are sent by other nodes
   */
  public void run() {
    String remoteAddress = client.getRemoteSocketAddress().toString().substring(1);
    try {
      BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
      boolean keepConnection = true;
      while (keepConnection) {
        String str = buf.readLine();
        if (Node.TERMINATION_MSG.equals(str) || str == null) {
          keepConnection = false;
          notifHandler.printNoticeMsg("Lost connection with " + remoteAddress);
        } else if ("[HB]".equals(str)) {
          if (!thisNode.getReadyFlag()) { continue; }

          // Reset failure detection timer
          failureDetectorTimer.cancel();
          failureDetectorTimer = new Timer(true);
          failureDetector.cancel();
          failureDetector = new FailureDetector(client, thisNode);
          failureDetectorTimer.schedule(failureDetector, 200);
        } else if (str.length() > 6 && "[FAIL]".equals(str.substring(0, 6))) {
          String ip = str.substring(6);
          thisNode.cancelConnectionWithIp(ip);
        } else {
          System.out.print(notifHandler.wrapNotif("[" + remoteAddress + "] " + str));
        }
      }
      client.close();
    } catch (SocketException e) {
      notifHandler.printNoticeMsg("Lost connection with " + remoteAddress);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
