import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/*
 * Deadlock in Java: T1 takes the monitor of a then b, T2 takes b then a.
 * The deadlock is found with ThreadMXBean.findDeadlockedThreads (as jstack
 * does); T1 and T2 are daemon threads, so the program still terminates.
 * To get the same report from an outside tool: jstack <pid>  or  jcmd <pid> Thread.print
 */
public class Deadlock {

	/* nap(10) makes the timing very likely but does not guarantee it; so nap()
	 * also waits until both first locks are held (the CountDownLatch version of the slides). */
	static final CountDownLatch bothFirstLocks = new CountDownLatch(2);

	static void nap(int ms) {
		bothFirstLocks.countDown();
		try {
			bothFirstLocks.await();
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	static void work() {
		System.out.println(Thread.currentThread().getName() + " worked");
	}

	static void run() throws InterruptedException {
		Object a = new Object(), b = new Object();

		Thread t1 = new Thread(() -> {
			synchronized (a) {
				nap(10);   /* widen the window */
				synchronized (b) { work(); }
			}
		}, "T1");
		Thread t2 = new Thread(() -> {
			synchronized (b) {
				nap(10);
				synchronized (a) { work(); }
			}
		}, "T2");
		t1.setDaemon(true);
		t2.setDaemon(true);
		t1.start(); t2.start();

		/* Finding the deadlock: the JVM looks for a cycle in the wait-for graph */
		ThreadMXBean mx =
			ManagementFactory.getThreadMXBean();
		long[] ids = null;
		for (int i = 0; i < 100 && ids == null; i++) {
			Thread.sleep(20);
			ids = mx.findDeadlockedThreads();
		}
		if (ids != null) {
			Arrays.sort(ids);   /* print T1 first */
			for (ThreadInfo ti : mx.getThreadInfo(ids))
				System.out.println(ti.getThreadName()
					+ " waits, lock owner "
					+ ti.getLockOwnerName());
		} else {
			System.out.println("no deadlock occurred");
		}
		System.out.println("T1: " + t1.getState() + ", T2: " + t2.getState());
	}

	public static void main(String[] args) throws InterruptedException {
		run();
		System.out.println("It detects but does not recover; daemon threads end with the JVM.");
	}
}

/* Expected output:
T1 waits, lock owner T2
T2 waits, lock owner T1
T1: BLOCKED, T2: BLOCKED
It detects but does not recover; daemon threads end with the JVM.
*/
