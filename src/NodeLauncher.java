import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
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
    thisNode.setupServer();

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

    /* Message input and multicast */
    boolean keepChatting = true;
    while (keepChatting) {
      System.out.print(">> ");
      String str = keyboardInput.readLine();
      if ("".equals(str)) { continue; }
      thisNode.multicastMessage(str);
      if (Node.TERMINATION_MSG.equals(str)) {
        thisNode.closeAllConnections();
        keepChatting = false;
      }
    }
    keyboardInput.close();
    System.exit(0);
  }
}
