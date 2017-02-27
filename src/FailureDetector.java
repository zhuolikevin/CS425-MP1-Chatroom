import mputil.*;
import java.net.Socket;
import java.util.TimerTask;

public class FailureDetector extends TimerTask {
  private Socket client;
  private Node thisNode;

  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public FailureDetector(Socket connectedClient, Node thisNode) {
    this.client = connectedClient;
    this.thisNode = thisNode;
  }

  @Override
  public void run() {
    if (thisNode.getReadyFlag()) {
      notifHandler.printNoticeMsg("Fail to receive HB from " + client.getRemoteSocketAddress());

      IpTools tool = new IpTools();
      String ip = tool.parseIpPort(client.getRemoteSocketAddress().toString().substring(1))[0];

      try {
        thisNode.cancelConnectionWithIp(ip);
      } catch (Exception e) {
        notifHandler.printExceptionMsg(e, "Cannot cancel connections with " + ip);
      }

    thisNode.multicastMessage("[FAIL]" + ip);
    }
  }
}
