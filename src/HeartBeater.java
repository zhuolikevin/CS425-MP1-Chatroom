import java.io.PrintStream;
import java.net.Socket;
import java.util.TimerTask;

public class HeartBeater extends TimerTask {
  protected Socket connectionSocket;
  protected PrintStream outgoingStream;
  protected Node thisNode;

  public HeartBeater(Socket connectionSocket, Node thisNode) throws Exception {
    this.thisNode = thisNode;
    this.connectionSocket = connectionSocket;
    this.outgoingStream = new PrintStream(connectionSocket.getOutputStream());
  }

  @Override
  public void run() {
    outgoingStream.println(thisNode.nodeId + "[HB]");
  }
}
