//Sasha Stavila and Nguyen Nguyen
//CSS430-Program 5
//The File System will implement the 8 system calls
public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    //Constructor
    public FileSystem(int diskBlocks) {
        superblock = new SuperBlock(diskBlocks);
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);
        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if (dirSize > 0) {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

    public void sync()
    {
        FileTableEntry root = open("/", "w");
        write(root, directory.directory2bytes());
        close(root);
        superblock.sync();
    }

    //Format's the disk and specifies the maximum number of files to be created.
    public boolean format(int files)
    {
        if(files <1)
        {
            return false;
        }

        superblock.format(files);
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);
        return true;
    }
    //Allocates a new file describor, and it depends on the mode
    public FileTableEntry open(String filename, String mode)
  	{
  		FileTableEntry ftEnt = filetable.falloc(filename, mode);
  		if(mode.equals("w"))
  		{
  			if(deallocAllBlocks(ftEnt) == false) // Need to implement
  			{
  				return null;
  			}
  		}
  		return ftEnt;
  	}

    //close should wait for all threads to finish with the file
     public boolean close(FileTableEntry ftEnt)
     {
         synchronized (ftEnt)
         {
             ftEnt.count--;
             if (ftEnt.count == 0) return filetable.ffree(ftEnt);
             return true;
         }
     }

    // returns the size in bytes of the file
    public int fsize(FileTableEntry ftEnt)
    {
      if(ftEnt == null)
      {
        return -1;
      }
        synchronized (ftEnt)
        {
            Inode tempInode = ftEnt.inode;
            return tempInode.length;
        }
    }

    //reads up to buffer length bytes from the file,
    //starting at where the seek pointer is at
    public synchronized int read(FileTableEntry ftEnt, byte[] buffer)
    {
        if ((ftEnt.mode == "w") || (ftEnt.mode == "a")) return -1;
        int size = buffer.length;
        int readBuffer = 0;
        int readError = -1;
        int blockSize = 512;
        int iterationSize = 0;

        synchronized (ftEnt)
        {
            while ((ftEnt.seekPtr < fsize(ftEnt) && (size > 0)))
            {
                int target = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                if (target == readError)  break;

                byte[] data = new byte[blockSize];
                SysLib.rawread(target, data);

                int dataOffset = ftEnt.seekPtr % blockSize;
                int blockLeft = blockSize - dataOffset;
                int fileLeft = fsize(ftEnt) - ftEnt.seekPtr;

                if (blockLeft < fileLeft)
                {
                    iterationSize = blockLeft;
                }
                else
                {
                    iterationSize = fileLeft;
                }

                if (iterationSize > size) iterationSize = size;

                System.arraycopy(data, dataOffset, buffer, readBuffer,
                                 iterationSize);
                ftEnt.seekPtr += iterationSize;
                readBuffer += iterationSize;
                size -= iterationSize;
            }
            return readBuffer;
        }
    }

    //writes the contents of the file, starting at the seek pointer.
    public synchronized int write(FileTableEntry ftEnt, byte[] buffer)
    {
        int bytesWritten = 0;
        int bufferSize = buffer.length;
        int blockSize = 512;

        if (ftEnt == null || ftEnt.mode == "r")
        {
            return -1;
        }

        synchronized (ftEnt)
        {
            while (bufferSize > 0)
            {
                int loc = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                if (loc == -1)
                {
                    short newLoc = (short) superblock.getFreeBlock();
                    int testPtr = ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, newLoc);

                    if(testPtr == -3)
                    {
                        short freeBlock = (short)superblock.getFreeBlock();
                        if (!ftEnt.inode.registerIndexBlock(freeBlock))
                        {
                           return -1;
                         }
                        if (ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, newLoc) != 0)
                        {
                            return -1;
                          }
                    }
                    else if (testPtr == -2 || testPtr == -1)
                    {
                      return -1;
                    }
                    loc = newLoc;
                }

                byte[] tempBuffer = new byte[blockSize];
                SysLib.rawread(loc, tempBuffer);

                int tempPtr = ftEnt.seekPtr % blockSize;
                int diff = blockSize - tempPtr;

                if (diff > bufferSize)
                {
                    System.arraycopy(buffer, bytesWritten, tempBuffer, tempPtr, bufferSize);
                    SysLib.rawwrite(loc, tempBuffer);

                    ftEnt.seekPtr += bufferSize;
                    bytesWritten += bufferSize;
                    bufferSize = 0;
                }
                else
                {
                    System.arraycopy(buffer, bytesWritten, tempBuffer, tempPtr, diff);
                    SysLib.rawwrite(loc, tempBuffer);

                    ftEnt.seekPtr += diff;
                    bytesWritten += diff;
                    bufferSize -= diff;
                }
            }

            if (ftEnt.seekPtr > ftEnt.inode.length)
            {
                ftEnt.inode.length = ftEnt.seekPtr;
            }

            ftEnt.inode.toDisk(ftEnt.iNumber);
            return bytesWritten;
        }
    }

    private boolean deallocAllBlocks(FileTableEntry ftEnt)
    {
        short notValid = -1;
        if (ftEnt.inode.count == notValid)
        {
            SysLib.cerr("Null pointer - could not deallocAllBlocks.\n");
            return false;
        }
        for (short blockId = 0; blockId < ftEnt.inode.directSize; blockId++)
        {
            if (ftEnt.inode.direct[blockId] != notValid)
            {
                superblock.returnBlock(blockId);
                ftEnt.inode.direct[blockId] = notValid;
            }
        }
        byte[] data = ftEnt.inode.unregisterIndexBlock();
        if (data != null)
        {
            short blockId;
            while ((blockId = SysLib.bytes2short(data, 0)) != notValid)
            {
                superblock.returnBlock(blockId);
            }
        }
        ftEnt.inode.toDisk(ftEnt.iNumber);
        return true;
    }
    //Deletes the file but can't delete until the last open one is closed
    boolean delete(String filename)
     {
         FileTableEntry fileTableEntry = open(filename, "w");
         short s = fileTableEntry.iNumber;
         return close(fileTableEntry) && directory.ifree(s);
     }

    //Sets the seek pointer
    public int seek(FileTableEntry ftEnt, int offset, int whence) {
        synchronized (ftEnt)
        {
            switch (whence)
            {
                case SEEK_SET:
                    ftEnt.seekPtr = offset;
                    break;
                case SEEK_CUR:
                    ftEnt.seekPtr += offset;
                    break;
                case SEEK_END:
                    ftEnt.seekPtr = ftEnt.inode.length + offset;
                    break;
                default:
                    return -1;
            }
            if (ftEnt.seekPtr < 0) {
              return -1;
            }

            if (ftEnt.seekPtr > ftEnt.inode.length)
            {
                ftEnt.seekPtr = ftEnt.inode.length;
            }

            return ftEnt.seekPtr;
        }
    }
}
