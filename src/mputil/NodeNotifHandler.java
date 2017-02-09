package mputil;

public class NodeNotifHandler {
  public NodeNotifHandler() {};

  /**
   * Print exception messages
   * @param e         Exception
   * @param errorMsg  User defined error message
   */
  public void printExceptionMsg(Exception e, String errorMsg) {
    System.out.println("[ERROR] " + errorMsg);
    e.printStackTrace();
  }

  /**
   * Print notice messages
   * @param noticeMsg Notice message
   */
  public void printNoticeMsg(String noticeMsg) {
    System.out.print(wrapNotif("[NOTICE] " + noticeMsg));
  }

  /**
   * Wrap a string with "\r" and "\n>>" for better console display
   * @param str  A string to be wrapped
   * @return     Wrapped string
   */
  public String wrapNotif(String str) {
    return "\r" + str + "\n>> ";
  }
}
