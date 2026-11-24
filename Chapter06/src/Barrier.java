import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/*
 * Synchronization barrier: 3 threads compute two phases; at the end of each
 * phase they meet at b.await(). The third to arrive opens the barrier and the
 * barrier action prints "phase N done". The barrier is reusable (cyclic).
 */
public class Barrier {
	static final int[] phase = { 0 };
	static final int[] computed = new int[3];

	static void compute(int id, int f) {
		computed[id] += f + 1;          /* the work of the phase */
	}

	public static void main(String[] args) throws InterruptedException {
		var b = new CyclicBarrier(3,
			() -> System.out.println(
					"phase " + phase[0]++ + " done"));
		Thread[] t = new Thread[3];
		for (int k = 0; k < 3; k++) {
			final int id = k;
			t[k] = new Thread(() -> {
				try {
					for (int f = 0; f < 2; f++) {
						compute(id, f); b.await();
					}
				} catch (InterruptedException | BrokenBarrierException e) {
					Thread.currentThread().interrupt();
				}
			});
			t[k].start();
		}
		for (Thread x : t)
			x.join();
	}
}

/* Expected output:
phase 0 done
phase 1 done
*/
