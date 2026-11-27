import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

// Java ForkJoinPool and work stealing: the array is split in two, the left
// half is queued with fork, the right half is computed in the same thread,
// then the left is joined.
// On the slides and in the lecture notes this class is called Sum; it is SumTask
// here only because Sum.java (the join example) is in the same default package.
public class SumTask extends RecursiveTask<Long> {
	private static final long serialVersionUID = 1L;
	static final int LIMIT = 2;
	final int[] a;
	final int lo, hi;

	SumTask(int[] a, int lo, int hi) {
		this.a = a;
		this.lo = lo;
		this.hi = hi;
	}

	@Override
	protected Long compute() {
		if (hi - lo <= LIMIT) {       // small: do it directly
			long s = 0;
			for (int i = lo; i < hi; i++) s += a[i];
			return s;
		}
		int mid = (lo + hi) / 2;
		SumTask left = new SumTask(a, lo, mid);
		SumTask right = new SumTask(a, mid, hi);
		left.fork();                  // start asynchronously
		long rightSum = right.compute();
		return left.join() + rightSum;
	}

	public static void main(String[] args) {
		// Code trace example: a single-worker pool, LIMIT = 2
		int[] a = {2, 7, 1, 8, 2, 8, 1, 8};
		try (ForkJoinPool pool = new ForkJoinPool(1)) {
			System.out.println("total = " + pool.invoke(new SumTask(a, 0, 8)));
		}

		// The array 1..100,000, common pool
		int[] b = new int[100_000];
		for (int i = 0; i < b.length; i++) b[i] = i + 1;
		long s = ForkJoinPool.commonPool()
				.invoke(new SumTask(b, 0, b.length));
		System.out.println("total = " + s);
	}
}

/* Expected output:
total = 37
total = 5000050000
*/
