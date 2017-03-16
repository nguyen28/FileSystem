//Nguyen Nguyen
//CSS430- Part 2
//2.23.17
import java.util.Date;

class Test3 extends Thread {
  private int pairs;
  private long submissionTime;
  private long finishedTime;

  public Test3(String[] args) {
    pairs = Integer.parseInt(args[0]);
  }

  public void run() {
    submissionTime = new Date().getTime(); //start
    String[] threads3a = SysLib.stringToArgs("TestThread3a");
    String[] threads3b = SysLib.stringToArgs("TestThread3b");

    //Executing the threads for numerical and rote read and write
    for (int i = 0; i < pairs; i++) {
      SysLib.exec(threads3a);
    //  SysLib.exec(threads3b);
    }

    for (int i = 0; i < pairs * 2; i++) {
      SysLib.join();
    }

    finishedTime = new Date().getTime();   //complete

    SysLib.cout("elapsed time = " + (finishedTime - submissionTime) + " msec.\n");
    SysLib.exit();
  }
}
