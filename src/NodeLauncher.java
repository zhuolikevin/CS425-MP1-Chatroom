import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Queue;
import java.util.Scanner;
import java.util.PriorityQueue;
import java.io.IOException;
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


   boolean keepChatting = true;
   int proposed_priority;

      while (keepChatting) {
        System.out.print(">> ");
        String str = null;
    try {
      str =  keyboardInput.readLine();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
        if ("".equals(str)) { continue; }

        else if (Node.TERMINATION_MSG.equals(str)) {
            thisNode.closeAllConnections();
            keepChatting = false;
          }


        //ISIS Send

        else {

         str = "[M]" + str ;
         Message co = null;

         // calculation of proposed priority

         if (thisNode.sendList.size() == 0) {
           proposed_priority = thisNode.total_priority + 1;
         }
         else {
           int count = 0;
           Queue<Message> rec = new PriorityQueue(thisNode.sendList);
           while (count < thisNode.sendList.size()) {
             co = rec.poll();
             count += 1;
           }
           proposed_priority = co.priority[0] + 1;
         }
         thisNode.total_priority = proposed_priority;

        // To Do: How to avoid concurrent modification of thisNode.total_priority

         Message m = new Message(str);
         m.original_priority[0] = proposed_priority;
         m.original_priority[1] = thisNode.portNum;
         m.priority[0] = proposed_priority;
         m.priority[1] = thisNode.portNum;
         thisNode.sendList.add(m);
         Queue<Message> temp = new PriorityQueue<Message>(1, MyComparator2);
         temp.add(m);
         thisNode.msgList.add(temp);

         str = String.format(str + " " + "[OP]%d.%d", proposed_priority, thisNode.portNum);
         thisNode.multicastMessage(str);
       }
      }

      try {
      keyboardInput.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
      System.exit(0);
        }

  public static Comparator<Message> MyComparator2 = new Comparator<Message>() {
    public int compare (Message msg1, Message msg2) {
      if (msg1.priority[0] < msg2.priority[0])   // the smaller, the latter
        return 1;
      else if (msg1.priority[0] == msg2.priority[0])
      {
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
