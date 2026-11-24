// Observing a race: two threads increment the same counter 1,000,000 times
// each, without a lock. counter++ is a read-increment-write triple; updates are lost.
public class Race {
	static int counter = 0;

	public static void main(String[] a) throws Exception {
		Runnable job = () -> {
			for (int i = 0; i < 1_000_000; i++) counter++;
		};
		Thread t1 = new Thread(job), t2 = new Thread(job);
		t1.start(); t2.start();
		t1.join();  t2.join();
		System.out.println("counter = " + counter);
	}
}

/* Note: the output of this program varies on every run (counter is below 2,000,000 and differs each time (race condition; if the JIT turns the loop body into a single addition, 2000000 can occasionally appear too));
   below is one sample run. */
/* Expected output:
counter = 1431210
*/
