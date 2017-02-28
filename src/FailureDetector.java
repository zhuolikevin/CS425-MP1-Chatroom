import mputil.*;
import java.net.Socket;
import java.util.TimerTask;

public class FailureDetector extends TimerTask {
  private String nodeId;
  private Socket client;
  private Node thisNode;

  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public FailureDetector(String nodeId, Socket client, Node thisNode) {
    this.nodeId = nodeId;
    this.client = client;
    this.thisNode = thisNode;
  }

  @Override
  public void run() {
    if (thisNode.getReadyFlag()) {
      notifHandler.printNoticeMsg("Fail to receive HB from " + client.getRemoteSocketAddress());

      try {
        thisNode.cancelConnectionWithIp(nodeId);
      } catch (Exception e) {
        notifHandler.printExceptionMsg(e, "Cannot cancel connections with " + client.getRemoteSocketAddress());
      }

    thisNode.multicastMessage(thisNode.nodeId + "[FAIL]" + nodeId);
    }
  }
}
