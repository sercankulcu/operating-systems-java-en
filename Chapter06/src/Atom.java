import java.util.concurrent.atomic.AtomicInteger;

/*
 * Atomic variables: two threads each increment an AtomicInteger and a counter
 * protected by synchronized 100_000 times; both reach 200000.
 * compareAndSet writes only if the expected value matches.
 */
public class Atom {
	static AtomicInteger atom = new AtomicInteger();
	static int plain = 0;
	static final Object lock = new Object();

	static void twoThreads(Runnable r) throws InterruptedException {
		Thread a = new Thread(r), b = new Thread(r);
		a.start(); b.start();
		a.join();  b.join();
	}

	public static void main(String[] args) throws InterruptedException {
		/* two threads, each 100_000 times: */
		twoThreads(() -> {
			for (int n = 0; n < 100_000; n++)
				atom.incrementAndGet();
		});
		twoThreads(() -> {
			for (int n = 0; n < 100_000; n++)
				synchronized (lock) { plain++; }
		});
		System.out.println("AtomicInteger = " + atom.get());
		System.out.println("synchronized  = " + plain);

		var v = new AtomicInteger(5);
		boolean r1 = v.compareAndSet(5, 6);   /* true,  v = 6 */
		System.out.println(r1 + " " + v.get());
		boolean r2 = v.compareAndSet(5, 7);   /* false, v = 6 */
		System.out.println(r2 + " " + v.get());
	}
}

/* Expected output:
AtomicInteger = 200000
synchronized  = 200000
true 6
false 6
*/
