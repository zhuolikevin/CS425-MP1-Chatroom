import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Timer;
import java.util.Queue;
import java.util.PriorityQueue;
import mputil.NodeNotifHandler;

public class MessageReceiver implements Runnable {
  private Socket client;

  private Timer failureDetectorTimer = new Timer(true);
  private FailureDetector failureDetector = null;

  private NodeNotifHandler notifHandler = new NodeNotifHandler();
  private Node thisNode = null;

  public MessageReceiver(Socket client, Node thisNode) {
    this.client = client;
    this.thisNode = thisNode;
  }

  /**
   * Read messages which are sent by other nodes
   */
  public void run() {
    String remoteAddress = client.getRemoteSocketAddress().toString().substring(1);
    String messageSender = null;
    String message = null;
    Socket clientSo = null;
    int index = 0;
    PrintStream ps;

    try {
      BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));

      boolean keepConnection = true;
      int proposedPriority;
      int[] idOriginalPriority = new int[2];

      while (keepConnection) {
        String str = buf.readLine();
        if (str == null) {
          notifHandler.printNoticeMsg("Lost connection with " + remoteAddress + " at: " + new Date().getTime());
          break;
        }

        // Extract Sender Node ID
        messageSender = str.substring(0, 5);
        str = str.substring(5);

        if (Node.TERMINATION_MSG.equals(str)) {
          /* Termination Message */
          keepConnection = false;
          notifHandler.printNoticeMsg("Lost connection with " + remoteAddress + " at: " + new Date().getTime());
        } else if ("[HB]".equals(str)) {
          /* Heartbeat */
          if (!thisNode.getReadyFlag()) { continue; }

          if (failureDetector != null) {
            // Reset failure detection timer
            failureDetectorTimer.cancel();
            failureDetectorTimer = new Timer(true);
            failureDetector.cancel();
          }
          failureDetector = new FailureDetector(messageSender, client, thisNode);
          failureDetectorTimer.schedule(failureDetector, 1600);
        } else if (str.length() > 6 && "[FAIL]".equals(str.substring(0, 6))) {
          /* Heartbeat Failure Informing */
          String failedNodeId = str.substring(6);
          thisNode.cancelConnectionWithNodeId(failedNodeId);
        } else if (str.substring(0, 3).equals("[M]")) {
          /* Messages */
          clientSo = thisNode.clientSockMap.get(messageSender);
          index = thisNode.clientSockList.indexOf(clientSo);
          ps = thisNode.outgoingStreamList.get(index);
          Message co = null;

          // calculation of proposed priority
          if (thisNode.sendList.size() == 0) {
            proposedPriority = thisNode.totalPriority + 1;
          } else {
            int count = 0;
            Queue<Message> rec = new PriorityQueue(thisNode.sendList);
            while (count < rec.size()) {
              co = rec.poll();
              count += 1;
            }
            proposedPriority = co.priority[0] + 1;
          }
          thisNode.totalPriority = proposedPriority;

          message = str.substring(3).split("\\[OP]")[0];
          Message m = new Message(message);
          m.original_priority[0] = Integer.parseInt(str.split("\\[OP]")[1].split("\\.")[0]);
          m.original_priority[1] = Integer.parseInt(str.split("\\[OP]")[1].split("\\.")[1]);
          m.priority[0] = proposedPriority;
          m.priority[1] = thisNode.portNum;
          thisNode.sendList.add(m);

          // send back to the sender of this message
          str = String.format("%s[PP]%d.%d" + "[OP]%d.%d", thisNode.nodeId, m.priority[0], m.priority[1],
              m.original_priority[0], m.original_priority[1]);
          ps.println(str);
        } else if (str.substring(0, 4).equals("[PP]")) {
          /* Proposed Priority */
          idOriginalPriority[0] = Integer.parseInt(str.split("\\[PP]|\\[OP]")[2].split("\\.")[0]);
          idOriginalPriority[1]  = Integer.parseInt(str.split("\\[PP]|\\[OP]")[2].split("\\.")[1]);
          Message mo = null;
          Queue<Message> msglo = null;

          for(Message n : thisNode.sendList){
            if (n.original_priority[0] == idOriginalPriority[0] &&
                n.original_priority[1] == idOriginalPriority[1]){
              mo = n;

              mo.priority[0] = Integer.parseInt(str.split("\\[PP]|\\[OP]")[1].split("\\.")[0]);
              mo.priority[1] = Integer.parseInt(str.split("\\[PP]|\\[OP]")[1].split("\\.")[1]);
              break;
            }
          }

          for(Queue<Message> msgl: thisNode.msgList) {
            Message msglM = msgl.peek();
            if (msglM.original_priority[0] == idOriginalPriority[0]
              && msglM.original_priority[1] == idOriginalPriority[1]) {
              msgl.add(mo);
              msglo = msgl;
              break;
            }
          }

          // check if agreed priority can be acquired
          if (msglo.size() == thisNode.totalNodeNum) {
            int[] agreed_priority = new int[2];
            agreed_priority = msglo.peek().priority;
            str = String.format("%s[AP]%d.%d" + "[OP]%d.%d", thisNode.nodeId, agreed_priority[0], agreed_priority[1],
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

            // find the existing max priority in sendList to update the totalPriority before check delivery
            int count = 0;
            Queue<Message> rec = new PriorityQueue<>(thisNode.sendList);
            while (count < thisNode.sendList.size()) {
              mo = rec.poll();
              count += 1;
            }
            thisNode.totalPriority = mo.priority[0];

            // self check for deliverable messages
            while (!thisNode.sendList.isEmpty() && thisNode.sendList.peek().label.equals("deliverable")) {
              mo = thisNode.sendList.poll();
              int senderPort = mo.getOriPrio()[1];
              //
              String syso = String.format("[%d]%s", senderPort, mo.message);
              System.out.println(syso);
            }
            System.out.print(">> ");
          }
        } else if (str.substring(0, 4).equals("[AP]")) { //when receive agreed priority from input stream
          Message mo = null;
          for (Message mn: thisNode.sendList) {
            if (mn.getOriPrio()[0] == Integer.parseInt(str.split("\\[OP]")[1].split("\\.")[0]) &&
                mn.getOriPrio()[1] == Integer.parseInt(str.split("\\[OP]")[1].split("\\.")[1])){
              mo = mn;
              break;
            }
          }
          thisNode.sendList.remove(mo);
          mo.getPriority()[0] = Integer.parseInt(str.split("\\[AP]|\\[OP]")[1].split("\\.")[0]);
          mo.getPriority()[1] = Integer.parseInt(str.split("\\[AP]|\\[OP]")[1].split("\\.")[1]);
          mo.label = "deliverable";
          thisNode.sendList.add(mo);

          // find the existing max priority in sendList to update the totalPriority before check delivery
          int count = 0;
          Queue<Message> rec = new PriorityQueue<>(thisNode.sendList);
          while (count < thisNode.sendList.size()) {
            mo = rec.poll();
            count += 1;
          }
          thisNode.totalPriority = mo.priority[0];

          // self check for deliverable messages
          while (!thisNode.sendList.isEmpty() && thisNode.sendList.peek().label.equals("deliverable")) {
            mo = thisNode.sendList.poll();
            int senderPort = mo.getOriPrio()[1];
            String syso = String.format("[%d]%s", senderPort, mo.message);
            System.out.println(syso);
          }
          System.out.print(">> ");
        } else { notifHandler.printNoticeMsg("Non-exhausted Condition"); }
      }
      client.close();
    } catch (SocketException e) {
      notifHandler.printNoticeMsg("Lost connection with " + remoteAddress);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
