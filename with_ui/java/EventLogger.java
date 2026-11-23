// Observer that prints every change from the guns (useful for testing).
public class EventLogger implements Observer {

	private int[] holster = {-1, -1, -1};
	private int[] trigger = {-1, -1, -1};
	private int[] hit = {-1, -1, -1};

	public void update(int p, int h, int t, int x, long time) {
		if (h != holster[p]) {
			if (h == 1) {
				System.out.println("   Player " + p + ": gun in holster");
			} else {
				System.out.println("   Player " + p + ": gun drawn");
			}
		}
		if (t != trigger[p] && t == 1) {
			System.out.println("   Player " + p + ": fired");
		}
		if (x != hit[p] && x == 1) {
			System.out.println("   Player " + p + ": got hit");
		}

		holster[p] = h;
		trigger[p] = t;
		hit[p] = x;
	}
}
