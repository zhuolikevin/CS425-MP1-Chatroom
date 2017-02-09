import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import mputil.NodeNotifHandler;

public class Node {
  public static final int DEFAULT_PORT = 10000;
  public static final String TERMINATION_MSG = "bye";

  protected int portNum;
  protected ArrayList<Socket> clientSockList = new ArrayList<>();
  protected ArrayList<PrintStream> outgoingStreamList = new ArrayList<>();
  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public Node() { this.portNum = DEFAULT_PORT; }

  public Node(int port) { this.portNum = port; }

  public void setupServer() {
    try {
      ServerSocket server = new ServerSocket(portNum);
      InetAddress currentIp = InetAddress.getLocalHost();
      System.out.println("Current IP Address: " +
              currentIp.getHostAddress() + ":" +
              String.valueOf(portNum));
      new Thread(new ConnectionListener(server)).start();
    } catch (Exception e) {
      notifHandler.printExceptionMsg(e, "Cannot set up server");
      System.exit(0);
    }
  }

  public boolean initConnections(String ip, int port) {
    try {
      Socket connectionSocket = new Socket(ip, port);
      clientSockList.add(connectionSocket);
      outgoingStreamList.add(new PrintStream(connectionSocket.getOutputStream()));
      return true;
    } catch (Exception e) {
      notifHandler.printExceptionMsg(e, "Cannot connect to node " + ip + ":" + port);
      return false;
    }
  }

  public void multicastMessage(String msg) {
    for (int i = 0; i < clientSockList.size(); i++) {
      outgoingStreamList.get(i).println(msg);
    }
  }

  public void closeAllConnections() {
    for (int i = 0; i < clientSockList.size(); i++) {
      if (clientSockList.get(i) != null) {
        try {
          clientSockList.get(i).close();
        } catch (Exception e) {
          notifHandler.printExceptionMsg(e, "Fail to close the connections");
        }
      }
    }
  }
}
