/*
 * Peterson's algorithm in Java: because the separate volatile fields (flag0,
 * flag1, turn) are read and written in a sequentially consistent way, the
 * algorithm works. Two threads each do lock(i); counter++; unlock(i) 100_000 times.
 */
public class Peterson {
	static volatile boolean flag0, flag1;
	static volatile int turn;
	static int counter = 0;

	static void lock(int i) {
		if (i == 0) { flag0 = true; turn = 1;
			while (flag1 && turn == 1) Thread.onSpinWait(); }
		else        { flag1 = true; turn = 0;
			while (flag0 && turn == 0) Thread.onSpinWait(); }
	}

	static void unlock(int i) {
		if (i == 0) flag0 = false; else flag1 = false;
	}

	public static void main(String[] args) throws InterruptedException {
		Thread[] t = new Thread[2];
		for (int k = 0; k < 2; k++) {
			final int i = k;
			t[k] = new Thread(() -> {
				for (int n = 0; n < 100_000; n++) {
					lock(i);
					counter++;
					unlock(i);
				}
			});
			t[k].start();
		}
		for (Thread x : t)
			x.join();
		System.out.println("counter = " + counter);
	}
}

/* Expected output:
counter = 200000
*/
