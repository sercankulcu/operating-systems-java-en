import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Java: ReentrantLock and Condition. A circular buffer with two slots; two
 * conditions share one lock, and signal wakes the right group. unlock() is in
 * a finally block. The producer puts 1..5; main calls take() 5 times.
 */
class Buffer {
	private final int[] buf = new int[2];
	private int head = 0, tail = 0, count = 0;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition notFull  = lock.newCondition();
	private final Condition notEmpty = lock.newCondition();

	void put(int x) throws InterruptedException {
		lock.lock();
		try {
			while (count == buf.length) notFull.await();
			buf[tail] = x; tail = (tail + 1) % buf.length; count++;
			notEmpty.signal();
		} finally { lock.unlock(); }
	}

	int take() throws InterruptedException {
		lock.lock();
		try {
			while (count == 0) notEmpty.await();
			int x = buf[head]; head = (head + 1) % buf.length; count--;
			notFull.signal();
			return x;
		} finally { lock.unlock(); }
	}

	public static void main(String[] args) throws InterruptedException {
		Buffer b = new Buffer();
		Thread producer = new Thread(() -> {
			try {
				for (int x = 1; x <= 5; x++)
					b.put(x);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		producer.start();
		StringBuilder sb = new StringBuilder();
		int sum = 0;
		for (int k = 0; k < 5; k++) {
			int x = b.take();
			sb.append(x).append(' ');
			sum += x;
		}
		producer.join();
		System.out.println(sb + "| sum = " + sum);
	}
}

/* Expected output:
1 2 3 4 5 | sum = 15
*/
