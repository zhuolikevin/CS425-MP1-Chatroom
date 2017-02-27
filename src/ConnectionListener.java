import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

import mputil.*;

public class ConnectionListener implements Runnable {
  private Socket connectedClient = null;
  private ServerSocket server;
  private Node thisNode;

  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public ConnectionListener(ServerSocket server, Node thisNode) {
    this.server = server;
    this.thisNode = thisNode;
  }

  /**
   * Listening for connections from other nodes
   */
  public void run() {
    while (true) {
      try {
        connectedClient = server.accept();
      } catch (IOException e) {
        e.printStackTrace();
      }
      notifHandler.printNoticeMsg("Connected with " + connectedClient.getRemoteSocketAddress());

      IpTools tool = new IpTools();
      String ip = tool.parseIpPort(connectedClient.getRemoteSocketAddress().toString().substring(1))[0];
      thisNode.putSocketToServerSockMap(ip, connectedClient);

      new Thread(new MessageReceiver(connectedClient, thisNode)).start();
    }
  }
}
