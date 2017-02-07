import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeLauncher {
  private static int DEFAULT_PORT = 11111;
  
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

    int portNumber = DEFAULT_PORT;
    if (args.length == 1) {
      portNumber = Integer.parseInt(args[0]);
    } else {
      System.out.println("Run in format: java NodeLauncher [port]");
      System.exit(0);
    }

    // Play as a server, which waits for connection requests
    ServerSocket server = new ServerSocket(portNumber);
    InetAddress currentIp = InetAddress.getLocalHost();
    System.out.println("Current IP Address: " + currentIp.getHostAddress() + ":" + String.valueOf(portNumber));
    new Thread(new ConnectionListener(server)).start();

    // Enter other nodes' IPs and ports
    int clientNumber = 4;
    Scanner scanner;
    String[] hostIpArray = new String[clientNumber];
    Socket[] clientArray = new Socket[clientNumber];
    int[] hostPortArray = new int[clientNumber];
    int i = 0;

    while (i < clientNumber) {
      scanner = new Scanner(System.in);
      System.out.print("Enter a node IP\n>> ");
      hostIpArray[i] = scanner.nextLine();
      if (isIP(hostIpArray[i]) == false) {
    	  continue;
      }
      System.out.print("Enter a node port\n>> ");
      try {
        hostPortArray[i] = scanner.nextInt();
      } catch (Exception e) {
        System.out.println("[ERROR] Invalid port number. Please double check and enter the address again!");
        continue;
      }
      if (hostPortArray[i] == portNumber) {
    	  System.out.println("[ERROR] Invalid port number. Please double check and enter the address again!");
    	  continue;
      }
      try {
        clientArray[i] = new Socket(hostIpArray[i], hostPortArray[i]);
      } catch (Exception e) {
        System.out.println("[ERROR] Cannot connect to this node. Please double check and enter the address again!");
        continue;
      }
      clientArray[i].setSoTimeout(10000);
      i++;
    }
    
    // Read from keyboard
    BufferedReader keyboradInput = new BufferedReader(new InputStreamReader(System.in));
    // Send message through socket
    PrintStream[] outputStreamArray = new PrintStream[clientNumber];
    for(i = 0; i < clientNumber; i++){
    outputStreamArray[i] = new PrintStream(clientArray[i].getOutputStream());}
    boolean flag = true;
    while (flag) {
      System.out.print(">> ");
      String str = keyboradInput.readLine();
      for (i = 0; i < clientNumber; i++){
      outputStreamArray[i].println(str);}
      if ("bye".equals(str)) {
        flag = false;
      }
    }
    keyboradInput.close();
    for (i = 0; i < clientNumber; i++){
      if (clientArray[i] != null) {
        clientArray[i].close();
      }
    }
    System.exit(0);
  }
}
