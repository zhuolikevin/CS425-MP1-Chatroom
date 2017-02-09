package mputil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpTools {
  public static final String RES_PREFIX = "../res/";
  private NodeNotifHandler notifHandler = new NodeNotifHandler();

  public IpTools() {}

  /**
   * Read address list from a file
   * @param filename  File name which stores the addresses
   * @return          An Java ArrayList with a address/port line in each element
   */
  public ArrayList<String> readAddressBook(String filename) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(RES_PREFIX + filename));
      String line = br.readLine();
      ArrayList<String> addressList = new ArrayList<>();
      while (line != null) {
        addressList.add(line);
        line = br.readLine();
      }
      br.close();
      return addressList;
    } catch(Exception e) {
      notifHandler.printExceptionMsg(e, "Cannot read address from " + filename);
      System.exit(0);
      return null;
    }
  }

  /**
   * Parse a "xx.xx.xx.xx:port" string to a string array [xx.xx.xx.xx, port]
   * @param ipPortLine  A string in "xx.xx.xx.xx:port" format
   * @return            A string array as [xx.xx.xx.xx, port]
   */
  public String[] parseIpPort(String ipPortLine) {
    String[] result = new String[2];
    result[0] = ipPortLine.split(":")[0].trim();
    result[1] = ipPortLine.split(":")[1].trim();
    return result;
  }

  /**
   * Check if the input is a valid IP address
   * @param addr  Input IP address, "localhost" is allowed
   * @return      Boolean value for validation
   */
  public boolean isValidIp(String addr) {
    if ("localhost".equals(addr)) { return true; }
    if(addr.length() < 7 || addr.length() > 15 || "".equals(addr)){ return false; }

    String re = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
    Pattern pat = Pattern.compile(re);
    Matcher mat = pat.matcher(addr);
    boolean ipAddress = mat.find();
    return ipAddress;
  }

  /**
   * Check if the address with port is the localhost
   * @param addr      IP address
   * @param samePort  A boolean variable whether the port is the same
   * @return          Boolean value for validation
   */
  public boolean isOwnAddress(String addr, boolean samePort) {
    if (!samePort) { return false; }
    if ("localhost".equals(addr)) { return true; }
    try {
      InetAddress selfAddress = InetAddress.getLocalHost();
      return selfAddress.getHostAddress().equals(addr);
    } catch (Exception e) {
      notifHandler.printExceptionMsg(e, "Cannot read this host IP");
      return false;
    }

  }
}
