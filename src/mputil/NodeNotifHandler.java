package mputil;

public class NodeNotifHandler {
  public NodeNotifHandler() {};
  public void printExceptionMsg(Exception e, String errorMsg) {
    System.out.println("[ERROR] " + errorMsg);
    e.printStackTrace();
  }
}
