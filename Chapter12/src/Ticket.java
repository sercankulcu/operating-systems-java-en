// Ticket lock: next hands out tickets, serving shows whose turn it is.
// The lock is fair: threads enter in ticket order.
import java.util.concurrent.atomic.*;

public class Ticket {
	static AtomicInteger next = new AtomicInteger();
	static volatile int serving = 0;
	static int counter = 0;

	static void lock() {
		int my = next.getAndIncrement();
		while (serving != my) Thread.onSpinWait();
	}

	static void unlock() { serving++; }   // only the lock holder writes

	public static void main(String[] a)
			throws Exception {
		Runnable job = () -> {
			for (int i = 0; i < 100_000; i++) {
				lock(); counter++; unlock(); } };
		Thread t0 = new Thread(job);
		Thread t1 = new Thread(job);
		t0.start(); t1.start(); t0.join(); t1.join();
		System.out.println("counter = " + counter);
	}
}

/* Expected output:
counter = 200000
*/
