import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import mputil.IpTools;
import mputil.NodeNotifHandler;

public class MessageReceiver implements Runnable {
  private Socket client;

  private Timer failureDetectorTimer = new Timer(true);
  private FailureDetector failureDetector;

  private NodeNotifHandler notifHandler = new NodeNotifHandler();
  private Node thisNode;

  public MessageReceiver(Socket client, Node thisNode){
    this.client = client;
    this.thisNode = thisNode;

    // For each message receiver, start a timer task for heartbeat
    this.failureDetector = new FailureDetector(client, thisNode);
    this.failureDetectorTimer.schedule(failureDetector, 5200);  // Setup delay for 5 seconds
  }

  /**
   * Read messages which are sent by other nodes
   */
  public void run() {
    String remoteAddress = client.getRemoteSocketAddress().toString().substring(1);
    String messageSender = null;
    Socket clientSo = null;
    int index = 0;
    PrintStream ps;
    
    try {
        
      BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));

      boolean keepConnection = true;
      int proposed_priority;
      int[] id_original_priority = new int[2];

      while (keepConnection) {
        String str = buf.readLine();
        messageSender = str.substring(0, 5);
        str = str.substring(5);
        
        if (Node.TERMINATION_MSG.equals(str) || str == null) {
          keepConnection = false;
          notifHandler.printNoticeMsg("Lost connection with " + remoteAddress);
        } else if ("[HB]".equals(str)) {
          if (!thisNode.getReadyFlag()) { continue; }

          // Reset failure detection timer
          failureDetectorTimer.cancel();
          failureDetectorTimer = new Timer(true);
          failureDetector.cancel();
          failureDetector = new FailureDetector(client, thisNode);
          failureDetectorTimer.schedule(failureDetector, 200);
        } else if (str.length() > 6 && "[FAIL]".equals(str.substring(0, 6))) {
          String ip = str.substring(6);
          thisNode.cancelConnectionWithIp(ip);
        } else if (str.substring(0, 3).equals("[M]")) {
          
          clientSo = thisNode.clientSockMap.get(messageSender);
          index = thisNode.clientSockList.indexOf(clientSo);
          ps = thisNode.outgoingStreamList.get(index);
          Message co = null;
           
          // calculation of proposed priority

          if (thisNode.sendList.size() == 0) {
            proposed_priority = thisNode.total_priority + 1;
          } else {
            int count = 0;
            Queue<Message> rec = new PriorityQueue(thisNode.sendList);
            while (count < rec.size()) {
              co = rec.poll();
              count += 1;
            }
            proposed_priority = co.priority[0] + 1;
          }
          thisNode.total_priority = proposed_priority;
         
          // To Do: How to avoid concurrent modification of thisNode.total_priority
          
          Message m = new Message(str);
          m.original_priority[0] = Integer.parseInt(str.split("\\[OP]")[1].split("\\.")[0]);
          m.original_priority[1] = Integer.parseInt(str.split("\\[OP]")[1].split("\\.")[1]);
          m.priority[0] = proposed_priority;
          m.priority[1] = thisNode.portNum;
          thisNode.sendList.add(m);

          // send back to the sender of this message
          str = String.format("%s[PP]%d.%d" + " " + "[OP]%d.%d", thisNode.nodeId, m.priority[0], m.priority[1], 
              m.original_priority[0], m.original_priority[1]);
          ps.println(str);
          System.out.println("proposed priority sent back to message sender!");
          continue;
        } else if (str.substring(0, 4).equals("[PP]")) {
          System.out.println("proposed priority received!");
          id_original_priority[0] = Integer.parseInt(str.split(" ")[1].split("]|\\.")[1]);
          id_original_priority[1]  = Integer.parseInt(str.split(" ")[1].split("]|\\.")[2]);
          Message mo = null;
          Queue<Message> msglo = null;
          System.out.println("initialization finished after receiveing proposed priority");

          for(Message n : thisNode.sendList){
            if (n.original_priority[0] == id_original_priority[0] &&
                n.original_priority[1] == id_original_priority[1]){
              mo = n;
              mo.priority[0] = Integer.parseInt(str.split(" ")[0].split("]|\\.")[1]);
              mo.priority[1] = Integer.parseInt(str.split(" ")[0].split("]|\\.")[2]);
              break;
            }
          }
          for(Queue<Message> msgl: thisNode.msgList){
            Message msglM = msgl.peek();
            if (msglM.original_priority[0] == id_original_priority[0]
              && msglM.original_priority[1] == id_original_priority[1]) {
              msgl.add(mo);
              msglo = msgl;
              break;
            }
          }

          // check if agreed priority can be acquired
          if (msglo.size() == thisNode.TotalNodeNum){
            int[] agreed_priority = new int[2];
            agreed_priority = msglo.peek().priority;
            str = String.format("%s[AP]%d.%d" + " " + "[OP]%d.%d", thisNode.nodeId, agreed_priority[0], agreed_priority[1],
                  msglo.peek().getOriPrio()[0],
                  msglo.peek().getOriPrio()[1]);
            thisNode.multicastMessage(str);
            // self update priority for this message
            for (Message me : thisNode.sendList) {
              if (me.getOriPrio()[0] == msglo.peek().getOriPrio()[0] &&
                  me.getOriPrio()[1] == msglo.peek().getOriPrio()[1]){
                mo = me;
                break;
              }
            }
            thisNode.sendList.remove(mo);
            mo.setPriority(agreed_priority);
            mo.label = "deliverable";
            thisNode.sendList.add(mo);
          
            // find the existing max priority in sendList to update the total_priority before check delivery
            int count = 0;
            Queue<Message> rec = new PriorityQueue<Message>(thisNode.sendList);
            while (count < thisNode.sendList.size()) {
              mo = rec.poll();
              count += 1;
            }
            thisNode.total_priority = mo.priority[0];
          
            // self check for deliverable messages
            while (thisNode.sendList.peek().label.equals("deliverable")) {
              mo = thisNode.sendList.poll();
              int senderPort = mo.getOriPrio()[1];
              String syso = String.format("[%d]%s", senderPort, mo.message.substring(3));
              System.out.println(syso);
            }
          }
          continue;
        } else if (str.substring(0, 4).equals("[AP]")) { //when receive agreed priority from input stream
          Message mo = null;
          for (Message mn: thisNode.sendList) {
            if (mn.getOriPrio()[0] == Integer.parseInt(str.split(" ")[1].split("]|\\.")[1]) &&
                mn.getOriPrio()[1] == Integer.parseInt(str.split(" ")[1].split("]|\\.")[2])){
              mo = mn;
              break;
            }
          }
          thisNode.sendList.remove(mo);
          mo.getPriority()[0] = Integer.parseInt(str.split(" ")[0].split("]|\\.")[1]);
          mo.getPriority()[1] = Integer.parseInt(str.split(" ")[0].split("]|\\.")[2]);
          mo.label = "deliverable";
          thisNode.sendList.add(mo);
      
          // find the existing max priority in sendList to update the total_priority before check delivery
          int count = 0;
          Queue<Message> rec = new PriorityQueue<Message>(thisNode.sendList);
          while (count < thisNode.sendList.size()) {
            mo = rec.poll();
            count += 1;
          }
          thisNode.total_priority = mo.priority[0];
      
          // self check for deliverable messages
      
          while (thisNode.sendList.peek().label.equals("deliverable")) {
            mo = thisNode.sendList.poll();
            int senderPort = mo.getOriPrio()[1];
            String syso = String.format("[%d]%s", senderPort, mo.message.substring(3));
            System.out.println(syso);
          }
          continue;
        }
      }
      client.close();
    } catch (SocketException e) {
      notifHandler.printNoticeMsg("Lost connection with " + remoteAddress);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
