import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import mputil.NodeNotifHandler;
import java.util.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Node {
  public static final int DEFAULT_PORT = 10000;
  public static final String TERMINATION_MSG = "bye";
  //revise the TotoalNodeNum to 8
  public static final int TotalNodeNum = 2;

  public int total_priority = 0;
//  public int proposed_priority = 0;
  //To Do: change to priority queue(heap) to reduce complexity

//  public List<Message> sendList = new ArrayList<Message>();
//  public List<List<Message>> msgList = new ArrayList<List<Message>>();
  public PriorityQueue<Message> sendList = new PriorityQueue<Message> (1, MyComparator1);
  public List<Queue<Message>> msgList = new ArrayList<Queue<Message>>();

  protected int portNum;
  protected boolean readyFlag = false;
  protected ArrayList<Socket> clientSockList = new ArrayList<>();
  protected HashMap<String, Socket> clientSockMap = new HashMap<>();
  protected HashMap<String, Socket> serverSockMap = new HashMap<>();
  protected ArrayList<PrintStream> outgoingStreamList = new ArrayList<>();

  protected HashMap<String, HeartBeater> heartBeaterTaskMap = new HashMap<>();

  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public Node() { this.portNum = DEFAULT_PORT;
  }

  public Node(int port) { this.portNum = port; }

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
      clientSockMap.put(ip, connectionSocket);
      outgoingStreamList.add(new PrintStream(connectionSocket.getOutputStream()));

      // Begin heartbeat to the node once connected
      heartBeaterTaskMap.put(ip, new HeartBeater(connectionSocket));
      Timer timer = new Timer(true);
      timer.schedule(heartBeaterTaskMap.get(ip), 0, 100);

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

  public HeartBeater getHeartBeater(String ip) { return this.heartBeaterTaskMap.get(ip); }

  public void cancelConnectionWithIp(String ip) throws Exception {

    HeartBeater curHearBeat = getHeartBeater(ip);
    if (curHearBeat != null) { curHearBeat.cancel(); }

    Socket outgoingSocket = getClientSocket(ip);
    removeSocketFromClientSockMap(ip);
    if (outgoingSocket != null) {
      int sockIndex = clientSockList.indexOf(outgoingSocket);
      removeSocketFromClientSockList(outgoingSocket);
      if (sockIndex != -1) { outgoingStreamList.remove(sockIndex); }
      outgoingSocket.close();
    }

    Socket incomingSocket = getServerSocket(ip);
    removeSocketFromServerSockMap(ip);
    if (incomingSocket != null) { incomingSocket.close(); }
  }

  public Socket getClientSocket(String ip) { return this.clientSockMap.get(ip); }
  public void removeSocketFromClientSockList(Socket removedSock) { this.clientSockList.remove(removedSock); }
  public void removeSocketFromClientSockMap(String ip) { this.clientSockMap.remove(ip); }

  public Socket getServerSocket(String ip) { return this.serverSockMap.get(ip); }
  public void putSocketToServerSockMap(String ip, Socket serverSock) { this.serverSockMap.put(ip, serverSock); }
  public void removeSocketFromServerSockMap(String ip) { this.serverSockMap.remove(ip); }

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
