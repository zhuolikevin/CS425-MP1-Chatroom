import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeLauncher {
  
  public static boolean isIP(String addr){
	  if(addr.length() < 7 || addr.length() > 15 || "".equals(addr)){
		  return false;
	  }
	  String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"; 
	  Pattern pat = Pattern.compile(rexp);
	  Matcher mat = pat.matcher(addr);
	  boolean ipAddress = mat.find();
	  return ipAddress;
  }

  public static void main(String[] args) throws Exception {

    int portNum;
    Node thisNode;

    if (args.length == 1) {
      portNum = Integer.parseInt(args[0]);
      thisNode = new Node(portNum);
    } else {
      System.out.println("[NOTICE] No port specified. Use default value.");
      thisNode = new Node();
    }

    thisNode.setupServer();

    // Enter other nodes' IPs and ports
    BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("How many nodes you wish to connect?\n>> ");
    int connectionNum = Integer.parseInt(keyboardInput.readLine());

    Scanner scanner;
    String inputIp;
    int inputPort;
    int i = 0;

    while (i < connectionNum) {
      scanner = new Scanner(System.in);
      System.out.print("Enter a node IP\n>> ");
      inputIp = scanner.nextLine();
      if (isIP(inputIp) == false) {
    	  continue;
      }
      System.out.print("Enter a node port\n>> ");
      try {
        inputPort = scanner.nextInt();
      } catch (Exception e) {
        System.out.println("[ERROR] Invalid port number. Please double check and enter the address again!");
        continue;
      }
//      if (inputPort == portNum) {
//    	  System.out.println("[ERROR] Invalid port number. Please double check and enter the address again!");
//    	  continue;
//      }
      if (thisNode.initConnections(inputIp, inputPort) == false) {
        continue;
      }
      i++;
    }

    boolean flag = true;
    while (flag) {
      System.out.print(">> ");
      String str = keyboardInput.readLine();
      thisNode.multicastMessage(str);
      if ("bye".equals(str)) {
        thisNode.closeAllConnections();
        flag = false;
      }
    }
    keyboardInput.close();
    System.exit(0);
  }
}
