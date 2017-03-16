//Nguyen Nguyen and Sasha Stavila
//CSS430-Program 5

public class Directory
{
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element is a different file size
   private char fnames[][];    // each element is different file name

   //Constructor
   public Directory( int maxFiles)
   {
      fsize = new int[maxFiles];
      for ( int i = 0; i < maxFiles; i++)
      {
        fsize[i] = 0;                 // all file size initialized to 0
      }
      fnames = new char[maxFiles][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );
      root.getChars( 0, fsize[0], fnames[0], 0 );
   }
   //data array is used to initializes the directory instance
   public int bytes2directory( byte data[] )
   {
     int offset = 0;
     for (int i = 0; i < fsize.length; i++, offset += 4)
     {
       fsize[i] = SysLib.bytes2int(data, offset);
     }
     for (int i = 0; i < fnames.length; i++)
     {
       String tmp = new String (data, offset, maxChars * 2);
       tmp.getChars(0, fsize[i], fnames[i], 0);
       offset += maxChars * 2;
     }
     return 0;
   }
//converts and returns directory info into byte array, written back to the disk
   public byte[] directory2bytes( )
   {
     int offset = 0;
     byte[] returnInfo = new byte[fsize.length * 4 + fnames.length + maxChars * 2];
     for (int i = 0; i < fsize.length; i++)
     {
       SysLib.int2bytes(fsize[i], returnInfo, offset);
       offset += 4;
     }
     for(int i = 0; i < fsize.length; i++)
     {
       String tmp = new String(fnames[i], 0, fsize[i]);
       byte[] tmpArr = tmp.getBytes();
       System.arraycopy(tmpArr, 0, returnInfo, offset, tmpArr.length);
       offset += maxChars * 2;
     }
     return returnInfo;
   }

   //a new inode number is allocated for this filename.
   public short ialloc( String filename )
   {
      for (short i = 0; i < fsize.length; i++)
      {
        if (fsize[i] == 0)
        {
          int fileNameSize = 0;
          if(filename.length() > maxChars)
          {
            fileNameSize = maxChars;
          }
          else
          {
            fileNameSize = filename.length();
          }
          fsize[i] = fileNameSize;
          filename.getChars(0, fsize[i], fnames[i], 0);
          return i;
        }
      }
      return (short) -1;
   }
   //deallocates inode number and deletes file
   public boolean ifree( short iNumber )
   {
      if (iNumber > 0 && iNumber < fsize.length)
      {
        fsize[iNumber] = 0;
        fnames[iNumber] = new char[maxChars];
        return true;
      }
      else
      {
        return false;
      }
   }

   //returns the inumber corresponding to this filename
   public short namei( String filename)
   {
     for (short i = 0; i < fsize.length; i++)
     {
       String temp = new String(fnames[i], 0, fsize[i]);
       if (filename.equals(temp))
       {
         return i;
       }
     }
     return (short) -1;
   }
}
