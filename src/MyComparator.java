import java.util.Comparator;
public class MyComparator implements Comparator<Message>{
  public int compare(Message msg1, Message msg2){
    if(msg1.priority[0] > msg2.priority[0])     //the bigger, the latter
      return 1;
    else if (msg1.priority[0] == msg2.priority[0])
    {
      if(msg1.priority[1] > msg2.priority[1])
        return 1;
      else if(msg1.priority[1] == msg2.priority[1])
        return 0;
      else return -1;
    }
    else return -1;
  }
}
