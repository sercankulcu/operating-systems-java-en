/*
 * Java: synchronized, wait and notify. Every object is a monitor: a one-slot
 * buffer is protected by put/take, and because of Mesa semantics the condition
 * is tested with while. The producer puts 10, 20, 30, 40; main reads with
 * take() four times.
 */
public class OneSlot {
	private int slot = 0;            /* 0 = empty */

	synchronized void put(int x) throws InterruptedException {
		while (slot != 0) wait();      /* releases the lock */
		slot = x;
		notifyAll();
	}

	synchronized int take() throws InterruptedException {
		while (slot == 0) wait();
		int x = slot;
		slot = 0;
		notifyAll();
		return x;
	}

	public static void main(String[] args) throws InterruptedException {
		OneSlot s = new OneSlot();
		Thread producer = new Thread(() -> {
			try {
				for (int x = 10; x <= 40; x += 10)
					s.put(x);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		producer.start();
		for (int k = 0; k < 4; k++)
			System.out.println("took " + s.take());
		producer.join();
	}
}

/* Expected output:
took 10
took 20
took 30
took 40
*/
