//Nguyen Nguyen and Sasha Stavila
//OS managed block and contains no other information and no user threads are able
//to access the SuperBlock
public class SuperBlock
{
	  public int totalBlocks; //the number of disk blocks
    public int totalInodes; //the number of inodes
    public int freeList;  //The block number of the free list's head
    public int inodeBlocks;
		private final static int BLOCKSIZE =512;
		private final static int  DEFAULT_BLOCKS=1000;

	public SuperBlock(int diskSize)
	{
		byte[] superBlock = new byte[BLOCKSIZE];
		SysLib.rawread(0, superBlock);
		totalBlocks = SysLib.bytes2int(superBlock,0);
		totalInodes = SysLib.bytes2int(superBlock,4);
		freeList = SysLib.bytes2int(superBlock,8);

		if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2){
			return;
		}
		else
		{
			totalBlocks = diskSize;
			format(64);
		}
	}
//Writing everything back to the disk
	public void sync()
	{
		byte[] newSuper = new byte[Disk.blockSize];
		SysLib.int2bytes(totalBlocks,newSuper,0);
		SysLib.int2bytes(totalInodes,newSuper,4);
		SysLib.int2bytes(freeList,newSuper,8);
		SysLib.rawwrite(0,newSuper);
	}
	//find where the current free block head is in the disk and reset it to the
	//new free block becuase the current one is called into usage by get fxn
	public int getFreeBlock()
	{
		if(freeList > 0 && freeList < totalBlocks)
		{
			byte[] freeBlock = new byte[BLOCKSIZE];
			SysLib.rawread(freeList, freeBlock);
			int currentList = freeList;
			freeList = SysLib.bytes2int(freeBlock, 0);
			return currentList;
		}
		return -1;
	}

//Obtain the block from disk and write the our current freeBlock head to it
//Set this new block as a free block and return true
	public boolean returnBlock(int blockNumber)
	{
		if(blockNumber > 0 && blockNumber < totalBlocks){
			int nextFree = freeList;
			int temp = 0;
			byte[] nextBlock = new byte[BLOCKSIZE];
			byte[] newBlock = new byte[BLOCKSIZE];

			for(int i = 0; i < BLOCKSIZE; i++)
			{
				newBlock[i] = 0;
			}
			SysLib.int2bytes(-1,newBlock,0);
			//traverse the free list until we reach the end
			while(nextFree != -1)
			{
				//reading the nexy block in the list
				SysLib.rawread(nextFree, nextBlock);
				temp = SysLib.bytes2int(nextBlock,0);
				if(temp == -1)
				{ //found the end
					SysLib.int2bytes(blockNumber,nextBlock,0);
					SysLib.rawwrite(nextFree, nextBlock);
					SysLib.rawwrite(blockNumber,newBlock);
					return true;
				}
				//not found
				nextFree = temp;
			}
		}
		return false;
	}
//Constructs disk block 0 to be the disk block
	public synchronized void format(int inodeFiles)
	{
		if(inodeFiles < 0)
		{
			inodeFiles = 64;
		}
		byte[] emptyData=null;
		totalBlocks = DEFAULT_BLOCKS;
		totalInodes = inodeFiles;
		inodeBlocks = totalInodes;

		for(short i=0; i < inodeBlocks; i++)
		{
			Inode dummyInode= new Inode();
			dummyInode.toDisk((short)i);
		}

		freeList = (inodeFiles / 16) + 2;

		for(int i=freeList; i <DEFAULT_BLOCKS; i++)
		{
			emptyData = new byte[BLOCKSIZE];
			for(int j = 0; j < BLOCKSIZE; j++)
			{
				emptyData[j] = (byte)0;
			}
			SysLib.int2bytes(i+1, emptyData, 0);
			SysLib.rawwrite(i, emptyData);
		}
		//The last block has a null pointer
		SysLib.int2bytes(-1, emptyData, 0);
		SysLib.rawwrite(totalBlocks - 1, emptyData);
		//Sync superblock to disk after format
		sync();
	}
}
