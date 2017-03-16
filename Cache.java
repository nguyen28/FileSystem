//Nguyen Nguyen
//CSS430
//3.4.17
//cache that stores frequently-accessed disk blocks into main memory. Subsquent access to the same
//disk block can quickly read the data cached in the memory.
//If the disk cache is full and another block needs to be cached, the OS must select a victim to replace.
// Using the enhanced second-chance algorithm algorithm to choose the block to replace.

public class Cache {
    
	//cache block entry
	private class Entry {
		int blockFrame;
		boolean referenceBit;
		boolean dirtyBit;
		byte[] block;

		private Entry(int blockSize) {
			block = new byte[blockSize];
			blockFrame = -1;
			referenceBit = false;
			dirtyBit = false;
		}
	}

	Entry[] pageTable;
	private int victim;
	private int blockSize;

    public Cache(int blockSize, int cacheBlock) {
        pageTable = new Entry[cacheBlock];
        this.blockSize = blockSize;
        victim = cacheBlock - 1;
        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = new Entry(blockSize);
        }
    }
	
    // Use the enhanced second chance algorithm to search for next victim
	private int nextVictim() {
		while (true) {
			victim = ((++victim) % pageTable.length);
			if (!pageTable[victim].referenceBit) {
				return victim;
			}
			pageTable[victim].referenceBit = false;
		}
	}
	
    //Write back to disk if dirty bit =1
	private void writeBack2Disk(int victimEntry) {
		if ((pageTable[victimEntry].dirtyBit) && (pageTable[victimEntry].blockFrame != -1))
        {
			SysLib.rawwrite(pageTable[victimEntry].blockFrame, pageTable[victimEntry].block);
            pageTable[victimEntry].dirtyBit = false;
		}
	}
	
    //Read the info in the cache at the blockId
	public synchronized boolean read(int blockId, byte[] buffer) {
		if (blockId >= 0) {
			int target = checkFor(blockId); // check if block exists in cache
			if (target != -2) {
				readFromCache(target, blockId, buffer);
				return true;
			}
            target = checkFor(-1); // Check if there is an empty spot
			if (target != -2) {
				SysLib.rawread(blockId, pageTable[target].block);
				readFromCache(target, blockId, buffer);
				return true;
			}
			
			writeBack2Disk(nextVictim());
			SysLib.rawread(blockId, pageTable[victim].block);
			readFromCache(victim, blockId, buffer);
			return true;
		}
		return false;
	}
	
    //Add the data to the cache.
	public synchronized boolean write(int blockId, byte[] buffer) {
		if (blockId >= 0) {
			int target = checkFor(blockId); // check if block exists in cache
			if (target != -2) {
				addToCache(target, blockId, buffer);
				return true;
			}
            target = checkFor(-1); // Check if there is an empty spot
			if (target != -2) {
				addToCache(target, blockId, buffer);
				return true;
			}
			writeBack2Disk(nextVictim());
			addToCache(victim, blockId, buffer);
			return true;
		}
		return false;
	}
	
    // Maintain clean block copies. 
	public synchronized void sync() {
		for (int i = 0; i < pageTable.length; i++) {
			writeBack2Disk(i);
		}
		SysLib.sync();
	}

	//Invalidating all cached blocks
	public synchronized void flush() {
		for (int i = 0; i < pageTable.length; i++) {
			writeBack2Disk(i);
			update(i, -1, false);
		}
		SysLib.sync();
	}

	//Check whether the blockId exists in the pageTable
	private int checkFor(int blockId) {
		for (int i = 0; i < pageTable.length; i++) {
			if (pageTable[i].blockFrame == blockId) {
				return i;
			}
		}
		return -2;
	}

	//Add the elements in the buffer to the cache.
	private void addToCache(int replacement, int newBlockId, byte[] buffer) {
		System.arraycopy(buffer, 0, pageTable[replacement].block, 0, blockSize);
		pageTable[replacement].dirtyBit = true;
		update(replacement, newBlockId, true);
	}
  
    //Read from the cache
	private void readFromCache(int replacement, int newBlockId, byte[] buffer) {
		System.arraycopy(pageTable[replacement].block, 0, buffer, 0, blockSize);
		update(replacement, newBlockId, true);
	}
    
    //Update the cache
	private void update(int replacement, int frameNumber, boolean referenceBit) {
		pageTable[replacement].blockFrame = frameNumber;
		pageTable[replacement].referenceBit = referenceBit;
	}
}
