import java.time.Duration;
import java.util.concurrent.*;
import java.util.stream.IntStream;

// Java 21 virtual threads: each of 10,000 tasks sleeps for 1 s in its own
// virtual thread; a few carrier threads are enough, so the total is about 2 s.
public class Virtual {
	public static void main(String[] args) {
		long start = System.nanoTime();
		try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
			IntStream.range(0, 10_000).forEach(i ->
				ex.submit(() -> {
					Thread.sleep(Duration.ofSeconds(1));
					return i;
				}));
		}  // close(): waits for all tasks
		long ms = (System.nanoTime() - start) / 1_000_000;
		System.out.println("done");
		System.out.println("time: " + ms + " ms");
	}
}

/* Note: the output of this program varies on every run (the time depends on the machine and the run);
   below is one sample run. */
/* Expected output:
done
time: 2083 ms
*/
