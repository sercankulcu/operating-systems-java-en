// Java: Thread, start and join. A new thread computes the sum 1..5;
// main waits with join. join also guarantees that main sees what t wrote.
public class Sum {
	static int total = 0;

	public static void main(String[] a)
			throws InterruptedException {
		Thread t = new Thread(() -> {
			for (int i = 1; i <= 5; i++) total += i;
		});
		System.out.println("state: " + t.getState());   // NEW
		t.start();
		System.out.println("main: waiting");
		t.join();
		System.out.println("state: " + t.getState());   // TERMINATED
		System.out.println("total = " + total);
	}
}

/* Expected output:
state: NEW
main: waiting
state: TERMINATED
total = 15
*/
