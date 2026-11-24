import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Attacking the "no preemption" condition: tryLock with a timeout and back-off.
 * A thread that cannot get the second lock within 50 ms releases the one it
 * holds itself and retries after a random delay. 2 threads x 1000 transfers
 * in opposite directions.
 */
public class TryLockBackoff {

	static class Account {
		final int id;
		int balance;
		final ReentrantLock lock = new ReentrantLock();

		Account(int id, int balance) {
			this.id = id;
			this.balance = balance;
		}
	}

	static final int ROUNDS = 1000;

	static boolean tryXfer(Account a, Account b, int m)
			throws InterruptedException {
		if (!a.lock.tryLock(50, MILLISECONDS))
			return false;
		try {
			if (!b.lock.tryLock(50, MILLISECONDS))
				return false;  /* a is released too */
			try { a.balance -= m; b.balance += m;
				return true; }
			finally { b.lock.unlock(); }
		} finally { a.lock.unlock(); }
	}

	/* caller: while (!tryXfer(x, y, 1)) Thread.sleep(random); */
	static Thread worker(String name, Account a, Account b, long seed, int[] backedOff) {
		return new Thread(() -> {
			Random random = new Random(seed);
			try {
				for (int i = 0; i < ROUNDS; i++)
					while (!tryXfer(a, b, 1)) {
						backedOff[0]++;
						Thread.sleep(random.nextInt(5));
					}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, name);
	}

	public static void main(String[] args) throws InterruptedException {
		Account x = new Account(1, 1000), y = new Account(2, 1000);
		int[] g1 = new int[1], g2 = new int[1];
		Thread t1 = worker("T1", x, y, 42, g1);   /* x -> y */
		Thread t2 = worker("T2", y, x, 43, g2);   /* y -> x */
		long start = System.nanoTime();
		t1.start(); t2.start();
		t1.join(); t2.join();
		long ms = (System.nanoTime() - start) / 1_000_000;
		System.out.println("x=" + x.balance + " y=" + y.balance
			+ " total=" + (x.balance + y.balance));
		System.out.println("no deadlock; backed-off attempts: " + (g1[0] + g2[0])
			+ ", time: " + ms + " ms");
	}
}

/* Note: the output of this program varies on every run (the number of backed-off attempts and the time);
   below is one sample run. */
/* Expected output:
x=1000 y=1000 total=2000
no deadlock; backed-off attempts: 4, time: 117 ms
*/
