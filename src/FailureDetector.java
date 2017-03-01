import mputil.*;
import java.net.Socket;
import java.util.Date;
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
    if (thisNode.getReadyFlag() && thisNode.connectionPool.contains(nodeId)) {
      notifHandler.printNoticeMsg("Fail to receive HB from [" + nodeId + "] at: " + new Date().getTime());

      try {
        thisNode.cancelConnectionWithNodeId(nodeId);
      } catch (Exception e) {
        notifHandler.printExceptionMsg(e, "Cannot cancel connections with [" + nodeId + "]");
      }

//      thisNode.multicastMessage(thisNode.nodeId + "[FAIL]" + nodeId);
    }
  }
}
