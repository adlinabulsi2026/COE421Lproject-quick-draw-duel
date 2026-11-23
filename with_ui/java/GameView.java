// Observer interface for game events (Observer design pattern).
// The GameController is the subject: it tells every view what happens in the game.
// Both the console Scoreboard and the GameWindow implement this.
public interface GameView {
	void showStatus(String phase, String message);
	void showRound(int round, String result, Player p1, Player p2);
	void showChampion(Player winner, Player p1, Player p2);
}
