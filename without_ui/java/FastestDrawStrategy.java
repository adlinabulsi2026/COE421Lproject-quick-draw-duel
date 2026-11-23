// Reaction mode: the player who draws first wins (no shooting needed).
public class FastestDrawStrategy implements WinnerStrategy {

	public String getName() {
		return "Reaction (fastest draw wins)";
	}

	// the round is done when both players have drawn
	public boolean isRoundDone(Player p1, Player p2) {
		return p1.getDrawTime() >= 0 && p2.getDrawTime() >= 0;
	}

	public int pickWinner(Player p1, Player p2) {
		long draw1 = p1.getDrawTime();
		long draw2 = p2.getDrawTime();

		if (draw1 < 0 && draw2 < 0) {
			return 0;
		}
		if (draw1 < 0) {
			return 2;
		}
		if (draw2 < 0) {
			return 1;
		}
		if (draw1 < draw2) {
			return 1;
		}
		if (draw2 < draw1) {
			return 2;
		}
		return 0;
	}
}
