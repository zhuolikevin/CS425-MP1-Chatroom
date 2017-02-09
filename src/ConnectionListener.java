import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import mputil.NodeNotifHandler;

public class ConnectionListener implements Runnable {
  private Socket connectedClient = null;
  private ServerSocket server;
  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public ConnectionListener(ServerSocket server) { this.server = server; }

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
      new Thread(new MessageReceiver(connectedClient)).start();
    }
  }
}
