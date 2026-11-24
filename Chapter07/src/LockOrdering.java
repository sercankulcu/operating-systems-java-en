import java.util.concurrent.locks.ReentrantLock;

/*
 * Attacking circular wait: lock ordering. The two locks are always taken from
 * the smaller id to the larger; even if T1 transfers x->y and T2 transfers
 * y->x, no cycle can form. Each does 100,000 transfers in opposite directions.
 */
public class LockOrdering {

	static class Account {
		final int id;
		int balance;
		final ReentrantLock lock = new ReentrantLock();

		Account(int id, int balance) {
			this.id = id;
			this.balance = balance;
		}
	}

	static final int ROUNDS = 100_000;

	static void transfer(Account from, Account to, int m) {
		Account first = from.id < to.id ? from : to;
		Account second = from.id < to.id ? to : from;
		first.lock.lock();
		try {
			second.lock.lock();
			try { from.balance -= m; to.balance += m; }
			finally { second.lock.unlock(); }
		} finally { first.lock.unlock(); }
	}

	public static void main(String[] args) throws InterruptedException {
		Account x = new Account(1, 1000), y = new Account(2, 1000);
		/* T1: transfer(x, y, 1)   x.id = 1
		   T2: transfer(y, x, 1)   y.id = 2 */
		Thread t1 = new Thread(() -> { for (int i = 0; i < ROUNDS; i++) transfer(x, y, 1); }, "T1");
		Thread t2 = new Thread(() -> { for (int i = 0; i < ROUNDS; i++) transfer(y, x, 1); }, "T2");
		t1.start(); t2.start();
		t1.join(); t2.join();
		System.out.println("x=" + x.balance + " y=" + y.balance + ", no deadlock");
	}
}

/* Expected output:
x=1000 y=1000, no deadlock
*/
