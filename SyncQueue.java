//Nguyen Nguyen
//CSS430- Part 1
//2.23.17
//Is a ThreadOS monitor that implements SysLib.join() and SysLib.exit()
public class SyncQueue
{
  private static final int DEFAULT_MAX=10;
  private static final int DEFAULT_TID =0;
  private QueueNode[] queue;

//Constructs a default queue
  public SyncQueue()
  {
    queue = new QueueNode[DEFAULT_MAX];
    for(int i=0; i < queue.length; i++){
        queue[i]= new QueueNode();
      }
  }

  public SyncQueue(int condMax)
  {
    queue = new QueueNode[condMax];
    for(int i=0; i < queue.length; i++){
        queue[i]= new QueueNode();
      }
  }
//Calls thread into the queue and let it sleeps until the condition passed in is satisfied
//returns ID of child that woke the thread
  public void enqueueAndSleep(int condition)
  {
    if(condition < queue.length && condition >=0)
    {
      queue[condition].sleep();
    }
  }
//Dequeues and wakes up a thread once given condition is met
  public void dequeueAndWakeup(int condition)
  {
    if(condition < queue.length && condition >=0){
      queue[condition].wake(DEFAULT_TID);
    }
  }
  //Dequeues only the one thread that was passed in from the enqueue above
    public void dequeueAndWakeup(int condition, int tid)
    {
      if(condition < queue.length && condition >=0){
        queue[condition].wake(tid);
      }
    }
}
