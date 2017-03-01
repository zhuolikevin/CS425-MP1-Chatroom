import java.util.TimerTask;

public class PrintMsgNum extends TimerTask {
    Node thisNode;
    public PrintMsgNum(Node thisNode) { this.thisNode = thisNode; }

    @Override
    public void run() {
        System.out.println("[EVALUATION]: " + thisNode.numHB);
    }
}
