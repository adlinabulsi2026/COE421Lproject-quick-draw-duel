// Strategy interface (Strategy design pattern).
// Each game mode decides when a round is done and who won it.
public interface WinnerStrategy {
	public String getName();
	public boolean isRoundDone(Player p1, Player p2);
	// returns 1 or 2 for the winner, 0 for a tie
	public int pickWinner(Player p1, Player p2);
}
