//Nguyen Nguyen
//OS CSS430 Program-Test4.java
//Test 4 will test the program 4 with and without cache  for 4 types of acceses.
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class Test4 extends Thread{
	private int testType = 0;
	
	public static final int BLOCKSIZE = 512;
	public static final int ARRAYSIZE = 350;
	private Date date;
	
	private byte[] writeBlock;
	private byte[] readBlock;
	
	private boolean caching;
    
    private long startWrite;
    private long endWrite;
    private long startRead;
    private long endRead;
	
	Random random = new Random();
    
	public Test4(String args[]){
		date = new Date();
		testType = Integer.parseInt(args[1]);
		random = new Random();
		writeBlock = new byte[512];
		
		writeBlock = new byte[BLOCKSIZE];
		readBlock = new byte[BLOCKSIZE];
		
		random.nextBytes(writeBlock);
		
		if(args[0].equals("enabled")){
			caching = true;
		}else{
			caching = false;
		}
	}
	
	//Runs all the tests
	public void run(){
		SysLib.flush();
		switch(testType){
			case 1:
				randomAccess();
				break;
			case 2:
				localizedAccess();
				break;
			case 3:
				mixedAccess();
				break;
			case 4:
				adversaryAccess();
				break;
			default:
				SysLib.cout("Invalid Argument Type \n");
				break;
		}
		sync();
		SysLib.exit();
	}
	
	
	public void sync(){
		if(caching){
			SysLib.csync();
		}else{
			SysLib.sync();
		}
	}
	
	// Random access with cache enabled and disabled
	public void randomAccess(){
		int[] randomBlock = new int[ARRAYSIZE];
		for(int i = 0; i < ARRAYSIZE; i++){
			randomBlock[i] = randomNum(BLOCKSIZE);
		}
		startWrite = new Date().getTime();
		for(int i = 0; i < ARRAYSIZE; i++){
			write(randomBlock[i], writeBlock);
		}
		endWrite = new Date().getTime();
		
        startRead = date.getTime();
		for(int i = 0; i < ARRAYSIZE; i++){
			read(randomBlock[i], readBlock);
		}
		endRead = new Date().getTime();
		
		if(!Arrays.equals(readBlock, writeBlock)){
			SysLib.cout("Read and write blocks are not equal");
		}
		
		long averageWriteTime = (endWrite - startWrite) / ARRAYSIZE;
		long averageReadTime = (endRead - startRead) / ARRAYSIZE;
		String caching = this.caching ? "Enabled" : "Disabled";
		SysLib.cout("Random Accesses" + " with caching " + caching + "\n");
		SysLib.cout("Average write time: " + averageWriteTime + " msec. Average read time " + averageReadTime + " msec\n");
	}
	
	//Localized access with cache enabled and disabled
	public void localizedAccess(){
		startWrite = new Date().getTime();
		for(int i = 0; i < ARRAYSIZE; i++){
			for(int j = 0; j < 10; j++){
				write(j, writeBlock);
			}
		}
		
		endWrite = new Date().getTime();
		
		startRead = new Date().getTime();
		for(int i = 0; i < ARRAYSIZE; i++){
			for(int j = 0; j < 10; j++){
				read(j, readBlock);
			}
		}
		
		endRead = new Date().getTime();
		if(!Arrays.equals(readBlock, writeBlock)){
			SysLib.cout("Read and write blocks are not equal");
		}
		long averageWriteTime = (endWrite - startWrite) / ARRAYSIZE;
		long averageReadTime = (endRead - startRead) / ARRAYSIZE;
		String caching = this.caching ? "Enabled" : "Disabled";
		SysLib.cout("Localized Accesses" + " with caching " + caching + "\n");
		SysLib.cout("Average write time: " + averageWriteTime + " msec. Average read time " + averageReadTime + " msec\n");
	}
	
	// Use both random accesses and localized accesses
	public void mixedAccess(){
		int[] mixedArray = new int[ARRAYSIZE];
		
		for(int i = 0; i < ARRAYSIZE; i++){
			if(randomNum(10) < 9 ){
				mixedArray[i] = randomNum(20);
			}else{
				mixedArray[i] = randomNum(BLOCKSIZE);
			}
		}
		
		startWrite = new Date().getTime();
		for(int i = 0; i < ARRAYSIZE; i++){
			write(mixedArray[i], writeBlock);
		}
		endWrite = new Date().getTime();
		
		startRead = new Date().getTime();
		for(int i = 0; i < ARRAYSIZE; i++){
			read(mixedArray[i], readBlock);
		}
		endRead = new Date().getTime();
		
		if(!Arrays.equals(readBlock, writeBlock)){
			SysLib.cout("Read and write blocks are not equal");
		}
		long averageWriteTime = (endWrite - startWrite) / ARRAYSIZE;
		long averageReadTime = (endRead - startRead) / ARRAYSIZE;
		String caching = this.caching ? "Enabled" : "Disabled";
		SysLib.cout("Mixed Accesses" + " with caching " + caching + "\n");
		SysLib.cout("Average write time: " + averageWriteTime + " msec. Average read time " + averageReadTime + " msec\n");
	}
	
	//Adversary access with cache enabled and disabled
	public void adversaryAccess(){
		startWrite = new Date().getTime();
		for(int i = 0; i < BLOCKSIZE; i++){
			write(i, writeBlock);
		}
		endWrite = new Date().getTime();
		
		startRead = new Date().getTime();
		for(int i = 0; i < BLOCKSIZE; i++){
			read(i, readBlock);
		}
		endRead = new Date().getTime();
		if(!Arrays.equals(readBlock, writeBlock)){
			SysLib.cout("Read and write blocks are not equal");
		}
        
		long averageWriteTime = (endWrite - startWrite) / ARRAYSIZE;
		long averageReadTime = (endRead - startRead) / ARRAYSIZE;
		String caching = this.caching ? "Enabled" : "Disabled";
		SysLib.cout("Adversary Accesses" + " with caching " + caching + "\n");
		SysLib.cout("Average write time: " + averageWriteTime + " msec. Average read time " + averageReadTime + " msec\n");
	}
	
    //If cache is true use cwrite, if cache is false use rawwrite
	public void write(int blockId, byte buffer[]){
		if(caching){
			SysLib.cwrite(blockId, buffer);
		}else{
			SysLib.rawwrite(blockId, buffer);
		}
	}
	
    //If cache is true use cread, if cache is false use rawread
	public void read(int blockId, byte buffer[]){
		if(caching){
			SysLib.cread(blockId, buffer);
		}else{
			SysLib.rawread(blockId, buffer);
		}
	}
	
	public int randomNum(int max){
		return (Math.abs(random.nextInt() % max));
	}
}
