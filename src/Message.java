public class Message {
  String message;
  String label = "undeliverable";
  int[] priority = new int[2];
  int[] original_priority = new int[2];

  public Message(String message){
    this.message = message;
  }

  public String getMessage(){
    return message;
  }
  public void setMessage(String message){
    this.message = message;
  }

  public String getLabel(){
    return label;
  }
  public void setLabel(String label){
    this.label = label;
    }

  public int[] getPriority(){
    return priority;
  }
  public void setPriority(int[] priority){
    this.priority = priority;
  }

  public int[] getOriPrio(){
    return original_priority;
  }
  public void setOriPrio(int[] original_priority){
    this.original_priority = original_priority;
  }
}
