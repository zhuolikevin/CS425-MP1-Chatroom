import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

import mputil.*;

public class NodeLauncher {
  private static NodeNotifHandler notifHandler = new NodeNotifHandler();

  public static void main(String[] args) throws Exception {
    int portNum;
    Node thisNode;
    boolean readFromFile = false;

    if (args.length == 2) {
      readFromFile = true;
      portNum = Integer.parseInt(args[0]);
      thisNode = new Node(portNum);
    } else if (args.length == 1) {
      portNum = Integer.parseInt(args[0]);
      thisNode = new Node(portNum);
    } else {
      System.out.println("[NOTICE] No port specified. Use default value.");
      thisNode = new Node();
    }

    /* Init server listening */
    thisNode.setupServer(thisNode);

    /* Setup connection with other nodes */
    String inputIp;
    int inputPort;
    IpTools tool = new IpTools();
    BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(System.in));
    if (readFromFile) {
      boolean readyForConnection = false;
      while (!readyForConnection) {
        System.out.print("Ready for connection? (y/n)\n>> ");
        readyForConnection = "y".equals(keyboardInput.readLine());
      }

      thisNode.setReadyFlag();

      ArrayList<String> addressList = tool.readAddressBook(args[1]);

      thisNode.totalNodeNum = addressList.size();

      for (int i = 0; i < addressList.size(); i++) {
        inputIp = tool.parseIpPort(addressList.get(i))[0];
        if (!tool.isValidIp(inputIp)) {
          notifHandler.printExceptionMsg(new IllegalArgumentException(), "Invalid IP address");
          System.exit(0);
        }
        try {
          inputPort = Integer.parseInt(tool.parseIpPort(addressList.get(i))[1]);
          if (tool.isOwnAddress(inputIp, inputPort == thisNode.portNum)) {
            continue;
          }
          if (!thisNode.initConnections(inputIp, inputPort)) { System.exit(0); }
        } catch (Exception e) {
          notifHandler.printExceptionMsg(e, "Invalid port number");
          System.exit(0);
        }
      }
    } else {
      System.out.print("How many nodes you wish to connect?\n>> ");
      int connectionNum = Integer.parseInt(keyboardInput.readLine());
      Scanner scanner;
      int i = 0;

      thisNode.totalNodeNum = connectionNum + 1;

      while (i < connectionNum) {
        scanner = new Scanner(System.in);
        System.out.print("Enter a node address in [xx.xx.xx.xx:port] format\n>> ");
        String inputIpPortLine = scanner.nextLine();
        inputIp = tool.parseIpPort(inputIpPortLine)[0];
        if (!tool.isValidIp(inputIp)) {
          notifHandler.printExceptionMsg(new IllegalArgumentException(), "Invalid IP address, please re-enter");
          continue;
        }
        try {
          inputPort = Integer.parseInt(tool.parseIpPort(inputIpPortLine)[1]);
        } catch (Exception e) {
          notifHandler.printExceptionMsg(e, "Invalid port number, please re-enter");
          continue;
        }
        if (tool.isOwnAddress(inputIp, inputPort == thisNode.portNum)) {
          notifHandler.printExceptionMsg(new IllegalArgumentException(), "Invalid self-connection");
          continue;
        }
        if (!thisNode.initConnections(inputIp, inputPort)) { continue; }
        i++;
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        System.out.println("Shutdown Timestamp:" + new Date().getTime());
      }
    });

    /* Keyboard Input */
    boolean keepChatting = true;
    int proposedPriority;
    String str = null;
    String message = null;

    while (keepChatting) {
      System.out.print(">> ");

      try { str =  keyboardInput.readLine(); }
      catch (IOException e) { e.printStackTrace(); }

      if ("".equals(str)) { continue; }
      else if (Node.TERMINATION_MSG.equals(str)) {
        thisNode.closeAllConnections();
        keepChatting = false;
      } else {

         str = thisNode.nodeId + "[M]" + str ;
         Message co = null;

         // calculation of proposed priority

         if (thisNode.sendList.size() == 0) {
           proposedPriority = thisNode.totalPriority + 1;
         } else {
           int count = 0;
           Queue<Message> rec = new PriorityQueue(thisNode.sendList);
           while (count < thisNode.sendList.size()) {
             co = rec.poll();
             count += 1;
           }
           proposedPriority = co.priority[0] + 1;
         }
         thisNode.totalPriority = proposedPriority;

        // To Do: How to avoid concurrent modification of thisNode.totalPriority
         message = str.substring(8);
         Message m = new Message(message);
         m.original_priority[0] = proposedPriority;
         m.original_priority[1] = thisNode.portNum;
         m.priority[0] = proposedPriority;
         m.priority[1] = thisNode.portNum;
         thisNode.sendList.add(m);
         Queue<Message> temp = new PriorityQueue<Message>(1, MyComparator2);
         temp.add(m);
         thisNode.msgList.add(temp);

         str = String.format(str + "[OP]%d.%d", proposedPriority, thisNode.portNum);
         thisNode.multicastMessage(str);
      }
    }

    try { keyboardInput.close(); }
    catch (IOException e) { e.printStackTrace(); }
    System.exit(0);
  }

  public static Comparator<Message> MyComparator2 = new Comparator<Message>() {
    public int compare (Message msg1, Message msg2) {
      if (msg1.priority[0] < msg2.priority[0]) { return 1; }
      else if (msg1.priority[0] == msg2.priority[0]) {
        if(msg1.priority[1] < msg2.priority[1])
          return 1;
        else if(msg1.priority[1] == msg2.priority[1])
          return 0;
        else return -1;
      }
      else return -1;
    }
  };
}
