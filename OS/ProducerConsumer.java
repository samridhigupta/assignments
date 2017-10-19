import java.util.*;

public class ProducerConsumer{
  static final int N = 100;
  static producer p = new producer();
  static consumer c = new consumer();
  static our_monitor mon = new our_monitor();
  static Random random = new Random();
  static int symbol = 0;
 
  public static void main(String[] args){
    p.start();  c.start();
  }

  static class producer extends Thread{
    public void run(){
     while (true){
       try { sleep(random.nextInt(500));
       } catch(InterruptedException e){};
       mon.insert(symbol);
       symbol = (symbol + 1) % 10;
     }
    }
  }

  static class consumer extends Thread{
    public void run(){
     while (true){
       try { sleep(random.nextInt(1000));
       } catch(InterruptedException e){};
       System.out.println("consumer " + mon.remove());
     }
    }
  }

  static class our_monitor{
     int[] buffer = new int[N];
     int count = 0, lo = 0, hi = 0;

     public synchronized void insert(int val){
       if (count == N) go_to_sleep();
       buffer[hi] = val;
       hi = (hi + 1) % N;
       count++;
       if (count == 1) notify();
     }

     public synchronized int remove(){
        if (count == 0) go_to_sleep();
        int val = buffer[lo];
        lo = (lo + 1) % N;
        count--;
        if (count == N - 1) notify();
        return val;
     }
     private void go_to_sleep(){ 
        try { wait(); } catch (InterruptedException e){};
     }
   }

}