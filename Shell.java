//Nguyen Nguyen
//CSS430 Program 1
//Ampersand is a concurrent execution where as Semicolon makes a sequential execution
//Making a Shell java class that cna read in strings of system calls and prints it according to the
//signs of & and ;
import java.util.*;

class Shell extends Thread
{
    public Shell(){
	SysLib.cout("Testing Shell...");
    }

    public void run(){
	boolean run=true;
	int num =1;
	while(run){ //while true continue printing
	    SysLib.cout("Shell[" +num+ "]: ");
	    StringBuffer s = new StringBuffer(); //constructs a string buffer with nothing in it
	    SysLib.cin(s);//read keyboard input into the string buffer
	    String[] commands = SysLib.stringToArgs(s.toString()); //convert the buffer to a string which then is spilt into an array arg
	    if(commands.length <1){
		continue; //reprint Shell[num]
	    }
	    run =(!(commands.toString( ).equals( "exit" )));

	    if(run == false){
		break;
	    }
	    SysLib.cout("\n"); //prints new line
	    num++;

	    //create the strings
	    String[] semiSign = s.toString().split(";");
	    String[] ampSign = s.toString().split("&");
	    String semiAmp = s.toString();

	    boolean Amp = false, semiC =false; //check to see if there's the ";" or the "&"
	    if((semiSign.length >1) && (ampSign.length >1)){ //if both signs exist
		String[] semiSpilt = semiSign.toString().split(";");

		for (int i = 0; i < semiSpilt.length; i++)
		    {
			SysLib.cout(semiSpilt[i]+ "\n");
			String[] ampSpilt = semiSpilt[i].split("&");
			if (ampSpilt.length == 1){
			    String [] args = SysLib.stringToArgs(semiSpilt[i]);
			    if (SysLib.exec(args) > 0)
				{
				    SysLib.join();
				}
			}else{ //work on the amp sign
			    int process1 = 0;
			    for (int j = 0; j < ampSpilt.length; j++){
				String[] calls = SysLib.stringToArgs(ampSpilt[j]);

				SysLib.cout(calls[j] + "\n\t");
				if (SysLib.exec(calls) > 0){ //print out the calls
				    process1--;
				}else{
				    process1++; //otherwise increment
				}
			    }
			    for (int k = 0; k < process1; k++){  //collect children
				SysLib.join(); //waits for termination and return child ID of the right one
			    }
			}
		    }
	    }else if(semiSign.length >1){ //if only the semicolon is there
		for (int i = 0; i < semiSign.length; i++){
		    String[] calls = SysLib.stringToArgs(semiSign[i]);
		    SysLib.cout(calls[0] + ": \n\t");
		    if (SysLib.exec(calls) < 0){
			return;
		    }
		    SysLib.join(); //wait for the termination
		}
	    }else{ //else only the ampersand sign is there
		int process = 0;
		for (int i = 0; i < ampSign.length; i++){
		    String[] calls = SysLib.stringToArgs(ampSign[i]);

		    SysLib.cout(calls[0] + "\n\t");
		    if (SysLib.exec(calls) < 0){
			process--;
		    }
		    process++; //otherwise increment
		}

		for (int j = 0; j < process; j++){  // children
		    SysLib.join(); //waits for termination, returns child ID
		}
	    }
	}
	SysLib.cout("DONE!\n");
	SysLib.sync(); //writes back all the memory data into a disk
	SysLib.exit();  //terminates the calling thread and wake up the parent
    }
}
