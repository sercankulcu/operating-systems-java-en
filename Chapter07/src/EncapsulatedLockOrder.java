import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * Encapsulation hides the lock order: a.addAll(b) first takes the lock of a
 * inside, then asks for the lock of b through b.toArray(). b.addAll(a) asks in
 * the opposite order at the same time -> deadlock. Fix: take the copy outside
 * the lock. The threads are daemons; the deadlock is found and reported with
 * ThreadMXBean.
 */
public class EncapsulatedLockOrder {

	static final int ROUNDS = 200_000;

	/* Runs two threads; if they deadlock, returns who waits for whom */
	static String race(Runnable r1, Runnable r2) throws InterruptedException {
		Thread t1 = new Thread(r1, "T1");
		Thread t2 = new Thread(r2, "T2");
		t1.setDaemon(true);
		t2.setDaemon(true);
		t1.start();
		t2.start();
		ThreadMXBean mx = ManagementFactory.getThreadMXBean();
		long end = System.currentTimeMillis() + 4000;
		while (System.currentTimeMillis() < end) {
			long[] ids = mx.findDeadlockedThreads();
			if (ids != null)    /* ignore threads left deadlocked by the earlier attempt */
				ids = Arrays.stream(ids)
					.filter(id -> id == t1.threadId() || id == t2.threadId())
					.toArray();
			if (ids != null && ids.length > 0) {
				Arrays.sort(ids);
				StringBuilder sb = new StringBuilder("DEADLOCKED:");
				for (ThreadInfo ti : mx.getThreadInfo(ids))
					sb.append(' ').append(ti.getThreadName())
						.append(" -> ").append(ti.getLockOwnerName()).append(';');
				return sb.toString();
			}
			if (!t1.isAlive() && !t2.isAlive())
				return "no deadlock, both threads finished";
			Thread.sleep(10);
		}
		return "did not finish within 4 s";
	}

	public static void main(String[] args) throws InterruptedException {
		var a = Collections.synchronizedList(
				new ArrayList<Integer>());
		var b = Collections.synchronizedList(
				new ArrayList<Integer>());
		/* inside a.addAll(b):               */
		/* synchronized (a) { b.toArray() }  */
		/*   -> synchronized (b) { ... }      */
		/* The lists stay empty: addAll still takes both locks. */
		System.out.println("Crossed addAll: " + race(
			() -> { for (int i = 0; i < ROUNDS; i++) a.addAll(b); },
			() -> { for (int i = 0; i < ROUNDS; i++) b.addAll(a); }));

		/* Fix: take the copy outside the lock (with new lists) */
		List<Integer> c = Collections.synchronizedList(new ArrayList<Integer>());
		List<Integer> d = Collections.synchronizedList(new ArrayList<Integer>());
		System.out.println("Fixed version: " + race(
			() -> {
				for (int i = 0; i < ROUNDS; i++) {
					Integer[] k = d.toArray(new Integer[0]);
					c.addAll(Arrays.asList(k));
				}
			},
			() -> {
				for (int i = 0; i < ROUNDS; i++) {
					Integer[] k = c.toArray(new Integer[0]);
					d.addAll(Arrays.asList(k));
				}
			}));
	}
}

/* Expected output:
Crossed addAll: DEADLOCKED: T1 -> T2; T2 -> T1;
Fixed version: no deadlock, both threads finished
*/
