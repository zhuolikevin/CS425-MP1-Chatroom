import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class NodeLauncher {
  private static int DEFAULT_PORT = 11111;

  public static void main(String[] args) throws Exception {

    int portNumber = DEFAULT_PORT;
    if (args.length == 1) {
      portNumber = Integer.parseInt(args[0]);
    } else {
      System.out.println("Run in format: java Server1 [port]");
      System.exit(0);
    }

    // Play as a server, which waits for connection requests
    ServerSocket server = new ServerSocket(portNumber);
    InetAddress currentIp = InetAddress.getLocalHost();
    System.out.println("Current IP Address: " + currentIp.getHostAddress() + ":" + String.valueOf(portNumber));
    new Thread(new ConnectionListener(server)).start();

    // Enter other nodes' IPs and ports
    // TODO: Rewrite the codes and take more than one pair of Node info
    Scanner scan = new Scanner(System.in);
    System.out.print("Enter a Host IP >> ");
    String hostIp = scan.nextLine();
    System.out.print("Enter a Host port >> ");
    int hostPort = scan.nextInt();

    Socket client = new Socket(hostIp, hostPort);
    client.setSoTimeout(10000);
    // Read from keyboard
    BufferedReader keyboradInput = new BufferedReader(new InputStreamReader(System.in));
    // Send message through socket
    PrintStream outputStream = new PrintStream(client.getOutputStream());
    boolean flag = true;
    while (flag) {
      System.out.print(">> ");
      String str = keyboradInput.readLine();
      outputStream.println(str);
      if ("bye".equals(str)) {
        flag = false;
      }
    }
    keyboradInput.close();
    if (client != null) {
      client.close();
    }
  }
}
