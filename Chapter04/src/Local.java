// Thread-local storage (TLS): ThreadLocal gives every thread its own copy;
// main sees the initial value (0).
public class Local {
	static ThreadLocal<Integer> id =
		ThreadLocal.withInitial(() -> 0);

	public static void main(String[] args) throws InterruptedException {
		String[] line = new String[2];
		Thread a = new Thread(() -> { id.set(1); line[0] = "A: " + id.get(); });
		Thread b = new Thread(() -> { id.set(2); line[1] = "B: " + id.get(); });
		a.start(); b.start();
		a.join();  b.join();
		System.out.println(line[0]);
		System.out.println(line[1]);
		System.out.println("main: " + id.get());
	}
}

/* Expected output:
A: 1
B: 2
main: 0
*/
