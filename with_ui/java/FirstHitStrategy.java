// Duel mode: the player who hits the opponent first wins.
public class FirstHitStrategy implements WinnerStrategy {

	public String getName() {
		return "Duel (first hit wins)";
	}

	// the round is done as soon as someone gets hit
	public boolean isRoundDone(Player p1, Player p2) {
		return p1.getHitTime() >= 0 || p2.getHitTime() >= 0;
	}

	public int pickWinner(Player p1, Player p2) {
		long hit1 = p1.getHitTime();
		long hit2 = p2.getHitTime();

		if (hit1 < 0 && hit2 < 0) {
			return 0;          // nobody got hit
		}
		if (hit1 < 0) {
			return 1;          // only player 2 got hit
		}
		if (hit2 < 0) {
			return 2;          // only player 1 got hit
		}
		if (hit1 < hit2) {
			return 2;          // player 1 got hit first
		}
		if (hit2 < hit1) {
			return 1;          // player 2 got hit first
		}
		return 0;              // same time
	}
}
