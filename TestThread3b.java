//Nguyen Nguyen
//CSS430- Part 2
//2.23.17
import java.util.Random;
import java.util.Date;

class TestThread3b extends Thread {
  private byte[] myJobs;

  public void run() {
  //  submissionTime = new Date().getTime();
    myJobs = new byte[512];
    Random generator = new Random();
    for (int i = 0; i < 30; i++) {
      // reads and writes random bytes from blocks with index 0-1000
      SysLib.rawwrite(generator.nextInt(1000), myJobs);
      SysLib.rawread(generator.nextInt(1000), myJobs);
    }

    //finishedTime = new Date().getTime();
    SysLib.cout("TestThread3b finished in sec.\n");
    SysLib.cout("Thread exiting...\n");
    SysLib.exit();
  }
}
