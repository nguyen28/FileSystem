//Nguyen Nguyen and Sasha Stavila
//CSS430-Program 5

public class Inode {
    public static final int iNodeSize = 32;
    public static final int directSize = 11;
    public int length;
    public short count;
    public short flag;
    public short[] direct = new short[directSize];
    public short indirect;

    Inode()
    {
        length = 0;
        count = 0;
        flag = 1;
        for (int i = 0; i < 11; ++i)
        {
            direct[i] = -1;
        }
        indirect = -1;
    }

    //Retrieves and read the INumber, locate the inode info and initializes the
    //new Inode with this information
    Inode(short inodeNum)
    {
        int bNum = 1 + inodeNum / 16;
        byte[] newData = new byte[Disk.blockSize];
        SysLib.rawread(bNum,newData);
        int offset = (inodeNum % 16) * iNodeSize;
        length = SysLib.bytes2int(newData, offset);
        count = SysLib.bytes2short(newData, (offset += 4));
        flag = SysLib.bytes2short(newData, (offset += 2));
        offset += 2;
        for (int i = 0; i < 11; ++i)
        {
            direct[i] = SysLib.bytes2short(newData, offset);
            offset += 2;
        }
        indirect = SysLib.bytes2short(newData, offset);
        offset += 2;
    }

    //Saving to the Disk
    public void toDisk(short inodeNum)
    {
        int bNum;
        byte[] newData = new byte[iNodeSize];
        int tempNum = 0;
        SysLib.int2bytes(length, newData, tempNum);
        SysLib.short2bytes(count, newData, (tempNum += 4));
        SysLib.short2bytes(flag, newData, (tempNum += 2));
        tempNum += 2;
        for (bNum = 0; bNum < 11; ++bNum)
        {
            SysLib.short2bytes((short)direct[bNum], newData, tempNum);
            tempNum += 2;
        }
        SysLib.short2bytes(indirect, newData, tempNum);
        tempNum += 2;
        bNum = 1 + inodeNum / 16;

        byte[] newData2 = new byte[512];
        SysLib.rawread(bNum, (byte[])newData2);
        tempNum = (inodeNum % 16) * iNodeSize;
        System.arraycopy(newData, 0, newData2, tempNum, iNodeSize);
        SysLib.rawwrite(bNum, (byte[])newData2);
    }

//Returns indirect pointer
    int findIndexBlock() {
      if(indirect> 0){
        return indirect;
      }
      return -2;
    }

    //Looking for the requested block based on it's offset
    public int findTargetBlock(int num)
    {
        int offset = num / 512;
        if (offset < directSize)
	       {
            return (int)direct[offset];
          }
        if (indirect < 0){
           return -1;
         }

        byte[] newData = new byte[512];
        SysLib.rawread((int)indirect,newData);
        int tempVar = offset - 11;
        return SysLib.bytes2short(newData,(tempVar * 2));
    }

    boolean registerIndexBlock(short inodeNum)
    {
        for (int i = 0; i < 11; ++i)
        {
            if (direct[i] != -1){
               continue;
             }
            return false;
        }

        if (indirect != -1)
        {
          return false;
        }
        indirect = inodeNum;
        byte[] newData = new byte[512];
        for (int j = 0; j < 256; ++j)
        {
            SysLib.short2bytes((short)-1, newData, (j * 2));
        }
        SysLib.rawwrite(inodeNum,newData);
        return true;
    }
    // Clear and return the indirect Pointer's info
   public byte[] unregisterIndexBlock()
	{
        if (indirect >= 0)
	{
            byte[] newData = new byte[512];
            SysLib.rawread((int)indirect, newData);
            this.indirect = -1;
            return newData;
        }
        return null;
    }

    //Return the target block pointed to by the offset, else return -1
    public int registerTargetBlock(int offset, short inodeNum)
    {
        int tempNum = offset / 512;
        if (tempNum < 11)
        {
            if (direct[tempNum] >= 0) {
              return -1;
            }
            if (tempNum > 0 && direct[tempNum - 1] == -1){
                return -2;
              }
            direct[tempNum] = inodeNum;
            return 0;
        }

        if (indirect < 0){
          return -3;
        }
        byte[] newData = new byte[512];
        SysLib.rawread((int)indirect, newData);
        int tempVar = tempNum - 11;
        if (SysLib.bytes2short(newData, (int)(tempVar * 2)) > 0)
        {
            SysLib.cerr((String)("indirectNumber= " + tempVar + " contains= "
            + SysLib.bytes2short(newData,(int)(tempVar * 2)) + "\n"));
            return -1;
        }
        SysLib.short2bytes(inodeNum, newData, (tempVar * 2));
        SysLib.rawwrite((int)indirect, newData);
        return 0;
    }
}
