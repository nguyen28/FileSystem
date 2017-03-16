//Nguyen Nguyen
//CSS430- Part 2
//2.23.17
import java.util.Date;

class TestThread3a extends Thread {
  private static int count = 0;
  //private static final int X_THREADS=10;

  public void run() {
    numCalc(5, 4);

    SysLib.cout("Thread exiting...\n");
    SysLib.exit();
  }

  private static void numCalc(int m, int n){
    if(m==0 && n==0 ){
      return;
    }
    while(n> 0 && m>0){
      int c=n+m;
      n--;
      m--;
    }
  }
}
