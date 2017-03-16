// Nguyen Nguyen and Sasha Stavila
//CSS430-Program 5

public class FileTableEntry
{
   public int seekPtr;
   public final Inode inode;           
   public final short iNumber;         //inode number
   public int count;                   //# threads sharing entry
   public final String mode;           //different modes: "r", "w", "w+", or "a"

   public FileTableEntry ( Inode i, short inumber, String m ) {
      seekPtr = 0;
      inode = i;
      iNumber = inumber;
      count = 1;               // at least one thread is using this entry
      mode = m;                // once access mode is set, it never changes
      if ( mode.compareTo( "a" ) == 0 ) // if append,then point to end of file
         seekPtr = inode.length;
   }
}
