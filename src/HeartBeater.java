import java.io.PrintStream;
import java.net.Socket;
import java.util.TimerTask;

public class HeartBeater extends TimerTask {
  protected Socket connectionSocket;
  protected PrintStream outgoingStream;

  public HeartBeater(Socket connectionSocket) throws Exception {
    this.connectionSocket = connectionSocket;
    this.outgoingStream = new PrintStream(connectionSocket.getOutputStream());
  }

  @Override
  public void run() {
    outgoingStream.println("[HB]");
  }
}
