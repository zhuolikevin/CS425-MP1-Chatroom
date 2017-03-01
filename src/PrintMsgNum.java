import java.util.TimerTask;

public class PrintMsgNum extends TimerTask{
    Node thisNode;
    public PrintMsgNum (Node thisNode) {this.thisNode = thisNode;}
    public void run () {
        System.out.println(thisNode.numHB);
    }
}
