import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Java ExecutorService: four tasks (1^2, 2^2, 3^2, 4^2) are submitted to a
// pool of two threads; Future.get() waits for each result.
public class Pool {
	public static void main(String[] args)
			throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(2);
		List<Future<Integer>> results = new ArrayList<>();
		for (int g = 1; g <= 4; g++) {
			final int n = g;
			results.add(pool.submit(() -> n * n));
		}
		int total = 0;
		for (Future<Integer> f : results)
			total += f.get();       // waits for the result
		System.out.println("total = " + total);
		pool.shutdown();
	}
}

/* Expected output:
total = 30
*/
