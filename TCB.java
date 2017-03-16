//Nguyen Nguyen and Sasha Stavila
//CSS430-Program 5
public class TCB
{
    private Thread thread = null;
    private int tid = 0;
    private int pid = 0;
    private boolean terminated = false;
    public FileTableEntry[] ftEnt = null; //each entry point to a file table entry

    public TCB( Thread newThread, int myTid, int parentTid ) {
        thread = newThread;
        tid = myTid;
        pid = parentTid;
        terminated = false;

        // Set entires in the file system to null, kee fd[0-2] null
        ftEnt = new FileTableEntry[32];
        for ( int i = 0; i < 32; i++ )
            ftEnt[i] = null;
    }
    //returns File Table Entry for the file descriptor
    public synchronized FileTableEntry getFtEnt(int fd)
    {
        if (fd < 32 && fd >= 3)
        {
            return ftEnt[fd];
        }
        else
        {
            return null;
        }
    }
    //returns the file descriptor for the file table entry
    public synchronized int getFd(FileTableEntry fte)
    {
        if (fte == null)
        {
            return -1; //file doesn't exist
        }
        for (int i = 3; i < 32; i++)
        {
            if (ftEnt[i] == null)
            {
                ftEnt[i] = fte;
                return i;
            }
        }
        return -1;
    }

    //returns the file table entry and sets it to null
    public synchronized FileTableEntry returnFd(int fd)
    {
        if (fd >= 3 &&  fd < 32)
        {
            FileTableEntry tmp = ftEnt[fd];
            ftEnt[fd] = null;
            return tmp;
        }
        else
        {
            return null;
        }
    }

    public synchronized Thread getThread() {
        return this.thread;
    }

    public synchronized boolean setTerminated()
    {
        this.terminated = true;
        return this.terminated;
    }

    public synchronized boolean getTerminated()
    {
        return this.terminated;
    }

    public synchronized int getTid()
    {
        return this.tid;
    }

    public synchronized int getPid()
    {
        return this.pid;
    }
}
