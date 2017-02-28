import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import mputil.NodeNotifHandler;

public class Node {
  public static final int DEFAULT_PORT = 10000;
  public static final String TERMINATION_MSG = "bye";

  // ISIS Total Ordering
  public int totalNodeNum;
  public int total_priority = 0;
  public PriorityQueue<Message> sendList = new PriorityQueue<> (1, MyComparator1);
  public List<Queue<Message>> msgList = new ArrayList<>();

  // Connection Status
  protected int portNum;
  protected String nodeId;
  
  protected boolean readyFlag = false;
  protected ArrayList<Socket> clientSockList = new ArrayList<>();
  protected HashMap<String, Socket> clientSockMap = new HashMap<>();
  protected HashMap<String, Socket> serverSockMap = new HashMap<>();
  protected ArrayList<PrintStream> outgoingStreamList = new ArrayList<>();

  // HeartbeaterTasks
  protected HashMap<String, HeartBeater> heartBeaterTaskMap = new HashMap<>();

  // Notification Utils
  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public Node() {
    this.portNum = DEFAULT_PORT;
    this.nodeId = String.valueOf(portNum);
  }
  public Node(int port) {
    this.portNum = port;
    this.nodeId = String.valueOf(portNum);
  }

  public void setupServer(Node thisNode) {
    try {
      ServerSocket server = new ServerSocket(portNum);
      InetAddress currentIp = InetAddress.getLocalHost();
      System.out.println("Current IP Address: " +
          currentIp.getHostAddress() + ":" +
          String.valueOf(portNum));
      new Thread(new ConnectionListener(server, thisNode)).start();
    } catch (Exception e) {
      notifHandler.printExceptionMsg(e, "Cannot set up server");
      System.exit(0);
    }
  }

  public boolean initConnections(String ip, int port) {
    try {
      Socket connectionSocket = new Socket(ip, port);
      clientSockList.add(connectionSocket);
      clientSockMap.put(String.valueOf(port), connectionSocket);
      outgoingStreamList.add(new PrintStream(connectionSocket.getOutputStream()));

      // Begin heartbeat to the node once connected
      heartBeaterTaskMap.put(String.valueOf(port), new HeartBeater(connectionSocket, this));
      Timer timer = new Timer(true);
      timer.schedule(heartBeaterTaskMap.get(String.valueOf(port)), 0, 100);

      return true;
    } catch (Exception e) {
      notifHandler.printExceptionMsg(e, "Cannot connect to node " + ip + ":" + port);
      return false;
    }
  }

  public void setReadyFlag() { readyFlag = true; }
  public boolean getReadyFlag() { return readyFlag; }

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

  public HeartBeater getHeartBeater(String nodeId) { return this.heartBeaterTaskMap.get(nodeId); }

  public void cancelConnectionWithIp(String nodeId) throws Exception {

    HeartBeater curHearBeat = getHeartBeater(nodeId);
    if (curHearBeat != null) { curHearBeat.cancel(); }

    Socket outgoingSocket = getClientSocket(nodeId);
    removeSocketFromClientSockMap(nodeId);
    if (outgoingSocket != null) {
      int sockIndex = clientSockList.indexOf(outgoingSocket);
      removeSocketFromClientSockList(outgoingSocket);
      if (sockIndex != -1) { outgoingStreamList.remove(sockIndex); }
      outgoingSocket.close();
    }

    Socket incomingSocket = getServerSocket(nodeId);
    removeSocketFromServerSockMap(nodeId);
    if (incomingSocket != null) { incomingSocket.close(); }
  }

  public Socket getClientSocket(String nodeId) { return this.clientSockMap.get(nodeId); }
  public void removeSocketFromClientSockList(Socket removedSock) { this.clientSockList.remove(removedSock); }
  public void removeSocketFromClientSockMap(String nodeId) { this.clientSockMap.remove(nodeId); }

  public Socket getServerSocket(String nodeId) { return this.serverSockMap.get(nodeId); }
  public void putSocketToServerSockMap(String nodeId, Socket serverSock) { this.serverSockMap.put(nodeId, serverSock); }
  public void removeSocketFromServerSockMap(String nodeId) { this.serverSockMap.remove(nodeId); }

  public static Comparator<Message> MyComparator1 = new Comparator<Message>() {
    public int compare (Message msg1, Message msg2) {
      if (msg1.priority[0] > msg2.priority[0])     //the bigger, the latter
        return 1;
      else if (msg1.priority[0] == msg2.priority[0])
      {
        if(msg1.priority[1] > msg2.priority[1])
          return 1;
        else if(msg1.priority[1] == msg2.priority[1])
          return 0;
        else return -1;
      }
      else return -1;
    }
  };
}
