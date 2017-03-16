//Nguyen Nguyen
//CSS430- Part 1
//2.23.17
import java.util.Vector;
//This class is for SyncQueue class to hold threads
//within a queue to be FCFS to put to sleep and notify to wake up
public class QueueNode {
    private Vector<Integer> queue;
    public QueueNode()
    {
       queue = new Vector<>();
     }

    //Sleep puts the thread to sleep using wait()
    public synchronized int sleep( ) {
        if(queue.size() == 0){
            try {
                wait( );

            } catch ( InterruptedException e ) {
                SysLib.cerr(e.toString());   //an error has occured
            }
            return queue.remove(0);
        }
        return -1;
    }

    // wakes up a thread by enqueuing it into the queue with tid identifier of the specific thread enqueued
    public synchronized void wake(int tid){
        queue.add(tid);
        notify();
    }
}
