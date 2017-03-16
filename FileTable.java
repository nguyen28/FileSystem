//Sasha Stavila and Nguyen Nguyen
//CSS430-Program 5

import java.util.Vector;
//Keeps track of all file table entries
public class FileTable {
   private Vector<FileTableEntry> table;   //All active FTEs
   private Directory dir;

   //Constructor
   public FileTable( Directory directory ) {
      table = new Vector<FileTableEntry>( );
      dir = directory;           //reference to the Director from File Sys
   }
   // allocate new file table entry for file,
   //register the inode using directory, increment count and write back to disk
   public synchronized FileTableEntry falloc( String filename, String mode )
   {
      short iNumber = -1;
      Inode iNode = null;
      // allocate/retrieve and register the corresponding inode using dir
      while(true)
      {
        iNumber = (filename.equals("/") ? 0: dir.namei(filename));
        if (iNumber >= 0)
        {
          iNode = new Inode(iNumber);
          if(mode.equals("r"))
          {
              if (iNode.flag == 0 || iNode.flag == 1)
              {
                  iNode.flag = 2; //read
                  break;
              }
              else if (iNode.flag == 3) // write
              {
                 try
                {
                    wait();
                } catch(InterruptedException e) {}
             }
          }
          else
          {
              if (iNode.flag == 0 || iNode.flag == 1)
              {
                 iNode.flag = 3;
                 break;
              }
              else
              {
                 try
                {
                  wait();
                } catch (InterruptedException e) {}
             }
          }
        }
        else if (mode.equals("w") || mode.equals("w+") || mode.equals("a"))
        {
           iNumber = dir.ialloc(filename);
           iNode = new Inode(iNumber);
           iNode.flag = 3;
        }
        else
        {
         return null;
        }
      }
        iNode.count++;
        iNode.toDisk(iNumber);
        FileTableEntry entry = new FileTableEntry(iNode, iNumber, mode);
        table.addElement(entry);
        return entry;
    }
    //save entry to inode to disk & free the entry
    public synchronized boolean ffree( FileTableEntry entry) {

      if(table.contains(entry)) { // receive a file table entry reference
      // free this file table entry.
        entry.inode.flag -= 1;
        entry.inode.flag = 0;

      entry.inode.toDisk(entry.iNumber); // save inode to the disk
      table.removeElement(entry);
      return true; //file table entry found in my table
    }

    return false;
 }

    public synchronized boolean fempty( )
    {
      return table.isEmpty( );  // return if table is empty
   }
}
